package com.hatopigeon.cubictimer.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.hatopigeon.cubicify.BuildConfig;
import com.hatopigeon.cubictimer.cube.CubeMove;
import com.hatopigeon.cubictimer.cube.CubeState;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.data.Data;

public class GanCubeManager extends BleManager {

    private static final String TAG = "GanCubeManager";

    private static final String CUBE_SERVICE_UUID = "6e400001-b5a3-f393-e0a9-e50e24dc4179";
    private static final String CUBE_COMMAND_CHAR_UUID = "28be4a4a-cd67-11e9-a32f-2a2ae2dbcce4";
    private static final String CUBE_STATE_CHAR_UUID = "28be4cb6-cd67-11e9-a32f-2a2ae2dbcce4";

    // GAN Gen2 state-event types (first 4 bits of a decrypted packet).
    private static final int EVENT_GYRO = 1, EVENT_MOVE = 2, EVENT_FACELETS = 4,
            EVENT_HARDWARE = 5, EVENT_BATTERY = 9, EVENT_DISCONNECT = 13;

    private static final byte[] KEY = {
        (byte)0x01, (byte)0x02, (byte)0x42, (byte)0x28,
        (byte)0x31, (byte)0x91, (byte)0x16, (byte)0x07,
        (byte)0x20, (byte)0x05, (byte)0x18, (byte)0x54,
        (byte)0x42, (byte)0x11, (byte)0x12, (byte)0x53
    };
    private static final byte[] IV = {
        (byte)0x11, (byte)0x03, (byte)0x32, (byte)0x28,
        (byte)0x21, (byte)0x01, (byte)0x76, (byte)0x27,
        (byte)0x20, (byte)0x95, (byte)0x78, (byte)0x14,
        (byte)0x32, (byte)0x12, (byte)0x02, (byte)0x43
    };

    public interface GanCubeCallback {
        void onCubeConnected();
        void onCubeDisconnected();
        void onCubeMoves(List<CubeMove> moves);
        void onCubeBatteryLevel(int level);
        void onCubeSolved();
        void onFaceletsReceived(int[] csFacelets);
        /** Cube orientation as a unit quaternion (w,x,y,z) from the gyroscope. */
        default void onGyroData(float w, float x, float y, float z) {}
    }

    private BluetoothGattCharacteristic cubeCommandCharacteristic;
    private BluetoothGattCharacteristic cubeStateCharacteristic;

    private GanCubeCallback callback;
    private final String macAddress;
    private SecretKeySpec keySpec;
    private IvParameterSpec ivSpec;
    private int lastBatteryLevel;

    private int lastSerial = -1;
    private long lastMoveTimestamp = 0;
    // Most recent facelet snapshot, logged together with each move (facelet events don't arrive
    // 1:1 with moves during fast turning, so we log the latest known state alongside the move).
    private String lastKociemba = "";
    private boolean lastSolved = false;

    private final CubeState cubeState = new CubeState();

    public GanCubeManager(@NonNull Context context, GanCubeCallback callback, String macAddress) {
        super(context);
        this.callback = callback;
        this.macAddress = macAddress;
        setupEncryption(macAddress);
    }

    public void setCallback(GanCubeCallback callback) {
        this.callback = callback;
    }

    public GanCubeCallback getCallback() {
        return callback;
    }

    public String getMacAddress() {
        return macAddress;
    }

    private void setupEncryption(String macAddress) {
        String[] parts = macAddress.split(":");
        byte[] salt = new byte[6];
        for (int i = 0; i < 6; i++) {
            salt[i] = (byte) Integer.parseInt(parts[5 - i], 16);
        }

        byte[] derivedKey = new byte[16];
        byte[] derivedIv = new byte[16];
        for (int i = 0; i < 16; i++) {
            derivedKey[i] = KEY[i];
            derivedIv[i] = IV[i];
        }
        // Mix the MAC-derived salt into the first 6 key/IV bytes. The modulo is 255 (NOT 256) to
        // match the GAN Gen2 reference implementation (afedotov/gan-web-bluetooth); changing it to
        // 256 would derive a different key and make the cube's data undecryptable.
        for (int i = 0; i < 6; i++) {
            int sk = (derivedKey[i] & 0xFF) + (salt[i] & 0xFF);
            int siv = (derivedIv[i] & 0xFF) + (salt[i] & 0xFF);
            derivedKey[i] = (byte) (sk % 255);
            derivedIv[i] = (byte) (siv % 255);
        }

        this.keySpec = new SecretKeySpec(derivedKey, "AES");
        this.ivSpec = new IvParameterSpec(derivedIv);

        Log.d(TAG, "AES-128-CBC key/IV derived from MAC " + macAddress);
    }

    @Override
    public int getMinLogPriority() {
        return Log.WARN;
    }

    @Override
    public void log(int priority, @NonNull String message) {
        Log.println(priority, TAG, message);
    }

    @Override
    protected boolean isRequiredServiceSupported(@NonNull BluetoothGatt gatt) {
        BluetoothGattService service = gatt.getService(UUID.fromString(CUBE_SERVICE_UUID));
        if (service != null) {
            cubeCommandCharacteristic = service.getCharacteristic(UUID.fromString(CUBE_COMMAND_CHAR_UUID));
            cubeStateCharacteristic = service.getCharacteristic(UUID.fromString(CUBE_STATE_CHAR_UUID));
            if (cubeCommandCharacteristic != null && cubeStateCharacteristic != null) {
                Log.d(TAG, "GAN Gen2 service found");
                return true;
            }
        }
        return false;
    }

    @Override
    protected void initialize() {
        requestMtu(517).enqueue();

        setNotificationCallback(cubeStateCharacteristic)
                .with((device, data) -> {
                    byte[] value = data.getValue();
                    if (value != null && value.length >= 16) {
                        onStateData(value);
                    }
                });
        // Notify the callback only once notifications are actually enabled, otherwise the
        // commands it issues (requestFacelets/requestBattery) can be sent before the cube's
        // replies can be received and the responses are lost.
        enableNotifications(cubeStateCharacteristic)
                .done(device -> {
                    if (callback != null) {
                        callback.onCubeConnected();
                    }
                })
                .enqueue();
    }

    private void onStateData(byte[] encrypted) {
        try {
            byte[] decrypted = decryptData(encrypted);
            if (decrypted == null) return;
            processCubeEvent(decrypted, System.currentTimeMillis());
        } catch (Exception e) {
            Log.e(TAG, "Error processing state data", e);
        }
    }

    // Single AES/CBC cipher reused for every packet (re-initialised per chunk). Cube notifications
    // are delivered on the main thread, so no synchronisation is needed; avoids the per-packet
    // Cipher.getInstance() provider lookup in this hot path.
    private Cipher aesCipher;

    private byte[] cipherChunk(int mode, byte[] src, int offset) throws GeneralSecurityException {
        if (aesCipher == null) {
            aesCipher = Cipher.getInstance("AES/CBC/NoPadding");
        }
        aesCipher.init(mode, keySpec, ivSpec);
        return aesCipher.doFinal(src, offset, 16);
    }

    private byte[] decryptData(byte[] data) {
        if (data.length < 16 || keySpec == null || ivSpec == null) return null;
        try {
            byte[] result = data.clone();
            // Decrypt last 16-byte chunk first (aligned to end), then first (aligned to start).
            // Each chunk uses the same IV, matching Gen2 protocol (no CBC chaining between chunks).
            if (result.length > 16) {
                byte[] tail = cipherChunk(Cipher.DECRYPT_MODE, result, result.length - 16);
                System.arraycopy(tail, 0, result, result.length - 16, 16);
            }
            byte[] head = cipherChunk(Cipher.DECRYPT_MODE, result, 0);
            System.arraycopy(head, 0, result, 0, 16);
            return result;
        } catch (GeneralSecurityException e) {
            Log.e(TAG, "Decrypt error", e);
            return null;
        }
    }

    private byte[] encryptData(byte[] data) {
        if (data.length < 16 || keySpec == null || ivSpec == null) return null;
        try {
            byte[] result = data.clone();
            // Encrypt first 16-byte chunk (aligned to start), then last (aligned to end).
            byte[] head = cipherChunk(Cipher.ENCRYPT_MODE, result, 0);
            System.arraycopy(head, 0, result, 0, 16);
            if (result.length > 16) {
                byte[] tail = cipherChunk(Cipher.ENCRYPT_MODE, result, result.length - 16);
                System.arraycopy(tail, 0, result, result.length - 16, 16);
            }
            return result;
        } catch (GeneralSecurityException e) {
            Log.e(TAG, "Encrypt error", e);
            return null;
        }
    }

    private void processCubeEvent(byte[] plain, long timestamp) {
        if (plain.length < 16) return;

        int eventType = getBitWord(plain, 0, 4);

        switch (eventType) {
            case EVENT_GYRO:
                handleGyroEvent(plain, timestamp);
                break;
            case EVENT_MOVE:
                handleMoveEvent(plain, timestamp);
                break;
            case EVENT_FACELETS:
                handleFaceletsEvent(plain, timestamp);
                break;
            case EVENT_HARDWARE:
                handleHardwareEvent(plain, timestamp);
                break;
            case EVENT_BATTERY:
                handleBatteryEvent(plain, timestamp);
                break;
            case EVENT_DISCONNECT:
                disconnect().enqueue();
                break;
            default:
                //Log.v(TAG, "Unknown event type: " + eventType);
        }
    }

    private int getBitWord(byte[] data, int offset, int bitLength) {
        if (bitLength <= 8) {
            int value = 0;
            for (int i = 0; i < bitLength; i++) {
                int pos = offset + i;
                int b = (data[pos / 8] >> (7 - (pos % 8))) & 1;
                value = (value << 1) | b;
            }
            return value;
        } else if (bitLength == 16) {
            return (getBitWord(data, offset, 8) << 8) | getBitWord(data, offset + 8, 8);
        } else if (bitLength == 32) {
            return (getBitWord(data, offset, 16) << 16) | getBitWord(data, offset + 16, 16);
        }
        return 0;
    }

    private void handleMoveEvent(byte[] data, long timestamp) {
        int serial = getBitWord(data, 4, 8);
        if (lastSerial == -1) {
            lastSerial = serial - 1;
        }

        // A move event carries at most the last 7 moves. If more than 7 happened since the last
        // event (a notification gap), the moves beyond 7 are unrecoverable and the tracked state
        // desyncs from the physical cube, so request a fresh facelet snapshot to resync.
        int rawDiff = (serial - lastSerial) & 0xFF;
        int diff = Math.min(rawDiff, 7);
        lastSerial = serial;
        if (rawDiff > 7) {
            requestFacelets();
        }

        if (diff > 0) {
            List<CubeMove> moves = new ArrayList<>();
            for (int i = diff - 1; i >= 0; i--) {
                int face = getBitWord(data, 12 + 5 * i, 4);
                int direction = getBitWord(data, 16 + 5 * i, 1);
                int elapsed = getBitWord(data, 47 + 16 * i, 16);
                if (elapsed == 0) {
                    elapsed = (int) (timestamp - lastMoveTimestamp);
                }

                int cubeFace = face;
                int cubeDir = direction == 0 ? CubeMove.CW : CubeMove.CCW;
                if (cubeFace >= 0 && cubeFace <= 5) {
                    moves.add(new CubeMove(cubeFace, cubeDir, timestamp));
                }
            }
            lastMoveTimestamp = timestamp;

            if (BuildConfig.DEBUG && !moves.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (CubeMove m : moves) sb.append(m.toNotation()).append(' ');
                Log.d(TAG, "Move serial=" + serial + " moves=" + sb.toString().trim()
                        + " kociemba=" + lastKociemba + " isSolved=" + lastSolved);
            }

            if (!moves.isEmpty() && callback != null) {
                callback.onCubeMoves(moves);
            }
        }
    }

    // Facelet maps in Kociemba order: U=0-8, R=9-17, F=18-26, D=27-35, L=36-44, B=45-53
    private static final int[][] CORNER_FACELET_MAP = {
        {8, 9, 20},   // URF
        {6, 18, 38},  // UFL
        {0, 36, 47},  // ULB
        {2, 45, 11},  // UBR
        {29, 26, 15}, // DFR
        {27, 44, 24}, // DLF
        {33, 53, 42}, // DBL
        {35, 17, 51}  // DRB
    };

    private static final int[][] EDGE_FACELET_MAP = {
        {5, 10},  // UR
        {7, 19},  // UF
        {3, 37},  // UL
        {1, 46},  // UB
        {32, 16}, // DR
        {28, 25}, // DF
        {30, 43}, // DL
        {34, 52}, // DB
        {23, 12}, // FR
        {21, 41}, // FL
        {50, 39}, // BL
        {48, 14}  // BR
    };

    // TS-to-CS position mapping: TS[i] → CS[posMap[i]]
    private static final int[] TS_TO_CS_POS = buildTsToCsMap();
    // TS face char → CS face index
    private static final int[] CHAR_TO_FACE = new int[256];
    static {
        CHAR_TO_FACE['U'] = 0;
        CHAR_TO_FACE['R'] = 3;
        CHAR_TO_FACE['F'] = 2;
        CHAR_TO_FACE['D'] = 5;
        CHAR_TO_FACE['L'] = 1;
        CHAR_TO_FACE['B'] = 4;
    }

    private static int[] buildTsToCsMap() {
        int[] map = new int[54];
        // TS: U=0-8, R=9-17, F=18-26, D=27-35, L=36-44, B=45-53
        // CS: U=0-8, L=9-17, F=18-26, R=27-35, B=36-44, D=45-53
        for (int i = 0; i < 9; i++) {
            map[i] = i;         // U → U
            map[9 + i] = 27 + i;  // R → R (TS R=9→CS R=27)
            map[18 + i] = 18 + i; // F → F
            map[27 + i] = 45 + i; // D → D (TS D=27→CS D=45)
            map[36 + i] = 9 + i;  // L → L (TS L=36→CS L=9)
            map[45 + i] = 36 + i; // B → B (TS B=45→CS B=36)
        }
        return map;
    }

    private void handleFaceletsEvent(byte[] data, long timestamp) {
        int serial = getBitWord(data, 4, 8);
        if (lastSerial == -1) lastSerial = serial;

        // Corner Permutation: bits 12-32 (7 values, 3 bits each)
        int[] cp = new int[8];
        int cpSum = 0;
        for (int i = 0; i < 7; i++) {
            cp[i] = getBitWord(data, 12 + i * 3, 3);
            cpSum += cp[i];
        }
        cp[7] = 28 - cpSum;

        // Corner Orientation: bits 33-46 (7 values, 2 bits each)
        int[] co = new int[8];
        int coSum = 0;
        for (int i = 0; i < 7; i++) {
            co[i] = getBitWord(data, 33 + i * 2, 2);
            coSum += co[i];
        }
        co[7] = (3 - (coSum % 3)) % 3;

        // Edge Permutation: bits 47-90 (11 values, 4 bits each)
        int[] ep = new int[12];
        int epSum = 0;
        for (int i = 0; i < 11; i++) {
            ep[i] = getBitWord(data, 47 + i * 4, 4);
            epSum += ep[i];
        }
        ep[11] = 66 - epSum;

        // Edge Orientation: bits 91-101 (11 values, 1 bit each)
        int[] eo = new int[12];
        int eoSum = 0;
        for (int i = 0; i < 11; i++) {
            eo[i] = getBitWord(data, 91 + i, 1);
            eoSum += eo[i];
        }
        eo[11] = (2 - (eoSum % 2)) % 2;

        // Build Kociemba facelet string
        String kociemba = toKociembaFacelets(cp, co, ep, eo);

        // Convert to CubeState internal format and check solved
        int[] csFacelets = convertToCsFacelets(kociemba);
        cubeState.setFacelets(csFacelets);
        boolean solved = cubeState.isSolved();
        lastKociemba = kociemba;
        lastSolved = solved;
        if (callback != null) callback.onFaceletsReceived(csFacelets);
        if (solved && callback != null) {
            callback.onCubeSolved();
        }
    }

    private String toKociembaFacelets(int[] cp, int[] co, int[] ep, int[] eo) {
        char[] faces = {'U', 'R', 'F', 'D', 'L', 'B'};
        char[] facelets = new char[54];
        for (int i = 0; i < 54; i++) facelets[i] = faces[i / 9];

        for (int i = 0; i < 8; i++) {
            for (int p = 0; p < 3; p++) {
                facelets[CORNER_FACELET_MAP[i][(p + co[i]) % 3]] =
                    faces[CORNER_FACELET_MAP[cp[i]][p] / 9];
            }
        }
        for (int i = 0; i < 12; i++) {
            for (int p = 0; p < 2; p++) {
                facelets[EDGE_FACELET_MAP[i][(p + eo[i]) % 2]] =
                    faces[EDGE_FACELET_MAP[ep[i]][p] / 9];
            }
        }
        return new String(facelets);
    }

    private int[] convertToCsFacelets(String kociemba) {
        int[] result = new int[54];
        for (int i = 0; i < 54; i++) {
            int csIdx = TS_TO_CS_POS[i];
            result[csIdx] = CHAR_TO_FACE[kociemba.charAt(i)];
        }
        return result;
    }

    // Last raw orientation reported by the cube and the "home" offset (inverse of the orientation
    // captured at the last resetGyro()). Delivered quaternion = raw * homeInverse, so the cube is
    // shown in its reference orientation when held as it was at reset time.
    private final float[] lastRawQuat = {1f, 0f, 0f, 0f};
    private final float[] gyroHomeInv = {1f, 0f, 0f, 0f};

    private void handleGyroEvent(byte[] data, long timestamp) {
        if (callback == null) return;
        // GAN Gen2 gyro event: quaternion components as signed 16-bit words.
        int rw = getBitWord(data, 4, 16);
        int rx = getBitWord(data, 20, 16);
        int ry = getBitWord(data, 36, 16);
        int rz = getBitWord(data, 52, 16);
        float w = quatComponent(rw);
        float x = quatComponent(rx);
        float y = quatComponent(ry);
        float z = quatComponent(rz);
        // Remap the GAN IMU axes to the 3D-model frame (Y up): (x, y, z) -> (x, z, -y).
        // Matches afedotov/gan-cube-sample: new THREE.Quaternion(qx, qz, -qy, qw).
        lastRawQuat[0] = w;
        lastRawQuat[1] = x;
        lastRawQuat[2] = z;
        lastRawQuat[3] = -y;
        // Apply the home offset on the LEFT (frame change): out = homeInverse * raw.
        float[] out = quatMul(gyroHomeInv, lastRawQuat);
        callback.onGyroData(out[0], out[1], out[2], out[3]);
    }

    /** Re-homes the gyroscope so the cube's current physical orientation becomes the reference. */
    public void resetGyro() {
        // homeInverse = conjugate(current raw orientation).
        gyroHomeInv[0] = lastRawQuat[0];
        gyroHomeInv[1] = -lastRawQuat[1];
        gyroHomeInv[2] = -lastRawQuat[2];
        gyroHomeInv[3] = -lastRawQuat[3];
    }

    // Hamilton product of two quaternions (w, x, y, z).
    private static float[] quatMul(float[] a, float[] b) {
        return new float[]{
            a[0] * b[0] - a[1] * b[1] - a[2] * b[2] - a[3] * b[3],
            a[0] * b[1] + a[1] * b[0] + a[2] * b[3] - a[3] * b[2],
            a[0] * b[2] - a[1] * b[3] + a[2] * b[0] + a[3] * b[1],
            a[0] * b[3] + a[1] * b[2] - a[2] * b[1] + a[3] * b[0],
        };
    }

    // Converts a raw 16-bit quaternion word to [-1, 1]: bit 15 is the sign, bits 0-14 the magnitude.
    private static float quatComponent(int raw) {
        return (1 - (raw >> 15) * 2) * (raw & 0x7FFF) / 32767.0f;
    }

    private void handleBatteryEvent(byte[] data, long timestamp) {
        int level = getBitWord(data, 8, 8);
        level = Math.min(level, 100);
        if (level != lastBatteryLevel && level > 0) {
            lastBatteryLevel = level;
            if (callback != null) callback.onCubeBatteryLevel(level);
        }
    }

    private void handleHardwareEvent(byte[] data, long timestamp) {
        StringBuilder name = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            int ch = getBitWord(data, 40 + i * 8, 8);
            if (ch >= 32 && ch <= 126) {
                name.append((char) ch);
            }
        }
        Log.d(TAG, "Hardware: " + name.toString().trim());
    }

    public void sendCommand(byte[] command) {
        if (cubeCommandCharacteristic == null) return;
        byte[] encrypted = encryptData(command);
        if (encrypted != null) {
            writeCharacteristic(cubeCommandCharacteristic, encrypted,
                    BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE).enqueue();
        }
    }

    public void requestFacelets() {
        byte[] cmd = new byte[20];
        cmd[0] = 0x04;
        sendCommand(cmd);
    }

    /** Requests the battery level; the cube replies with a type-9 event -> onCubeBatteryLevel. */
    public void requestBattery() {
        byte[] cmd = new byte[20];
        cmd[0] = 0x09;
        sendCommand(cmd);
    }

    /**
     * Resets the cube's internal facelet state to solved (GAN Gen2 reset command). Use when the
     * tracked state has drifted from the physical cube: physically solve the cube, then send this.
     */
    public void requestReset() {
        int[] reset = {0x0A, 0x05, 0x39, 0x77, 0x00, 0x00, 0x01, 0x23, 0x45, 0x67, 0x89, 0xAB};
        byte[] cmd = new byte[20];
        for (int i = 0; i < reset.length; i++) cmd[i] = (byte) reset[i];
        sendCommand(cmd);
    }

    @Override
    protected void onServicesInvalidated() {
        cubeCommandCharacteristic = null;
        cubeStateCharacteristic = null;
        lastSerial = -1;
        if (callback != null) callback.onCubeDisconnected();
    }
}

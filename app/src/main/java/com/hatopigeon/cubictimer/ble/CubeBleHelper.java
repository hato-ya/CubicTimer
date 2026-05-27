package com.hatopigeon.cubictimer.ble;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.afollestad.materialdialogs.MaterialDialog;
import com.hatopigeon.cubicify.R;
import com.hatopigeon.cubictimer.CubicTimer;
import com.hatopigeon.cubictimer.cube.CubeMove;
import com.hatopigeon.cubictimer.utils.ThemeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

import static com.hatopigeon.cubictimer.utils.TTIntent.ACTION_CUBE_CONNECTED;
import static com.hatopigeon.cubictimer.utils.TTIntent.ACTION_CUBE_DISCONNECTED;
import static com.hatopigeon.cubictimer.utils.TTIntent.CATEGORY_UI_INTERACTIONS;
import static com.hatopigeon.cubictimer.utils.TTIntent.broadcast;

public class CubeBleHelper {

    private static final String TAG = "CubeBleHelper";

    private static final GanCubeManager.GanCubeCallback sDefaultCallback =
            new GanCubeManager.GanCubeCallback() {
        @Override
        public void onCubeConnected() {
            Log.d(TAG, "Cube connected (default callback)");
            broadcast(CATEGORY_UI_INTERACTIONS, ACTION_CUBE_CONNECTED);
        }

        @Override
        public void onCubeDisconnected() {
            Log.d(TAG, "Cube disconnected (default callback)");
            broadcast(CATEGORY_UI_INTERACTIONS, ACTION_CUBE_DISCONNECTED);
        }

        @Override
        public void onCubeMoves(List<CubeMove> moves) {}

        @Override
        public void onCubeBatteryLevel(int level) {}

        @Override
        public void onCubeSolved() {}
    };

    private static MaterialDialog sDialog;
    private static ArrayList<BluetoothDevice> sDevices;
    private static long sScanPeriod;
    private static boolean sIsScanning = false;
    private static ScanCallback sScanCallback;
    private static GanCubeManager.GanCubeCallback sPendingCallback;

    public static void startScan(@NonNull Activity activity,
                                 @Nullable GanCubeManager.GanCubeCallback callback) {
        if (!activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.d(TAG, "No BLE support");
            return;
        }

        GanCubeManager mgr = CubicTimer.getCubeBleManager();
        if (mgr != null && mgr.isConnected()) {
            CubicTimer.clearCubeBleManager();
            broadcast(CATEGORY_UI_INTERACTIONS, ACTION_CUBE_DISCONNECTED);
            return;
        }

        if (sIsScanning) {
            Log.d(TAG, "Already scanning");
            return;
        }

        sPendingCallback = callback;

        ArrayList<String> requestPermissions = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_SCAN)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions.add(Manifest.permission.BLUETOOTH_SCAN);
            }
        } else {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ThemeUtils.roundDialog(activity, new MaterialDialog.Builder(activity)
                        .title(R.string.ble_permission_title)
                        .content(R.string.ble_permission_content)
                        .positiveText(R.string.ble_permission_next)
                        .onPositive((dialog, which) ->
                            ActivityCompat.requestPermissions(activity,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    9801))
                        .show());
                return;
            }
        }

        if (!requestPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(activity,
                    requestPermissions.toArray(new String[0]), 9801);
            return;
        }

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) return;
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, 9802);
            return;
        }

        startScanInternal(activity, 500);
    }

    private static void startScanInternal(Activity activity, long reportDelayMillis) {
        if (sIsScanning) return;
        sIsScanning = true;
        sScanPeriod = reportDelayMillis;
        if (sDevices == null) {
            sDevices = new ArrayList<>();
        }

        if (sDialog == null) {
            sDialog = ThemeUtils.roundDialog(activity, new MaterialDialog.Builder(activity)
                    .title(R.string.smart_cube_scan_title)
                    .content(R.string.ble_scan_content)
                    .items(new ArrayList<CharSequence>())
                    .itemsCallback((dialog, view, which, text) -> {
                        if (sDevices == null || which < 0 || which >= sDevices.size()
                                || sDevices.get(which) == null) return;
                        BluetoothDevice device = sDevices.get(which);
                        connectToCube(activity, device);
                        cleanupScan();
                    })
                    .negativeText(R.string.ble_scan_cancel)
                    .onAny((dialog, which) -> cleanupScan())
                    .show());
            sDialog.setOnCancelListener(dialog -> cleanupScan());
        }

        BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        ScanSettings settings = new ScanSettings.Builder()
                .setLegacy(false)
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setReportDelay(reportDelayMillis)
                .setUseHardwareBatchingIfSupported(false)
                .build();
        List<ScanFilter> filters = new ArrayList<>();

        sScanCallback = new ScanCallback() {
            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
                Log.d(TAG, "Batch results: " + results.size());

                for (ScanResult result : results) {
                    String name = result.getDevice().getName();
                    if (name != null && name.toUpperCase(Locale.US).startsWith("GAN")
                            && !name.contains("Timer")) {
                        if (!sDevices.contains(result.getDevice())) {
                            sDevices.add(result.getDevice());
                        }
                    }
                }
                Collections.sort(sDevices,
                        (d1, d2) -> d1.getAddress().compareTo(d2.getAddress()));

                ArrayList<CharSequence> items = new ArrayList<>();
                for (BluetoothDevice device : sDevices) {
                    items.add(device.getName() != null
                            ? device.getName() + " (" + device.getAddress() + ")"
                            : device.getAddress());
                }
                if (sDialog != null) {
                    sDialog.setItems(items.toArray(new CharSequence[0]));
                }

                if (sScanPeriod == 500 && !results.isEmpty()) {
                    stopScanner();
                    startScanInternal(activity, 5000);
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.d(TAG, "Scan failed: " + errorCode);
                cleanupScan();
            }
        };

        scanner.startScan(filters, settings, sScanCallback);
    }

    private static void connectToCube(Activity activity, BluetoothDevice device) {
        String mac = device.getAddress();
        Log.d(TAG, "Connecting to cube: " + mac);

        GanCubeManager.GanCubeCallback cb = sPendingCallback != null
                ? sPendingCallback : sDefaultCallback;
        sPendingCallback = null;

        GanCubeManager mgr = new GanCubeManager(activity, cb, mac);
        CubicTimer.setCubeBleManager(mgr, mac);
        mgr.connect(device).enqueue();
    }

    private static void stopScanner() {
        if (sScanCallback != null) {
            BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
            scanner.stopScan(sScanCallback);
            sScanCallback = null;
        }
        sIsScanning = false;
    }

    private static void cleanupScan() {
        stopScanner();
        if (sDialog != null) {
            sDialog.dismiss();
            sDialog = null;
        }
        sDevices = null;
        sPendingCallback = null;
    }
}

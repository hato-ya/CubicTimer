package com.hatopigeon.cubictimer.layout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnimationUtils;

import androidx.annotation.Nullable;

import com.hatopigeon.cubicify.R;
import com.hatopigeon.cubictimer.cube.CubeMove;
import com.hatopigeon.cubictimer.cube.CubeState;
import com.hatopigeon.cubictimer.utils.Prefs;

import java.util.ArrayDeque;

/**
 * Canvas-based 3D view of a 3x3 cube built from 26 small cubies (each a little cube with up to 3
 * coloured stickers on a black body). Oriented by a quaternion (e.g. live from a smart cube
 * gyroscope) and seen from a fixed 3/4 camera so three faces always show with real depth.
 *
 * Each move turns a layer: the cubies of that layer are pre-rotated by an animated angle, then the
 * facelet colours are permuted at the end of the animation (BLE facelet pushes are too slow to
 * follow individual moves, so the move permutation drives the colours).
 *
 * Facelet indexing matches CubeState: U=0-8, L=9-17, F=18-26, R=27-35, B=36-44, D=45-53.
 */
public class Cube3DView extends View {

    private static final float CUBIE_HALF = 0.47f;   // half-size of each small cubie
    private static final float STICKER_HALF = 0.40f; // coloured sticker half-size on a cubie face
    private static final float CUBE_RADIUS = 2.7f;   // bounding radius so the whole cube fits
    private static final float CAM_DIST = 12f;       // camera distance for perspective (bigger = flatter)
    private static final int BODY_COLOR = 0xFF161616;
    private static final int ANIM_DURATION_MS = 80;

    // Fixed 3/4 camera (applied after the gyroscope): yaw then pitch. Shows White/Orange/Green.
    private static final float SY = (float) Math.sin(Math.toRadians(35));
    private static final float CY = (float) Math.cos(Math.toRadians(35));
    private static final float SP = (float) Math.sin(Math.toRadians(28));
    private static final float CP = (float) Math.cos(Math.toRadians(28));

    // faceDir indices: 0:+x(R) 1:-x(L) 2:+y(U) 3:-y(D) 4:+z(F) 5:-z(B)
    private static final int[][] DIR_VEC = {{1,0,0},{-1,0,0},{0,1,0},{0,-1,0},{0,0,1},{0,0,-1}};
    private static final int[] LOOKUP = new int[27 * 6]; // [cubie*6 + faceDir] -> facelet, or -1

    /** One drawable cubie face (black body quad, plus a coloured sticker quad if exterior). */
    private static final class Tile {
        final int[] cubie = new int[3];        // cubie position, each in {-1,0,1}
        final float[][] body = new float[4][3];
        final float[] normal = new float[3];
        boolean exterior;
        int facelet = -1;
        final float[][] sticker = new float[4][3];
    }
    private static final Tile[] TILES;
    static { TILES = buildGeometry(); }

    private static final float SMOOTH = 0.3f; // orientation interpolation per frame (0..1)

    private final int[] facelets = new int[54];
    private final float[] quat = {1f, 0f, 0f, 0f};       // displayed orientation
    private final float[] targetQuat = {1f, 0f, 0f, 0f}; // latest gyro orientation
    private final int[] faceColors = new int[6];

    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();

    // Animation state.
    private final ArrayDeque<CubeMove> pending = new ArrayDeque<>();
    private final CubeState animHelper = new CubeState();
    private boolean animating;
    private int animAxis;        // 0=x,1=y,2=z
    private int animLayerSign;   // -1 or +1
    private float animTotalAngle;// radians
    private long animStart;
    private int animDuration;
    private CubeMove animMove;

    // Per-frame scratch.
    private final float[][][] pBody = new float[156][4][2];
    private final float[][][] pStick = new float[156][4][2];
    private final float[] depth = new float[156];
    private final boolean[] vis = new boolean[156];
    private final Integer[] order = new Integer[156];

    public Cube3DView(Context context) { this(context, null); }

    public Cube3DView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        for (int i = 0; i < 54; i++) facelets[i] = i / 9;
        for (int i = 0; i < order.length; i++) order[i] = i;
        fill.setStyle(Paint.Style.FILL);
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setColor(0xFF0D0D0D);
        stroke.setStrokeWidth(1.5f);
        stroke.setStrokeJoin(Paint.Join.ROUND);
        loadColorScheme();
    }

    public void loadColorScheme() {
        faceColors[0] = parse(Prefs.getString(R.string.pk_cube_top_color, "FFFFFF"));
        faceColors[1] = parse(Prefs.getString(R.string.pk_cube_left_color, "FF8B24"));
        faceColors[2] = parse(Prefs.getString(R.string.pk_cube_front_color, "02D040"));
        faceColors[3] = parse(Prefs.getString(R.string.pk_cube_right_color, "EC0000"));
        faceColors[4] = parse(Prefs.getString(R.string.pk_cube_back_color, "304FFE"));
        faceColors[5] = parse(Prefs.getString(R.string.pk_cube_down_color, "FDD835"));
        postInvalidate();
    }

    private static int parse(String hex) {
        try { return Color.parseColor("#" + hex); } catch (Exception e) { return Color.GRAY; }
    }

    /** Replaces the displayed state. Ignored while turning so move animations stay authoritative. */
    public void setFacelets(int[] csFacelets) {
        if (csFacelets == null || csFacelets.length < 54) return;
        if (animating || !pending.isEmpty()) return;
        System.arraycopy(csFacelets, 0, facelets, 0, 54);
        postInvalidate();
    }

    public void setQuaternion(float w, float x, float y, float z) {
        float n = (float) Math.sqrt(w * w + x * x + y * y + z * z);
        if (n < 1e-6f) return;
        // Store as the target; onDraw eases the displayed orientation toward it for smoothness.
        targetQuat[0] = w / n; targetQuat[1] = x / n; targetQuat[2] = y / n; targetQuat[3] = z / n;
        postInvalidateOnAnimation();
    }

    // Eases the displayed quaternion toward the target (shortest path). Returns true when settled.
    private boolean stepOrientation() {
        float[] q = quat, t = targetQuat;
        float dot = q[0] * t[0] + q[1] * t[1] + q[2] * t[2] + q[3] * t[3];
        float s = dot < 0 ? -1f : 1f; // take the shortest arc
        if (Math.abs(dot) > 0.99995f) {
            q[0] = s * t[0]; q[1] = s * t[1]; q[2] = s * t[2]; q[3] = s * t[3];
            return true;
        }
        for (int i = 0; i < 4; i++) q[i] += (s * t[i] - q[i]) * SMOOTH;
        float n = (float) Math.sqrt(q[0] * q[0] + q[1] * q[1] + q[2] * q[2] + q[3] * q[3]);
        if (n > 1e-6f) { q[0] /= n; q[1] /= n; q[2] /= n; q[3] /= n; }
        return false;
    }

    /** Queues a move to animate. Safe to call from any thread. */
    public void animateMove(CubeMove move) {
        if (move == null) return;
        post(() -> {
            pending.add(move);
            if (!animating) startNextMove();
            postInvalidateOnAnimation();
        });
    }

    private void startNextMove() {
        CubeMove m = pending.poll();
        if (m == null) { animating = false; return; }
        animMove = m;
        // Per move face (URFDLB): layer axis and outer sign.
        switch (m.face) {
            case CubeMove.U: animAxis = 1; animLayerSign = 1; break;
            case CubeMove.D: animAxis = 1; animLayerSign = -1; break;
            case CubeMove.R: animAxis = 0; animLayerSign = 1; break;
            case CubeMove.L: animAxis = 0; animLayerSign = -1; break;
            case CubeMove.F: animAxis = 2; animLayerSign = 1; break;
            default:         animAxis = 2; animLayerSign = -1; break; // B
        }
        // CW (viewed from outside the face) = -90deg about the outward normal; about the +axis that
        // is sign * -90deg. direction: CW=+1, CCW=-1, DOUBLE=+2.
        float base = (float) Math.toRadians(-90.0 * m.direction);
        animTotalAngle = animLayerSign * base;
        animDuration = pending.size() >= 4 ? 30 : ANIM_DURATION_MS; // catch up on bursts
        animStart = AnimationUtils.currentAnimationTimeMillis();
        animating = true;
    }

    private void finalizeMove() {
        animHelper.setFacelets(facelets);
        animHelper.applyMove(animMove);
        System.arraycopy(animHelper.getFacelets(), 0, facelets, 0, 54);
        startNextMove();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        // Leave a little headroom for the perspective foreshortening of near vertices.
        float scale = Math.min(getWidth(), getHeight()) / 2f / (CUBE_RADIUS * 1.08f);

        // Ease the displayed orientation toward the latest gyro reading (smooths jitter/steps).
        boolean quatSettled = stepOrientation();

        // Advance the animation.
        float angle = 0f;
        int curAxis = -1, curLayer = 0;
        if (animating) {
            long now = AnimationUtils.currentAnimationTimeMillis();
            float t = (now - animStart) / (float) animDuration;
            if (t >= 1f) {
                finalizeMove();
                if (animating) {
                    t = (now - animStart) / (float) animDuration;
                    angle = animTotalAngle * Math.min(Math.max(t, 0f), 1f);
                    curAxis = animAxis; curLayer = animLayerSign;
                }
            } else {
                angle = animTotalAngle * t;
                curAxis = animAxis; curLayer = animLayerSign;
            }
        }
        float ac = (float) Math.cos(angle), as = (float) Math.sin(angle);

        for (int i = 0; i < TILES.length; i++) {
            Tile tl = TILES[i];
            boolean turning = curAxis >= 0 && tl.cubie[curAxis] == curLayer;

            float[] n = turning ? rotate(layerRotate(tl.normal, curAxis, ac, as)) : rotate(tl.normal);
            vis[i] = n[2] > 0.01f;
            if (!vis[i]) { depth[i] = 0; continue; }

            float zSum = 0;
            for (int k = 0; k < 4; k++) {
                float[] src = turning ? layerRotate(tl.body[k], curAxis, ac, as) : tl.body[k];
                float[] r = rotate(src);
                float f = CAM_DIST / (CAM_DIST - r[2]);
                pBody[i][k][0] = cx + r[0] * scale * f;
                pBody[i][k][1] = cy - r[1] * scale * f;
                zSum += r[2];
                if (tl.exterior) {
                    float[] s2 = turning ? layerRotate(tl.sticker[k], curAxis, ac, as) : tl.sticker[k];
                    float[] rs = rotate(s2);
                    float fs = CAM_DIST / (CAM_DIST - rs[2]);
                    pStick[i][k][0] = cx + rs[0] * scale * fs;
                    pStick[i][k][1] = cy - rs[1] * scale * fs;
                }
            }
            depth[i] = zSum / 4f;
        }

        java.util.Arrays.sort(order, (a, b) -> Float.compare(depth[a], depth[b]));

        for (int oi = 0; oi < order.length; oi++) {
            int i = order[oi];
            if (!vis[i]) continue;
            Tile tl = TILES[i];
            fill.setColor(BODY_COLOR);
            drawQuad(canvas, pBody[i], true);
            if (tl.exterior) {
                fill.setColor(faceColors[facelets[tl.facelet]]);
                drawQuad(canvas, pStick[i], false);
            }
        }

        if (animating || !quatSettled) postInvalidateOnAnimation();
    }

    private void drawQuad(Canvas canvas, float[][] q, boolean withStroke) {
        path.rewind();
        path.moveTo(q[0][0], q[0][1]);
        path.lineTo(q[1][0], q[1][1]);
        path.lineTo(q[2][0], q[2][1]);
        path.lineTo(q[3][0], q[3][1]);
        path.close();
        canvas.drawPath(path, fill);
        if (withStroke) canvas.drawPath(path, stroke);
    }

    // Rotate a point about a +axis by the given cos/sin (layer-turn animation).
    private final float[] lr = new float[3];
    private float[] layerRotate(float[] v, int axis, float c, float s) {
        float x = v[0], y = v[1], z = v[2];
        switch (axis) {
            case 0: lr[0] = x;            lr[1] = y * c - z * s; lr[2] = y * s + z * c; break;
            case 1: lr[0] = x * c + z * s; lr[1] = y;            lr[2] = -x * s + z * c; break;
            default: lr[0] = x * c - y * s; lr[1] = x * s + y * c; lr[2] = z; break;
        }
        return lr;
    }

    // Gyroscope quaternion rotation, then the fixed 3/4 camera (yaw around Y, pitch around X).
    private final float[] tmp = new float[3];
    private float[] rotate(float[] v) {
        float w = quat[0], qx = quat[1], qy = quat[2], qz = quat[3];
        float tx = 2f * (qy * v[2] - qz * v[1]);
        float ty = 2f * (qz * v[0] - qx * v[2]);
        float tz = 2f * (qx * v[1] - qy * v[0]);
        float rx = v[0] + w * tx + (qy * tz - qz * ty);
        float ry = v[1] + w * ty + (qz * tx - qx * tz);
        float rz = v[2] + w * tz + (qx * ty - qy * tx);
        float yx = rx * CY + rz * SY;
        float yz = -rx * SY + rz * CY;
        tmp[0] = yx;
        tmp[1] = ry * CP - yz * SP;
        tmp[2] = ry * SP + yz * CP;
        return tmp;
    }

    // ── Geometry ──
    private interface PosFn { int[] at(int r, int c); }

    private static Tile[] buildGeometry() {
        java.util.Arrays.fill(LOOKUP, -1);
        // Map each facelet to (cubie, faceDir) using the CubeState net layout.
        buildLookup(0, 2,  (r, c) -> new int[]{c - 1, 1, r - 1});   // U  (+y)
        buildLookup(9, 1,  (r, c) -> new int[]{-1, 1 - r, c - 1});  // L  (-x)
        buildLookup(18, 4, (r, c) -> new int[]{c - 1, 1 - r, 1});   // F  (+z)
        buildLookup(27, 0, (r, c) -> new int[]{1, 1 - r, 1 - c});   // R  (+x)
        buildLookup(36, 5, (r, c) -> new int[]{1 - c, 1 - r, -1});  // B  (-z)
        buildLookup(45, 3, (r, c) -> new int[]{c - 1, -1, 1 - r});  // D  (-y)

        java.util.ArrayList<Tile> tiles = new java.util.ArrayList<>();
        for (int cx = -1; cx <= 1; cx++)
            for (int cy = -1; cy <= 1; cy++)
                for (int cz = -1; cz <= 1; cz++) {
                    if (cx == 0 && cy == 0 && cz == 0) continue;
                    for (int dir = 0; dir < 6; dir++) {
                        tiles.add(makeTile(cx, cy, cz, dir));
                    }
                }
        return tiles.toArray(new Tile[0]);
    }

    private static void buildLookup(int base, int faceDir, PosFn fn) {
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++) {
                int[] p = fn.at(r, c);
                int cubie = (p[0] + 1) * 9 + (p[1] + 1) * 3 + (p[2] + 1);
                LOOKUP[cubie * 6 + faceDir] = base + 3 * r + c;
            }
    }

    private static Tile makeTile(int cx, int cy, int cz, int dir) {
        Tile t = new Tile();
        t.cubie[0] = cx; t.cubie[1] = cy; t.cubie[2] = cz;
        int[] nrm = DIR_VEC[dir];
        t.normal[0] = nrm[0]; t.normal[1] = nrm[1]; t.normal[2] = nrm[2];

        // Tangent axes for this face.
        float[] u, v;
        if (nrm[0] != 0) { u = new float[]{0, 1, 0}; v = new float[]{0, 0, 1}; }
        else if (nrm[1] != 0) { u = new float[]{1, 0, 0}; v = new float[]{0, 0, 1}; }
        else { u = new float[]{1, 0, 0}; v = new float[]{0, 1, 0}; }

        float[] center = {cx + CUBIE_HALF * nrm[0], cy + CUBIE_HALF * nrm[1], cz + CUBIE_HALF * nrm[2]};
        int[][] signs = {{-1, -1}, {1, -1}, {1, 1}, {-1, 1}};
        for (int k = 0; k < 4; k++) {
            for (int a = 0; a < 3; a++) {
                t.body[k][a] = center[a] + CUBIE_HALF * signs[k][0] * u[a] + CUBIE_HALF * signs[k][1] * v[a];
            }
        }

        int cubieIdx = (cx + 1) * 9 + (cy + 1) * 3 + (cz + 1);
        int facelet = LOOKUP[cubieIdx * 6 + dir];
        t.exterior = facelet >= 0;
        t.facelet = facelet;
        if (t.exterior) {
            // Coloured sticker inset and pushed slightly out to sit on top of the body.
            float[] sc = {center[0] + 0.01f * nrm[0], center[1] + 0.01f * nrm[1], center[2] + 0.01f * nrm[2]};
            for (int k = 0; k < 4; k++) {
                for (int a = 0; a < 3; a++) {
                    t.sticker[k][a] = sc[a] + STICKER_HALF * signs[k][0] * u[a] + STICKER_HALF * signs[k][1] * v[a];
                }
            }
        }
        return t;
    }
}

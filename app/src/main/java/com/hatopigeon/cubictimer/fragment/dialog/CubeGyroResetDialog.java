package com.hatopigeon.cubictimer.fragment.dialog;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.hatopigeon.cubicify.R;
import com.hatopigeon.cubictimer.CubicTimer;
import com.hatopigeon.cubictimer.ble.GanCubeManager;
import com.hatopigeon.cubictimer.layout.Cube3DView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Popup that shows the live 3D cube model driven by the smart cube gyroscope. Tapping the cube
 * re-homes the orientation so the model snaps to the default view (White top / Orange left /
 * Green front). A "Reset cube state" button tells the cube its current position is solved.
 *
 * While open it takes over the cube callback (gyro, moves, facelets), restoring it on dismiss.
 */
public class CubeGyroResetDialog extends DialogFragment {

    private Unbinder mUnbinder;

    @BindView(R.id.cube_preview) Cube3DView cubePreview;
    @BindView(R.id.button_reset_state) TextView buttonResetState;

    private GanCubeManager.GanCubeCallback previousCallback;

    private final GanCubeManager.GanCubeCallback gyroCallback = new GanCubeManager.GanCubeCallback() {
        @Override
        public void onGyroData(float w, float x, float y, float z) {
            if (cubePreview != null) cubePreview.setQuaternion(w, x, y, z);
        }

        @Override
        public void onCubeMoves(java.util.List<com.hatopigeon.cubictimer.cube.CubeMove> moves) {
            if (cubePreview != null) for (com.hatopigeon.cubictimer.cube.CubeMove m : moves) cubePreview.animateMove(m);
        }

        @Override
        public void onFaceletsReceived(int[] csFacelets) {
            if (cubePreview != null) cubePreview.setFacelets(csFacelets);
        }

        @Override public void onCubeConnected() {}
        @Override public void onCubeDisconnected() {}
        @Override public void onCubeBatteryLevel(int level) {}
        @Override public void onCubeSolved() {}
    };

    public static CubeGyroResetDialog newInstance() {
        return new CubeGyroResetDialog();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View dialogView = inflater.inflate(R.layout.dialog_cube_gyro_reset, container);
        mUnbinder = ButterKnife.bind(this, dialogView);

        cubePreview.loadColorScheme();
        cubePreview.setOnClickListener(v -> onReset());
        buttonResetState.setOnClickListener(v -> onResetState());

        GanCubeManager mgr = CubicTimer.getCubeBleManager();
        if (mgr != null && mgr.isConnected()) {
            previousCallback = mgr.getCallback();
            mgr.setCallback(gyroCallback);
            mgr.requestFacelets(); // show the cube's real pattern
        }

        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialogView;
    }

    private void onReset() {
        GanCubeManager mgr = CubicTimer.getCubeBleManager();
        if (mgr == null || !mgr.isConnected()) {
            Toast.makeText(getContext(), R.string.smart_cube_reset_gyro_not_connected,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        mgr.resetGyro();
        // Snap the preview to the default orientation immediately for instant feedback.
        if (cubePreview != null) cubePreview.setQuaternion(1f, 0f, 0f, 0f);
    }

    private void onResetState() {
        GanCubeManager mgr = CubicTimer.getCubeBleManager();
        if (mgr != null && mgr.isConnected()) {
            mgr.requestReset();
            Toast.makeText(getContext(), R.string.smart_cube_reset_state_done,
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), R.string.smart_cube_reset_state_not_connected,
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        GanCubeManager mgr = CubicTimer.getCubeBleManager();
        if (mgr != null && mgr.getCallback() == gyroCallback) {
            mgr.setCallback(previousCallback);
        }
        previousCallback = null;
        mUnbinder.unbind();
    }
}

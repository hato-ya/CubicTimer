package com.hatopigeon.cubictimer.fragment.dialog;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.DialogFragment;

import com.hatopigeon.cubicify.R;
import com.hatopigeon.cubictimer.utils.Prefs;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Popup that shows the cube as a coloured net and lets the user pick which face the smart cube
 * should detect as the cross. Selecting nothing means "auto" (detect any solved cross).
 *
 * Stored in pk_smart_cube_cross_face as "auto" or a CubeState face index (top=0, left=1, front=2,
 * right=3, back=4, down=5), matching the values the timer reads.
 */
public class CubeCrossFaceSelectDialog extends DialogFragment {

    private static final String CROSS_BADGE = "✚";

    private Unbinder mUnbinder;

    @BindView(R.id.top)   TextView top;
    @BindView(R.id.left)  TextView left;
    @BindView(R.id.front) TextView front;
    @BindView(R.id.right) TextView right;
    @BindView(R.id.back)  TextView back;
    @BindView(R.id.down)  TextView down;
    @BindView(R.id.instruction) TextView instruction;
    @BindView(R.id.legend) TextView legend;
    @BindView(R.id.button_save) ImageView save;

    // Net position -> CubeState cross-face index.
    private static final int IDX_TOP = 0, IDX_LEFT = 1, IDX_FRONT = 2,
            IDX_RIGHT = 3, IDX_BACK = 4, IDX_DOWN = 5;

    private String crossValue; // "auto" or "0".."5"

    public static CubeCrossFaceSelectDialog newInstance() {
        return new CubeCrossFaceSelectDialog();
    }

    private int faceIndexForView(View v) {
        int id = v.getId();
        if (id == R.id.top)   return IDX_TOP;
        if (id == R.id.left)  return IDX_LEFT;
        if (id == R.id.front) return IDX_FRONT;
        if (id == R.id.right) return IDX_RIGHT;
        if (id == R.id.back)  return IDX_BACK;
        if (id == R.id.down)  return IDX_DOWN;
        return -1;
    }

    private final View.OnClickListener clickListener = v -> {
        String tapped = String.valueOf(faceIndexForView(v));
        crossValue = tapped.equals(crossValue) ? "auto" : tapped; // tapping the selected one = auto
        refresh();
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View dialogView = inflater.inflate(R.layout.dialog_cube_orientation_select, container);
        mUnbinder = ButterKnife.bind(this, dialogView);

        setColor(top,   Prefs.getString(R.string.pk_cube_top_color, "FFFFFF"));
        setColor(left,  Prefs.getString(R.string.pk_cube_left_color, "FF8B24"));
        setColor(front, Prefs.getString(R.string.pk_cube_front_color, "02D040"));
        setColor(right, Prefs.getString(R.string.pk_cube_right_color, "EC0000"));
        setColor(back,  Prefs.getString(R.string.pk_cube_back_color, "304FFE"));
        setColor(down,  Prefs.getString(R.string.pk_cube_down_color, "FDD835"));

        for (TextView face : new TextView[]{top, left, front, right, back, down}) {
            face.setOnClickListener(clickListener);
        }

        instruction.setText(R.string.smart_cube_cross_face_instruction);
        crossValue = Prefs.getString(R.string.pk_smart_cube_cross_face, "auto");
        refresh();

        save.setOnClickListener(v -> {
            Prefs.edit().putString(R.string.pk_smart_cube_cross_face, crossValue).apply();
            dismiss();
        });

        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialogView;
    }

    private void refresh() {
        boolean auto = "auto".equals(crossValue);
        for (TextView face : new TextView[]{top, left, front, right, back, down}) {
            boolean selected = !auto && crossValue.equals(String.valueOf(faceIndexForView(face)));
            face.setText(selected ? CROSS_BADGE : "");
            face.setAlpha(auto || selected ? 1f : 0.4f);
        }
        legend.setText(auto ? getString(R.string.smart_cube_cross_face_auto) : "");
    }

    private void setColor(View view, String hex) {
        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.square);
        Drawable wrap = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(wrap, Color.parseColor("#" + hex));
        DrawableCompat.setTintMode(wrap, PorterDuff.Mode.MULTIPLY);
        view.setBackground(wrap.mutate());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }
}

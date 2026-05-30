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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.DialogFragment;

import com.hatopigeon.cubicify.R;
import com.hatopigeon.cubictimer.utils.Prefs;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Popup that shows the cube as a coloured net and lets the user pick which face they hold on top
 * and which in front, so smart-cube scramble highlighting works for any holding orientation.
 *
 * Selections are stored as notation letters in pk_smart_cube_top_face / pk_smart_cube_front_face.
 * Each net position maps to a fixed face letter (top=U, left=L, front=F, right=R, back=B, down=D),
 * coloured with the user's colour scheme.
 */
public class CubeOrientationSelectDialog extends DialogFragment {

    private static final String TOP_BADGE = "▲";   // ▲
    private static final String FRONT_BADGE = "■"; // ■

    private Unbinder mUnbinder;

    @BindView(R.id.top)   TextView top;
    @BindView(R.id.left)  TextView left;
    @BindView(R.id.front) TextView front;
    @BindView(R.id.right) TextView right;
    @BindView(R.id.back)  TextView back;
    @BindView(R.id.down)  TextView down;
    @BindView(R.id.legend) TextView legend;
    @BindView(R.id.button_save) ImageView save;

    private String topLetter;
    private String frontLetter;

    public static CubeOrientationSelectDialog newInstance() {
        return new CubeOrientationSelectDialog();
    }

    private static String oppositeLetter(String f) {
        switch (f) {
            case "U": return "D";
            case "D": return "U";
            case "F": return "B";
            case "B": return "F";
            case "R": return "L";
            case "L": return "R";
            default:  return "";
        }
    }

    private String letterForView(View v) {
        int id = v.getId();
        if (id == R.id.top)   return "U";
        if (id == R.id.left)  return "L";
        if (id == R.id.front) return "F";
        if (id == R.id.right) return "R";
        if (id == R.id.back)  return "B";
        if (id == R.id.down)  return "D";
        return "";
    }

    private final View.OnClickListener clickListener = v -> {
        String letter = letterForView(v);
        if (letter.equals(topLetter)) {
            topLetter = null;            // tapping the current top clears it
        } else if (letter.equals(frontLetter)) {
            frontLetter = null;          // tapping the current front clears it
        } else if (topLetter == null) {
            topLetter = letter;          // first pick = top
        } else if (frontLetter == null) {
            frontLetter = letter;        // second pick = front
        } else {
            frontLetter = letter;        // both set: replace front, keep top
        }
        refreshBadges();
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

        topLetter = Prefs.getString(R.string.pk_smart_cube_top_face, "U");
        frontLetter = Prefs.getString(R.string.pk_smart_cube_front_face, "F");
        legend.setText(TOP_BADGE + " " + getString(R.string.smart_cube_orientation_top_label)
                + "    " + FRONT_BADGE + " " + getString(R.string.smart_cube_orientation_front_label));
        refreshBadges();

        save.setOnClickListener(v -> onSave());

        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialogView;
    }

    private void onSave() {
        if (topLetter == null || frontLetter == null
                || topLetter.equals(frontLetter) || oppositeLetter(topLetter).equals(frontLetter)) {
            Toast.makeText(getContext(), R.string.smart_cube_orientation_invalid,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        Prefs.edit()
                .putString(R.string.pk_smart_cube_top_face, topLetter)
                .putString(R.string.pk_smart_cube_front_face, frontLetter)
                .apply();
        dismiss();
    }

    private void refreshBadges() {
        for (TextView face : new TextView[]{top, left, front, right, back, down}) {
            String letter = letterForView(face);
            if (letter.equals(topLetter)) {
                face.setText(TOP_BADGE);
                face.setAlpha(1f);
            } else if (letter.equals(frontLetter)) {
                face.setText(FRONT_BADGE);
                face.setAlpha(1f);
            } else {
                face.setText("");
                face.setAlpha(0.4f);
            }
        }
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

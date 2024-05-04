package com.hatopigeon.cubictimer.fragment.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.ColorInt;
import androidx.fragment.app.DialogFragment;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.hatopigeon.cubicify.R;
import com.hatopigeon.cubictimer.CubicTimer;
import com.hatopigeon.cubictimer.activity.MainActivity;
import com.hatopigeon.cubictimer.spans.ChromaDialogFixed;
import com.hatopigeon.cubictimer.utils.Prefs;
import com.hatopigeon.cubictimer.utils.PuzzleUtils;
import com.hatopigeon.cubictimer.utils.ThemeUtils;
import com.pavelsikun.vintagechroma.IndicatorMode;
import com.pavelsikun.vintagechroma.OnColorSelectedListener;
import com.pavelsikun.vintagechroma.colormode.ColorMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Ari on 09/02/2016.
 */
public class SchemeSelectDialogMain extends DialogFragment {
    /**
     * A "tag" to identify this class in log messages.
     */
    private static final String TAG = SchemeSelectDialogMain.class.getSimpleName();

    private Unbinder mUnbinder;
    private Context mContext;

    private String mColorSchemeType;
    private String mColorSchemeName;

    @BindView(R.id.reset) TextView reset;
    @BindView(R.id.done)  TextView done;

    private int[] viewIdsCube = {R.id.top, R.id.left, R.id.front, R.id.right, R.id.back, R.id.down};
    private int[] viewIdsMega = {R.id.megaBL, R.id.megaBR, R.id.megaL, R.id.megaU, R.id.megaR, R.id.megaF, R.id.megaB, R.id.megaDBR, R.id.megaD, R.id.megaDBL, R.id.megaDR, R.id.megaDL};
    private int[] viewIdsPyra = {R.id.pyraL, R.id.pyraF, R.id.pyraR, R.id.pyraD};

    public static SchemeSelectDialogMain newInstance() {
        return new SchemeSelectDialogMain();
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(CubicTimer.getAppContext());
            final SharedPreferences.Editor editor = sp.edit();
            String currentHex = getHex(view.getId());
            new ChromaDialogFixed.Builder()
                    .initialColor(Color.parseColor("#" + currentHex))
                    .colorMode(ColorMode.RGB)
                    .indicatorMode(IndicatorMode.HEX)
                    .onColorSelected(new OnColorSelectedListener() {
                        @Override
                        public void onColorSelected(@ColorInt int color) {
                            String hexColor = Integer.toHexString(color).toUpperCase().substring(2);
                            setAndSaveColor(view, PuzzleUtils.colorInfo.get(view.getId()).face, hexColor);
                        }
                    })
                    .create()
                    .show(getFragmentManager(), "ChromaDialog");

        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View dialogView = inflater.inflate(R.layout.dialog_scheme_select_main, container);
        mUnbinder = ButterKnife.bind(this, dialogView);

        mContext = getContext();

        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        String currentPuzzle = Prefs.getString(R.string.pk_last_used_puzzle, PuzzleUtils.TYPE_333);
        mColorSchemeType = PuzzleUtils.getColorSchemeType(currentPuzzle);
        mColorSchemeName = PuzzleUtils.getColorSchemeName(currentPuzzle);
        Log.d(TAG, "CurrentPuzzle = " + currentPuzzle + ", colorSchemeType = " + mColorSchemeType+ ", colorSchemeName = " + mColorSchemeName);

        int[] viewIds;
        int idRightMost;
        switch (mColorSchemeType) {
            default:
            case PuzzleUtils.TYPE_333:
                viewIds = viewIdsCube;
                idRightMost = R.id.back;
                break;
            case PuzzleUtils.TYPE_MEGA:
                viewIds = viewIdsMega;
                idRightMost = R.id.megaDBL;
                break;
            case PuzzleUtils.TYPE_PYRA:
                viewIds = viewIdsPyra;
                idRightMost = R.id.pyraR;
                break;
/*
            case PuzzleUtils.TYPE_CLOCK:
                break;
 */
        }

        for (int viewId : viewIds) {
            View view = dialogView.findViewById(viewId);
            setColor(view, getColor(viewId));
            view.setOnClickListener(clickListener);
            view.setVisibility(View.VISIBLE);
            if (mColorSchemeType.equals(PuzzleUtils.TYPE_MEGA)) {
                ((PolygonView) view).setRegion(5);
            } else if (mColorSchemeType.equals(PuzzleUtils.TYPE_PYRA)) {
                ((PolygonView) view).setRegion(3);
            }
        }

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) done.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_RIGHT, idRightMost);
        done.setLayoutParams(params);

        reset.setOnClickListener(view -> ThemeUtils.roundAndShowDialog(mContext, new MaterialDialog.Builder(mContext)
                .content(R.string.reset_colorscheme)
                .positiveText(R.string.action_reset_colorscheme)
                .negativeText(R.string.action_cancel)
                .onPositive((dialog, which) -> {
                    for (int viewId : viewIds) {
                        PuzzleUtils.ColorInfo colorInfo = PuzzleUtils.colorInfo.get(viewId);
                        setAndSaveColor(dialogView.findViewById(viewId), colorInfo.face, colorInfo.defaultColor);
                    }
                })
                .build()));

        done.setOnClickListener(view -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).onRecreateRequired();
            }
            dismiss();
        });

        return dialogView;
    }

    private String getHex(int id) {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(CubicTimer.getAppContext());
        return sp.getString(PuzzleUtils.colorInfo.get(id).face + mColorSchemeName, PuzzleUtils.colorInfo.get(id).defaultColor);
    }
    private int getColor(int id) {
        return Color.parseColor("#"+getHex(id));
    }

    private void setAndSaveColor(View view, String key, String color) {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(CubicTimer.getAppContext());
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key + mColorSchemeName, color).apply();
        setColor(view, Color.parseColor("#"+color));
    }

    private void setColor(View view, int color) {
        int Rid;
        switch (mColorSchemeType) {
            default:
            case PuzzleUtils.TYPE_333:
                Rid = R.drawable.square;
                break;
            case PuzzleUtils.TYPE_MEGA:
                Rid = R.drawable.pentagon;
                break;
            case PuzzleUtils.TYPE_PYRA:
                Rid = R.drawable.triangle;
                break;
/*
            case PuzzleUtils.TYPE_CLOCK:
                break;
*/
        }
        Drawable drawable = ContextCompat.getDrawable(getContext(), Rid);
        Drawable wrap = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(wrap, color);
        DrawableCompat.setTintMode(wrap, PorterDuff.Mode.MULTIPLY);
        wrap = wrap.mutate();
        view.setBackground(wrap);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (getActivity() != null) {
            ((MainActivity) getActivity()).onRecreateRequired();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }
}

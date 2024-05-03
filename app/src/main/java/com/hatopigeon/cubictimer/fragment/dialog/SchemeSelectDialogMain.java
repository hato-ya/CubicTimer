package com.hatopigeon.cubictimer.fragment.dialog;

import android.content.Context;
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
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hatopigeon.cubicify.R;
import com.hatopigeon.cubictimer.CubicTimer;
import com.hatopigeon.cubictimer.activity.MainActivity;
import com.hatopigeon.cubictimer.fragment.TimerFragmentMain;
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
    private static final String TAG = TimerFragmentMain.class.getSimpleName();

    private Unbinder mUnbinder;
    private Context mContext;

    private String mColorSchemeType;

    @BindView(R.id.top)   View top;
    @BindView(R.id.left)  View left;
    @BindView(R.id.front) View front;
    @BindView(R.id.right) View right;
    @BindView(R.id.back)  View back;
    @BindView(R.id.down)  View down;
    @BindView(R.id.reset) TextView reset;
    @BindView(R.id.done)  TextView done;

    public static SchemeSelectDialogMain newInstance() {
        return new SchemeSelectDialogMain();
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(CubicTimer.getAppContext());
            final SharedPreferences.Editor editor = sp.edit();
            String currentHex = "FFFFFF";
            switch (view.getId()) {
                case R.id.top:
                    currentHex = sp.getString("cubeTop" + mColorSchemeType, "FFFFFF");
                    break;
                case R.id.left:
                    currentHex = sp.getString("cubeLeft" + mColorSchemeType, "FF8B24");
                    break;
                case R.id.front:
                    currentHex = sp.getString("cubeFront" + mColorSchemeType, "02D040");
                    break;
                case R.id.right:
                    currentHex = sp.getString("cubeRight" + mColorSchemeType, "EC0000");
                    break;
                case R.id.back:
                    currentHex = sp.getString("cubeBack" + mColorSchemeType, "304FFE");
                    break;
                case R.id.down:
                    currentHex = sp.getString("cubeDown" + mColorSchemeType, "FDD835");
                    break;
            }

            new ChromaDialogFixed.Builder()
                    .initialColor(Color.parseColor("#" + currentHex))
                    .colorMode(ColorMode.RGB)
                    .indicatorMode(IndicatorMode.HEX)
                    .onColorSelected(new OnColorSelectedListener() {
                        @Override
                        public void onColorSelected(@ColorInt int color) {
                            String hexColor = Integer.toHexString(color).toUpperCase().substring(2);
                            switch (view.getId()) {
                                case R.id.top:
                                    setColor(top, Color.parseColor("#" + hexColor));
                                    editor.putString("cubeTop" + mColorSchemeType, hexColor);
                                    break;
                                case R.id.left:
                                    setColor(left, Color.parseColor("#" + hexColor));
                                    editor.putString("cubeLeft" + mColorSchemeType, hexColor);
                                    break;
                                case R.id.front:
                                    setColor(front, Color.parseColor("#" + hexColor));
                                    editor.putString("cubeFront" + mColorSchemeType, hexColor);
                                    break;
                                case R.id.right:
                                    setColor(right, Color.parseColor("#" + hexColor));
                                    editor.putString("cubeRight" + mColorSchemeType, hexColor);
                                    break;
                                case R.id.back:
                                    setColor(back, Color.parseColor("#" + hexColor));
                                    editor.putString("cubeBack" + mColorSchemeType, hexColor);
                                    break;
                                case R.id.down:
                                    setColor(down, Color.parseColor("#" + hexColor));
                                    editor.putString("cubeDown" + mColorSchemeType, hexColor);
                                    break;
                            }
                            editor.apply();
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
        Log.d(TAG, "CurrentPuzzle = " + currentPuzzle + ", colorSchemeType = " + mColorSchemeType);

        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(CubicTimer.getAppContext());

        setColor(top, Color.parseColor("#" + sp.getString("cubeTop" + mColorSchemeType, "FFFFFF")));
        setColor(left, Color.parseColor("#" + sp.getString("cubeLeft" + mColorSchemeType, "FF8B24")));
        setColor(front, Color.parseColor("#" + sp.getString("cubeFront" + mColorSchemeType, "02D040")));
        setColor(right, Color.parseColor("#" + sp.getString("cubeRight" + mColorSchemeType, "EC0000")));
        setColor(back, Color.parseColor("#" + sp.getString("cubeBack" + mColorSchemeType, "304FFE")));
        setColor(down, Color.parseColor("#" + sp.getString("cubeDown" + mColorSchemeType, "FDD835")));

        top.setOnClickListener(clickListener);
        left.setOnClickListener(clickListener);
        front.setOnClickListener(clickListener);
        right.setOnClickListener(clickListener);
        back.setOnClickListener(clickListener);
        down.setOnClickListener(clickListener);

        reset.setOnClickListener(view -> ThemeUtils.roundAndShowDialog(mContext, new MaterialDialog.Builder(mContext)
                .content(R.string.reset_colorscheme)
                .positiveText(R.string.action_reset_colorscheme)
                .negativeText(R.string.action_cancel)
                .onPositive((dialog, which) -> {
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("cubeTop" + mColorSchemeType, "FFFFFF");
                    editor.putString("cubeLeft" + mColorSchemeType, "EF6C00");
                    editor.putString("cubeFront" + mColorSchemeType, "02D040");
                    editor.putString("cubeRight" + mColorSchemeType, "EC0000");
                    editor.putString("cubeBack" + mColorSchemeType, "304FFE");
                    editor.putString("cubeDown" + mColorSchemeType, "FDD835");
                    editor.apply();
                    setColor(top, Color.parseColor("#FFFFFF"));
                    setColor(left, Color.parseColor("#EF6C00"));
                    setColor(front, Color.parseColor("#02D040"));
                    setColor(right, Color.parseColor("#EC0000"));
                    setColor(back, Color.parseColor("#304FFE"));
                    setColor(down, Color.parseColor("#FDD835"));
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

    private void setColor(View view, int color) {
        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.square);
        Drawable wrap = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(wrap, color);
        DrawableCompat.setTintMode(wrap, PorterDuff.Mode.MULTIPLY);
        wrap = wrap.mutate();
        view.setBackground(wrap);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }
}

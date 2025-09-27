package com.hatopigeon.cubictimer.utils;

import android.content.res.Configuration;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class InsetsUtils {
    // Add padding considered status bar, navigation bar and display cutout
    public static void applySafeInsetsPadding(View v, boolean isDrawer) {

        final int baseL = v.getPaddingLeft();
        final int baseT = v.getPaddingTop();
        final int baseR = v.getPaddingRight();
        final int baseB = v.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(v, (view, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets cut = insets.getInsets(WindowInsetsCompat.Type.displayCutout());

            final int addL = Math.max(sys.left,   cut.left);
            final int addT = Math.max(sys.top,    cut.top);
            final int addR = Math.max(sys.right,  cut.right);
            final int addB = Math.max(sys.bottom, cut.bottom);

            view.setPadding(
                    baseL + addL,
                    baseT + addT,
                    baseR + (!isDrawer ? addR : 0),
                    baseB + addB
            );

            // enlarge drawer width
            if (isDrawer) {
                ViewGroup.LayoutParams lp = v.getLayoutParams();
                lp.width += addL;
                v.setLayoutParams(lp);
            }

            return insets;
        });

        ViewCompat.requestApplyInsets(v);
    }
}

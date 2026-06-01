package com.hatopigeon.cubictimer.utils;

import android.content.Context;

import com.hatopigeon.cubicify.R;

public final class StepNames {

    private StepNames() {}

    public static String localized(Context ctx, String canonical) {
        if (ctx == null || canonical == null) return canonical == null ? "" : canonical;
        switch (canonical) {
            case "Cross":             return ctx.getString(R.string.step_name_cross);
            case "F2L":               return ctx.getString(R.string.step_name_f2l);
            case "OLL":               return ctx.getString(R.string.step_name_oll);
            case "PLL":               return ctx.getString(R.string.step_name_pll);
            case "First layer":       return ctx.getString(R.string.step_name_first_layer);
            case "Second layer":      return ctx.getString(R.string.step_name_second_layer);
            case "Opposite cross":    return ctx.getString(R.string.step_name_opposite_cross);
            case "Opposite edges":    return ctx.getString(R.string.step_name_opposite_edges);
            case "Corners position":  return ctx.getString(R.string.step_name_corners_position);
            case "Corners orient":    return ctx.getString(R.string.step_name_corners_orient);
            default:                  return canonical;
        }
    }
}

package com.hatopigeon.cubictimer.spans;

import com.github.mikephil.charting.formatter.ValueFormatter;
import com.hatopigeon.cubictimer.utils.PuzzleUtils;
import com.github.mikephil.charting.components.AxisBase;

/**
 * Created by Ari on 06/02/2016.
 */
public class TimeFormatter extends ValueFormatter {

    private final String mPuzzleType;

    public TimeFormatter(String puzzleType) {
        mPuzzleType = puzzleType;
    }

    @Override
    public String getAxisLabel(float value, AxisBase axis) {
        return PuzzleUtils.convertTimeToString((long) (value * 1_000L), PuzzleUtils.FORMAT_NO_MILLI_AXIS,
                mPuzzleType);
    }
}

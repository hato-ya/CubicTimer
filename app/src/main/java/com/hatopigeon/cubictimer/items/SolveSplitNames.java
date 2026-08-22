package com.hatopigeon.cubictimer.items;

import com.hatopigeon.cubicify.R;

public final class SolveSplitNames {
    private SolveSplitNames() {}

    public static int getSourceNameResId(int sourceId) {
        switch (sourceId) {
            case SolveSplit.SOURCE_MULTI_PHASE:
                return R.string.solve_split_source_multi_phase;
            case SolveSplit.SOURCE_SMART_CUBE:
                return R.string.solve_split_source_smart_cube;
            default:
                return R.string.solve_split_unknown;
        }
    }

    public static int getMethodNameResId(int methodId) {
        switch (methodId) {
            case SolveSplit.METHOD_ADVANCED:
                return R.string.solve_split_method_advanced;
            case SolveSplit.METHOD_INTERMEDIATE:
                return R.string.solve_split_method_intermediate;
            case SolveSplit.METHOD_BEGINNER:
                return R.string.solve_split_method_beginner;
            case SolveSplit.METHOD_NONE:
                return R.string.solve_split_method_none;
            default:
                return R.string.solve_split_unknown;
        }
    }

    public static int getStepNameResId(int stepId) {
        switch (stepId) {
            case SolveSplit.STEP_CROSS:
                return R.string.solve_split_step_cross;
            case SolveSplit.STEP_F2L:
                return R.string.solve_split_step_f2l;
            case SolveSplit.STEP_OLL:
                return R.string.solve_split_step_oll;
            case SolveSplit.STEP_PLL:
                return R.string.solve_split_step_pll;
            case SolveSplit.STEP_FIRST_LAYER:
                return R.string.solve_split_step_first_layer;
            case SolveSplit.STEP_SECOND_LAYER:
                return R.string.solve_split_step_second_layer;
            case SolveSplit.STEP_OPPOSITE_CROSS:
                return R.string.solve_split_step_opposite_cross;
            case SolveSplit.STEP_OPPOSITE_EDGES:
                return R.string.solve_split_step_opposite_edges;
            case SolveSplit.STEP_CORNERS_POSITION:
                return R.string.solve_split_step_corners_position;
            case SolveSplit.STEP_CORNERS_ORIENT:
                return R.string.solve_split_step_corners_orient;
            default:
                return R.string.solve_split_unknown;
        }
    }

    public static int getCaseNameResId(int caseId) {
        switch (caseId) {
            case SolveSplit.CASE_NONE:
                return R.string.solve_split_case_none;
            default:
                return R.string.solve_split_unknown;
        }
    }
}

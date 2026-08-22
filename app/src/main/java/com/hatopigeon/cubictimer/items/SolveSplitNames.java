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
                return R.string.solve_split_source_unknown;
        }
    }

    public static int getMethodNameResId(int methodId) {
        switch (methodId) {
            case SolveSplit.METHOD_NONE:
                return R.string.solve_split_method_none;
            case SolveSplit.METHOD_CFOP:
                return R.string.solve_split_method_cfop;
            default:
                return R.string.solve_split_method_unknown;
        }
    }

    public static int getStepNameResId(int stepId) {
        switch (stepId) {
            case SolveSplit.STEP_PICK:
                return R.string.solve_split_step_pick;
            case SolveSplit.STEP_CROSS:
                return R.string.solve_split_step_cross;
            case SolveSplit.STEP_F2L:
                return R.string.solve_split_step_f2l;
            case SolveSplit.STEP_F2L1:
                return R.string.solve_split_step_f2l1;
            case SolveSplit.STEP_F2L2:
                return R.string.solve_split_step_f2l2;
            case SolveSplit.STEP_F2L3:
                return R.string.solve_split_step_f2l3;
            case SolveSplit.STEP_F2L4:
                return R.string.solve_split_step_f2l4;
            case SolveSplit.STEP_OLL:
                return R.string.solve_split_step_oll;
            case SolveSplit.STEP_PLL:
                return R.string.solve_split_step_pll;
            case SolveSplit.STEP_AUF:
                return R.string.solve_split_step_auf;
            case SolveSplit.STEP_DROP:
                return R.string.solve_split_step_drop;
            case SolveSplit.STEP_FIRST_LAYER:
                return R.string.solve_split_step_first_layer;
            case SolveSplit.STEP_SECOND_LAYER:
                return R.string.solve_split_step_second_layer;
            case SolveSplit.STEP_EDGE_OLL:
                return R.string.solve_split_step_edge_oll;
            case SolveSplit.STEP_CORNER_OLL:
                return R.string.solve_split_step_corner_oll;
            case SolveSplit.STEP_CORNER_PLL:
                return R.string.solve_split_step_corner_pll;
            case SolveSplit.STEP_EDGE_PLL:
                return R.string.solve_split_step_edge_pll;
            default:
                return R.string.solve_split_step_unknown;
        }
    }

    public static String getStepCanonicalName(int stepId) {
        switch (stepId) {
            case SolveSplit.STEP_PICK:
                return "Pick";
            case SolveSplit.STEP_CROSS:
                return "Cross";
            case SolveSplit.STEP_F2L1:
                return "F2L1";
            case SolveSplit.STEP_F2L2:
                return "F2L2";
            case SolveSplit.STEP_F2L3:
                return "F2L3";
            case SolveSplit.STEP_F2L4:
                return "F2L4";
            case SolveSplit.STEP_OLL:
                return "OLL";
            case SolveSplit.STEP_PLL:
                return "PLL";
            case SolveSplit.STEP_AUF:
                return "AUF";
            case SolveSplit.STEP_DROP:
                return "Drop";
            case SolveSplit.STEP_FIRST_LAYER:
                return "FirstLayer";
            case SolveSplit.STEP_SECOND_LAYER:
                return "SecondLayer";
            case SolveSplit.STEP_EDGE_OLL:
                return "EdgeOLL";
            case SolveSplit.STEP_CORNER_OLL:
                return "CornerOLL";
            case SolveSplit.STEP_CORNER_PLL:
                return "CornerPLL";
            case SolveSplit.STEP_EDGE_PLL:
                return "EdgePLL";
            default:
                return "Unknown";
        }
    }

    public static int getCaseNameResId(int caseId) {
        switch (caseId) {
            case SolveSplit.CASE_NONE:
                return R.string.solve_split_case_none;
            default:
                return R.string.solve_split_case_unknown;
        }
    }
}

package com.hatopigeon.cubictimer.items;

public class SolveSplit {
    public static final int SOURCE_MULTI_PHASE = 0;
    public static final int SOURCE_SMART_CUBE = 1;

    public static final int METHOD_NONE = 0;
    public static final int METHOD_CFOP = 1;

    public static final int STEP_NONE = 0;
    public static final int STEP_PICK = 1;
    public static final int STEP_CROSS = 2;
    public static final int STEP_F2L = 3;
    public static final int STEP_F2L1 = 4;
    public static final int STEP_F2L2 = 5;
    public static final int STEP_F2L3 = 6;
    public static final int STEP_F2L4 = 7;
    public static final int STEP_OLL = 8;
    public static final int STEP_PLL = 9;
    public static final int STEP_AUF = 10;
    public static final int STEP_DROP = 11;
    public static final int STEP_FIRST_LAYER = 12;
    public static final int STEP_SECOND_LAYER = 13;
    public static final int STEP_EDGE_OLL = 14;
    public static final int STEP_CORNER_OLL = 15;
    public static final int STEP_CORNER_PLL = 16;
    public static final int STEP_EDGE_PLL = 17;

    public static final int CASE_NONE = 0;

    long id;
    long solveId;
    int sourceId;
    int methodId;
    int splitOrder;
    int stepId;
    int caseId;
    long recogTimeMs;
    long execTimeMs;
    int moveCount;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSolveId() {
        return solveId;
    }

    public void setSolveId(long solveId) {
        this.solveId = solveId;
    }

    public int getSourceId() {
        return sourceId;
    }

    public void setSourceId(int sourceId) {
        this.sourceId = sourceId;
    }

    public int getMethodId() {
        return methodId;
    }

    public void setMethodId(int methodId) {
        this.methodId = methodId;
    }

    public int getSplitOrder() {
        return splitOrder;
    }

    public void setSplitOrder(int splitOrder) {
        this.splitOrder = splitOrder;
    }

    public int getStepId() {
        return stepId;
    }

    public void setStepId(int stepId) {
        this.stepId = stepId;
    }

    public int getCaseId() {
        return caseId;
    }

    public void setCaseId(int caseId) {
        this.caseId = caseId;
    }

    public long getRecogTimeMs() {
        return recogTimeMs;
    }

    public void setRecogTimeMs(long recogTimeMs) {
        this.recogTimeMs = recogTimeMs;
    }

    public long getExecTimeMs() {
        return execTimeMs;
    }

    public void setExecTimeMs(long execTimeMs) {
        this.execTimeMs = execTimeMs;
    }

    public int getMoveCount() {
        return moveCount;
    }

    public void setMoveCount(int moveCount) {
        this.moveCount = moveCount;
    }

    /** Returns true for per-pair F2L steps that can be summed into the aggregate F2L step. */
    public static boolean isF2lDetailStep(int stepId) {
        return stepId == STEP_F2L1 || stepId == STEP_F2L2 || stepId == STEP_F2L3 || stepId == STEP_F2L4;
    }
}

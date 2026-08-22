package com.hatopigeon.cubictimer.items;

public class SolveSplit {
    public static final int SOURCE_MULTI_PHASE = 0;
    public static final int SOURCE_SMART_CUBE = 1;

    public static final int METHOD_NONE = 0;
    public static final int METHOD_BEGINNER = 1;
    public static final int METHOD_INTERMEDIATE = 2;
    public static final int METHOD_ADVANCED = 3;

    public static final int STEP_NONE = 0;
    public static final int STEP_CROSS = 1;
    public static final int STEP_F2L = 2;
    public static final int STEP_OLL = 3;
    public static final int STEP_PLL = 4;
    public static final int STEP_FIRST_LAYER = 5;
    public static final int STEP_SECOND_LAYER = 6;
    public static final int STEP_OPPOSITE_CROSS = 7;
    public static final int STEP_OPPOSITE_EDGES = 8;
    public static final int STEP_CORNERS_POSITION = 9;
    public static final int STEP_CORNERS_ORIENT = 10;

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
}

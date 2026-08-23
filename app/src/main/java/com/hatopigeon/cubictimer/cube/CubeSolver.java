package com.hatopigeon.cubictimer.cube;

public class CubeSolver {
    private final CubeState state = new CubeState();
    private int moveCount = 0;
    private boolean solved = false;

    public void reset() {
        state.reset();
        moveCount = 0;
        solved = false;
    }

    public void addMove(CubeMove move) {
        state.applyMove(move);
        moveCount++;
        solved = state.isSolved();
    }

    public boolean isSolved() {
        return solved;
    }

    public boolean isCrossSolved() {
        return state.isCrossSolved();
    }

    public boolean isCrossSolved(int face) {
        return state.isCrossSolved(face);
    }

    public int getCrossFace() {
        return state.getCrossFace();
    }

    public boolean isF2lSolved(int crossFace) {
        return state.isF2lSolved(crossFace);
    }

    public boolean isF2lSolved(int crossFace, int num) {
        return state.isF2lSolved(crossFace, num);
    }

    public boolean isOllSolved(int crossFace) {
        return state.isOllSolved(crossFace);
    }

    public boolean isPllSolved(int crossFace) {
        return state.isPllSolved(crossFace);
    }

    // ─── Beginner (LBL) steps ───
    public boolean isFirstLayerSolved(int crossFace) {
        return state.isFirstLayerSolved(crossFace);
    }

    public boolean isEdgeOllSolved(int crossFace) {
        return state.isEdgeOllSolved(crossFace);
    }

    public boolean isCornerPllSolved(int crossFace) {
        return state.isCornerPllSolved(crossFace);
    }

    public boolean isLLCrossSolved(int crossFace) {
        return state.isLLCrossSolved(crossFace);
    }

    public boolean areLLCornersPositioned(int crossFace) {
        return state.areLLCornersPositioned(crossFace);
    }

    public void setStateFacelets(int[] facelets) {
        state.setFacelets(facelets);
        solved = state.isSolved();
    }

    public int getNumMoves() {
        return moveCount;
    }
}

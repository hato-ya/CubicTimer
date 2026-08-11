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

    public boolean isF2LSolved(int crossFace) {
        return state.isF2LSolved(crossFace);
    }

    public boolean isOLLSolved(int crossFace) {
        return state.isOLLSolved(crossFace);
    }

    // ─── Beginner (LBL) steps ───
    public boolean isFirstLayerSolved(int crossFace) {
        return state.isFirstLayerSolved(crossFace);
    }

    public boolean isLLCrossOriented(int crossFace) {
        return state.isLLCrossOriented(crossFace);
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

package com.hatopigeon.cubictimer.cube;

public class CubeSolver {
    private final CubeState state = new CubeState();
    private int moveCount = 0;
    private long elapsedTime = 0;
    private boolean solved = false;

    public void reset() {
        state.reset();
        moveCount = 0;
        elapsedTime = 0;
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

    public void setStateFacelets(int[] facelets) {
        state.setFacelets(facelets);
        solved = state.isSolved();
    }

    public int getNumMoves() {
        return moveCount;
    }

    public void setElapsedTime(long ms) {
        elapsedTime = ms;
    }

    public double getTps() {
        if (elapsedTime <= 0) return 0;
        return moveCount / (elapsedTime / 1000.0);
    }
}

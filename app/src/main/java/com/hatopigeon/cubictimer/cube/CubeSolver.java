package com.hatopigeon.cubictimer.cube;

import java.util.ArrayList;

public class CubeSolver {
    private final CubeState state = new CubeState();
    private final ArrayList<CubeMove> moves = new ArrayList<>();
    private int moveCount = 0;
    private long elapsedTime = 0;
    private boolean solved = false;
    public void reset() {
        state.reset();
        moves.clear();
        moveCount = 0;
        elapsedTime = 0;
        solved = false;
    }

    public void addMove(CubeMove move) {
        state.applyMove(move);
        moves.add(move);
        moveCount++;
        solved = state.isSolved();
    }

    public boolean isSolved() {
        return solved;
    }

    public boolean isCrossSolved() {
        return state.isCrossSolved();
    }

    public int getCrossFace() {
        return state.getCrossFace();
    }

    public boolean isF2LSolved() {
        return state.isF2LSolved();
    }

    public boolean isF2LSolved(int crossFace) {
        return state.isF2LSolved(crossFace);
    }

    public boolean isOLLSolved() {
        return state.isOLLSolved();
    }

    public boolean isOLLSolved(int crossFace) {
        return state.isOLLSolved(crossFace);
    }

    public void setStateFacelets(int[] facelets) {
        state.setFacelets(facelets);
        solved = state.isSolved();
    }

    public int[] getStateFacelets() {
        return state.getFacelets();
    }

    public ArrayList<CubeMove> getMoves() {
        return moves;
    }

    public int getNumMoves() {
        return moveCount;
    }

    public void setElapsedTime(long ms) {
        elapsedTime = ms;
    }

    public double getTPS(long elapsedMillis) {
        if (elapsedMillis <= 0) return 0;
        return moveCount / (elapsedMillis / 1000.0);
    }

    public double getTps() {
        return getTPS(elapsedTime);
    }
}

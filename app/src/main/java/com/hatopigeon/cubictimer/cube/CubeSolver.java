package com.hatopigeon.cubictimer.cube;

import java.util.ArrayList;
import java.util.List;

public class CubeSolver {
    private final CubeState state = new CubeState();
    private final ArrayList<CubeMove> moves = new ArrayList<>();
    private int moveCount = 0;
    private long firstMoveTime = 0;
    private long elapsedTime = 0;
    private boolean solved = false;

    public void reset() {
        state.reset();
        moves.clear();
        moveCount = 0;
        firstMoveTime = 0;
        elapsedTime = 0;
        solved = false;
    }

    public boolean applyMoves(List<CubeMove> batch) {
        for (CubeMove m : batch) {
            applyMove(m);
        }
        return solved;
    }

    public void applyMove(CubeMove move) {
        state.applyMove(move);
        moves.add(move);
        moveCount++;
        if (firstMoveTime == 0) firstMoveTime = move.timestamp;
        solved = state.isSolved();
    }

    public void addMove(CubeMove move) {
        applyMove(move);
    }

    public boolean isSolved() {
        return solved;
    }

    public int getMoveCount() {
        return moveCount;
    }

    public int getNumMoves() {
        return moveCount;
    }

    public long getFirstMoveTime() {
        return firstMoveTime;
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

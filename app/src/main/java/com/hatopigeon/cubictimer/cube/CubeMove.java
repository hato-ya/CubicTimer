package com.hatopigeon.cubictimer.cube;

public class CubeMove {
    public static final int U = 0, R = 1, F = 2, D = 3, L = 4, B = 5;
    public static final int CW = 1, CCW = -1, DOUBLE = 2;

    public final int face;
    public final int direction;
    public final long timestamp;

    public CubeMove(int face, int direction, long timestamp) {
        this.face = face;
        this.direction = direction;
        this.timestamp = timestamp;
    }

    public String toNotation() {
        String f = "URFDLB".substring(face, face + 1);
        if (direction == DOUBLE) return f + "2";
        return direction == CW ? f : f + "'";
    }

    @Override
    public String toString() {
        return toNotation();
    }

}

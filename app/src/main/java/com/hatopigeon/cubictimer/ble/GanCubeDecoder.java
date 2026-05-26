package com.hatopigeon.cubictimer.ble;

import com.hatopigeon.cubictimer.cube.CubeMove;

import java.util.ArrayList;
import java.util.List;

public class GanCubeDecoder {

    private static final int[] FACE_MAP = {
        CubeMove.U, CubeMove.U, CubeMove.U,  // 0x00=U CW, 0x01=U DOUBLE, 0x02=U CCW
        CubeMove.R, CubeMove.R, CubeMove.R,  // 0x03=R CW, 0x04=R DOUBLE, 0x05=R CCW
        CubeMove.F, CubeMove.F, CubeMove.F,  // 0x06=F CW, 0x07=F DOUBLE, 0x08=F CCW
        CubeMove.D, CubeMove.D, CubeMove.D,  // 0x09=D CW, 0x0a=D DOUBLE, 0x0b=D CCW
        CubeMove.L, CubeMove.L, CubeMove.L,  // 0x0c=L CW, 0x0d=L DOUBLE, 0x0e=L CCW
        CubeMove.B, CubeMove.B, CubeMove.B,  // 0x0f=B CW, 0x10=B DOUBLE, 0x11=B CCW
    };

    private static final int[] DIR_MAP = {
        CubeMove.CW, CubeMove.DOUBLE, CubeMove.CCW,
        CubeMove.CW, CubeMove.DOUBLE, CubeMove.CCW,
        CubeMove.CW, CubeMove.DOUBLE, CubeMove.CCW,
        CubeMove.CW, CubeMove.DOUBLE, CubeMove.CCW,
        CubeMove.CW, CubeMove.DOUBLE, CubeMove.CCW,
        CubeMove.CW, CubeMove.DOUBLE, CubeMove.CCW,
    };

    private int previousMoveCount = -1;
    private long previousTimestamp = 0;

    public void reset() {
        previousMoveCount = -1;
        previousTimestamp = 0;
    }

    public List<CubeMove> decodeMoves(byte[] data, long timestamp) {
        List<CubeMove> moves = new ArrayList<>();

        if (data == null || data.length < 19) return moves;

        int moveCount = data[12] & 0xFF;

        if (previousMoveCount < 0) {
            previousMoveCount = moveCount;
            previousTimestamp = timestamp;
            return moves;
        }

        int diff = (moveCount - previousMoveCount) & 0xFF;
        if (diff == 0 || diff > 6) {
            previousMoveCount = moveCount;
            previousTimestamp = timestamp;
            return moves;
        }

        for (int i = 0; i < diff; i++) {
            int raw = data[18 - i] & 0xFF;
            CubeMove move = parseMove(raw, timestamp);
            if (move != null) {
                moves.add(move);
            }
        }

        previousMoveCount = moveCount;
        previousTimestamp = timestamp;
        return moves;
    }

    private CubeMove parseMove(int raw, long timestamp) {
        if (raw < 0 || raw >= FACE_MAP.length) return null;
        int face = FACE_MAP[raw];
        int dir = DIR_MAP[raw];
        if (face < 0 || dir == 0) return null;
        return new CubeMove(face, dir, timestamp);
    }
}

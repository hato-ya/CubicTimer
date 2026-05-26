package com.hatopigeon.cubictimer.ble;

import com.hatopigeon.cubictimer.cube.CubeMove;

import java.util.ArrayList;
import java.util.List;

public class GanCubeDecoder {

    private static final int[] FACE_MAP = {
        CubeMove.U, -1, CubeMove.U,  // 0x00=U CW, 0x01=?, 0x02=U CCW
        CubeMove.R, -1, CubeMove.R,  // 0x03=R CW, 0x04=?, 0x05=R CCW
        CubeMove.F, -1, CubeMove.F,  // 0x06=F CW, 0x07=?, 0x08=F CCW
        CubeMove.D, -1, CubeMove.D,  // 0x09=D CW, 0x0a=?, 0x0b=D CCW
        CubeMove.L, -1, CubeMove.L,  // 0x0c=L CW, 0x0d=?, 0x0e=L CCW
        CubeMove.B, -1, CubeMove.B,  // 0x0f=B CW, 0x10=?, 0x11=B CCW
    };

    private static final int[] DIR_MAP = {
        CubeMove.CW, 0, CubeMove.CCW,
        CubeMove.CW, 0, CubeMove.CCW,
        CubeMove.CW, 0, CubeMove.CCW,
        CubeMove.CW, 0, CubeMove.CCW,
        CubeMove.CW, 0, CubeMove.CCW,
        CubeMove.CW, 0, CubeMove.CCW,
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

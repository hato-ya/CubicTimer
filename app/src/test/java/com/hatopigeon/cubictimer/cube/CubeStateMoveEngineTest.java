package com.hatopigeon.cubictimer.cube;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Verifies that {@link CubeState}'s move engine (the generated PERM_CW permutations) forms a
 * physically consistent cube group: the permutations must compose correctly across different
 * faces, not just individually. This guards the bug where hand-written sticker cycles passed
 * "each face ^4 = identity" yet drifted on mixed-face sequences (e.g. (R U R' U') x6), making the
 * 3D model show wrong colours during fast multi-move bursts.
 */
public class CubeStateMoveEngineTest {

    private static final String LETTERS = "URFDLB"; // index = CubeMove face

    private static CubeState solved() {
        CubeState s = new CubeState();
        s.reset();
        return s;
    }

    /** Applies a WCA sequence like "R U R' U2 F'" to the state. */
    private static void apply(CubeState s, String seq) {
        for (String tok : seq.trim().split("\\s+")) {
            if (tok.isEmpty()) continue;
            int face = LETTERS.indexOf(tok.charAt(0));
            int dir = tok.indexOf('2') >= 0 ? CubeMove.DOUBLE
                    : (tok.indexOf('\'') >= 0 ? CubeMove.CCW : CubeMove.CW);
            s.applyMove(new CubeMove(face, dir, 0));
        }
    }

    private static void repeat(CubeState s, String seq, int times) {
        for (int i = 0; i < times; i++) apply(s, seq);
    }

    @Test
    public void eachFaceQuarterTurnTimes4IsIdentity() {
        for (char f : LETTERS.toCharArray()) {
            CubeState s = solved();
            repeat(s, String.valueOf(f), 4);
            assertTrue(f + " x4 should be identity", s.isSolved());
        }
    }

    @Test
    public void sexyMoveTimes6IsIdentity() {
        CubeState s = solved();
        repeat(s, "R U R' U'", 6);
        assertTrue("(R U R' U') x6 should be identity", s.isSolved());
    }

    @Test
    public void tPermTimes2IsIdentity() {
        CubeState s = solved();
        repeat(s, "R U R' U' R' F R2 U' R' U' R U R' F'", 2);
        assertTrue("T-perm x2 should be identity", s.isSolved());
    }

    @Test
    public void suneTimes6IsIdentity() {
        CubeState s = solved();
        repeat(s, "R U R' U R U2 R'", 6);
        assertTrue("Sune x6 should be identity", s.isSolved());
    }

    @Test
    public void scrambleThenInverseIsIdentity() {
        CubeState s = solved();
        apply(s, "R U F' L D B' R U' B2 D' F R'");
        apply(s, "R F' D B2 U R' B D' L' F U' R'"); // exact inverse, reversed order
        assertTrue("scramble + inverse should be identity", s.isSolved());
    }
}

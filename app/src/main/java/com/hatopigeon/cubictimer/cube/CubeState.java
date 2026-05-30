package com.hatopigeon.cubictimer.cube;

import java.util.Arrays;

public class CubeState {
    public static final int NUM_FACELETS = 54;
    public static final int U = 0, L = 1, F = 2, R = 3, B = 4, D = 5;

    // For each of the 6 faces (U=0,L=1,F=2,R=3,B=4,D=5), the 4 cross edge pairs:
    // {edgeOnFace, adjacentFacelet, adjacentCenterIndex}
    // Verified against EDGE_FACELET_MAP in GanCubeManager.
    private static final int[][] CROSS_EDGE_PAIRS = {
        {1, 37, 40}, {3, 10, 13}, {5, 28, 31}, {7, 19, 22},
        {10, 3, 4}, {12, 41, 40}, {14, 21, 22}, {16, 48, 49},
        {19, 7, 4}, {21, 14, 13}, {23, 30, 31}, {25, 46, 49},
        {28, 5, 4}, {30, 23, 22}, {32, 39, 40}, {34, 50, 49},
        {37, 1, 4}, {39, 32, 31}, {41, 12, 13}, {43, 52, 49},
        {46, 25, 22}, {48, 16, 13}, {50, 34, 31}, {52, 43, 40},
    };

    private final int[] facelets = new int[NUM_FACELETS];

    private static final int[] SOLVED = new int[54];
    static {
        for (int f = 0; f < 6; f++)
            Arrays.fill(SOLVED, f * 9, (f + 1) * 9, f);
    }

    // Opposite face mapping: U↔D, L↔R, F↔B
    private static final int[] OPPOSITE = {5, 3, 4, 1, 2, 0};

    // For each cross face (0-5), the 33 facelet indices that must match their face center
    // when F2L is solved: 9 from the cross face + 6×4 from the 4 side faces.
    // The opposite face and the 3 facelets of the last-layer ring on each side face are skipped.
    private static final int[][] F2L_CHECK = {
        // Cross U(0), opposite D(5): side faces L,F,R,B, skip row 2 on each
        {0,1,2,3,4,5,6,7,8,
         9,10,11,12,13,14,
         18,19,20,21,22,23,
         27,28,29,30,31,32,
         36,37,38,39,40,41},
        // Cross L(1), opposite R(3): last layer is R, so skip each side face's R-adjacent column.
        // That is the right column of U/F/D, but the LEFT column of B (the back face is mirrored).
        {9,10,11,12,13,14,15,16,17,
         0,1,3,4,6,7,
         18,19,21,22,24,25,
         45,46,48,49,51,52,
         37,38,40,41,43,44},
        // Cross F(2), opposite B(4): side faces U,L,D,R
        {18,19,20,21,22,23,24,25,26,
         3,4,5,6,7,8,
         10,11,13,14,16,17,
         45,46,47,48,49,50,
         27,28,30,31,33,34},
        // Cross R(3), opposite L(1): last layer is L, so skip each side face's L-adjacent column.
        // That is the left column of U/F/D, but the RIGHT column of B (the back face is mirrored).
        {27,28,29,30,31,32,33,34,35,
         1,2,4,5,7,8,
         19,20,22,23,25,26,
         46,47,49,50,52,53,
         36,37,39,40,42,43},
        // Cross B(4), opposite F(2): side faces U,L,D,R
        {36,37,38,39,40,41,42,43,44,
         0,1,2,3,4,5,
         9,10,12,13,15,16,
         48,49,50,51,52,53,
         28,29,31,32,34,35},
        // Cross D(5), opposite U(0): side faces L,F,R,B, skip row 0 on each
        {45,46,47,48,49,50,51,52,53,
         12,13,14,15,16,17,
         21,22,23,24,25,26,
         30,31,32,33,34,35,
         39,40,41,42,43,44},
    };

    private static final int[][] PERM_CW = createPermutations();

    // ─── OLL recognition ───
    //
    // The legacy PERM_CW tables above are only ever used to *track* moves between full facelet
    // snapshots; the app never relies on them forming a physically consistent cube group (they
    // don't — composing different faces drifts). OLL recognition, however, must rotate the cube
    // accurately, so it uses the three permutations below, which were generated from a 3D cubie
    // model and verified (sexy x6 = identity, all 57 OLL cases recognized) in CubeStateOllTest.
    //
    // new[i] = old[perm[i]] (same convention as PERM_CW). Direction -1 applies the inverse.
    //   OLL_U      : a U-layer quarter turn (used to cycle through the 4 AUF positions)
    //   OLL_ROT_X  : whole-cube rotation that brings F onto U (x)
    //   OLL_ROT_Z  : whole-cube rotation that brings L onto U (z)
    private static final int[] OLL_U = {
        6, 3, 0, 7, 4, 1, 8, 5, 2, 18, 19, 20, 12, 13, 14, 15, 16, 17,
        27, 28, 29, 21, 22, 23, 24, 25, 26, 36, 37, 38, 30, 31, 32, 33, 34, 35,
        9, 10, 11, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53,
    };
    private static final int[] OLL_ROT_X = {
        18, 19, 20, 21, 22, 23, 24, 25, 26, 11, 14, 17, 10, 13, 16, 9, 12, 15,
        45, 46, 47, 48, 49, 50, 51, 52, 53, 33, 30, 27, 34, 31, 28, 35, 32, 29,
        8, 7, 6, 5, 4, 3, 2, 1, 0, 44, 43, 42, 41, 40, 39, 38, 37, 36,
    };
    private static final int[] OLL_ROT_Z = {
        15, 12, 9, 16, 13, 10, 17, 14, 11, 51, 48, 45, 52, 49, 46, 53, 50, 47,
        24, 21, 18, 25, 22, 19, 26, 23, 20, 6, 3, 0, 7, 4, 1, 8, 5, 2,
        38, 41, 44, 37, 40, 43, 36, 39, 42, 33, 30, 27, 34, 31, 28, 35, 32, 29,
    };

    // The 57 OLL reference patterns (alg_reference_OLL in reference_states.xml) describe a 5x5
    // grid (index = 5*row + col) seen from above the last layer (with the front face toward the
    // bottom of the grid). The four corners are "don't care" (X). 'Y' = sticker shows the last
    // layer color, 'N' = it doesn't. This maps each of the 25 grid cells to a facelet index
    // assuming the last layer is U (=0). -1 marks the four ignored corners.
    //
    //         .  c1 c2 c3  .          back row    = B stickers adjacent to U
    //         c5 c6 c7 c8 c9          left/right  = L / R stickers adjacent to U
    //        c10 ...      c14         center 3x3  = U face
    //        c15 ...      c19
    //         . c21 c22 c23 .         front row   = F stickers adjacent to U
    private static final int[] OLL_GRID_TO_FACELET = {
        -1, 38, 37, 36, -1,
         9,  0,  1,  2, 29,
        10,  3,  4,  5, 28,
        11,  6,  7,  8, 27,
        -1, 18, 19, 20, -1,
    };

    public CubeState() {
        reset();
    }

    public void reset() {
        System.arraycopy(SOLVED, 0, facelets, 0, 54);
    }

    public void setFacelets(int[] newFacelets) {
        System.arraycopy(newFacelets, 0, facelets, 0, 54);
    }

    public int[] getFacelets() {
        return facelets.clone();
    }

    public boolean isSolved() {
        for (int i = 0; i < 54; i++)
            if (facelets[i] != SOLVED[i]) return false;
        return true;
    }

    public boolean isF2LSolved(int crossFace) {
        if (crossFace < 0) return false;

        for (int idx : F2L_CHECK[crossFace]) {
            int faceCenterIdx = (idx / 9) * 9 + 4;
            if (facelets[idx] != facelets[faceCenterIdx]) return false;
        }
        return true;
    }

    public int getCrossFace() {
        for (int face = 0; face < 6; face++) {
            int faceCenterIdx = face * 9 + 4;
            boolean faceCrossSolved = true;
            for (int e = 0; e < 4; e++) {
                int idx = face * 4 + e;
                int edgeFc = CROSS_EDGE_PAIRS[idx][0];
                int adjFc = CROSS_EDGE_PAIRS[idx][1];
                int adjCnt = CROSS_EDGE_PAIRS[idx][2];
                if (facelets[edgeFc] != facelets[faceCenterIdx]
                        || facelets[adjFc] != facelets[adjCnt]) {
                    faceCrossSolved = false;
                    break;
                }
            }
            if (faceCrossSolved) return face;
        }
        return -1;
    }

    public boolean isOLLSolved(int crossFace) {
        if (crossFace < 0) return false;
        int opp = OPPOSITE[crossFace];
        int oppCenter = opp * 9 + 4;
        for (int i = opp * 9; i < opp * 9 + 9; i++) {
            if (facelets[i] != facelets[oppCenter]) return false;
        }
        return true;
    }

    /**
     * Recognizes the exact OLL case (1-57) of the last layer (the face opposite the cross face),
     * independent of the U-layer rotation (AUF). Returns the OLL number (1-57), 0 if the last
     * layer is already oriented (OLL skip), or -1 if F2L is not solved or no case matches.
     *
     * The last layer is rotated onto U, then matched against the {@code references} array
     * (R.array.alg_reference_OLL) over all four AUF rotations. Because OLL is defined up to a
     * U-layer turn, mirror cases such as OLL 3 / OLL 4 are distinguished correctly: each case has
     * a unique orientation signature across its four AUFs, so only one reference can match.
     *
     * @param crossFace  the solved cross face (0-5), as returned by {@link #getCrossFace()}
     * @param references the 57 OLL reference strings from reference_states.xml
     */
    public int getOllCase(int crossFace, String[] references) {
        if (crossFace < 0 || references == null || references.length == 0) return -1;
        if (!isF2LSolved(crossFace)) return -1;

        CubeState work = new CubeState();
        work.setFacelets(this.facelets);
        work.orientLastFaceToU(OPPOSITE[crossFace]);

        if (work.isLastLayerOriented()) return 0;

        for (int auf = 0; auf < 4; auf++) {
            String sig = work.ollSignatureU();
            for (int i = 0; i < references.length; i++) {
                if (sig.equals(references[i])) return i + 1;
            }
            work.applyPerm(OLL_U, 1);
        }
        return -1;
    }

    /**
     * Recognizes the OLL case without knowing the cross face: tries all six faces as the cross
     * and returns the first that yields a real OLL (1-57). Returns 0 if some face has F2L solved
     * with the last layer already oriented (solved / OLL skip), or -1 if no face has F2L solved.
     *
     * Useful for a trainer view where the cube is solved except for a last-layer orientation set
     * up by hand: only the genuine cross face produces a non-trivial OLL.
     */
    public int getOllCase(String[] references) {
        boolean oriented = false;
        for (int crossFace = 0; crossFace < 6; crossFace++) {
            int c = getOllCase(crossFace, references);
            if (c > 0) return c;
            if (c == 0) oriented = true;
        }
        return oriented ? 0 : -1;
    }

    /** True when every facelet of the U face already shows the U center color. */
    private boolean isLastLayerOriented() {
        int center = facelets[U * 9 + 4];
        for (int i = U * 9; i < U * 9 + 9; i++) {
            if (facelets[i] != center) return false;
        }
        return true;
    }

    /**
     * Builds the 25-character OLL signature for the current state assuming the last layer is U,
     * using the same Y/N/X convention as the reference patterns.
     */
    private String ollSignatureU() {
        int center = facelets[U * 9 + 4];
        char[] sig = new char[25];
        for (int i = 0; i < 25; i++) {
            int fc = OLL_GRID_TO_FACELET[i];
            if (fc < 0) {
                sig[i] = 'X';
            } else {
                sig[i] = (facelets[fc] == center) ? 'Y' : 'N';
            }
        }
        return new String(sig);
    }

    /** Rotates the whole cube so the given face becomes U, preserving orientation otherwise. */
    private void orientLastFaceToU(int face) {
        switch (face) {
            case U: break;                                            // already on top
            case D: applyPerm(OLL_ROT_X, 1); applyPerm(OLL_ROT_X, 1); break; // x2
            case F: applyPerm(OLL_ROT_X, 1); break;                   // x  (F -> U)
            case B: applyPerm(OLL_ROT_X, -1); break;                  // x' (B -> U)
            case L: applyPerm(OLL_ROT_Z, 1); break;                   // z  (L -> U)
            case R: applyPerm(OLL_ROT_Z, -1); break;                  // z' (R -> U)
        }
    }

    public boolean isCrossSolved() {
        return getCrossFace() >= 0;
    }

    public boolean isCrossSolved(int face) {
        int faceCenterIdx = face * 9 + 4;
        for (int e = 0; e < 4; e++) {
            int idx = face * 4 + e;
            int edgeFc = CROSS_EDGE_PAIRS[idx][0];
            int adjFc = CROSS_EDGE_PAIRS[idx][1];
            int adjCnt = CROSS_EDGE_PAIRS[idx][2];
            if (facelets[edgeFc] != facelets[faceCenterIdx]
                    || facelets[adjFc] != facelets[adjCnt]) {
                return false;
            }
        }
        return true;
    }

    private static final int[] FACE_MAP_CUBEMOVE_TO_CUBESTATE = {0, 3, 2, 5, 1, 4};

    public void applyMove(CubeMove move) {
        int face = FACE_MAP_CUBEMOVE_TO_CUBESTATE[move.face];
        if (move.direction == CubeMove.DOUBLE) {
            applyMove(face, 1);
            applyMove(face, 1);
        } else if (move.direction == CubeMove.CCW) {
            applyMove(face, -1);
        } else if (move.direction == CubeMove.CW) {
            applyMove(face, 1);
        }
    }

    public void applyMove(int face, int direction) {
        int[] perm;
        if (direction == 1) {
            perm = PERM_CW[face];
        } else {
            perm = invertPerm(PERM_CW[face]);
        }

        int[] next = new int[54];
        for (int i = 0; i < 54; i++) {
            next[i] = facelets[perm[i]];
        }
        System.arraycopy(next, 0, facelets, 0, 54);
    }

    private void applyPerm(int[] perm, int direction) {
        int[] p = (direction == 1) ? perm : invertPerm(perm);
        int[] next = new int[54];
        for (int i = 0; i < 54; i++) {
            next[i] = facelets[p[i]];
        }
        System.arraycopy(next, 0, facelets, 0, 54);
    }

    private static int[] invertPerm(int[] perm) {
        int[] inv = new int[54];
        for (int i = 0; i < 54; i++) {
            inv[perm[i]] = i;
        }
        return inv;
    }

    // Facelet layout (cross net):
    //           0  1  2
    //           3  4  5
    //           6  7  8
    //  9 10 11 18 19 20 27 28 29 36 37 38
    // 12 13 14 21 22 23 30 31 32 39 40 41
    // 15 16 17 24 25 26 33 34 35 42 43 44
    //          45 46 47
    //          48 49 50
    //          51 52 53
    //
    // U=0-8, L=9-17, F=18-26, R=27-35, B=36-44, D=45-53
    //
    // For each CW permutation:
    //   new[i] = old[perm[i]]

    private static int[][] createPermutations() {
        int[][] p = new int[6][54];
        for (int f = 0; f < 6; f++)
            for (int i = 0; i < 54; i++)
                p[f][i] = i;

        // ─── U CW ───
        // U face (0-8) rotates CW
        //   corners: 0→2→8→6→0  edges: 1→5→7→3→1
        // Adjacent: F(18,19,20) → L(9,10,11)  [top of F → top of L]
        //           L(9,10,11) → B(36,37,38)  [top of L → top of B]
        //           B(36,37,38) → R(27,28,29) [top of B → top of R]
        //           R(27,28,29) → F(18,19,20) [top of R → top of F]
        int[] u = fresh();
        u[2] = 0;  u[5] = 1;  u[8] = 2;
        u[1] = 3;  u[4] = 4;  u[7] = 5;
        u[0] = 6;  u[3] = 7;  u[6] = 8;

        u[9] = 18;  u[10] = 19;  u[11] = 20;
        u[36] = 9;  u[37] = 10;  u[38] = 11;
        u[27] = 36; u[28] = 37;  u[29] = 38;
        u[18] = 27; u[19] = 28;  u[20] = 29;
        p[U] = u;

        // ─── L CW ───
        // L face (9-17) rotates CW
        //   corners: 9→11→17→15→9  edges: 10→14→16→12→10
        // Adjacent: U(0,3,6) → F(18,21,24) [left col of U → left col of F]
        //           F(18,21,24) → D(45,48,51) [left col of F → left col of D]
        //           D(45,48,51) → B(42,39,36) [left col of D → right col of B, reversed]
        //           B(42,39,36) → U(6,3,0) [right col of B → left col of U, reversed]
        int[] l = fresh();
        l[11] = 9;  l[14] = 10; l[17] = 11;
        l[10] = 12; l[13] = 13; l[16] = 14;
        l[9] = 15;  l[12] = 16; l[15] = 17;

        l[18] = 0;  l[21] = 3;  l[24] = 6;
        l[45] = 18; l[48] = 21; l[51] = 24;
        l[42] = 45; l[39] = 48; l[36] = 51;
        l[0] = 42;  l[3] = 39;  l[6] = 36;
        p[L] = l;

        // ─── F CW ───
        // F face (18-26) rotates CW
        //   corners: 18→20→26→24→18  edges: 19→23→25→21→19
        // Adjacent: U(6,7,8) → R(27,30,33) [bottom of U → left col of R]
        //           R(27,30,33) → D(47,46,45) [left col of R → top of D, reversed]
        //           D(47,46,45) → L(11,14,17) [top of D → right col of L]
        //           L(11,14,17) → U(8,7,6) [right col of L → bottom of U, reversed]
        int[] f = fresh();
        f[20] = 18; f[23] = 19; f[26] = 20;
        f[19] = 21; f[22] = 22; f[25] = 23;
        f[18] = 24; f[21] = 25; f[24] = 26;

        f[27] = 6;  f[30] = 7;  f[33] = 8;
        f[47] = 27; f[46] = 30; f[45] = 33;
        f[11] = 47; f[14] = 46; f[17] = 45;
        f[6] = 11;  f[7] = 14;  f[8] = 17;
        p[F] = f;

        // ─── R CW ───
        // R face (27-35) rotates CW
        //   corners: 27→29→35→33→27  edges: 28→32→34→30→28
        // Adjacent: U(2,5,8) → B(36,39,42) [right col of U → left col of B]
        //           B(36,39,42) → D(47,50,53) [left col of B → right col of D]
        //           D(47,50,53) → F(20,23,26) [right col of D → right col of F, reversed]
        //           F(20,23,26) → U(8,5,2) [right col of F → right col of U, reversed]
        int[] r = fresh();
        r[29] = 27; r[32] = 28; r[35] = 29;
        r[28] = 30; r[31] = 31; r[34] = 32;
        r[27] = 33; r[30] = 34; r[33] = 35;

        r[36] = 2;  r[39] = 5;  r[42] = 8;
        r[47] = 36; r[50] = 39; r[53] = 42;
        r[20] = 47; r[23] = 50; r[26] = 53;
        r[2] = 20;  r[5] = 23;  r[8] = 26;
        p[R] = r;

        // ─── B CW ───
        // B face (36-44) rotates CW
        //   corners: 36→38→44→42→36  edges: 37→41→43→39→37
        // Adjacent: U(6,7,8) → L(9,12,15) [back of U → left col of L, reversed]
        //           L(9,12,15) → D(51,52,53) [left col of L → bottom of D]
        //           D(51,52,53) → R(33,30,27) [bottom of D → right col of R, reversed]
        //           R(33,30,27) → U(6,7,8) [right col of R → back of U]
        int[] b2 = fresh();
        // B face corners
        b2[38] = 36; b2[44] = 38; b2[42] = 44; b2[36] = 42;
        // B face edges
        b2[41] = 37; b2[43] = 41; b2[39] = 43; b2[37] = 39;
        // Corner cycles
        b2[8] = 6;   b2[29] = 9;
        b2[53] = 8;  b2[35] = 29;
        b2[51] = 53; b2[17] = 35;
        b2[6] = 51;  b2[9] = 17;
        // Edge cycles (adjacent facelets only)
        b2[32] = 7;
        b2[52] = 32;
        b2[14] = 52;
        b2[7] = 14;
        p[B] = b2;

        // ─── D CW ───
        // D face (45-53) rotates CW
        //   corners: 45→47→53→51→45  edges: 46→50→52→48→46
        // Adjacent: F(24,25,26) → R(33,34,35) [bottom of F → bottom of R]
        //           R(33,34,35) → B(42,43,44) [bottom of R → bottom of B]
        //           B(42,43,44) → L(15,16,17) [bottom of B → bottom of L]
        //           L(15,16,17) → F(24,25,26) [bottom of L → bottom of F]
        int[] d = fresh();
        d[47] = 45; d[50] = 46; d[53] = 47;
        d[46] = 48; d[49] = 49; d[52] = 50;
        d[45] = 51; d[48] = 52; d[51] = 53;

        d[33] = 24; d[34] = 25; d[35] = 26;
        d[42] = 33; d[43] = 34; d[44] = 35;
        d[15] = 42; d[16] = 43; d[17] = 44;
        d[24] = 15; d[25] = 16; d[26] = 17;
        p[D] = d;

        return p;
    }

    private static int[] fresh() {
        int[] a = new int[54];
        for (int i = 0; i < 54; i++) a[i] = i;
        return a;
    }
}

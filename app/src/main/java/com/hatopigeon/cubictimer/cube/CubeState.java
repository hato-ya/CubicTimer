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
        // Cross L(1), opposite R(3): side faces U,F,D,B, skip right col on each
        {9,10,11,12,13,14,15,16,17,
         0,1,3,4,6,7,
         18,19,21,22,24,25,
         45,46,48,49,51,52,
         36,37,39,40,42,43},
        // Cross F(2), opposite B(4): side faces U,L,D,R
        {18,19,20,21,22,23,24,25,26,
         3,4,5,6,7,8,
         10,11,13,14,16,17,
         45,46,47,48,49,50,
         27,28,30,31,33,34},
        // Cross R(3), opposite L(1): side faces U,F,D,B, skip left col on each
        {27,28,29,30,31,32,33,34,35,
         1,2,4,5,7,8,
         19,20,22,23,25,26,
         46,47,49,50,52,53,
         37,38,40,41,43,44},
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

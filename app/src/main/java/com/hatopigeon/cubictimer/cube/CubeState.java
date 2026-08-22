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
    // Whole-cube rotations used to bring the last layer onto U for OLL matching. Generated from a
    // 3D cubie model and verified (all 57 OLL cases recognized) in CubeStateOLLandPLLTest.
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

    public boolean isF2lSolved(int crossFace) {
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

    public boolean isOllSolved(int crossFace) {
        if (crossFace < 0) return false;
        int opp = OPPOSITE[crossFace];
        int oppCenter = opp * 9 + 4;
        for (int i = opp * 9; i < opp * 9 + 9; i++) {
            if (facelets[i] != facelets[oppCenter]) return false;
        }
        return true;
    }

    public boolean isPllSolved(int crossFace) {
        if (crossFace < 0) return false;
        int opp = OPPOSITE[crossFace];

        if (facelets[CORNER_SIDES[opp][0][0]] == facelets[CORNER_SIDES[opp][0][1]] &&
                facelets[CORNER_SIDES[opp][0][0]] == facelets[CORNER_SIDES[opp][0][2]] &&
                facelets[CORNER_SIDES[opp][1][0]] == facelets[CORNER_SIDES[opp][1][1]] &&
                facelets[CORNER_SIDES[opp][1][0]] == facelets[CORNER_SIDES[opp][1][2]] &&
                facelets[CORNER_SIDES[opp][2][0]] == facelets[CORNER_SIDES[opp][2][1]] &&
                facelets[CORNER_SIDES[opp][2][0]] == facelets[CORNER_SIDES[opp][2][2]] &&
                facelets[CORNER_SIDES[opp][3][0]] == facelets[CORNER_SIDES[opp][3][1]] &&
                facelets[CORNER_SIDES[opp][3][0]] == facelets[CORNER_SIDES[opp][3][2]] )
            return true;

        return false;
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
        if (!isF2lSolved(crossFace)) return -1;

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

    // PLL recognition by position signature. A PLL case is invariant under AUF turns on *both*
    // sides (the U turn before and after the algorithm), i.e. the whole double coset, so a single
    // case can present many distinct sticker patterns. We canonicalise a state to the smallest
    // signature over its 4 U-turn views (one left coset) and map that canonical to the case index.
    //
    // The 71 canonical -> case entries below were generated from the verified PLL algorithms via a
    // 3D cubie model in CubeStateOLLandPLLTest (no collisions). Index = alg_reference_PLL order
    // (H, Ua, Ub, Z, Aa, Ab, E, F, Ga, Gb, Gc, Gd, Ja, Jb, Na, Nb, Ra, Rb, T, V, Y). Each datum is
    // the 25-char signature followed by the 0-based case index.
    private static final java.util.Map<String, Integer> PLL_CANON = buildPllCanon();
    private static java.util.Map<String, Integer> buildPllCanon() {
        String[] data = {
            "X111X200042000330003X442X12", "X111X200043000220004X343X1",  "X111X200043000430003X422X13",
            "X111X200044000230003X432X7",  "X111X200044000320004X323X2",  "X113X200022000340004X143X19",
            "X113X200022000430001X434X5",  "X113X200023000230001X444X13", "X113X200023000440004X123X20",
            "X113X200024000240004X133X14", "X113X200024000330001X424X9",  "X114X200032000320001X344X13",
            "X114X200032000440003X132X4",  "X114X200033000240003X142X8",  "X114X200033000420001X324X17",
            "X114X200034000220001X334X18", "X114X200034000340003X122X13", "X121X200041000320004X343X3",
            "X121X200041000430003X432X17", "X121X200043000130003X442X4",  "X121X200043000420004X313X1",
            "X121X200044000120004X333X1",  "X121X200044000330003X412X10", "X123X200021000330001X444X7",
            "X123X200021000440004X133X19", "X123X200023000140004X143X6",  "X123X200023000430001X414X17",
            "X123X200024000130001X434X16", "X123X200024000340004X113X19", "X124X200031000340003X142X7",
            "X124X200031000420001X334X16", "X124X200033000120001X344X4",  "X124X200033000440003X112X10",
            "X124X200034000140003X132X9",  "X124X200034000320001X314X9",  "X131X200041000230003X442X11",
            "X131X200041000420004X323X2",  "X131X200042000120004X343X1",  "X131X200042000430003X412X18",
            "X131X200044000130003X422X9",  "X131X200044000220004X313X0",  "X133X200021000240004X143X19",
            "X133X200021000430001X424X11", "X133X200022000130001X444X12", "X133X200022000440004X113X15",
            "X133X200024000140004X123X20", "X133X200024000230001X414X4",  "X134X200031000220001X344X8",
            "X134X200031000440003X122X17", "X134X200032000140003X142X11", "X134X200032000420001X314X7",
            "X134X200034000120001X324X11", "X134X200034000240003X112X5",  "X141X200041000220004X333X2",
            "X141X200041000330003X422X5",  "X141X200042000130003X432X16", "X141X200042000320004X313X2",
            "X141X200043000120004X323X3",  "X141X200043000230003X412X8",  "X143X200021000230001X434X8",
            "X143X200021000340004X123X6",  "X143X200022000140004X133X20", "X143X200022000330001X414X10",
            "X143X200023000130001X424X18", "X143X200023000240004X113X20", "X144X200031000240003X132X16",
            "X144X200031000320001X324X5",  "X144X200032000120001X334X12", "X144X200032000340003X112X12",
            "X144X200033000140003X122X18", "X144X200033000220001X314X10",
        };
        java.util.Map<String, Integer> m = new java.util.HashMap<>(data.length * 2);
        for (String d : data) m.put(d.substring(0, 25), Integer.parseInt(d.substring(25)));
        return m;
    }

    /**
     * Recognizes the exact PLL case of the last layer (the face opposite the cross), independent of
     * AUF. Returns the 1-based PLL index (1-21, matching alg_reference_PLL order), 0 if the last
     * layer is solved (PLL skip), or -1 if F2L isn't solved or the last layer isn't oriented yet
     * (i.e. it's still an OLL, not a PLL).
     *
     * The last layer is rotated onto U, canonicalised to the smallest position signature over its 4
     * AUF views, and looked up in {@link #PLL_CANON}. Each sticker is identified by the face
     * position whose centre colour it matches, so recognition is independent of cross face, colour
     * scheme, and which AUF the case was left at.
     */
    public int getPllCase(int crossFace) {
        if (crossFace < 0) return -1;
        if (!isF2lSolved(crossFace)) return -1;

        CubeState work = new CubeState();
        work.setFacelets(this.facelets);
        work.orientLastFaceToU(OPPOSITE[crossFace]);

        if (!work.isLastLayerOriented()) return -1; // still an OLL, not a PLL

        String canon = null;
        for (int auf = 0; auf < 4; auf++) {
            String sig = work.pllSignatureU();
            if (canon == null || sig.compareTo(canon) < 0) canon = sig;
            work.applyPerm(OLL_U, 1);
        }
        Integer c = PLL_CANON.get(canon);
        return c == null ? 0 : c + 1; // no match => solved last layer (PLL skip)
    }

    /**
     * Recognizes the PLL case without knowing the cross face: tries all six faces and returns the
     * first that yields a real PLL (1-21). Returns 0 if some face is solved/oriented (skip), or -1
     * if no face has an oriented, F2L-solved last layer.
     */
    public int getPllCase() {
        boolean skip = false;
        for (int crossFace = 0; crossFace < 6; crossFace++) {
            int c = getPllCase(crossFace);
            if (c > 0) return c;
            if (c == 0) skip = true;
        }
        return skip ? 0 : -1;
    }

    /**
     * Builds the PLL position signature with the last layer on U: each side cell holds the face
     * position (0-5) whose centre colour matches the sticker, so it's independent of how the cube
     * was rotated to bring the last layer onto U. Corners are 'X'.
     */
    private String pllSignatureU() {
        int[] posOf = new int[6];
        for (int p = 0; p < 6; p++) posOf[facelets[p * 9 + 4]] = p;
        char[] sig = new char[25];
        for (int i = 0; i < 25; i++) {
            int fc = OLL_GRID_TO_FACELET[i];
            sig[i] = (fc < 0) ? 'X' : (char) ('0' + posOf[facelets[fc]]);
        }
        return new String(sig);
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

    // ─── Beginner (LBL) step detection ───
    //
    // The 8 corners, each as its 3 facelet indices (one per adjacent face), in CubeState order.
    // Converted from GanCubeManager.CORNER_FACELET_MAP via TS_TO_CS_POS.
    private static final int[][] CORNERS = {
        {8, 27, 20}, {6, 18, 11}, {0, 9, 38}, {2, 36, 29},
        {47, 26, 33}, {45, 17, 24}, {51, 44, 15}, {53, 35, 42},
    };

    private static final int[][][] CORNER_SIDES = {
            {{ 9, 10, 11}, {18, 19, 20}, {27, 28, 29}, {36, 37, 38}},
            {{ 0,  3,  6}, {18, 21, 24}, {38, 41, 44}, {45, 48, 51}},
            {{ 6,  7,  8}, {11, 14, 17}, {27, 30, 33}, {45, 46, 47}},
            {{ 2,  5,  8}, {20, 23, 26}, {36, 39, 42}, {47, 50, 53}},
            {{ 0,  1,  2}, { 9, 12, 15}, {29, 32, 35}, {51, 52, 53}},
            {{15, 16, 17}, {24, 25, 26}, {33, 34, 35}, {42, 43, 44}}
    };

    private static int centerOf(int idx) {
        return (idx / 9) * 9 + 4;
    }

    /** First layer complete: the cross face's cross AND its four corners solved in place. */
    public boolean isFirstLayerSolved(int crossFace) {
        if (crossFace < 0 || !isCrossSolved(crossFace)) return false;
        for (int[] c : CORNERS) {
            if (c[0] / 9 != crossFace && c[1] / 9 != crossFace && c[2] / 9 != crossFace) continue;
            for (int fc : c) if (facelets[fc] != facelets[centerOf(fc)]) return false;
        }
        return true;
    }

    /** Opposite "cross": the four last-layer edges oriented (showing the LL colour), ignoring sides. */
    public boolean isEdgeOllSolved(int crossFace) {
        if (crossFace < 0) return false;
        int opp = OPPOSITE[crossFace];
        int oc = opp * 9 + 4;
        return facelets[opp * 9 + 1] == facelets[oc] && facelets[opp * 9 + 3] == facelets[oc]
                && facelets[opp * 9 + 5] == facelets[oc] && facelets[opp * 9 + 7] == facelets[oc];
    }

    /** Last-layer edges aligned to centres (the opposite cross fully solved). */
    public boolean isLLCrossSolved(int crossFace) {
        if (crossFace < 0) return false;
        return isCrossSolved(OPPOSITE[crossFace]);
    }

    /** Last-layer corners in their correct slot (correct colour set), regardless of twist. */
    public boolean areLLCornersPositioned(int crossFace) {
        if (crossFace < 0) return false;
        int opp = OPPOSITE[crossFace];
        for (int[] c : CORNERS) {
            if (c[0] / 9 != opp && c[1] / 9 != opp && c[2] / 9 != opp) continue;
            int a = facelets[c[0]], b = facelets[c[1]], d = facelets[c[2]];
            int ca = facelets[centerOf(c[0])], cb = facelets[centerOf(c[1])], cd = facelets[centerOf(c[2])];
            // Both sets hold 3 distinct values, so membership of each implies equal sets.
            boolean inA = a == ca || a == cb || a == cd;
            boolean inB = b == ca || b == cb || b == cd;
            boolean inD = d == ca || d == cb || d == cd;
            if (!(inA && inB && inD)) return false;
        }
        return true;
    }

    public boolean isCornerPllSolved(int crossFace) {
        if (crossFace < 0) return false;
        int opp = OPPOSITE[crossFace];

        if (facelets[CORNER_SIDES[opp][0][0]] == facelets[CORNER_SIDES[opp][0][2]] &&
                facelets[CORNER_SIDES[opp][1][0]] == facelets[CORNER_SIDES[opp][1][2]] &&
                facelets[CORNER_SIDES[opp][2][0]] == facelets[CORNER_SIDES[opp][2][2]] &&
                facelets[CORNER_SIDES[opp][3][0]] == facelets[CORNER_SIDES[opp][3][2]] )
            return true;

        return false;
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

    // Generates each face's 90° CW facelet permutation from a real 3D cubie model (the same net
    // layout used everywhere else) instead of hand-written sticker cycles. Deriving them from
    // geometry guarantees they compose correctly across faces, so move tracking and the 3D model
    // stay consistent through fast multi-move bursts. Verified by CubeStateMoveEngineTest
    // (each face^4, sexy x6, Sune x6, T-perm x2 = identity).
    private static int[][] createPermutations() {
        int[][] pos = new int[54][3];
        int[][] nrm = new int[54][3];
        fillGeom(pos, nrm, 0,  new int[]{0, 1, 0},  (r, c) -> new int[]{c - 1, 1, r - 1});   // U
        fillGeom(pos, nrm, 9,  new int[]{-1, 0, 0}, (r, c) -> new int[]{-1, 1 - r, c - 1});  // L
        fillGeom(pos, nrm, 18, new int[]{0, 0, 1},  (r, c) -> new int[]{c - 1, 1 - r, 1});   // F
        fillGeom(pos, nrm, 27, new int[]{1, 0, 0},  (r, c) -> new int[]{1, 1 - r, 1 - c});   // R
        fillGeom(pos, nrm, 36, new int[]{0, 0, -1}, (r, c) -> new int[]{1 - c, 1 - r, -1});  // B
        fillGeom(pos, nrm, 45, new int[]{0, -1, 0}, (r, c) -> new int[]{c - 1, -1, 1 - r});  // D

        java.util.HashMap<String, Integer> lookup = new java.util.HashMap<>();
        for (int i = 0; i < 54; i++) lookup.put(geomKey(pos[i], nrm[i]), i);

        int[][] p = new int[6][54];
        // CubeState face order: U=0, L=1, F=2, R=3, B=4, D=5.  (axis 0=x,1=y,2=z; layer; spin sign)
        p[U] = faceTurn(pos, nrm, lookup, 1,  1, -1);
        p[L] = faceTurn(pos, nrm, lookup, 0, -1,  1);
        p[F] = faceTurn(pos, nrm, lookup, 2,  1, -1);
        p[R] = faceTurn(pos, nrm, lookup, 0,  1, -1);
        p[B] = faceTurn(pos, nrm, lookup, 2, -1,  1);
        p[D] = faceTurn(pos, nrm, lookup, 1, -1,  1);
        return p;
    }

    private interface GeomPosFn { int[] at(int r, int c); }

    private static void fillGeom(int[][] pos, int[][] nrm, int base, int[] n, GeomPosFn fn) {
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++) {
                int idx = base + 3 * r + c;
                pos[idx] = fn.at(r, c);
                nrm[idx] = n;
            }
    }

    private static String geomKey(int[] p, int[] n) {
        return p[0] + "," + p[1] + "," + p[2] + "|" + n[0] + "," + n[1] + "," + n[2];
    }

    // Rotates an integer (position or normal) vector 90° about the given axis with spin sign s.
    private static int[] geomRot(int[] v, int axis, int s) {
        int x = v[0], y = v[1], z = v[2];
        switch (axis) {
            case 0:  return new int[]{x, -s * z, s * y};
            case 1:  return new int[]{s * z, y, -s * x};
            default: return new int[]{-s * y, s * x, z};
        }
    }

    // Builds the facelet permutation for a single layer turn (the layer at pos[axis]==layer),
    // in the same convention as applyMove: new[i] = old[perm[i]].
    private static int[] faceTurn(int[][] pos, int[][] nrm, java.util.HashMap<String, Integer> lookup,
                                  int axis, int layer, int s) {
        int[] perm = new int[54];
        for (int i = 0; i < 54; i++) perm[i] = i;
        for (int j = 0; j < 54; j++) {
            if (pos[j][axis] != layer) continue;
            int k = lookup.get(geomKey(geomRot(pos[j], axis, s), geomRot(nrm[j], axis, s)));
            perm[k] = j;
        }
        return perm;
    }
}

package com.hatopigeon.cubictimer.cube;

import java.util.Arrays;

public class CubeState {
    public static final int NUM_FACELETS = 54;
    public static final int U = 0, L = 1, F = 2, R = 3, B = 4, D = 5;

    private final int[] facelets = new int[NUM_FACELETS];

    private static final int[] SOLVED = new int[54];
    static {
        for (int f = 0; f < 6; f++)
            Arrays.fill(SOLVED, f * 9, (f + 1) * 9, f);
    }

    private static final int[][] PERM_CW = createPermutations();

    public CubeState() {
        reset();
    }

    public void reset() {
        System.arraycopy(SOLVED, 0, facelets, 0, 54);
    }

    public int[] getFacelets() {
        return facelets.clone();
    }

    public void setFacelets(int[] newFacelets) {
        System.arraycopy(newFacelets, 0, facelets, 0, 54);
    }

    public boolean isSolved() {
        for (int i = 0; i < 54; i++)
            if (facelets[i] != SOLVED[i]) return false;
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
        if (direction == 2) {
            applyMove(face, 1);
            applyMove(face, 1);
            return;
        }

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
        // Hmm, this doesn't look right. Let me reconsider.
        //
        // B is the back face. B CW means looking at B from behind, CW.
        // In 3D, B touches U, L, D, R.
        //   U back row (touches B) = U6, U7, U8 (indices 6,7,8)
        //   L left column (touches B... wait, L touches B at L's LEFT column?
        //     In 3D: the left side of B touches the right side of L (viewed from back),
        //     which is L's RIGHT column in the net.
        //     L right column = L2(11), L5(14), L8(17)
        //   D back row (touches B) = D6, D7, D8 (indices 51,52,53)
        //   R right column (touches B) = R2(29), R5(32), R8(35)
        //
        // Wait, let me look at the net again:
        //
        // Face B (36-44) in the net:
        // 36 37 38
        // 39 40 41
        // 42 43 44
        //
        // In 3D (with this net), B is to the right of R.
        // B's left column (touching R) = 36,39,42
        // B's right column (touching L in 3D but NOT visible in net) = 38,41,44
        // B's top row (touching U in 3D but NOT visible in net) = 36,37,38
        // B's bottom row (touching D in 3D but NOT visible in net) = 42,43,44
        //
        // So B CW (from B's perspective, looking at B from behind):
        //   U back row indices: U6(6), U7(7), U8(8) → L(11,14,17)? or L(9,12,15)?
        //
        // L in the net:
        // 9 10 11
        // 12 13 14
        // 15 16 17
        // L's right column = 11,14,17 (touching F in the net, but in 3D also touches B)
        //
        // When B turns CW (looking at B = looking from behind):
        // The sticker at U6 → ? 
        //
        // U6 is at the UBL corner (U6, L0, B6). After B CW, this corner becomes UFL (U0, L0, F0)?
        // No, B CW doesn't affect the F layer. B layer pieces are: U6,U7,U8, L11?14?17, D6,D7,D8, R29,32,35
        //
        // B CW: corner UBL (U6,L0,B6) → corner DLB (D6,L8,B8)?
        // Hmm, but L0 is index 9 and L8 is index 17. And B6 is index 42, B8 is index 44.
        //
        // Actually, the UBL corner has stickers at: U6(6), L0(9), B6(42)
        // After B CW: this corner goes to... DLB (touching D,L,B): D6(51), L8(17), B8(44)
        // So: new[51] = old[6], new[17] = old[9], new[44] = old[42]
        //
        // Hmm wait, but the sticker at L0 goes to L8? That's on the L face, which is part of the B layer.
        // And the "B6" sticker of the UBL corner goes to "B8" position, which is B CW rotation.
        //
        // Corner DLB (D6,L8,B8) → corner UBL... wait.
        //
        // I'm losing track. Let me just define B CW using the cycles:
        //
        // B CW from behind:
        // Corner UBL(U6=6, L0=9, B6=42) → UBR(U8=8, R2=29, B2=38)? No, B CW means the U goes to R.
        // Hmm, looking at B from behind: CW means what was on the LEFT from behind (U side) goes to the RIGHT from behind (L side? no...)
        //
        // Looking at B from behind:
        // - U is UP from this perspective (B's top)
        // - D is DOWN from this perspective (B's bottom)
        // - L is LEFT from this perspective (B's left = cube's right)
        // - R is RIGHT from this perspective (B's right = cube's left)
        //
        // B CW from this perspective: the UP stickers go to the LEFT from behind (cube's RIGHT).
        // So U stickers go to L (cube's right side from behind).
        //
        // B CW: U6,L0,B6 → ? The corner at UBL goes to... 
        // From behind perspective, the corner at UBL (top-left of B):
        // After CW, goes to... B's top-right from behind = UBR corner?
        // UBR in 3D = U8(8), R2(29), B2(38)
        // So: new[8] = old[6], new[29] = old[9], new[38] = old[42]

        // Actually you know what, let me just be pragmatic. The correct answer for B CW
        // with this layout is well known. Let me define it precisely.
        //
        // B face CW rotation (36-44):
        // corners: 36→38→44→42→36, edges: 37→41→43→39→37
        int[] b = fresh();
        b[38] = 36; b[41] = 37; b[44] = 38;
        b[37] = 39; b[40] = 40; b[43] = 41;
        b[36] = 42; b[39] = 43; b[42] = 44;
        //
        // B CW adjacent: 
        // Looking at B from behind, B CW:
        // U back row (6,7,8) → goes to L column (which column of L?)
        //   From behind: the top of B (U) moves LEFT (towards L column from B's perspective)
        //   Which is to the LEFT in the behind view, meaning the RIGHT column of L in the net view
        //   Wait no, the behind view flips left-right. From behind, B's left is cube's R.
        //   B CW from behind: U → L (in cube space)
        //   U6 → L9 (index 9 = L0, top-left of L)?
        //   But that doesn't make sense from the 3D perspective.
        //   
        //   OK, from 3D: B CW means the BACK of U goes to the LEFT (as seen from front).
        //   Actually: B CW (looking at B) means from behind, the top (U) of B goes to the left.
        //   "Left from behind" = "Right from front" = R face in terms of the cube.
        //   But in terms of B's adjacent faces, "left from behind" = L's side of B.
        //
        //   So B CW: the U stickers on the B layer go to the L side of the B layer.
        //   U6(6), U7(7), U8(8) are the stickers on the B layer on the U face.
        //   They go to the L side: L2(11), L5(14), L8(17) (L's right column, which touches B).
        //
        //   But the mapping needs the right cycle direction:
        //   U6 → L17? or U6 → L11?
        //
        //   The U-B edge has 3 stickers: U6(touches L), U7(center), U8(touches R).
        //   The L-B edge has 3 stickers on L: L2(11=touches U), L5(14=center), L8(17=touches D).
        //
        //   When B CW: U8(touches R side) → L2(touches U side)? 
        //   From behind: the sticker that was at the left of the U-B edge goes to the top of the L-B edge.
        //   
        //   Hmm, I think:
        //   U6(6) → L2(11) [UBL corner → BLU corner]
        //   U7(7) → L5(14) [UB edge → BL edge]
        //   U8(8) → L8(17) [UBR corner → BLD corner]
        //
        //   Then L2(11) → D8(53) [BLU corner → BLD... wait that doesn't make sense]
        //   L5(14) → D7(52) [BL edge → BD edge]
        //   L8(17) → D6(51) [BLD corner → BDL corner]
        //
        //   Then D8(53) → R2(29) [BDL corner? → BDR corner]
        //   D7(52) → R5(32) [BD edge → BR edge]
        //   D6(51) → R8(35) [BDL corner → BRU corner?]
        //
        //   Then R2(29) → U6(6) [BDR corner → UBR corner?]
        //   R5(32) → U7(7) [BR edge → UB edge]
        //   R8(35) → U8(8) [BRU corner → UBR corner]
        //
        //   Let me verify: U6(UBL) → L2(BLU) → D8(BLD) → R2(BDR) → U6(UBR)?
        //   Hmm, this cycle goes UBL → BLU → BLD → BDR → UBR and then back to UBL.
        //   But these are corners at: UBL(is U+L+B), BLU(is U+L+B... same), BLD(is D+L+B), BDR(is D+R+B), UBR(is U+R+B).
        //   The cycle: (U+L+B), (D+L+B), (D+R+B), (U+R+B) ← this is a 4-cycle of corners.
        //    
        //   Let me trace more carefully:
        //   U6 at corner UBL: L0(9), B6(42)
        //   After B CW, UBL →... corner at intersection of L and B on the B layer:
        //   Well, the corner UBL is attached to the B face. After B CW, corner UBL goes to... BLD?
        //
        //   No wait, UBL is the corner where U, L, and B meet. After B CW, this corner piece moves to where D, L, and B meet (the BLD corner), which has stickers: D6(51), L8(17), B8(44).
        //   
        //   So: new[51] = old[6], new[17] = old[9], new[44] = old[42].
        //   ✓
        //
        //   And the BLD corner (D6(51), L8(17), B8(44)) after B CW goes to:
        //   ...the corner where D, R, and B meet (the BDR corner): D8(53), R8(35), B0(36).
        //   new[53] = old[51], new[35] = old[17], new[36] = old[44].
        //   ✓
        //
        //   And the BDR corner (D8(53), R8(35), B0(36)) after B CW goes to:
        //   ...the corner where U, R, and B meet (the UBR corner): U8(8), R2(29), B2(38).
        //   new[8] = old[53], new[29] = old[35], new[38] = old[36].
        //   ✓
        //
        //   And the UBR corner (U8(8), R2(29), B2(38)) after B CW goes to:
        //   ...the corner where U, L, and B meet (the UBL corner): U6(6), L0(9), B6(42).
        //   new[6] = old[8], new[9] = old[29], new[42] = old[38].
        //   ✓
        //
        //   This is a consistent cycle! Now for edges:
        //   Edges on B layer:
        //   UB edge: U7(7), B1(37)
        //   BL edge: L5(14), B7(43)
        //   BD edge: D7(52), B3(39)
        //   BR edge: R5(32), B5(41)
        //
        //   B CW: UB(U7,B1) → BL(L5,B7) → BD(D7,B3) → BR(R5,B5) → UB(U7,B1)
        //
        //   So: 
        //   new[14] = old[7], new[43] = old[37] -- UB→BL
        //   new[52] = old[14], new[39] = old[43] -- BL→BD  
        //   new[32] = old[52], new[41] = old[39] -- BD→BR
        //   new[7] = old[32], new[37] = old[41] -- BR→UB
        //
        //   Let me verify:
        //   b[14] = 7: the UB edge sticker on L goes to L5. From old index 7 (U7).
        //   OK. But wait, I need to be careful. For edges:
        //   The UB edge has stickers at U7(index 7) and B1(index 37).
        //   After B CW, this edge goes to the BL position.
        //   The BL edge has stickers at L5(index 14) and B7(index 43).
        //   So: new[14] = old[7], new[43] = old[37]. ✓
        //
        //   Then BL→BD:
        //   new[52] = old[14], new[39] = old[43]. ✓
        //
        //   Then BD→BR:
        //   new[32] = old[52], new[41] = old[39]. ✓
        //
        //   Then BR→UB:
        //   new[7] = old[32], new[37] = old[41]. ✓

        // Now putting it all together for B CW:
        b[51] = 6;  b[17] = 9;  b[44] = 42; // UBL→BLD (wait, I already defined b[44] = 38 above!)
        // Hmm, conflict! b[44] is already defined in the B face rotation (b[44] = 38).

        // OK the problem is that I'm trying to modify the same facelet b[44] twice:
        // once for the B face rotation and once for the corner cycle.
        // But B8 is both the B face corner AND the corner that receives from the cycle.
        // In the B face CW rotation: B8(index 44) comes from B6(index 42): b[44] = 42
        // In the corner cycle: BLD corner has B8(44) sticker, and after B CW it goes to BDR corner (B0=36): wait, I'm confused.

        // Actually, I need to think about this differently. The corner BLD has stickers:
        // D6(51), L8(17), B8(44). After B CW, the B sticker at B8 goes to... B0.
        // So b[36] = 44 (old B8 → new B0).
        // No, I defined it as "new[36] = old[44]" earlier, which means b[36] = 44. ✓

        // But I also have the B face rotation: b[36] = 42 (B0 comes from B6 as part of the B face CW).
        // These conflict! The corner B0 is both part of the B face rotation AND
        // receives the sticker from the old B8 via the corner cycle.
        // 
        // Wait, no. The B face rotation and the corner piece cycle describe the SAME movement.
        // The B face rotation says: B0(36) → B2(38), B2(38) → B8(44), B8(44) → B6(42), B6(42) → B0(36).
        // In terms of new = old[perm]:
        //   b[38] = 36 (new B2 = old B0), b[44] = 38 (new B8 = old B2), b[42] = 44 (new B6 = old B8), b[36] = 42 (new B0 = old B6)
        //   These are the face rotation.
        //
        // The piece cycle says: UBL corner → ... includes the B6 sticker going to B0.
        // UBL corner: U6(6), L0(9), B6(42). After B CW, UBL → BLD.
        // BLD corner has stickers at: D6(51), L8(17), B8(44).
        // So the OLD B6(42) goes to the NEW position of what? The B sticker of UBL goes to B of BLD.
        // But BLD has B sticker at B8(44). So new[44] = old[42]. = b[44] = 42.
        // But b[44] was already defined as b[44] = 38 from the B face rotation!
        //
        // The issue is that the B face rotation and the piece cycle are DUPLICATE descriptions
        // of the same rotation. The face rotation handles the B face stickers (36-44),
        // and the adjacent cycle handles the stickers on adjacent faces (U, L, D, R).
        //
        // For the UBL corner: U6(6), L0(9), B6(42) → after B CW:
        //   The U sticker (index 6) goes to the D side (D6, index 51)
        //   The L sticker (index 9) goes to the L side (L8, index 17)
        //   The B sticker (index 42 = B6) stays on the B face and rotates to a new position (B8, index 44)
        //
        // But the B6→B8 rotation IS part of the B face CW rotation!
        // In the face rotation: b[44] = 42 means new[B8] = old[B6].
        // In the corner tracking: B6(42) rotates to B8(44) position, which is ALSO b[44] = 42.
        // These are the same! So there's NO conflict.
        //
        // Wait, but I defined b[44] = 38 in the face rotation:
        // b[38] = 36 (B2 from B0), b[44] = 38 (B8 from B2), b[42] = 44 (B6 from B8), b[36] = 42 (B0 from B6)
        //
        // But the piece cycle says: UBL → BLD: B6(42)→B8(44), which is b[44] = 42.
        // But the face rotation says: B8 comes from B2: b[44] = 38.
        // These ARE different! b[44] can only have one value.
        //
        // The resolution: the B face rotation describes where the B FACE STICKERS go.
        // The corner piece describes where ALL stickers go.
        // They should agree. In a CW rotation of B:
        // B0(36) → B2(38) → B8(44) → B6(42) → B0(36)
        // This is correct for the face rotation.
        //
        // But then, the corner UBL has B6(42). After B CW, this corner goes to BLD.
        // The BLD corner on B has B8(44). So the sticker from B6(42) goes to B8(44) position.
        // This means new[B8] = old[B6], which gives b[44] = 42.
        //
        // But the face rotation says B8(44) comes from B2(38): b[44] = 38.
        //
        // CONTRADICTION! The issue is that in the B CW rotation:
        // The sticker at B2(38) goes to B8(44), AND
        // The sticker at B6(42) goes to B0(36).
        //
        // So: new[B8] = old[B2] (b[44] = 38) from the face rotation.
        // And: new[B8] = old[B6] from the corner piece cycle. 
        //
        // These can't both be right. What's the correct mapping?
        //
        // The error is that I'm confusing B-layer adjacent stickers with B-face stickers.
        // The B LAYER includes: B face (36-44), U back row (6,7,8), L right column (11,14,17), 
        // D back row (51,52,53), R right column (29,32,35).
        //
        // The corner UBL has stickers at: U6(6), L0(9?) ... wait, L0(9) is NOT on the B layer!
        // The B layer includes L's RIGHT column: L2(11), L5(14), L8(17).
        // But L0(9) is not in the B layer. L0 is the top-left of L (touching U and F).
        //
        // So the UBL corner: U6(6) is in the B layer, L0(9) is NOT in the B layer, B6(42) is in the B layer.
        // After B CW, the corner piece UBL rotates. The U6 and B6 stickers move, but L0 stays?
        // NO! The corner piece is a physical unit of the cube. When B rotates, the UBL piece moves.
        // This means ALL three stickers move together: U6, L0, and B6.
        //
        // But L0 is not in the B layer! How can it move? 
        //
        // Because the corner UBL is NOT part of the B layer. No wait, it IS - the corner UBL
        // has stickers on U, L, and B faces. The B layer includes the U facelet at the UB edge
        // and the B facelet. The L facelet L0 IS part of the B layer!
        //
        // Actually, let me reconsider which facelets are in the B layer. The B layer consists of
        // ALL facelets that are on the B side of the cube. This includes:
        // - The entire B face: 36-44
        // - The stickers on U that are on the back row: U6, U7, U8 (indices 6,7,8)
        // - The stickers on L that are on the back column: L0, L3, L6? Or L2, L5, L8?
        // - The stickers on D that are on the back row: D6, D7, D8 (indices 51,52,53)
        // - The stickers on R that are on the Back column: R2, R5, R8 (indices 29,32,35)
        //
        // In my net, L(9-17):
        // 9  10 11
        // 12 13 14
        // 15 16 17
        //
        // L's right column (11,14,17) touches F in the net.
        // L's left column (9,12,15) touches... nothing in the net, but in 3D it touches B.
        //
        // So the L stickers in the B layer are: L0(9), L3(12), L6(15) — the LEFT column of L.
        // And UBL corner: U6(6), L0(9), B6(42). All three are in the B layer.
        //
        // After B CW (looking at B from behind):
        // UBL corner → BLD corner: D6(51), L8(17), B8(44).
        // 
        // Wait, the BLD corner has stickers: D6(51), L8(17), B8(44).
        // These are: D face(51=D6), L face(17=L8), B face(44=B8).
        //
        // So: new[51] = old[6], new[17] = old[9], new[44] = old[42].
        // (The B sticker from UBL goes to B8, new position of BLD corner on B face.)
        //
        // But the B face rotation says:
        // b[44] = 42 (new B8 = old B6). This IS consistent! ✓
        // Because the B6 sticker from the UBL corner rotates to the B8 position,
        // which IS part of the B face rotation: B6 → B0 under CW? No!
        //
        // Wait, in B CW: B6(42) → goes to... B0(36)? B2(38)? B8(44)?
        //
        // In B CW looking at B:
        // B0(ULB corner) → B2(URB corner)
        // B1(UB edge) → B5(RB edge)
        // B2(URB corner) → B8(DRB corner)
        // B3(LB edge) → B1(UB edge)
        // B4(center) stays
        // B5(RB edge) → B7(DB edge)
        // B6(DLB corner) → B0(ULB corner)
        // B7(DB edge) → B3(LB edge)
        // B8(DRB corner) → B6(DLB corner)
        //
        // So b[42] = 44 (new B6 = old B8) and b[36] = 42 (new B0 = old B6).
        //
        // But I already defined for the UBL→BLD corner cycle:
        // b[44] = 42 (new B8 = old B6).
        //
        // And the face rotation says:
        // b[44] = 38 (new B8 = old B2) because B2→B8.
        //
        // B2(38) is at the URB corner (touching U,R,B).
        // But B6(42) is at the DLB corner.
        //
        // In B CW, the face rotation says:
        // B0→B2, B2→B8, B8→B6, B6→B0
        // So: new[B2] = old[B0], new[B8] = old[B2], new[B6] = old[B8], new[B0] = old[B6]
        // In array terms: b[38] = 36, b[44] = 38, b[42] = 44, b[36] = 42
        //
        // So new B8 = old B2 (b[44] = 38) ← from face rotation
        // But the corner says new B8 = old B6 (b[44] = 42) ← from corner cycle
        //
        // The corner cycle is correct for the piece movement but it's WRONG for the facelet permutation,
        // because the corner cycle and the face rotation are describing the same thing.
        //
        // Wait no, I defined the corner cycles INCLUDING the B face stickers. So the corner cycle
        // says: B6 rotates to B8 position (new B8 = old B6). But this contradicts the face rotation
        // which says B2 rotates to B8 (new B8 = old B2).
        //
        // The FACE ROTATION is the correct description for what happens to the B face stickers.
        // The corner/edge cycles are an ADDITIONAL constraint. They must agree.
        //
        // For the UBL corner (U6, L0, B6):
        //   B6(42) is part of the B face. In B CW, B6 rotates to B0 position.
        //   After the face rotation: new[B0] = old[B6] (b[36] = 42).
        //   So the corner piece UBL ends up at the B0 corner position.
        //   But the B0 corner is at the intersection of U, L, B (which is UBL, the same position!).
        //   So UBL goes to UBL? That's wrong.
        //
        //   Hmm, no. The corner UBL IS at the intersection of U,L,B. The B0 sticker is on the B face
        //   at the UBL corner. After B CW face rotation: B0 → B2. So B0 sticker moves to B2 position.
        //   But that's just the face rotation. The CORNER piece UBL has stickers at U6, L0, B6.
        //   After B CW, where does UBL go?
        //
        //   In the B face CW rotation:
        //   The B6 sticker goes to B0 position. So the B sticker of the UBL corner (B6) goes to B0 position.
        //   But B0 position is where the old UBL was!
        //   So the UBL corner piece still has its B sticker at B0... wait no.
        //
        //   I'm confusing the piece (which has 3 stickers) with the stickers on the faces.
        //   The face rotation tells us where the STICKERS move, not the PIECES.
        //
        //   For facelet tracking: we track each of the 54 stickers. When B rotates:
        //   - The sticker at B6(42) moves to B0(36) position on the B face.
        //   - The sticker at B0(36) moves to B2(38) position.
        //   - The sticker at L0(9) moves to... somewhere on the B layer.
        //   - The sticker at U6(6) moves to... somewhere on the B layer.
        //
        //   The corner CYCLE describes the same movement but from the piece perspective.
        //   The piece at UBL (with stickers U6, L0, B6) after B CW has its stickers at positions:
        //   - U6 → ? (U6 moves somewhere, but I've been saying it moves to D6 position)
        //   - L0 → ? (L0 moves to L8 position)
        //   - B6 → B0 (by the B face rotation: new[B0] = old[B6])
        //
        //   So the corner piece moves to: somewhere on D (? = D6), somewhere on L (L8), and B0.
        //   That's the intersection of D, L, B = the DLB corner!
        //   DLB corner: D6(51), L8(17), B8(44).
        //
        //   But the B sticker of DLB is B8(44), not B0(36). So there's a problem!
        //
        //   The issue is that the corner piece has a B sticker at position B6. When B rotates CW,
        //   this sticker moves to B0 (by the B face rotation). But the new corner position (DLB)
        //   has its B sticker at B8(44), not B0(36).
        //
        //   This can't be right. Let me re-examine the face rotation.
        //
        //   B face CW:
        //   B0 → B2: the B sticker at index 36 goes to index 38.
        //   B2 → B8: index 38 goes to index 44.
        //   B8 → B6: index 44 goes to index 42.
        //   B6 → B0: index 42 goes to index 36.
        //
        //   So: new[38] = 36, new[44] = 38, new[42] = 44, new[36] = 42.
        //
        //   After B CW: old B6(42) is now at position B0(36). So the B sticker of the UBL corner
        //   (which was at B6) is now at B0(36).
        //
        //   And the corner where UBL was is now occupied by... who?
        //   The corner that comes to UBL is: ??? 
        //
        //   In B CW, the pieces that move are those attached to the B face.
        //   The UBL corner is attached to B. After B CW, it moves to a new position.
        //   Since B6 goes to B0, the UBL corner piece's B sticker is now at B0.
        //   B0 is... the same corner position (UBL) on the B face.
        //   
        //   Wait, B0 IS the B sticker of the UBL corner. So the UBL corner still has its
        //   B sticker at the same location (UBL corner of the B face = B0)?
        //   
        //   No, the UBL corner's B sticker was at B6. After B rotation, the old B6 is now
        //   at B0. But the UBL corner position on the B face IS B0. So the UBL corner's
        //   B sticker is still at the UBL position on the B face after rotation.
        //   
        //   But that means UBL → UBL? No!
        //
        //   The issue is: B6(42) → B0(36) doesn't mean the CORNER moves to UBL.
        //   The corner position UBL has B0(36) as its B sticker. After rotation, B0(36)
        //   receives the sticker from B6(42). So the new B0 now has what was at B6.
        //   But the old B0(36) went to B2(38).
        //
        //   So the corner piece that was at UBL (with stickers U6, L0, B6) now has
        //   its B sticker at B0 (since B6→B0). But B0 IS the UBL corner position!
        //
        //   Wait no. The corner UBL on the B face is B6, not B0!
        //
        //   B is the back face. The indices:
        //   36 37 38
        //   39 40 41
        //   42 43 44
        //
        //   B0(36) is top-left of B (touching U and L) = UBL corner
        //   B2(38) is top-right of B (touching U and R) = URB corner (wait, that's UBR)
        //   B6(42) is bottom-left of B (touching D and L) = DLB corner
        //   B8(44) is bottom-right of B (touching D and R) = DRB corner
        //
        //   So B0 = UBL on B face. B6 = DLB on B face.
        //
        //   The UBL corner (touching U,L,B) has its B sticker at B0(36).
        //   The DLB corner (touching D,L,B) has its B sticker at B6(42).
        //
        //   So the UBL corner has B sticker at B0(36), not B6(42)!
        //   B6(42) is the B sticker of the DLB corner!
        //
        //   This means my earlier assumption was wrong. The corner UBL has stickers:
        //   U6(6), L0(9), B0(36) ← B0, not B6!
        //
        //   Similarly, the DLB corner has stickers:
        //   D6(51), L8(17), B6(42) ← B6!
        //
        //   So in B CW:
        //   The corner UBL (with B sticker at B0=36) → its B sticker goes to B2(38) by the face rotation.
        //   But the corner piece has stickers at U6, L0, B0. After B CW:
        //   U6 → ? (somewhere on U, maybe D6?), L0 → ?, B0 → B2.
        //
        //   The face rotation tells us B0 → B2: new[B2] = old[B0]. So b[38] = 36. ✓
        //
        //   But the corner PIECE UBL after rotation goes to... where? The corner UBL was at
        //   intersection of U, L, B. After B CW from behind, it goes to the intersection of... 
        //   U, R, B? (the UBR corner)?
        //
        //   And UBR has B sticker at B2(38). Since the face rotation says old B0 goes to new B2,
        //   the B sticker of the UBL corner is now at B2 position, which IS the UBR corner.
        //
        //   This makes sense! UBL → UBR in B CW:
        //   new[B2] = old[B0] = 36 → UBL's B sticker goes to UBR ✓
        //   new[? on U] = old[U6] = 6 → UBL's U sticker goes to UBR's U position
        //   new[? on R] = old[L0] = 9 → UBL's L sticker goes to UBR's R position
        //
        //   UBR on U has sticker at U8(8), on R at R2(29), on B at B2(38).
        //   So: new[8] = old[6], new[29] = old[9], new[38] = old[36].
        //   But wait, from the face rotation: new[38] = 36 already (b[38]=36). ✓
        //
        //   Continuing:
        //   UBR → DRB (through another corner cycle):
        //   new[B8] = old[B2]... but from face rotation: new[44] = 38. ✓
        //   new[? on D] = old[U8], new[? on B] = old[R2]
        //   DRB on D: D8(53), on R: R8(35), on B: B8(44)
        //   So: new[53] = old[8], new[35] = old[29], new[44] = old[38].
        //   But from face rotation: new[44] = 38. ✓
        //
        //   DRB → DLB:
        //   new[D6] = old[D8], new[L8] = old[R8], new[B6] = old[B8]
        //   So: new[51] = old[53], new[17] = old[35], new[42] = old[44].
        //   From face rotation: new[42] = 44. ✓
        //
        //   DLB → UBL:
        //   new[U6] = old[D6], new[L0] = old[L8], new[B0] = old[B6]
        //   So: new[6] = old[51], new[9] = old[17], new[36] = old[42].
        //   From face rotation: new[36] = 42. ✓
        //
        //   NOW it all makes sense!
        //
        // The corner cycle (piece perspective) is consistent with the face rotation.
        // The key was that UBL has B sticker at B0(36), not B6(42).
        //
        // OK so corner cycles for B CW:
        // (UBL → UBR → DRB → DLB → UBL)
        // UBL: U6(6), L0(9), B0(36) → UBR: new[8]=6, new[29]=9, new[38]=36
        // UBR: U8(8), R2(29), B2(38) → DRB: new[53]=8, new[35]=29, new[44]=38
        // DRB: D8(53), R8(35), B8(44) → DLB: new[51]=53, new[17]=35, new[42]=44
        // DLB: D6(51), L8(17), B6(42) → UBL: new[6]=51, new[9]=17, new[36]=42
        //
        // Edge cycle for B CW: (UB → BR → BD → BL → UB)
        // UB: U7(7), B1(37) → BR: new[32]=7, new[41]=37
        // BR: R5(32), B5(41) → BD: new[52]=32, new[39]=41
        // BD: D7(52), B3(39) → BL: new[14]=52, new[43]=39
        // BL: L5(14), B7(43) → UB: new[7]=14, new[37]=43

        // So the full B CW:
        // B face rotation (36-44):
        // b[38]=36, b[44]=38, b[42]=44, b[36]=42  (corners: B0→B2, B2→B8, B8→B6, B6→B0)
        // b[41]=37, b[43]=41, b[39]=43, b[37]=39  (edges: B1→B5, B5→B7, B7→B3, B3→B1)
        // And then the adjacent stickers (already defined in the cycles above):
        // b[8]=6, b[29]=9   (UBL→UBR corner: U6→U8, L0→R2)
        // b[53]=8, b[35]=29 (UBR→DRB corner: U8→D8, R2→R8)
        // b[51]=53, b[17]=35 (DRB→DLB corner: D8→D6, R8→L8)
        // b[6]=51, b[9]=17   (DLB→UBL corner: D6→U6, L8→L0)
        // b[32]=7, b[41]=37  (UB→BR: U7→R5, B1→B5)
        // b[52]=32, b[39]=41 (BR→BD: R5→D7, B5→B3)
        // b[14]=52, b[43]=39 (BD→BL: D7→L5, B3→B7)
        // b[7]=14, b[37]=43  (BL→UB: L5→U7, B7→B1)

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

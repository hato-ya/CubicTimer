package com.hatopigeon.cubictimer.cube;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * End-to-end validation of {@link CubeState#getOllCase(int, String[])}.
 *
 * A self-contained, physically-consistent cube model (built from 3D cubie geometry) is used only
 * to *generate* test positions: for each OLL we apply the inverse of a known algorithm to a solved
 * cube, then optionally rotate the whole cube so the cross lands on each of the six faces. The
 * resulting facelets are fed into a real {@link CubeState}, and the production recognizer must
 * return the right OLL number every time — including mirror pairs such as OLL 3 / OLL 4, and
 * regardless of AUF or which face the cross is on.
 *
 * The same model proves the move engine is sound (sexy x6 = identity, etc.) before it is trusted
 * to build the OLL positions.
 */
public class CubeStateOllTest {

    private String[] references;

    @Before
    public void setUp() throws Exception {
        references = loadReferences();
        assertNotNull(references);
        assertEquals(57, references.length);
        Model.init();
    }

    @Test
    public void modelIsPhysicallyConsistent() {
        Model.reset(); for (int k = 0; k < 6; k++) Model.alg("R U R' U'");
        assertTrue("sexy x6 must be identity", Model.solved());

        Model.reset(); for (int k = 0; k < 2; k++) Model.alg("R U R' U' R' F R2 U' R' U' R U R' F'");
        assertTrue("T-perm x2 must be identity", Model.solved());

        Model.reset(); for (int k = 0; k < 6; k++) Model.alg("R U R' U R U2 R'");
        assertTrue("Sune x6 must be identity", Model.solved());

        Model.reset(); for (int k = 0; k < 4; k++) Model.mv('x', 1);
        assertTrue("x4 must be identity", Model.solved());
    }

    @Test
    public void recognizesAll57OnTop() {
        List<String> failures = new ArrayList<>();
        for (int oll = 1; oll <= 57; oll++) {
            Model.reset();
            Model.algInv(OLL_ALGS[oll - 1]);  // cross on D, OLL on U
            CubeState state = new CubeState();
            state.setFacelets(Model.facelets());
            int detected = state.getOllCase(5, references);   // cross face = D
            if (detected != oll) failures.add("OLL " + oll + " -> " + detected);
        }
        assertTrue("Misrecognized: " + failures, failures.isEmpty());
    }

    @Test
    public void recognizesAcrossEveryCrossFace() {
        // For each possible last-layer face, generate a *canonical-frame* OLL position (centers in
        // their home slots, exactly as a real cube reports them): rotate the face up, apply the
        // OLL, rotate back. The cross then sits on the opposite face, as production would detect.
        int[] opposite = {5, 3, 4, 1, 2, 0};
        List<String> failures = new ArrayList<>();
        for (int oll = 1; oll <= 57; oll++) {
            for (int lastFace = 0; lastFace <= 5; lastFace++) {
                Model.setupOllOnFace(lastFace, OLL_ALGS[oll - 1]);
                CubeState state = new CubeState();
                state.setFacelets(Model.facelets());
                int crossFace = opposite[lastFace];
                int detected = state.getOllCase(crossFace, references);
                if (detected != oll || !state.isF2LSolved(crossFace)) {
                    failures.add("OLL " + oll + " lastFace=" + lastFace + " cross=" + crossFace
                            + " f2l=" + state.isF2LSolved(crossFace) + " -> " + detected);
                }
            }
        }
        assertTrue("Misrecognized: " + failures, failures.isEmpty());
    }

    @Test
    public void f2lSolvedForEveryCrossFaceWhenSolved() {
        CubeState solved = new CubeState();
        for (int f = 0; f <= 5; f++) {
            assertTrue("cross " + f, solved.isCrossSolved(f));
            assertTrue("f2l " + f, solved.isF2LSolved(f));
        }
    }

    @Test
    public void autoDetectsAcrossEveryFaceWithoutKnowingCross() {
        // The trainer / live-menu path: the cross face is unknown, so getOllCase(references) tries
        // every face and must still return the right case wherever the OLL sits on the cube.
        List<String> failures = new ArrayList<>();
        for (int oll = 1; oll <= 57; oll++) {
            for (int lastFace = 0; lastFace <= 5; lastFace++) {
                Model.setupOllOnFace(lastFace, OLL_ALGS[oll - 1]);
                CubeState state = new CubeState();
                state.setFacelets(Model.facelets());
                int detected = state.getOllCase(references);
                if (detected != oll) {
                    failures.add("OLL " + oll + " lastFace=" + lastFace + " -> " + detected);
                }
            }
        }
        assertTrue("Misrecognized: " + failures, failures.isEmpty());
    }

    @Test
    public void autoDetectReturnsZeroOnSolvedCube() {
        CubeState solved = new CubeState();
        assertEquals(0, solved.getOllCase(references));
    }

    @Test
    public void returnsZeroWhenAlreadyOriented() {
        // Solved last layer with a scrambled-but-permuted U layer is still "oriented" (OLL skip).
        Model.reset();
        Model.alg("R U R' U' R' F R2 U' R' U' R U R' F'"); // a PLL: orientation stays solved
        CubeState state = new CubeState();
        state.setFacelets(Model.facelets());
        assertEquals(0, state.getOllCase(5, references));
    }

    @Test
    public void returnsMinusOneWhenF2lNotSolved() {
        Model.reset();
        Model.alg("R U R'"); // breaks F2L
        CubeState state = new CubeState();
        state.setFacelets(Model.facelets());
        assertEquals(-1, state.getOllCase(5, references));
    }

    // ─── reference loader ───

    private static String[] loadReferences() throws Exception {
        File xml = new File("src/main/res/values/reference_states.xml");
        if (!xml.exists()) xml = new File("app/src/main/res/values/reference_states.xml");
        String content = new String(Files.readAllBytes(xml.toPath()), StandardCharsets.UTF_8);
        int start = content.indexOf("name=\"alg_reference_OLL\"");
        int a = content.indexOf('>', start) + 1;
        int e = content.indexOf("</string-array>", a);
        Matcher m = Pattern.compile("<item>([^<]*)</item>").matcher(content.substring(a, e));
        List<String> it = new ArrayList<>();
        while (m.find()) it.add(m.group(1).trim());
        return it.toArray(new String[0]);
    }

    /** First algorithm variant for each OLL (1-57), copied from AlgUtils.getDefaultAlgs("OLL"). */
    private static final String[] OLL_ALGS = {
        "R U2 R2' F R F' U2 R' F R F'", "F R U R' U' F' f R U R' U' f'",
        "y' f R U R' U' f' U' F R U R' U' F'", "y' f R U R' U' f' U F R U R' U' F'",
        "r' U2 R U R' U r", "r U2 R' U' R U' r'", "r U R' U R U2 r'", "y2 r' U' R U' R' U2 r",
        "y R U R' U' R' F R2 U R' U' F'", "R U R' U R' F R F' R U2 R'", "r' R2 U R' U R U2 R' U M'",
        "F R U R' U' F' U F R U R' U' F'", "r U' r' U' r U r' F' U F", "R' F R U R' F' R F U' F'",
        "r' U' r R' U' R U r' U r", "r U r' R U R' U' r U' r'", "R U R' U R' F R F' U2 R' F R F'",
        "r U R' U R U2 r2 U' R U' R' U2 r", "M U R U R' U' M' R' F R F'", "M U R U R' U' M2 U R U' r'",
        "y R U2 R' U' R U R' U' R U' R'", "R U2 R2 U' R2 U' R2 U2 R", "R2 D R' U2 R D' R' U2 R'",
        "r U R' U' r' F R F'", "y F' r U R' U' r' F R", "y R U2 R' U' R U' R'", "R U R' U R U2 R'",
        "r U R' U' M U R U' R'", "M U R U R' U' R' F R F' M'", "M U' L' U' L U L F' L' F M'",
        "R' U' F U R U' R' F' R", "S R U R' U' R' F R f'", "R U R' U' R' F R F'",
        "y2 R U R' U' B' R' F R F' B", "R U2 R2' F R F' R U2 R'", "y2 L' U' L U' L' U L U L F' L' F",
        "F R U' R' U' R U R' F'", "R U R' U R U' R' U' R' F R F'", "y L F' L' U' L U F U' L'",
        "y R' F R U R' U' F' U R", "y2 R U R' U R U2' R' F R U R' U' F'", "R' U' R U' R' U2 R F R U R' U' F'",
        "f' L' U' L U f", "f R U R' U' f'", "F R U R' U' F'", "R' U' R' F R F' U R",
        "F' L' U' L U L' U' L U F", "F R U R' U' R U R' U' F'", "y2 r U' r2 U r2 U r2 U' r",
        "r' U r2 U' r2' U' r2 U r'", "f R U R' U' R U R' U' f'", "R U R' U R d' R U' R' F'",
        "r' U' R U' R' U R U' R' U2 r", "r U R' U R U' R' U R U2 r'", "R U2 R2 U' R U' R' U2 F R F'",
        "r U r' U R U' R' U R U' R' r U' r'", "R U R' U' M' U R U' r'",
    };

    // ─── self-contained, physically-correct cube model (test scaffolding only) ───

    static final class Model {
        static int[][] POS = new int[54][3];
        static int[][] NRM = new int[54][3];
        static Map<String, Integer> LOOKUP = new HashMap<>();
        static int[] PU, PD, PR, PL, PF, PB, PM, PE, PS, RX, RY, RZ;
        static int[] cur = new int[54];
        static boolean ready = false;

        static void init() {
            if (ready) return;
            fill(0,  new int[]{0,1,0},  (r,c) -> new int[]{c-1, 1, r-1});   // U
            fill(9,  new int[]{-1,0,0}, (r,c) -> new int[]{-1, 1-r, c-1});  // L
            fill(18, new int[]{0,0,1},  (r,c) -> new int[]{c-1, 1-r, 1});   // F
            fill(27, new int[]{1,0,0},  (r,c) -> new int[]{1, 1-r, 1-c});   // R
            fill(36, new int[]{0,0,-1}, (r,c) -> new int[]{1-c, 1-r, -1});  // B
            fill(45, new int[]{0,-1,0}, (r,c) -> new int[]{c-1, -1, 1-r});  // D
            for (int i = 0; i < 54; i++) LOOKUP.put(key(POS[i], NRM[i]), i);
            PU = move(1, 1, -1);  PD = move(1, -1, 1);
            PR = move(0, 1, -1);  PL = move(0, -1, 1);
            PF = move(2, 1, -1);  PB = move(2, -1, 1);
            PM = move(0, 0, 1);   PE = move(1, 0, 1);  PS = move(2, 0, -1);
            RX = move(0, 2, -1);  RY = move(1, 2, -1); RZ = move(2, 2, -1);
            ready = true;
        }

        interface PosFn { int[] at(int r, int c); }
        static void fill(int base, int[] n, PosFn fn) {
            for (int r = 0; r < 3; r++) for (int c = 0; c < 3; c++) {
                int idx = base + 3*r + c;
                POS[idx] = fn.at(r, c);
                NRM[idx] = n;
            }
        }
        static String key(int[] p, int[] n) { return p[0]+","+p[1]+","+p[2]+"|"+n[0]+","+n[1]+","+n[2]; }
        static int[] rot(int[] v, int axis, int s) {
            int x=v[0], y=v[1], z=v[2];
            switch (axis) {
                case 0: return new int[]{x, -s*z, s*y};
                case 1: return new int[]{s*z, y, -s*x};
                default: return new int[]{-s*y, s*x, z};
            }
        }
        static int[] move(int axis, int layer, int s) {
            int[] perm = new int[54];
            for (int i = 0; i < 54; i++) perm[i] = i;
            for (int j = 0; j < 54; j++) {
                if (layer != 2 && POS[j][axis] != layer) continue;
                int k = LOOKUP.get(key(rot(POS[j], axis, s), rot(NRM[j], axis, s)));
                perm[k] = j;
            }
            return perm;
        }
        static int[] inv(int[] p) { int[] q = new int[54]; for (int i = 0; i < 54; i++) q[p[i]] = i; return q; }
        static void reset() { for (int i = 0; i < 54; i++) cur[i] = i / 9; }
        static void apply(int[] perm) {
            int[] next = new int[54];
            for (int i = 0; i < 54; i++) next[i] = cur[perm[i]];
            cur = next;
        }
        static boolean solved() { for (int i = 0; i < 54; i++) if (cur[i] != i / 9) return false; return true; }
        static int[] facelets() { return cur.clone(); }

        static void mv(char b, int dir) {
            if (dir == 2) { mv(b, 1); mv(b, 1); return; }
            switch (b) {
                case 'U': apply(d(PU, dir)); return;  case 'D': apply(d(PD, dir)); return;
                case 'L': apply(d(PL, dir)); return;  case 'R': apply(d(PR, dir)); return;
                case 'F': apply(d(PF, dir)); return;  case 'B': apply(d(PB, dir)); return;
                case 'M': apply(d(PM, dir)); return;  case 'E': apply(d(PE, dir)); return;
                case 'S': apply(d(PS, dir)); return;
                case 'u': apply(d(PU, dir));  apply(d(PE, -dir)); return;
                case 'd': apply(d(PD, dir));  apply(d(PE, dir));  return;
                case 'r': apply(d(PR, dir));  apply(d(PM, -dir)); return;
                case 'l': apply(d(PL, dir));  apply(d(PM, dir));  return;
                case 'f': apply(d(PF, dir));  apply(d(PS, dir));  return;
                case 'b': apply(d(PB, dir));  apply(d(PS, -dir)); return;
                case 'x': apply(d(RX, dir)); return;
                case 'y': apply(d(RY, dir)); return;
                case 'z': apply(d(RZ, dir)); return;
                default: throw new RuntimeException("Unknown move " + b);
            }
        }
        static int[] d(int[] p, int dir) { return dir == 1 ? p : inv(p); }

        /**
         * Builds a canonical-frame position with the given OLL on {@code lastFace}: rotate that
         * face up, apply the inverse algorithm (placing the OLL on top with the cross on the
         * bottom), then rotate back so the centers return to their home slots.
         */
        static void setupOllOnFace(int lastFace, String a) {
            reset();
            switch (lastFace) {
                case 0: algInv(a); return;                              // U
                case 5: mv('x',2); algInv(a); mv('x',2); return;        // D
                case 2: mv('x',1);  algInv(a); mv('x',-1); return;      // F: x up (F->U), x' back
                case 4: mv('x',-1); algInv(a); mv('x',1); return;       // B: x' up (B->U), x back
                case 1: mv('z',1);  algInv(a); mv('z',-1); return;      // L: z up (L->U), z' back
                case 3: mv('z',-1); algInv(a); mv('z',1); return;       // R: z' up (R->U), z back
            }
        }

        static void alg(String a) { for (String t : a.trim().split("\\s+")) if (!t.isEmpty()) tok(t, false); }
        static void algInv(String a) {
            String[] ts = a.trim().split("\\s+");
            for (int i = ts.length - 1; i >= 0; i--) if (!ts[i].isEmpty()) tok(ts[i], true);
        }
        static void tok(String t, boolean invFlag) {
            char b = t.charAt(0);
            boolean dbl = t.indexOf('2') >= 0;
            boolean pr = t.indexOf('\'') >= 0 || t.indexOf('’') >= 0;
            int dir = dbl ? 2 : (pr ? -1 : 1);
            if (invFlag && dir != 2) dir = -dir;
            mv(b, dir);
        }
    }
}

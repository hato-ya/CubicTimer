// Baby FTO Solver is based on FTO Solver

// FTO Solver is ported from csTimer that licensed under GPL-3.0
// https://github.com/cs0x7f/cstimer

package com.hatopigeon.cubictimer.puzzle;

import android.util.Log;

import com.hatopigeon.cubictimer.puzzle.FTODrawer.BabyFTODrawer;

import org.worldcubeassociation.tnoodle.scrambles.InvalidScrambleException;
import org.worldcubeassociation.tnoodle.scrambles.Puzzle;
import org.worldcubeassociation.tnoodle.scrambles.PuzzleStateAndGenerator;
import org.worldcubeassociation.tnoodle.svglite.Color;
import org.worldcubeassociation.tnoodle.svglite.Dimension;
import org.worldcubeassociation.tnoodle.svglite.Svg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class BabyFtoPuzzle extends Puzzle {
    private static final int MIN_SCRAMBLE_LENGTH = 10;
    public BabyFtoPuzzle() {
        wcaMinScrambleDistance = 5;
    }

    @Override
    public String getShortName() {
        return "babyfto";
    }

    @Override
    public String getLongName() {
        return "BabyFto";
    }

    private static HashMap<String, Color> defaultColorScheme = new HashMap<String, Color>();
    static {
        defaultColorScheme.put("B",  new Color(0x304FFE));
        defaultColorScheme.put("BL", new Color(0xFF8B24));
        defaultColorScheme.put("BR", new Color(0x999999));
        defaultColorScheme.put("D",  new Color(0xFDD835));
        defaultColorScheme.put("F",  new Color(0x02D040));
        defaultColorScheme.put("L",  new Color(0x8A1AFF));
        defaultColorScheme.put("R",  new Color(0xEC0000));
        defaultColorScheme.put("U",  new Color(0xFFFFFF));
    }
    @Override
    public HashMap<String, Color> getDefaultColorScheme() {
        return new HashMap<String, Color>(defaultColorScheme);
    }

    @Override
    public Dimension getPreferredSize() {
        // dummy value
        return new Dimension(98, 98);
    }

    @Override
    public PuzzleState getSolvedState() {
        return new BabyFtoState();
    }

    @Override
    protected int getRandomMoveCount() {
        // dummy value
        return 30;
    }

    @Override
    public PuzzleStateAndGenerator generateRandomMoves(Random r) {
        Puzzle.PuzzleState pState;

        if (false) {
            // test scramble
            //String scramble = "D B' L B' D' B' D' B' F' L D B D' R F' D' L F D R F' BR' R' L B F' BR";
            //String scramble = "D U' R' BL L' D BR' L' U F' D L R' F' B' BR D' F' B' D L' D' BL' F U F' D' U' R F";
            //String scramble = "R L' U L F U F U'";
            String scramble = "BR R D R BR' F' R' F' BR' F'";

            try {
                pState = this.getSolvedState().applyAlgorithm(scramble);
            } catch (InvalidScrambleException var6) {
                InvalidScrambleException e = var6;
                throw new RuntimeException(e);
            }
        } else {
            pState = ((BabyFtoState) this.getSolvedState()).generateRandomState(r);
        }

        String scrambleStr = ((BabyFtoState) pState).solver();

        /*
        // BabyFto solver test
        for (int i = 0; i < 1000000; i++) {
            pState = ((BabyFtoState) this.getSolvedState()).generateRandomState(r);
            scrambleStr = ((BabyFtoState) pState).solver();
            Log.d("BabyFtoPuzzle", "Test loop : " + i);
        }
         */

        return new PuzzleStateAndGenerator(pState, scrambleStr);
    }

    public class BabyFtoState extends PuzzleState {
        private static class BabyFtoInnerState {
            int[] cp;
            int[] co;
            int[] uf;
            int[] rl;

            BabyFtoInnerState() {
                cp = new int[]{0, 1, 2, 3, 4, 5};
                co = new int[]{0, 0, 0, 0, 0, 0};
                uf = new int[]{0, 1, 2, 3};
                rl = new int[]{0, 1, 2, 3};
            }

            BabyFtoInnerState(int[] cp, int[] co, int[] uf, int[] rl) {
                this.cp = cp;
                this.co = co;
                this.uf = uf;
                this.rl = rl;
            }

            BabyFtoInnerState(BabyFtoInnerState src) {
                this.cp = src.cp.clone();
                this.co = src.co.clone();
                this.uf = src.uf.clone();
                this.rl = src.rl.clone();
            }

            public BabyFtoInnerState apply(BabyFtoInnerState op) {
                BabyFtoInnerState org = new BabyFtoInnerState(this);
                for (int i = 0; i < this.cp.length; i++) {
                    this.cp[i] = org.cp[op.cp[i]];
                    this.co[i] = org.co[op.cp[i]] ^ op.co[i];
                }
                for (int i = 0; i < this.uf.length; i++) {
                    this.uf[i] = org.uf[op.uf[i]];
                    this.rl[i] = org.rl[op.rl[i]];
                }
                return this;
            }

            public BabyFtoInnerState applyNew(BabyFtoInnerState op) {
                BabyFtoInnerState ret = new BabyFtoInnerState(this);
                return ret.apply(op);
            }

            public boolean equals(BabyFtoInnerState other) {
                return Arrays.equals(this.cp, other.cp) &&
                        Arrays.equals(this.co, other.co) &&
                        Arrays.equals(this.uf, other.uf) &&
                        Arrays.equals(this.rl, other.rl);
            }

            public long ccHash() {
                long hash = 0;
                for (int i = 0; i < cp.length; i++) {
                    hash += ((long) cp[i]) << (i * 4);
                }
                for (int i = 0; i < co.length; i++) {
                    hash += ((long) co[i]) << ((i + cp.length) * 4);
                }
                return hash;
            }

            public long ctHash() {
                long hash = 0;
                for (int i = 0; i < uf.length; i++) {
                    hash += ((long) uf[i]) << (i * 4);
                }
                for (int i = 0; i < rl.length; i++) {
                    hash += ((long) rl[i]) << (i * 4 + uf.length * 4);
                }
                return hash;
            }

            public int hashCode() {
                return Arrays.hashCode(cp) ^
                        Arrays.hashCode(co) ^
                        Arrays.hashCode(uf) ^
                        Arrays.hashCode(rl);
            }

            public String toString() {
                String str;
                str = "cp: " + Arrays.toString(cp) + "\nco: " + Arrays.toString(co)
                        + "\nuf: " + Arrays.toString(uf) + "\nrl: " + Arrays.toString(rl);
                return str;
            }

            static private int[] generateRandomPermutation(int num, Random r) {
                List<Integer> list = new ArrayList<>();
                for (int i = 0; i < num; i++) {
                    list.add(i);
                }
                Collections.shuffle(list, r);

                // guarantee even permutation
                int inversions = 0;
                for (int i = 0; i < list.size(); i++) {
                    for (int j = i + 1; j < list.size(); j++) {
                        if (list.get(i) > list.get(j)) {
                            inversions++;
                        }
                    }
                }
                if (inversions % 2 == 1) {
                    Collections.swap(list, 0, 1);
                }

                int[] ret = new int[list.size()];
                for (int i = 0; i < ret.length; i++) {
                    ret[i] = list.get(i);
                }

                return ret;
            }

            static private int[] generateRandomOrientation(Random r) {
                int num = 6;
                int[] ret = new int[num];
                int sum = 0;
                for (int i = 0; i < ret.length; i++) {
                    ret[i] = r.nextInt(2);
                    sum += ret[i];
                }

                if (sum % 2 == 1) {
                    int inv = r.nextInt(ret.length);
                    ret[inv] = ret[inv] ^ 1;
                }

                return ret;
            }

            static public BabyFtoInnerState generateRandomState(Random r) {
                int[] cp = generateRandomPermutation(6, r);
                int[] co = generateRandomOrientation(r);
                int[] uf = generateRandomPermutation(4, r);
                int[] rl = generateRandomPermutation(4, r);

                return new BabyFtoInnerState(cp, co, uf, rl);
            }
        }

        private BabyFtoInnerState bfto;

        public BabyFtoState() {
            bfto = new BabyFtoInnerState();
        }

        public BabyFtoState(BabyFtoState src) {
            this.bfto = new BabyFtoInnerState(src.bfto);
        }

        public BabyFtoState(BabyFtoInnerState src) {
            this.bfto = new BabyFtoInnerState(src);
        }

        public BabyFtoState generateRandomState(Random r) {
            return new BabyFtoState(BabyFtoInnerState.generateRandomState(r));
        }

        @Override
        public LinkedHashMap<String, ? extends PuzzleState> getSuccessorsByName() {
            LinkedHashMap<String, PuzzleState> successors = new LinkedHashMap<String, PuzzleState>();

            for (int i = 0; i < moveName.length; i++) {
                BabyFtoState state = new BabyFtoState(this.bfto.applyNew(moveOp[i]));
                successors.put(moveName[i], state);
            }

            return successors;
        }

        @Override
        public boolean equals(Object other) {
            return bfto.equals(((BabyFtoState) other).bfto);
        }

        @Override
        public int hashCode() {
            return bfto.hashCode();
        }

        @Override
        public PuzzleState getNormalized() {
            BabyFtoInnerState state2 = new BabyFtoInnerState();
            int rot;
            // find a rotation where the D-B-BR-BL corner is in the correct position
            for (rot = 0; rot < rotOp.length; rot++) {
                state2 = bfto.applyNew(rotOp[rot]);
                if (state2.cp[3] == 3 && state2.co[3] == 0) break;
            }
            return new BabyFtoState(state2);
        }

        @Override
        protected Svg drawScramble(HashMap<String, Color> colorScheme) {
            /*   State index of Baby FTO Drawer
                       U               B
                     L   R          BR   BL
                       F               D
                   00 01 02        20 21 22
                04    03    14  16    23    24
                05 06    12 13  18 19    25 26
                07    08    15  17    31    27
                   09 10 11        30 29 28
             */

            int[][] state_corner = {
                    {3, 12, 8, 6}, {2, 20, 16, 14}, {0, 4, 24, 22},
                    {25, 31, 19, 23}, {9, 28, 27, 7}, {17, 30, 11, 15}
            };
            int[] state_upfront = {1, 10, 18, 26};
            int[] state_rightleft = {29, 21, 5, 13};
            int[] state = new int[72];

            for (int i = 0; i < state_corner.length; i++) {
                for (int j = 0; j < state_corner[i].length; j++) {
                    int[][] corner = {
                            {0, 3, 2, 1}, {0, 5, 4, 3}, {0, 1, 6, 5},
                            {6, 7, 4, 5}, {2, 7, 6, 1}, {4, 7, 2, 3}
                    };
                    int[][] orientation = {{0, 1, 2, 3}, {2, 3, 0, 1}};
                    state[state_corner[i][j]] = corner[bfto.cp[i]][orientation[bfto.co[i]][j]];
                }
            }
            for (int i = 0; i < state_upfront.length; i++) {
                int[] upfront = {0, 2, 4, 6};
                state[state_upfront[i]] = upfront[bfto.uf[i]];
            }
            for (int i = 0; i < state_rightleft.length; i++) {
                int[] rightleft = {7, 5, 1, 3};
                state[state_rightleft[i]] = rightleft[bfto.rl[i]];
            }

            String[] keys = {"U", "L", "F", "R", "BR", "B", "BL", "D"};
            String[] colorHexCodes = new String[keys.length];

            for (int i = 0; i < keys.length; i++) {
                Color color = colorScheme.get(keys[i]);
                colorHexCodes[i] = String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
            }
            return new FtoSvg(getPreferredSize(), BabyFTODrawer.getSVG(state, colorHexCodes));
        }

        //
        // definition and initialization of move operations and rotate operations
        //
        private static final String[] moveName = new String[]{
                "U", "U'", "F", "F'", "BR", "BR'", "BL", "BL'",
                "D", "D'", "B", "B'", "R", "R'", "L", "L'"
        };

        private static final BabyFtoInnerState[] moveOp = new BabyFtoInnerState[16];
        private static final BabyFtoInnerState[] rotOp = new BabyFtoInnerState[12];

        private static void initOp() {
            // U
            moveOp[0] = new BabyFtoInnerState(
                    new int[]{1, 2, 0, 3, 4, 5},
                    new int[]{0, 0, 0, 0, 0, 0},
                    new int[]{0, 1, 2, 3},
                    new int[]{0, 2, 3, 1});
            // F
            moveOp[2] = new BabyFtoInnerState(
                    new int[]{4, 1, 2, 3, 5, 0},
                    new int[]{1, 0, 0, 0, 1, 0},
                    new int[]{0, 1, 2, 3},
                    new int[]{3, 1, 0, 2});
            // BR
            moveOp[4] = new BabyFtoInnerState(
                    new int[]{0, 5, 2, 1, 4, 3},
                    new int[]{0, 1, 0, 0, 0, 1},
                    new int[]{0, 1, 2, 3},
                    new int[]{1, 3, 2, 0});
            // BL
            moveOp[6] = new BabyFtoInnerState(
                    new int[]{0, 1, 3, 4, 2, 5},
                    new int[]{0, 0, 1, 1, 0, 0},
                    new int[]{0, 1, 2, 3},
                    new int[]{2, 0, 1, 3});
            // D
            moveOp[8] = new BabyFtoInnerState(
                    new int[]{0, 1, 2, 5, 3, 4},
                    new int[]{0, 0, 0, 0, 0, 0},
                    new int[]{0, 3, 1, 2},
                    new int[]{0, 1, 2, 3});
            // B
            moveOp[10] = new BabyFtoInnerState(
                    new int[]{0, 3, 1, 2, 4, 5},
                    new int[]{0, 1, 1, 0, 0, 0},
                    new int[]{2, 1, 3, 0},
                    new int[]{0, 1, 2, 3});
            // R
            moveOp[12] = new BabyFtoInnerState(
                    new int[]{5, 0, 2, 3, 4, 1},
                    new int[]{1, 1, 0, 0, 0, 0},
                    new int[]{1, 2, 0, 3},
                    new int[]{0, 1, 2, 3});
            // L
            moveOp[14] = new BabyFtoInnerState(
                    new int[]{2, 1, 4, 3, 0, 5},
                    new int[]{1, 0, 1, 0, 0, 0},
                    new int[]{3, 0, 2, 1},
                    new int[]{0, 1, 2, 3});

            // prime
            for (int i = 0; i < moveOp.length; i += 2) {
                moveOp[i + 1] = moveOp[i].applyNew(moveOp[i]);
            }

            // rotation
            rotOp[0] = new BabyFtoInnerState(
                    new int[]{0, 1, 2, 3, 4, 5},
                    new int[]{0, 0, 0, 0, 0, 0},
                    new int[]{0, 1, 2, 3},
                    new int[]{0, 1, 2, 3});
            rotOp[1] = new BabyFtoInnerState(
                    new int[]{1, 2, 0, 4, 5, 3},
                    new int[]{0, 0, 0, 0, 0, 0},
                    new int[]{0, 2, 3, 1},
                    new int[]{0, 2, 3, 1});
            rotOp[2] = new BabyFtoInnerState(
                    new int[]{2, 0, 1, 5, 3, 4},
                    new int[]{0, 0, 0, 0, 0, 0},
                    new int[]{0, 3, 1, 2},
                    new int[]{0, 3, 1, 2});
            rotOp[3] = new BabyFtoInnerState(
                    new int[]{0, 4, 5, 3, 1, 2},
                    new int[]{1, 0, 1, 1, 0, 1},
                    new int[]{1, 0, 3, 2},
                    new int[]{1, 0, 3, 2});
            rotOp[4] = new BabyFtoInnerState(
                    new int[]{4, 5, 0, 1, 2, 3},
                    new int[]{0, 1, 1, 0, 1, 1},
                    new int[]{1, 3, 2, 0},
                    new int[]{1, 3, 2, 0});
            rotOp[5] = new BabyFtoInnerState(
                    new int[]{5, 0, 4, 2, 3, 1},
                    new int[]{1, 1, 0, 1, 1, 0},
                    new int[]{1, 2, 0, 3},
                    new int[]{1, 2, 0, 3});
            rotOp[6] = new BabyFtoInnerState(
                    new int[]{3, 1, 5, 0, 4, 2},
                    new int[]{1, 1, 0, 1, 1, 0},
                    new int[]{2, 3, 0, 1},
                    new int[]{2, 3, 0, 1});
            rotOp[7] = new BabyFtoInnerState(
                    new int[]{1, 5, 3, 4, 2, 0},
                    new int[]{1, 0, 1, 1, 0, 1},
                    new int[]{2, 0, 1, 3},
                    new int[]{2, 0, 1, 3});
            rotOp[8] = new BabyFtoInnerState(
                    new int[]{5, 3, 1, 2, 0, 4},
                    new int[]{0, 1, 1, 0, 1, 1},
                    new int[]{2, 1, 3, 0},
                    new int[]{2, 1, 3, 0});
            rotOp[9] = new BabyFtoInnerState(
                    new int[]{3, 4, 2, 0, 1, 5},
                    new int[]{0, 1, 1, 0, 1, 1},
                    new int[]{3, 2, 1, 0},
                    new int[]{3, 2, 1, 0});
            rotOp[10] = new BabyFtoInnerState(
                    new int[]{4, 2, 3, 1, 5, 0},
                    new int[]{1, 1, 0, 1, 1, 0},
                    new int[]{3, 1, 0, 2},
                    new int[]{3, 1, 0, 2});
            rotOp[11] = new BabyFtoInnerState(
                    new int[]{2, 3, 4, 5, 0, 1},
                    new int[]{1, 0, 1, 1, 0, 1},
                    new int[]{3, 0, 2, 1},
                    new int[]{3, 0, 2, 1});
        }

        //
        // common functions for table initialization
        //
        private interface HashFunction {
            long get(BabyFtoInnerState state);
        }

        private interface MoveFunction {
            int get(int idx, int move);
        }

        private static Object[] createMoveTable(int[] moves, HashFunction hash) {
            List<BabyFtoInnerState> states = new ArrayList<>();
            states.add(new BabyFtoInnerState());

            HashMap<Long, Integer> idx = new HashMap<>();
            idx.put(hash.get(states.get(0)), 0);

            List<List<Integer>> moveTable = new ArrayList<>();
            for (int m = 0; m < moves.length; m++) {
                moveTable.add(new ArrayList<>());
            }

            for (int i = 0; i < states.size(); i++) {
                BabyFtoInnerState state = states.get(i);

                for (int m = 0; m < moves.length; m++) {
                    BabyFtoInnerState newState = state.applyNew(moveOp[moves[m]]);
                    long newHash = hash.get(newState);

                    if (!idx.containsKey(newHash)) {
                        idx.put(newHash, states.size());
                        states.add(newState);
                    }

                    moveTable.get(m).add(idx.get(newHash));
                }
            }

            return new Object[]{moveTable, idx};
        }

        private static byte[] createPruningTable(int size, int moves, int maxDepth, MoveFunction move) {
            byte[] prun = new byte[size];
            LinkedList<Integer> nextDepth = new LinkedList<>();
            int count = 0;

            Arrays.fill(prun, (byte) -1);

            prun[0] = 0;
            nextDepth.push(0);
            count++;

            for (int l = 0; l <= maxDepth; l++) {
                boolean done = true;
                LinkedList<Integer> currentDepth = nextDepth;
                nextDepth = new LinkedList<>();
                for (int p : currentDepth) {
                    for (int m = 0; m < moves; m++) {
                        int q = p;
                        for (int c = 0; c < 2; c++) {
                            q = move.get(q, m);
                            if (prun[q] != -1) continue;
                            prun[q] = (byte) (l + 1);
                            nextDepth.push(q);
                            count++;
                            done = false;
                        }
                    }
                }
//                Log.d("BabyFtoPuzzle", "    createPruningTable depth " + l + " count " + count);
                if (done) break;
            }

            return prun;
        }

        private static int[] createInvalidTable(int[] moves) {
            int[] invalidTable = new int[moves.length];
            for (int m1 = 0; m1 < moves.length; m1++) {
                invalidTable[m1] = 1 << m1;
                for (int m2 = 0; m2 < m1; m2++) {
                    BabyFtoInnerState m1m2 = moveOp[moves[m1]].applyNew(moveOp[moves[m2]]);
                    BabyFtoInnerState m2m1 = moveOp[moves[m2]].applyNew(moveOp[moves[m1]]);
                    if (m1m2.equals(m2m1)) {
                        invalidTable[m1] += 1 << m2;
                    }
                }
            }
            return invalidTable;
        }

        //
        // definition and initialization of phase1 tables
        //
        private static int[] p1Moves = new int[] {0, 12, 14, 8, 2};

        private static int p1ccSize;
        private static List<List<Integer>> p1ccMoveTable;
        private static HashMap<Long, Integer> p1ccIdx;
        private static int p1ctSize;
        private static List<List<Integer>> p1ctMoveTable;
        private static HashMap<Long, Integer> p1ctIdx;

        private static byte[] p1Pruning;

        private static int[] p1InvalidTable;

        static class p1ccHash implements HashFunction {
            @Override
            public long get(BabyFtoInnerState state) {
                return state.ccHash();
            }
        }

        static class p1ctHash implements HashFunction {
            @Override
            public long get(BabyFtoInnerState state) {
                return state.ctHash();
            }
        }

        static class p1MoveIdx implements MoveFunction {
            @Override
            public int get(int idx, int move) {
                int cc = idx % p1ccSize;
                int ct = idx / p1ccSize;
                return p1ctMoveTable.get(move).get(ct) * p1ccSize + p1ccMoveTable.get(move).get(cc);
            }
        }

        private static void initPhase1() {
            Log.d("BabyFtoPuzzle", "  p1ccHash");
            Object[] p1cc = createMoveTable(p1Moves, new p1ccHash());
            p1ccMoveTable = (List<List<Integer>>) p1cc[0];
            p1ccIdx = (HashMap<Long, Integer>) p1cc[1];
            p1ccSize = p1ccIdx.size();

            Log.d("BabyFtoPuzzle", "  p1ctHash");
            Object[] p1ct = createMoveTable(p1Moves, new p1ctHash());
            p1ctMoveTable = (List<List<Integer>>) p1ct[0];
            p1ctIdx = (HashMap<Long, Integer>) p1ct[1];
            p1ctSize = p1ctIdx.size();

            Log.d("BabyFtoPuzzle", "  p1Pruning " + p1ccSize * p1ctSize);
            p1Pruning = createPruningTable(p1ccSize * p1ctSize, p1Moves.length, 14, new p1MoveIdx());

            Log.d("BabyFtoPuzzle", "  p1InvalidTable");
            p1InvalidTable = createInvalidTable(p1Moves);
        }

        //
        // Static initializer
        //
        private static final CompletableFuture<Void> initialization;
        static {
            Log.d("BabyFtoPuzzle", "Static initializer");
            initOp();

            initialization = CompletableFuture.runAsync(() -> {
                long startTime = System.currentTimeMillis();

                Log.d("BabyFtoPuzzle", "Initialize phase1");
                initPhase1();

                Log.d("BabyFtoPuzzle", "Initialize complete : " + (System.currentTimeMillis() - startTime) + "ms");
            });
        }

        //
        // FTO Solver common definition
        //
        private interface IndexMoveFunction {
            int[] get(int[] idx, int move);
        }

        private interface PruningFunction {
            int get(int[] idx);
        }

        private class SolveParam {
            int[] idxs;
        }

        private class SolveResult {
            LinkedList<Integer> solve;
            int num;
        }

        //
        // FTO Solver phase1
        //
        private SolveParam[] phase1PreProcess() {
            SolveParam[] p1Params = new SolveParam[1];

            SolveParam solveParam = new SolveParam();
            solveParam.idxs = new int[2];
            solveParam.idxs[0] = p1ccIdx.get((new p1ccHash()).get(bfto));
            solveParam.idxs[1] = p1ctIdx.get((new p1ctHash()).get(bfto));
            p1Params[0] = solveParam;

            return p1Params;
        }

        static class p1IndexMove implements IndexMoveFunction {
            @Override
            public int[] get(int[] idxs, int move) {
                return new int[] {p1ccMoveTable.get(move).get(idxs[0]), p1ctMoveTable.get(move).get(idxs[1])};
            }
        }

        static class p1Pruning implements PruningFunction {
            @Override
            public int get(int[] idxs) {
                return p1Pruning[idxs[0] + idxs[1] * p1ccSize];
            }
        }

        private void phase1PostProcess(SolveParam[] p1Params, List<SolveResult> p1SolveResultList) {
            for (SolveResult solveResult : p1SolveResultList) {
                for (int i = 0; i < solveResult.solve.size(); i++) {
                    int move = p1Moves[solveResult.solve.get(i) % p1Moves.length] + solveResult.solve.get(i) / p1Moves.length;
                    solveResult.solve.set(i, move);
                }
            }
        }

        //
        // FTO Solver
        //
        private enum SearchStatus {
            COMPLETE_SEARCH,
            ABORT_SEARCH,
            ABORT_AXIS_SEARCH
        }

        private SearchStatus idaSearch(int[] idxs, int num, int moves, int maxSolves, int remainingDepth, LinkedList<Integer> solve, List<SolveResult> solveResultList, int[] invalidTable, IndexMoveFunction move, PruningFunction pruning) {
            int prun = pruning.get(idxs);

            if (prun > remainingDepth) {
                return prun > remainingDepth + 1 ? SearchStatus.ABORT_AXIS_SEARCH : SearchStatus.ABORT_SEARCH;
            } else if (remainingDepth == 0) {
                // This means prun <= remainingDepth && remeiningDepth == 0 -> prun == 0
                // Since prun == 0, it indicates that it was solved exactly at the depth
                SolveResult solveResult = new SolveResult();
                solveResult.solve = new LinkedList<>(solve);
                solveResult.num = num;
                solveResultList.add(solveResult);

                Log.d("BabyFtoPuzzle", solve.toString());

                if (solveResultList.size() >= maxSolves) {
                    return SearchStatus.COMPLETE_SEARCH;
                } else {
                    return SearchStatus.ABORT_SEARCH;
                }
            } else if (prun == 0 && remainingDepth == 1) {
                return SearchStatus.ABORT_SEARCH;
            }

            for (int m = 0; m < moves; m++) {
                if (!solve.isEmpty() && ((invalidTable[solve.getLast() % moves] >> m) & 1) == 1) continue;
                int[] idxs1 = idxs.clone();
                for (int c = 0; c < 2; c++) {
                    idxs1 = move.get(idxs1, m);
                    solve.add(m + c * moves);
                    SearchStatus status = idaSearch(idxs1, num, moves, maxSolves, remainingDepth - 1, solve, solveResultList, invalidTable, move, pruning);
                    if (status == SearchStatus.COMPLETE_SEARCH) {
                        return SearchStatus.COMPLETE_SEARCH;
                    }
                    solve.removeLast();
                    if (status == SearchStatus.ABORT_AXIS_SEARCH) {
                        break;
                    }
                }
            }

            return SearchStatus.ABORT_SEARCH;
        }

        private List<SolveResult> solveMulti(SolveParam[] params, int moves, int maxSolves, int maxDepth, int[] invalidTable, IndexMoveFunction move, PruningFunction pruning) {
            List<SolveResult> solveResultList = new ArrayList<>();
            for (int depth = MIN_SCRAMBLE_LENGTH; depth <= maxDepth; depth++) {
                for (int i = 0; i < params.length; i++) {
                    LinkedList<Integer> solve = new LinkedList<>();
                    Log.d("BabyFtoPuzzle", "idaSearch idx : " + i + " depth : " + depth);
                    if (idaSearch(params[i].idxs, i, moves, maxSolves, depth, solve, solveResultList, invalidTable, move, pruning) == SearchStatus.COMPLETE_SEARCH) {
                        return solveResultList;
                    }
                }
            }
            return solveResultList;
        }

        protected String solver() {
            Log.d("BabyFtoPuzzle", "Wait initialize");
            try {
                initialization.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("BabyFtoState initialization failed", e);
            }

            long startTime = System.currentTimeMillis();

            // phase1
            Log.d("BabyFtoPuzzle", "Solve phase1");
            SolveParam[] p1Params = phase1PreProcess();
            List<SolveResult> p1SolveResultList;
            p1SolveResultList = solveMulti(p1Params, p1Moves.length, 1,20, p1InvalidTable, new p1IndexMove(), new p1Pruning());
            phase1PostProcess(p1Params, p1SolveResultList);

            SolveResult solveResult = p1SolveResultList.get(0);

            // construct scramble string
            StringBuilder strScrambleBuilder = new StringBuilder();
            for (int i = solveResult.solve.size() - 1; i >= 0; i--) {
                strScrambleBuilder.append(moveName[solveResult.solve.get(i) ^ 1]).append(" ");
            }
            String strScramble = strScrambleBuilder.toString();
            Log.d("BabyFtoPuzzle", "Scramble result : " + strScramble);

/*
            StringBuilder strSolveBuilder = new StringBuilder();
            for (int i = 0; i < solveResult.solve.size(); i++) {
                strSolveBuilder.append(moveName[solveResult.solve.get(i)]).append(" ");
            }
            String strSolve = strSolveBuilder.toString();
            Log.d("BabyFtoPuzzle", "Solve result : " + strSolve);

            Log.d("BabyFtoPuzzle", "Original state");
            Log.d("BabyFtoPuzzle", bfto.toString());

            BabyFtoInnerState conf = new BabyFtoInnerState(bfto);
            for (int i = 0; i < solveResult.solve.size(); i++) {
                conf.apply(moveOp[solveResult.solve.get(i)]);
            }
            BabyFtoState test = new BabyFtoState(conf);

            BabyFtoInnerState norm = ((BabyFtoState)test.getNormalized()).bfto;
            Log.d("BabyFtoPuzzle", "Confirm");
            Log.d("BabyFtoPuzzle", norm.toString());

            if (!test.isSolved()) {
                throw new IllegalStateException("solve error\n" + bfto);
            }
 */

            Log.d("BabyFtoPuzzle", "Scramble complete : " + (System.currentTimeMillis() - startTime) + "ms");

            return strScramble;
        }

        /*
        @Override
        public String solveIn(int n) {
            String str = super.solveIn(10);
            if (str != null) {
                Log.d("BabyFtoPuzzle", "solveIn : " + str);
            }
            return super.solveIn(n);
        }
         */
    }
}

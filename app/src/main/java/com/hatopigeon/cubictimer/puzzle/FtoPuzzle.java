package com.hatopigeon.cubictimer.puzzle;

import com.hatopigeon.cubictimer.puzzle.FTODrawer.FTOMain;

import org.worldcubeassociation.tnoodle.scrambles.InvalidScrambleException;
import org.worldcubeassociation.tnoodle.scrambles.Puzzle;
import org.worldcubeassociation.tnoodle.scrambles.PuzzleStateAndGenerator;
import org.worldcubeassociation.tnoodle.svglite.Color;
import org.worldcubeassociation.tnoodle.svglite.Dimension;
import org.worldcubeassociation.tnoodle.svglite.Svg;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Random;

public class FtoPuzzle extends Puzzle {
    @Override
    public String getShortName() {
        return "fto";
    }

    @Override
    public String getLongName() {
        return "Fto";
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
        return new FtoState();
    }

    @Override
    protected int getRandomMoveCount() {
        return 30;
    }

    @Override
    public PuzzleStateAndGenerator generateRandomMoves(Random r) {
        StringBuilder scramble = new StringBuilder();

//        scramble.append("R' D B L' B' R B' BR D R D BR' R BR' D L' BR' R' L' BL' U R' F' D BL BR'");
//        scramble.append("D' B' L R D' B R' D BR R' D R L BR D R' L BR' D' BR B' F' U' L' F BL U'");
//        scramble.append("BL B' L' BR R BL' U' B F BR F' R B BL' B BR BL B BR' R B R' D' F B' U' BR L' D BL");

        String[] face = {"U", "L", "F", "R", "BR", "B", "BL", "D"};
        String[] dir = {" ", "' "};
        int[] opposite = {7, 4, 5, 6, 1, 2, 3, 0};

        int prevFace = -1;
        int prev2Face = -1;
        for (int i = 0; i < getRandomMoveCount(); i++) {
            int curFace;
            do {
                curFace = r.nextInt(8);
            } while (curFace == prevFace || (curFace == prev2Face && prevFace == opposite[curFace]));
            prev2Face = prevFace;
            prevFace = curFace;
            scramble.append(face[curFace]);

            int curDir = r.nextInt(2);
            scramble.append(dir[curDir]);
        }

        String scrambleStr = scramble.toString().trim();
//        Log.d("FtoPuzzle", "scramble : " + scrambleStr);

        Puzzle.PuzzleState pState;
        try {
            pState = this.getSolvedState().applyAlgorithm(scrambleStr);
        } catch (InvalidScrambleException var6) {
            InvalidScrambleException e = var6;
            throw new RuntimeException(e);
        }

        return new PuzzleStateAndGenerator(pState, scrambleStr);
    }

    public class FtoState extends PuzzleState {
        private int[] cp;
        private int[] co;
        private int[] ep;
        private int[] uf;
        private int[] rl;

        public FtoState() {
            cp = new int[] {0,1,2,3,4,5};
            co = new int[] {0,0,0,0,0,0};
            ep = new int[] {0,1,2,3,4,5,6,7,8,9,10,11};
            uf = new int[] {0,1,2,3,4,5,6,7,8,9,10,11};
            rl = new int[] {0,1,2,3,4,5,6,7,8,9,10,11};
        }

        public FtoState(FtoState src) {
            this.cp = src.cp.clone();
            this.co = src.co.clone();
            this.ep = src.ep.clone();
            this.uf = src.uf.clone();
            this.rl = src.rl.clone();
        }

        private static class TurnData {
            int[] cp;
            int[] co;
            int[] ep;
            int[] uf;
            int[] rl;

            TurnData(int[] cp, int[] co, int[] ep, int[] uf, int[] rl) {
                this.cp = cp;
                this.co = co;
                this.ep = ep;
                this.uf = uf;
                this.rl = rl;
            }
        }

        private static final HashMap<String, TurnData> turnMap = new HashMap<String, TurnData>();

        static {
            turnMap.put("U", new TurnData(
                        new int[] {1,2,0,3,4,5},
                        new int[] {0,0,0,0,0,0},
                        new int[] {2,0,1,3,4,5,6,7,8,9,10,11},
                        new int[] {1,2,0,3,4,5,6,7,8,9,10,11},
                        new int[] {0,1,2,3,6,7,11,9,8,5,10,4}));
            turnMap.put("L", new TurnData(
                        new int[] {2,1,4,3,0,5},
                        new int[] {1,0,1,0,0,0},
                        new int[] {0,8,2,3,4,5,6,1,7,9,10,11},
                        new int[] {11,1,10,2,0,5,6,7,8,9,3,4},
                        new int[] {0,1,2,3,4,5,7,8,6,9,10,11}));
            turnMap.put("F", new TurnData(
                        new int[] {4,1,2,3,5,0},
                        new int[] {1,0,0,0,1,0},
                        new int[] {0,1,2,3,4,6,7,5,8,9,10,11},
                        new int[] {0,1,2,4,5,3,6,7,8,9,10,11},
                        new int[] {0,9,10,3,4,5,2,7,1,8,6,11}));
            turnMap.put("R", new TurnData(
                        new int[] {5,0,2,3,4,1},
                        new int[] {1,1,0,0,0,0},
                        new int[] {6,1,2,3,4,5,11,7,8,9,10,0},
                        new int[] {5,3,2,8,4,7,6,0,1,9,10,11},
                        new int[] {0,1,2,3,4,5,6,7,8,10,11,9}));
            turnMap.put("BR", new TurnData(
                        new int[] {0,5,2,1,4,3},
                        new int[] {0,1,0,0,0,1},
                        new int[] {0,1,2,3,10,5,6,7,8,9,11,4},
                        new int[] {0,1,2,3,4,5,7,8,6,9,10,11},
                        new int[] {5,3,2,11,4,10,6,7,8,9,0,1}));
            turnMap.put("B", new TurnData(
                        new int[] {0,3,1,2,4,5},
                        new int[] {0,1,1,0,0,0},
                        new int[] {0,1,10,3,4,5,6,7,8,2,9,11},
                        new int[] {0,6,7,3,4,5,11,9,8,2,10,1},
                        new int[] {0,1,2,4,5,3,6,7,8,9,10,11}));
            turnMap.put("BL", new TurnData(
                        new int[] {0,1,3,4,2,5},
                        new int[] {0,0,1,1,0,0},
                        new int[] {0,1,2,8,4,5,6,7,9,3,10,11},
                        new int[] {0,1,2,3,4,5,6,7,8,10,11,9},
                        new int[] {8,1,7,2,0,5,6,3,4,9,10,11}));
            turnMap.put("D", new TurnData(
                        new int[] {0,1,2,5,3,4},
                        new int[] {0,0,0,0,0,0},
                        new int[] {0,1,2,4,5,3,6,7,8,9,10,11},
                        new int[] {0,1,2,3,9,10,5,7,4,8,6,11},
                        new int[] {1,2,0,3,4,5,6,7,8,9,10,11}));
        }

        protected void turn(String face) {
            FtoState org = new FtoState(this);
            for (int i = 0; i < this.cp.length; i++) {
                this.cp[i] = org.cp[turnMap.get(face).cp[i]];
                this.co[i] = org.co[turnMap.get(face).cp[i]] ^ turnMap.get(face).co[i];
            }
            for (int i = 0; i < this.ep.length; i++) {
                this.ep[i] = org.ep[turnMap.get(face).ep[i]];
                this.uf[i] = org.uf[turnMap.get(face).uf[i]];
                this.rl[i] = org.rl[turnMap.get(face).rl[i]];
            }
        }

        @Override
        public LinkedHashMap<String, ? extends PuzzleState> getSuccessorsByName() {
            LinkedHashMap<String, PuzzleState> successors = new LinkedHashMap<String, PuzzleState>();
            String[] face = {"U", "L", "F", "R", "BR", "B", "BL", "D"};

            for (String f : face) {
                FtoState state = new FtoState(this);
                state.turn(f);
                successors.put(f, state);
                FtoState statePrime = new FtoState(state);
                statePrime.turn(f);
                successors.put(f+"'", statePrime);
            }

            return successors;
        }

        @Override
        public boolean equals(Object other) {
            return false;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        protected Svg drawScramble(HashMap<String, Color> colorScheme) {
            /*   State index of FTODrawer
                         U                  B
                       L   R             BR   BL
                         F                  D
                   0  1  2  3  4      45 46 47 48 49
                 9    5  6  7   31  36   50 51 52   54
                10 11    8   29 30  38 39   53   55 56
                14 12 13  27 28 34  37 43 44  58 59 57
                15 16   18   32 33  41 42   71   60 61
                17   19 20 21   35  40   70 69 68   62
                  22 23 24 25 26      67 66 65 64 63
             */

            int[][] state_corner = {
                    {8, 27,18,13}, {4, 45,36,31}, {0, 9, 54,49},
                    {58,71,44,53}, {22,63,62,17}, {40,67,26,35}
            };
            int[][] state_edge = {
                    {7, 29}, {5, 11}, {2, 47}, {60,68}, {42,70}, {24,65},
                    {21,32}, {19,16}, {57,14}, {55,52}, {39,50}, {37,34}
            };
            int[] state_upfront = {6,3,1, 20,23,25, 43,38,41, 59,61,56};
            int[] state_rightleft = {69,66,64, 51,48,46, 12,10,15, 28,33,30};

            int[] state = new int[72];

            for (int i = 0; i < state_corner.length; i++) {
                for (int j = 0; j < state_corner[i].length; j++) {
                    int[][] corner = {
                            {0,3,2,1}, {0,5,4,3}, {0,1,6,5},
                            {6,7,4,5}, {2,7,6,1}, {4,7,2,3}
                    };
                    int[][] orientation = {{0,1,2,3}, {2,3,0,1}};
                    state[state_corner[i][j]] = corner[cp[i]][orientation[co[i]][j]];
                }
            }
            for (int i = 0; i < state_edge.length; i++) {
                int[][] edge = {
                        {0,3}, {0,1}, {0,5}, {6,7}, {4,7}, {2,7},
                        {2,3}, {2,1}, {6,1}, {6,5}, {4,5}, {4,3}
                };
                for (int j = 0; j < state_edge[i].length; j++) {
                    state[state_edge[i][j]] = edge[ep[i]][j];
                }
            }
            for (int i = 0; i < state_upfront.length; i++) {
                int[] upfront = {0,0,0, 2,2,2, 4,4,4, 6,6,6};
                state[state_upfront[i]] = upfront[uf[i]];
            }
            for (int i = 0; i < state_rightleft.length; i++) {
                int[] rightleft = {7,7,7, 5,5,5, 1,1,1, 3,3,3};
                state[state_rightleft[i]] = rightleft[rl[i]];
            }

            String[] keys = {"U", "L", "F", "R", "BR", "B", "BL", "D"};
            String[] colorHexCodes = new String[keys.length];

            for (int i = 0; i < keys.length; i++) {
                Color color = colorScheme.get(keys[i]);
                colorHexCodes[i] = String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
            }
            return new FtoSvg(getPreferredSize(), FTOMain.getSVG(state, colorHexCodes));
        }

        @Override
        public String solveIn(int n) {
            return null;
        }
    }

}

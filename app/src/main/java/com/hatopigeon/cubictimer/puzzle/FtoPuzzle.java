package com.hatopigeon.cubictimer.puzzle;

import android.util.Log;

import com.hatopigeon.cubictimer.puzzle.FTODrawer.FTO;
import com.hatopigeon.cubictimer.puzzle.FTODrawer.FTOMain;

import org.worldcubeassociation.tnoodle.scrambles.InvalidMoveException;
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
        int[] opposit = {7, 4, 5, 6, 1, 2, 3, 0};

        int prevFace = -1;
        int prev2Face = -1;
        for (int i = 0; i < getRandomMoveCount(); i++) {
            int curFace;
            do {
                curFace = r.nextInt(8);
            } while (curFace == prevFace || (curFace == prev2Face && prevFace == opposit[curFace]));
            prev2Face = prevFace;
            prevFace = curFace;
            scramble.append(face[curFace]);

            int curDir = r.nextInt(2);
            scramble.append(dir[curDir]);
        }

        String scrambleStr = scramble.toString().trim();
//        Log.d("FtoPuzzle", "scramble : " + scrambleStr);
        PuzzleState state = new FtoState();
        return new PuzzleStateAndGenerator(state, scrambleStr);
    }

    public class FtoState extends PuzzleState {
        private FTO fto;

        FtoState() {
            fto = new FTO();
        }

        @Override
        public LinkedHashMap<String, ? extends PuzzleState> getSuccessorsByName() {
            return null;
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
            String[] keys = {"U", "L", "F", "R", "BR", "B", "BL", "D"};
            String[] colorHexCodes = new String[keys.length];

            for (int i = 0; i < keys.length; i++) {
                Color color = colorScheme.get(keys[i]);
                colorHexCodes[i] = String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
            }
            return new FtoSvg(getPreferredSize(), FTOMain.getSVG(fto.getState(), colorHexCodes));
        }

        @Override
        public String solveIn(int n) {
            return null;
        }

        @Override
        public PuzzleState applyAlgorithm(String algorithm) throws InvalidScrambleException {
            FtoState ftoPuzzleState = new FtoState();
            ftoPuzzleState.fto.doMoves(algorithm);
            return ftoPuzzleState;
        }
    }

}

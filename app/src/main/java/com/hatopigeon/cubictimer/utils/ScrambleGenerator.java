package com.hatopigeon.cubictimer.utils;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;

import com.hatopigeon.cubictimer.puzzle.NbyNCubePuzzle;
import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;

import org.worldcubeassociation.tnoodle.scrambles.InvalidScrambleException;
import org.worldcubeassociation.tnoodle.scrambles.Puzzle;

import org.worldcubeassociation.tnoodle.puzzle.ClockPuzzle;
import org.worldcubeassociation.tnoodle.puzzle.FourByFourCubePuzzle;
import org.worldcubeassociation.tnoodle.puzzle.MegaminxPuzzle;
import org.worldcubeassociation.tnoodle.puzzle.NoInspectionFiveByFiveCubePuzzle;
import org.worldcubeassociation.tnoodle.puzzle.NoInspectionFourByFourCubePuzzle;
import org.worldcubeassociation.tnoodle.puzzle.NoInspectionThreeByThreeCubePuzzle;
import org.worldcubeassociation.tnoodle.puzzle.PyraminxPuzzle;
import org.worldcubeassociation.tnoodle.puzzle.SkewbPuzzle;
import org.worldcubeassociation.tnoodle.puzzle.SquareOnePuzzle;
import org.worldcubeassociation.tnoodle.puzzle.ThreeByThreeCubeFewestMovesPuzzle;
import org.worldcubeassociation.tnoodle.puzzle.ThreeByThreeCubePuzzle;
import org.worldcubeassociation.tnoodle.puzzle.TwoByTwoCubePuzzle;

/**
 * Util for generating and drawing scrambles
 */
public class ScrambleGenerator {
    private Puzzle puzzle;
    private String puzzleType;

    public ScrambleGenerator(String type) {
        puzzleType = type;
        switch (type) {
            case PuzzleUtils.TYPE_222:
                puzzle = new TwoByTwoCubePuzzle();
                break;
            case PuzzleUtils.TYPE_333:
                puzzle = new ThreeByThreeCubePuzzle();
                break;
            case PuzzleUtils.TYPE_444:
                puzzle = new FourByFourCubePuzzle();
                break;
            case PuzzleUtils.TYPE_555:
                puzzle = new NbyNCubePuzzle(5);
                break;
            case PuzzleUtils.TYPE_666:
                puzzle = new NbyNCubePuzzle(6);
                break;
            case PuzzleUtils.TYPE_777:
                puzzle = new NbyNCubePuzzle(7);
                break;
            case PuzzleUtils.TYPE_MEGA:
                puzzle = new MegaminxPuzzle();
                break;
            case PuzzleUtils.TYPE_PYRA:
                puzzle = new PyraminxPuzzle();
                break;
            case PuzzleUtils.TYPE_SKEWB:
                puzzle = new SkewbPuzzle();
                break;
            case PuzzleUtils.TYPE_CLOCK:
                puzzle = new ClockPuzzle();
                break;
            case PuzzleUtils.TYPE_SQUARE1:
                puzzle = new SquareOnePuzzle();
                break;
            case PuzzleUtils.TYPE_333BLD:
                puzzle = new NoInspectionThreeByThreeCubePuzzle();
                break;
            case PuzzleUtils.TYPE_444BLD:
                puzzle = new NoInspectionFourByFourCubePuzzle();
                break;
            case PuzzleUtils.TYPE_555BLD:
                puzzle = new NoInspectionFiveByFiveCubePuzzle();
                break;
            case PuzzleUtils.TYPE_333FMC:
                puzzle = new ThreeByThreeCubeFewestMovesPuzzle();
                break;
            case PuzzleUtils.TYPE_OTHER:
                puzzle = null;
                break;
            default:
                puzzle = new ThreeByThreeCubePuzzle();
                break;
        }
    }

    public Puzzle getPuzzle() {
        return puzzle;
    }

    public void setPuzzle(Puzzle puzzle) {
        this.puzzle = puzzle;
    }

    /**
     * Returns a scramble drawable showing the puzzled scrambled
     * Uses Tnoodle lib
     *
     * @return
     */

    public Drawable generateImageFromScramble(SharedPreferences sp, String scramble) {
        // Getting the color scheme
        String top;
        String left;
        String front;
        String right;
        String back;
        String down;
        // Due to a bug in the TNoodle library, the default Skewb scheme has the faces in a different order,
        // so we must account for this by creating a special case with some default colors flipped
        if (! puzzleType.equals(PuzzleUtils.TYPE_SKEWB)) {
            top = sp.getString("cubeTop", "FFFFFF");
            left = sp.getString("cubeLeft", "FF8B24");
            front = sp.getString("cubeFront", "02D040");
            right = sp.getString("cubeRight", "EC0000");
            back = sp.getString("cubeBack", "304FFE");
            down = sp.getString("cubeDown", "FDD835");
        } else {
            top = sp.getString("cubeTop", "FFFFFF");
            left = sp.getString("cubeFront", "02D040");
            front = sp.getString("cubeRight", "EC0000");
            right = sp.getString("cubeBack", "304FFE");
            back = sp.getString("cubeLeft", "EF6C00");
            down = sp.getString("cubeDown", "FDD835");
        }

        String cubeImg = null;
        Drawable pic = null;

        if (!puzzleType.equals(PuzzleUtils.TYPE_OTHER)) {
            try {
                cubeImg = puzzle.drawScramble(scramble, puzzle.parseColorScheme(back + "," + down + "," + front + "," + left + "," + right + "," + top)).toString();
            } catch (InvalidScrambleException e) {
                e.printStackTrace();
            }
        } else {
            cubeImg = "";
        }

        if (cubeImg != null) {
            try {
                pic = new PictureDrawable(SVG.getFromString(cubeImg).renderToPicture());
            } catch (SVGParseException e) {
                e.printStackTrace();
            }
        }

        return pic;
    }
}

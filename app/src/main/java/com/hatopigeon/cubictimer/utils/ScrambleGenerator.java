package com.hatopigeon.cubictimer.utils;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.util.Log;

import com.hatopigeon.cubicify.R;
import com.hatopigeon.cubictimer.fragment.dialog.SchemeSelectDialogMain;
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
import org.worldcubeassociation.tnoodle.svglite.Color;

import java.util.HashMap;

/**
 * Util for generating and drawing scrambles
 */
public class ScrambleGenerator {
    private Puzzle puzzle;
    private String puzzleType;

    // define the color scheme for the clock puzzle to closer to the current tnoodle-lib color
    private static HashMap<String, Color> clockColorScheme = new HashMap<String, Color>();
    static {
        Color bright = new Color(0xccddee);
        Color dark = new Color(0x113366);

        clockColorScheme.put("Front", dark);
        clockColorScheme.put("Back", bright);
        clockColorScheme.put("FrontClock", bright);
        clockColorScheme.put("BackClock", dark);
        clockColorScheme.put("Hand", new Color(0xf5fffa));
        clockColorScheme.put("HandBorder", new Color(0x708090));
        clockColorScheme.put("PinUp", new Color(0x88aacc));
        clockColorScheme.put("PinDown", new Color(0x446699));
    }

    public ScrambleGenerator(String type) {
        puzzleType = type;
        try {
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
                case PuzzleUtils.TYPE_333OH:
                    puzzle = new ThreeByThreeCubePuzzle();
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
                case PuzzleUtils.TYPE_333MBLD:
                    puzzle = new NoInspectionThreeByThreeCubePuzzle();
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
        } catch (Exception e) {
            puzzle = null;
        }
    }

    public Puzzle getPuzzle() {
        return puzzle;
    }

    public void setPuzzle(Puzzle puzzle) {
        this.puzzle = puzzle;
    }

    private String getColorHex(SharedPreferences sp, int id, String colorSchemeName) {
        return sp.getString(PuzzleUtils.colorInfo.get(id).face + colorSchemeName, PuzzleUtils.colorInfo.get(id).defaultColor);
    }

    /**
     * Returns a scramble drawable showing the puzzled scrambled
     * Uses Tnoodle lib
     *
     * @return
     */
    public Drawable generateImageFromScramble(SharedPreferences sp, String scramble) {
        HashMap<String, Color> colorScheme = new HashMap<String, Color>();
        String colorSchemeName = PuzzleUtils.getColorSchemeName(puzzleType);

        String cubeImg = null;
        Drawable pic = null;

        if (!(puzzleType.equals(PuzzleUtils.TYPE_OTHER) || puzzleType.equals(PuzzleUtils.TYPE_333MBLD))) {
            // To draw old clock scramble, remove pin moves at the end of scramble
//            if (puzzleType.equals(PuzzleUtils.TYPE_CLOCK)) {
//                scramble = scramble.replaceAll("(UR|UL|DR|DL| )+$", "");
//            }
            switch (PuzzleUtils.getColorSchemeType(puzzleType)) {
                default:
                case PuzzleUtils.TYPE_333:
                    // Getting the color scheme
                    String top   = getColorHex(sp, R.id.top,   colorSchemeName);
                    String left  = getColorHex(sp, R.id.left,  colorSchemeName);
                    String front = getColorHex(sp, R.id.front, colorSchemeName);
                    String right = getColorHex(sp, R.id.right, colorSchemeName);
                    String back  = getColorHex(sp, R.id.back,  colorSchemeName);
                    String down  = getColorHex(sp, R.id.down,  colorSchemeName);
                    // Due to a bug in the TNoodle library, the default Skewb scheme has the faces in a different order,
                    // so we must account for this by creating a special case with some default colors flipped
                    if (! puzzleType.equals(PuzzleUtils.TYPE_SKEWB)) {
                        colorScheme = puzzle.parseColorScheme(back + "," + down + "," + front + "," + left + "," + right + "," + top);
                    } else {
                        colorScheme = puzzle.parseColorScheme(left + "," + down + "," + right + "," + front + "," + back + "," + top);
                    }
                    break;
                case PuzzleUtils.TYPE_MEGA:
                    {
                        // Getting the color scheme
                        String BL  = getColorHex(sp, R.id.megaBL, colorSchemeName);
                        String BR  = getColorHex(sp, R.id.megaBR, colorSchemeName);
                        String L   = getColorHex(sp, R.id.megaL, colorSchemeName);
                        String U   = getColorHex(sp, R.id.megaU, colorSchemeName);
                        String RT  = getColorHex(sp, R.id.megaR, colorSchemeName);    // Avoid confusion with R by abbreviating to two letters
                        String F   = getColorHex(sp, R.id.megaF, colorSchemeName);
                        String B   = getColorHex(sp, R.id.megaB, colorSchemeName);
                        String DBR = getColorHex(sp, R.id.megaDBR, colorSchemeName);
                        String D   = getColorHex(sp, R.id.megaD, colorSchemeName);
                        String DBL = getColorHex(sp, R.id.megaDBL, colorSchemeName);
                        String DR  = getColorHex(sp, R.id.megaDR, colorSchemeName);
                        String DL  = getColorHex(sp, R.id.megaDL, colorSchemeName);
                        colorScheme = puzzle.parseColorScheme(B + "," + BL + "," + BR + "," + D + "," + DBL + "," + DBR + "," + DL + "," + DR + "," + F + "," + L + "," + RT + "," + U);
                    }
                    break;
                case PuzzleUtils.TYPE_PYRA:
                    {
                        // Getting the color scheme
                        String L  = getColorHex(sp, R.id.pyraL, colorSchemeName);
                        String F  = getColorHex(sp, R.id.pyraF, colorSchemeName);
                        String RT = getColorHex(sp, R.id.pyraR, colorSchemeName);    // Avoid confusion with R by abbreviating to two letters
                        String D  = getColorHex(sp, R.id.pyraD, colorSchemeName);
                        Log.d("ScrambleGenerator", "pyra " + L + " " + F + " " + RT + " " + D);
                        colorScheme = puzzle.parseColorScheme(D + "," + F + "," + L + "," + RT);
                    }
                    break;
                case PuzzleUtils.TYPE_CLOCK:
                    break;
            }

            try {
                if (puzzleType.equals(PuzzleUtils.TYPE_CLOCK)) {
                    cubeImg = puzzle.drawScramble(scramble, clockColorScheme).toString();
                } else {
                    cubeImg = puzzle.drawScramble(scramble, colorScheme).toString();
                }
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

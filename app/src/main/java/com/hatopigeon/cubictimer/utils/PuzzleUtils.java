package com.hatopigeon.cubictimer.utils;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.StringRes;

import com.hatopigeon.cubicify.R;
import com.hatopigeon.cubictimer.items.Solve;
import com.hatopigeon.cubictimer.stats.AverageCalculator;
import com.hatopigeon.cubictimer.stats.Statistics;

import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.util.HashMap;
import java.util.Map;

import static com.hatopigeon.cubictimer.stats.AverageCalculator.tr;

/**
 * Created by Ari on 17/01/2016.
 */
public class PuzzleUtils {
    public static final String TYPE_222     = "222";
    public static final String TYPE_333     = "333";
    public static final String TYPE_444     = "444";
    public static final String TYPE_555     = "555";
    public static final String TYPE_666     = "666";
    public static final String TYPE_777     = "777";
    public static final String TYPE_MEGA    = "mega";
    public static final String TYPE_PYRA    = "pyra";
    public static final String TYPE_SKEWB   = "skewb";
    public static final String TYPE_CLOCK   = "clock";
    public static final String TYPE_SQUARE1 = "sq1";
    public static final String TYPE_333BLD  = "333bld";
    public static final String TYPE_444BLD  = "444bld";
    public static final String TYPE_555BLD  = "555bld";
    public static final String TYPE_333FMC  = "333fmc";
    public static final String TYPE_OTHER   = "other";

    public static final int NO_PENALTY       = 0;
    public static final int PENALTY_PLUSTWO  = 1;
    public static final int PENALTY_DNF      = 2;
    // The following penalty is a workaround to implement subtypes in the timer
    // Every time query should ignore every time that has a penalty of 10
    public static final int PENALTY_HIDETIME = 10;

    public static final int TIME_DNF = - 1;


    // -- Format constants for timeToString --
    public static final int FORMAT_SINGLE = 0;
    public static final int FORMAT_STATS = 1;
    public static final int FORMAT_SMALL_MILLI = 2;
    public static final int FORMAT_NO_MILLI = 3;
    public static final int FORMAT_LARGE = 4;
    // --                                    --

    public PuzzleUtils() {
    }

    public static String getPuzzleInPosition(int position) {
        // IMPORTANT: Keep this in sync with the order in "R.array.puzzles".
        switch (position) {
            default:
            case  0: return TYPE_222;
            case  1: return TYPE_333;
            case  2: return TYPE_444;
            case  3: return TYPE_555;
            case  4: return TYPE_666;
            case  5: return TYPE_777;
            case  6: return TYPE_SKEWB;
            case  7: return TYPE_MEGA;
            case  8: return TYPE_PYRA;
            case  9: return TYPE_SQUARE1;
            case 10: return TYPE_CLOCK;
            case 11: return TYPE_333BLD;
            case 12: return TYPE_444BLD;
            case 13: return TYPE_555BLD;
            case 14: return TYPE_333FMC;
            case 15: return TYPE_OTHER;
        }
    }

    /**
     * Gets the position of the given puzzle type when presented in a spinner or other list. This
     * is the inverse of {@link #getPuzzleInPosition(int)}.
     *
     * @param puzzleType The name of the type of puzzle.
     *
     * @return The position (zero-based) of the puzzle within a list.
     */
    public static int getPositionOfPuzzle(String puzzleType) {
        // IMPORTANT: Keep this in sync with the order in "R.array.puzzles".
        switch (puzzleType) {
            default:
            case TYPE_222:     return  0;
            case TYPE_333:     return  1;
            case TYPE_444:     return  2;
            case TYPE_555:     return  3;
            case TYPE_666:     return  4;
            case TYPE_777:     return  5;
            case TYPE_SKEWB:   return  6;
            case TYPE_MEGA:    return  7;
            case TYPE_PYRA:    return  8;
            case TYPE_SQUARE1: return  9;
            case TYPE_CLOCK:   return 10;
            case TYPE_333BLD:  return 11;
            case TYPE_444BLD:  return 12;
            case TYPE_555BLD:  return 13;
            case TYPE_333FMC:  return 14;
            case TYPE_OTHER:   return 15;
        }
    }

    /**
     * Gets the string id of the name of a puzzle
     *
     * @param puzzle
     *
     * @return
     */
    public static
    @StringRes
    int getPuzzleName(String puzzle) {
        switch (puzzle) {
            case TYPE_333:     return R.string.cube_333_informal;
            case TYPE_222:     return R.string.cube_222_informal;
            case TYPE_444:     return R.string.cube_444_informal;
            case TYPE_555:     return R.string.cube_555_informal;
            case TYPE_666:     return R.string.cube_666_informal;
            case TYPE_777:     return R.string.cube_777_informal;
            case TYPE_CLOCK:   return R.string.cube_clock;
            case TYPE_MEGA:    return R.string.cube_mega;
            case TYPE_PYRA:    return R.string.cube_pyra;
            case TYPE_SKEWB:   return R.string.cube_skewb;
            case TYPE_SQUARE1: return R.string.cube_sq1;
            case TYPE_333BLD:  return R.string.cube_333bld_informal;
            case TYPE_444BLD:  return R.string.cube_444bld_informal;
            case TYPE_555BLD:  return R.string.cube_555bld_informal;
            case TYPE_333FMC:  return R.string.cube_333fmc_informal;
            case TYPE_OTHER:   return R.string.cube_other;
            default:           return 0;
        }
    }

    public static @StringRes int getPuzzleNameFromType(String puzzle) {
        // IMPORTANT: Keep this in sync with the order in "R.array.puzzles".
        switch (puzzle) {
            default:
            case TYPE_333:     return R.string.cube_333;
            case TYPE_222:     return R.string.cube_222;
            case TYPE_444:     return R.string.cube_444;
            case TYPE_555:     return R.string.cube_555;
            case TYPE_666:     return R.string.cube_666;
            case TYPE_777:     return R.string.cube_777;
            case TYPE_CLOCK:   return R.string.cube_clock;
            case TYPE_MEGA:    return R.string.cube_mega;
            case TYPE_PYRA:    return R.string.cube_pyra;
            case TYPE_SKEWB:   return R.string.cube_skewb;
            case TYPE_SQUARE1: return R.string.cube_sq1;
            case TYPE_333BLD:  return R.string.cube_333bld;
            case TYPE_444BLD:  return R.string.cube_444bld;
            case TYPE_555BLD:  return R.string.cube_555bld;
            case TYPE_333FMC:  return R.string.cube_333fmc;
            case TYPE_OTHER:   return R.string.cube_other;
        }
    }

    /**
     * Converts a duration value in milliseconds to a String
     * For other than FMC
     *   - FORMAT_SINGLE / FORMAT_STATS
     *     - xx:xx.xx
     *   - FORMAT_SMALL_MILLI
     *     - xx:xx.xx (.xx is small)
     *   - FORMAT_NO_MILLI
     *     - xx:xx
     *   - FORMAT_LARGE (used for Total Time)
     *     - xxh xxm
     * For FMC
     *   - FORMAT_SINGLE
     *     - xx
     *   - FORMAT_STATS
     *     - xx.xx
     *   - FORMAT_SMALL_MILLI
     *     - xx
     *   - FORMAT_NO_MILLI
     *     - xx
     *   - FORMAT_LARGE (used for Total Time)
     *     - "--"
     *
     * @param time
     *      the time in milliseconds
     * @param format
     *      the format. see FORMAT constants in {@link PuzzleUtils}
     * @param puzzleType
     *      current puzzle type
     * @return
     *      a String containing the converted time
     */
    public static String convertTimeToString(long time, int format, String puzzleType) {
        if (time == TIME_DNF)
            return "DNF";
        if (time == 0)
            return "--";
        if (format == FORMAT_LARGE && puzzleType.equals(TYPE_333FMC))
            return "--";

        StringBuilder formattedString = new StringBuilder();

        if (!puzzleType.equals(TYPE_333FMC)) {
            // PeriodFormatter ignores appends (and suffixes) if time is not enough to convert.
            // If the time ir smaller than 10_000 milliseconds (10 seconds), do not pad it with
            // a zero
            Period period;
            try {
                period = new Period(time);
            } catch (ArithmeticException e) {
                return "";
            }
            PeriodFormatterBuilder periodFormatterBuilder = new PeriodFormatterBuilder();
            PeriodFormatter periodFormatter;

            if (format == FORMAT_LARGE) {
                periodFormatter = periodFormatterBuilder
                        .appendHours().appendSuffix("h ")
                        .printZeroAlways()
                        .appendMinutes().appendSuffix("m")
                        .toFormatter();
            } else {
                periodFormatter = periodFormatterBuilder
                        .appendHours().appendSuffix("h ")
                        .appendMinutes().appendSuffix(":")
                        .printZeroAlways()
                        .minimumPrintedDigits(time < (10_000) ? 1 : 2)
                        .appendSeconds()
                        .toFormatter();
            }

            formattedString.append(period.toString(periodFormatter));
        } else {
            // simply append move count
            formattedString.append(time/1000);
        }

        // Restrict millis to 2 digits
        String millis = String.format("%02d", (time % 1000) / 10);

        // Append millis
        switch (format) {
            case FORMAT_SINGLE:
                if (!puzzleType.equals(TYPE_333FMC)) {
                    formattedString.append(".");
                    formattedString.append(millis);
                }
                break;
            case FORMAT_STATS:
                formattedString.append(".");
                formattedString.append(millis);
                break;
            case FORMAT_SMALL_MILLI:
                if (!puzzleType.equals(TYPE_333FMC)) {
                    formattedString.append("<small>.");
                    formattedString.append(millis);
                    formattedString.append("</small>");
                }
                break;
            case FORMAT_NO_MILLI:
            default:
                break;
        }

        return formattedString.toString();
    }

    /**
     * Converts times in the format "M:SS.s", or "S.s" into an integer number of milliseconds. The
     * minutes value may be padded with zeros. The "ss" is the fractional number of seconds and may
     * be given to any desired precision, but will be rounded to a whole number of milliseconds.
     * For example, "1:23.45" is parsed to 83,450 ms and "95.6789" is parsed to 95,679 ms. Where
     * minutes are present, the seconds can be padded with zeros or not, but the number of seconds
     * must be less than 60, or the time will be treated as invalid. Where minutes are not present,
     * the number of seconds can be 60 or greater.
     *
     * @param time
     *     The time string to be parsed. Leading and trailing whitespace will be trimmed before
     *     the time is parsed. If minus signs are present, the result is not defined.
     *
     * @return
     *     The parsed time in milliseconds. The value is rounded to the nearest multiple of 10
     *     milliseconds. If the time cannot be parsed because the format does not conform to the
     *     requirements, zero is returned.
     */
    public static int parseTime(String time) {
        final String timeStr = time.trim();
        final int colonIdx = timeStr.indexOf(':');
        int parsedTime = 0;

        try {
            if (colonIdx != -1) {
                if (colonIdx > 0 && colonIdx < timeStr.length()) {
                    // At least one digit for the minutes, so still a valid time format. Format is
                    // expected to be "M:S.s" (zero padding to "MM" and "SS" is optional).
                    final int minutes = Integer.parseInt(timeStr.substring(0, colonIdx));
                    final float seconds = Float.parseFloat(timeStr.substring(colonIdx + 1));

                    if (seconds < 60f) {
                        parsedTime += 60_000 * minutes + Math.round(seconds * 1_000f);
                    } // else "parsedTime" remains zero as seconds value is out of range (>= 60).
                } // else "parsedTime" remains zero as there is nothing before or after the colon.
            } else {
                // Format is expected to be "S.s", with arbitrary precision and padding.
                parsedTime = Math.round(Float.parseFloat(timeStr.substring(colonIdx + 1)) * 1_000f);
            }
        } catch (NumberFormatException ignore) {
            parsedTime = 0; // Invalid time format.
        }

        return 10 * ((parsedTime + 5) / 10);
    }

    /**
     * Parses a time in the format hh'h'mm:ss.SS and returns it in milliseconds
     * @param time the string to be parsed
     * @return the time in milliseconds
     */
    public static long parseAddedTime(String time) {
        long timeMillis = 0;
        String[] times = time.split("[h]|:|\\.");

        switch (times.length) {
            case 2: // ss.SS
                timeMillis += Long.valueOf(times[0]) * 1_000
                              + Long.valueOf(times[1]) * 10;
                break;
            case 3: // mm:ss.SS
                timeMillis += Long.valueOf(times[0]) * 60_000
                              + Long.valueOf(times[1]) * 1_000
                              + Long.valueOf(times[2]) * 10;
                break;
            case 4: // hh'h'mm:ss.SS
                timeMillis += Long.valueOf(times[0]) * 3_600_000
                              + Long.valueOf(times[1]) * 60_000
                              + Long.valueOf(times[2]) * 1_000
                              + Long.valueOf(times[3]) * 10;
                break;
        }
        return timeMillis;
    }

    /**
     * Applies a penalty to a solve
     *
     * @param solve   A {@link Solve}
     * @param penalty The penalty (refer to static constants on top)
     *
     * @return The solve with the penalty applied
     */
    public static Solve applyPenalty(Solve solve, int penalty) {
        switch (penalty) {
            case PENALTY_DNF:
                if (solve.getPenalty() == PENALTY_PLUSTWO)
                    solve.setTime(solve.getTime() - 2000);
                solve.setPenalty(PENALTY_DNF);
                break;
            case PENALTY_PLUSTWO:
                if (solve.getPenalty() != PENALTY_PLUSTWO)
                    solve.setTime(solve.getTime() + 2000);
                solve.setPenalty(PENALTY_PLUSTWO);
                break;
            case NO_PENALTY:
                if (solve.getPenalty() == PENALTY_PLUSTWO)
                    solve.setTime(solve.getTime() - 2000);
                solve.setPenalty(NO_PENALTY);
                break;
        }
        return solve;
    }

    /**
     * Formats the details of the most recent average-of-N calculation for times recorded in the
     * current session. The string shows the average value and the list of times that contributed
     * to the calculation of that average. If the average calculation requires the elimination of
     * the best and worst times, these times are shown in parentheses.
     *
     * @param n     The value of "N" for which the "average-of-N" is required.
     * @param stats The statistics from which to get the details of the average calculation.
     * @param puzzleType The puzzle type for which the "average-of-N" is required.
     *
     * @return
     *     The average-of-N in string format; or {@code null} if there is no average calculated for
     *     that value of "N", or if insufficient (less than "N") times have been recorded in the
     *     current session, of if {@code stats} is {@code null}.
     */
    private static String formatAverageOfN(int n, Statistics stats, String puzzleType) {
        if (stats == null || stats.getAverageOf(n, true) == null) {
            return null;
        }

        final AverageCalculator.AverageOfN aoN = stats.getAverageOf(n, true).getAverageOfN();
        final long[] times = aoN.getTimes();
        final long average = aoN.getAverage();

        if (average == AverageCalculator.UNKNOWN || times == null) {
            return null;
        }

        final StringBuilder s = new StringBuilder(convertTimeToString(tr(average), PuzzleUtils.FORMAT_STATS, puzzleType));

        s.append(" = ");

        for (int i = 0; i < n; i++) {
            final String time = convertTimeToString(tr(times[i]), PuzzleUtils.FORMAT_SINGLE, puzzleType);

            // The best and worst indices may be -1, but that is OK: they just will not be marked.
            if (i == aoN.getBestTimeIndex() || i == aoN.getWorstTimeIndex()) {
                s.append('(').append(time).append(')');
            } else {
                s.append(time);
            }

            if (i < n - 1) {
                s.append(", ");
            }
        }

        return s.toString();
    }

    private static String replaceAll(String str, HashMap map) {
        StringBuilder rotated = new StringBuilder();
        Character move;

        for (String turn : str.split(" ")) {
            // If a turn is a prime move, get only the first char (F' becomes F)
            move = turn.charAt(0);
            if (map.containsKey(move.toString()))
                rotated.append(map.get(move.toString())).append(turn.length() > 1 ? turn.charAt(1) + " " : " ");
            else
                rotated.append(turn).append(" ");
        }

        return rotated.toString();
    }

    // returns new string with transformed algorithm.
    // Returnes sequence of moves that get the cube to the same position as (alg + rot) does, but without cube rotations.
    // Example: applyRotationForAlgorithm("R U R'", "y") = "F U F'"
    public static String applyRotationForAlgorithm(String alg, String rot) {
        HashMap<String, String> map;
        switch (rot) {
            case "y":
                map = new HashMap<String, String>() {{
                   put("R", "F");
                   put("F", "L");
                   put("L", "B");
                   put("B", "R");
                }};
                break;
            case "y'":
                map = new HashMap<String, String>() {{
                    put("R", "B");
                    put("B", "L");
                    put("L", "F");
                    put("F", "R");
                }};
                break;
            case "y2":
                map = new HashMap<String, String>() {{
                    put("R", "L");
                    put("L", "R");
                    put("B", "F");
                    put("F", "B");
                }};
                break;
            default:
                return alg;
        }

        return replaceAll(alg, map);
    }

        /**
         * Shares an average-of-N, formatted to a simple string.
         *
         * @param n
         *     The value of "N" for which the average is required.
         * @param puzzleType
         *     The name of the type of puzzle being shared.
         * @param stats
         *     The statistics that contain the required details about the average.
         * @param activityContext
         *     An activity context required to start the sharing activity. An application context is
         *     not appropriate, as using it may disrupt the task stack.
         *
         * @return
         *     {@code true} if it is possible to share the average; or {@code false} if it is not.
         */
    public static boolean shareAverageOf(
            int n, String puzzleType, Statistics stats, Activity activityContext) {
        final String averageOfN = formatAverageOfN(n, stats, puzzleType);

        if (averageOfN != null) {
            final Intent shareIntent = new Intent();

            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    activityContext.getString(getPuzzleName(puzzleType))
                            + ": " + formatAverageOfN(n, stats, puzzleType));
            shareIntent.setType("text/plain");
            activityContext.startActivity(shareIntent);

            return true;
        }

        Toast.makeText(activityContext, R.string.fab_share_error, Toast.LENGTH_SHORT).show();
        return false;
    }

    /**
     * Creates a histogram of the frequencies of solve times for the current session. Times are
     * truncated to whole seconds.
     *
     * @param stats The statistics from which to get the frequencies.
     * @param puzzleType The puzzle type for which to get the frequencies.
     *
     * @return
     *     A multi-line string presenting the histogram using "ASCII art"; or an empty string if
     *     the statistics are {@code null}, or if no times have been recorded.
     */
    public static String createHistogramOf(Statistics stats, String puzzleType) {
        final StringBuilder histogram = new StringBuilder(1_000);

        if (stats != null) {
            final Map<Long, Integer> timeFreqs = stats.getSessionTimeFrequencies();

            // Iteration order starts with DNF and then goes by increasing time.
            for (Long time : timeFreqs.keySet()) {
                histogram
                        .append('\n')
                        .append(convertTimeToString(tr(time), FORMAT_NO_MILLI, puzzleType))
                        .append(": ")
                        .append(convertToBars(timeFreqs.get(time))); // frequency value.
            }
        }

        return histogram.toString();
    }

    /**
     * Shares a histogram showing the frequency of solve times falling into intervals of one
     * second. Only times for the current session are presented in the histogram.
     *
     * @param puzzleType
     *     The name of the type of puzzle being shared.
     * @param stats
     *     The statistics that contain the required details to present the histogram.
     * @param activityContext
     *     An activity context required to start the sharing activity. An application context is
     *     not appropriate, as using it may disrupt the task stack.
     *
     * @return
     *     {@code true} if it is possible to share the histogram; or {@code false} if it is not.
     */
    public static boolean shareHistogramOf(
            String puzzleType, Statistics stats, Activity activityContext) {
        final int solveCount = stats != null ? stats.getSessionNumSolves() : 0; // Includes DNFs.

        if (solveCount > 0) {
            final Intent shareIntent = new Intent();

            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                activityContext.getString(R.string.fab_share_histogram_solvecount,
                    activityContext.getString(getPuzzleName(puzzleType)), solveCount) + ":" +
                    createHistogramOf(stats, puzzleType));
            shareIntent.setType("text/plain");
            activityContext.startActivity(shareIntent);

            return true;
        }

        return false;
    }

    /**
     * Takes an int N and converts it to bars █. Used for histograms
     *
     * @param n
     *
     * @return
     */
    private static String convertToBars(int n) {
        StringBuilder temp = new StringBuilder();
        for (int i = 0; i < n; i++) {
            temp.append("█");
        }
        return temp.toString();
    }

    /**
     * Confirm inspection is enabled or not in current puzzle type.
     * If puzzle type is BLD or FMC, inspection is disabled.
     *
     * @param puzzleType current puzzle type
     *
     * @return
     */
    public static boolean isInspectionEnabled (String puzzleType) {
        return !(puzzleType.equals(TYPE_333BLD) || puzzleType.equals(TYPE_444BLD)
                || puzzleType.equals(TYPE_555BLD) || puzzleType.equals(TYPE_333FMC));
    }

    public static boolean isForceMo3Enabled (String puzzleType) {
        return puzzleType.equals(TYPE_666) || puzzleType.equals(TYPE_777)
                || puzzleType.equals(TYPE_333BLD) || puzzleType.equals(TYPE_444BLD)
                || puzzleType.equals(TYPE_555BLD) || puzzleType.equals(TYPE_333FMC);
    }
}

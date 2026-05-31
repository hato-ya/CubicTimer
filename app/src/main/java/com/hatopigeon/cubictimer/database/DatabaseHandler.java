package com.hatopigeon.cubictimer.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.hatopigeon.cubicify.R;
import com.hatopigeon.cubictimer.CubicTimer;
import com.hatopigeon.cubictimer.fragment.dialog.ExportImportDialog;
import com.hatopigeon.cubictimer.items.Algorithm;
import com.hatopigeon.cubictimer.items.Solve;
import com.hatopigeon.cubictimer.stats.ChartStatistics;
import com.hatopigeon.cubictimer.stats.Statistics;
import com.hatopigeon.cubictimer.utils.AlgUtils;
import com.hatopigeon.cubictimer.utils.Prefs;
import com.hatopigeon.cubictimer.utils.PuzzleUtils;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Ari on 03/06/2015.
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    public static final String TABLE_TIMES = "times";

    // Times table
    public static final String KEY_ID       = "_id";
    public static final String KEY_TYPE     = "type";
    public static final String KEY_SUBTYPE  = "subtype";
    public static final String KEY_TIME     = "time";
    public static final String KEY_DATE     = "date";
    public static final String KEY_SCRAMBLE = "scramble";
    public static final String KEY_PENALTY  = "penalty";
    public static final String KEY_COMMENT  = "comment";
    public static final String KEY_HISTORY  = "history";
    public static final String KEY_MOVE_COUNT = "move_count";
    public static final String KEY_TPS        = "tps";
    public static final String KEY_CROSS_TIME = "cross_time";
    public static final String KEY_CROSS_MOVE_COUNT = "cross_move_count";
    public static final String KEY_F2L_TIME = "f2l_time";
    public static final String KEY_F2L_MOVE_COUNT = "f2l_move_count";
    public static final String KEY_OLL_TIME = "oll_time";
    public static final String KEY_OLL_MOVE_COUNT = "oll_move_count";
    public static final String KEY_PLL_TIME = "pll_time";
    public static final String KEY_PLL_MOVE_COUNT = "pll_move_count";
    // Generic per-step breakdown for non-advanced detection methods (intermediate/beginner),
    // serialized as "name:timeMs:moves;name:timeMs:moves;...". Empty for the advanced method.
    public static final String KEY_STEP_SPLITS = "step_splits";
    // Index value of the keys of the "times" table *only* for a full "SELECT * FROM times".
    // Added these to make code in places like "MainActivity" (export/import) a bit more readable,
    // as it was using "magic numbers". However, it would be better if such ad hoc reads were moved
    // back into this class.
    public static final int IDX_ID       = 0;
    public static final int IDX_TYPE     = 1;
    public static final int IDX_SUBTYPE  = 2;
    public static final int IDX_TIME     = 3;
    public static final int IDX_DATE     = 4;
    public static final int IDX_SCRAMBLE = 5;
    public static final int IDX_PENALTY  = 6;
    public static final int IDX_COMMENT  = 7;
    public static final int IDX_HISTORY  = 8;
    public static final int IDX_MOVE_COUNT = 9;
    public static final int IDX_TPS        = 10;
    public static final int IDX_CROSS_TIME = 11;
    public static final int IDX_CROSS_MOVE_COUNT = 12;
    public static final int IDX_F2L_TIME = 13;
    public static final int IDX_F2L_MOVE_COUNT = 14;
    public static final int IDX_OLL_TIME = 15;
    public static final int IDX_OLL_MOVE_COUNT = 16;
    public static final int IDX_PLL_TIME = 17;
    public static final int IDX_PLL_MOVE_COUNT = 18;
    public static final int IDX_STEP_SPLITS = 19;

    // Algs table
    public static final String TABLE_ALGS   = "algs";
    public static final String KEY_SUBSET   = "subset";
    public static final String KEY_NAME     = "name";
    public static final String KEY_STATE    = "state";
    public static final String KEY_ALGS     = "algs";
    public static final String KEY_PROGRESS = "progress";

    public static final String SUBSET_OLL = "OLL";
    public static final String SUBSET_PLL = "PLL";

    // Database Version
    private static final int    DATABASE_VERSION   = 16;
    // Database Name
    private static final String DATABASE_NAME      = "databaseManager";
    private static final String CREATE_TABLE_TIMES =
        "CREATE TABLE " + TABLE_TIMES + "("
            + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_TYPE + " TEXT,"
            + KEY_SUBTYPE + " TEXT,"
            + KEY_TIME + " INTEGER,"
            + KEY_DATE + " INTEGER NOT NULL DEFAULT (strftime('%s', 'now')),"
            + KEY_SCRAMBLE + " TEXT,"
            + KEY_PENALTY + " INTEGER,"
            + KEY_COMMENT + " TEXT,"
            + KEY_HISTORY + " BOOLEAN,"
            + KEY_MOVE_COUNT + " INTEGER DEFAULT 0,"
            + KEY_TPS + " REAL DEFAULT 0.0,"
            + KEY_CROSS_TIME + " INTEGER DEFAULT -1,"
            + KEY_CROSS_MOVE_COUNT + " INTEGER DEFAULT 0,"
            + KEY_F2L_TIME + " INTEGER DEFAULT -1,"
            + KEY_F2L_MOVE_COUNT + " INTEGER DEFAULT 0,"
            + KEY_OLL_TIME + " INTEGER DEFAULT -1,"
            + KEY_OLL_MOVE_COUNT + " INTEGER DEFAULT 0,"
            + KEY_PLL_TIME + " INTEGER DEFAULT -1,"
            + KEY_PLL_MOVE_COUNT + " INTEGER DEFAULT 0,"
            + KEY_STEP_SPLITS + " TEXT DEFAULT ''"
            + ")";
    private static final String CREATE_TABLE_ALGS  =
        "CREATE TABLE " + TABLE_ALGS + "("
            + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_SUBSET + " TEXT,"
            + KEY_NAME + " TEXT,"
            + KEY_STATE + " TEXT,"
            + KEY_ALGS + " TEXT,"
            + KEY_PROGRESS + " INTEGER"
            + ")";

    /**
     * An interface for notification of the progress of bulk database operations.
     */
    public interface ProgressListener {
        /**
         * Notifies the listener of the progress of a bulk operation. This may be called many
         * times during the operation.
         *
         * @param numCompleted
         *     The number of sub-operations of the bulk operation that have been completed.
         * @param total
         *     The total number of sub-operations that must be completed before the the bulk
         *     operation is complete.
         */
        void onProgress(int numCompleted, int total);
    }

    public DatabaseHandler() {
        super(CubicTimer.getAppContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_TIMES);
        db.execSQL(CREATE_TABLE_ALGS);
        createInitialAlgs(db);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if existed
        Log.d("Database upgrade", "Upgrading from"
            + Integer.toString(oldVersion) + " to " + Integer.toString(newVersion));
        switch (oldVersion) {
            case 6:
                db.execSQL("ALTER TABLE times ADD COLUMN " + KEY_HISTORY + " BOOLEAN DEFAULT 0");
                // Fall through to the next upgrade step.
            case 8:
                Prefs.edit()
                        .putInt(R.string.pk_timer_text_size,
                                Prefs.getInt(R.string.pk_timer_text_size, 10) * 10)
                        .apply();
                // Fall through to add new columns for existing users.
            case 10:
                try {
                    db.execSQL("ALTER TABLE times ADD COLUMN " + KEY_MOVE_COUNT + " INTEGER DEFAULT 0");
                } catch (Exception ignored) {}
                try {
                    db.execSQL("ALTER TABLE times ADD COLUMN " + KEY_TPS + " REAL DEFAULT 0.0");
                } catch (Exception ignored) {}
                // Fall through to add cross_time.
            case 11:
                try {
                    db.execSQL("ALTER TABLE times ADD COLUMN " + KEY_CROSS_TIME + " INTEGER DEFAULT -1");
                } catch (Exception ignored) {}
                // Fall through to add cross_move_count.
            case 12:
                try {
                    db.execSQL("ALTER TABLE times ADD COLUMN " + KEY_CROSS_MOVE_COUNT + " INTEGER DEFAULT 0");
                } catch (Exception ignored) {}
                // Fall through to add f2l_time and f2l_move_count.
            case 13:
                try {
                    db.execSQL("ALTER TABLE times ADD COLUMN " + KEY_F2L_TIME + " INTEGER DEFAULT -1");
                } catch (Exception ignored) {}
                try {
                    db.execSQL("ALTER TABLE times ADD COLUMN " + KEY_F2L_MOVE_COUNT + " INTEGER DEFAULT 0");
                } catch (Exception ignored) {}
                // Fall through to add oll_time, oll_move_count, pll_time, pll_move_count.
            case 14:
                try {
                    db.execSQL("ALTER TABLE times ADD COLUMN " + KEY_OLL_TIME + " INTEGER DEFAULT -1");
                } catch (Exception ignored) {}
                try {
                    db.execSQL("ALTER TABLE times ADD COLUMN " + KEY_OLL_MOVE_COUNT + " INTEGER DEFAULT 0");
                } catch (Exception ignored) {}
                try {
                    db.execSQL("ALTER TABLE times ADD COLUMN " + KEY_PLL_TIME + " INTEGER DEFAULT -1");
                } catch (Exception ignored) {}
                try {
                    db.execSQL("ALTER TABLE times ADD COLUMN " + KEY_PLL_MOVE_COUNT + " INTEGER DEFAULT 0");
                } catch (Exception ignored) {}
                // Fall through to add step_splits.
            case 15:
                try {
                    db.execSQL("ALTER TABLE times ADD COLUMN " + KEY_STEP_SPLITS + " TEXT DEFAULT ''");
                } catch (Exception ignored) {}
        }
    }

    private void createAlg(SQLiteDatabase db, String subset, String name, String state, String algs) {
        ContentValues values = new ContentValues();
        values.put(KEY_SUBSET, subset);
        values.put(KEY_NAME, name);
        values.put(KEY_STATE, state);
        values.put(KEY_ALGS, algs);
        values.put(KEY_PROGRESS, 0);
        db.insert(TABLE_ALGS, null, values);
    }

    /**
     * Loads an algorithm from the database for the given algorithm ID.
     *
     * @param algID
     *     The ID of the algorithm to be loaded.
     *
     * @return
     *     An {@link Algorithm} object created from the details loaded from the database for the
     *     algorithm record matching the given ID, or {@code null} if no algorithm matching the
     *     given ID was found.
     */
    public Algorithm getAlgorithm(long algID) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_ALGS,
                new String[] { KEY_ID, KEY_SUBSET, KEY_NAME, KEY_STATE, KEY_ALGS, KEY_PROGRESS },
                KEY_ID + "=?", new String[] { String.valueOf(algID) }, null, null, null, null);

        try {
            if (cursor.moveToFirst()) {
                return new Algorithm(
                        cursor.getLong(0),   // id
                        cursor.getString(1), // subset
                        cursor.getString(2), // name
                        cursor.getString(3), // state
                        cursor.getString(4), // algs
                        cursor.getInt(5));   // progress
            }

            // No algorithm matched the given ID.
            return null;
        } finally {
            cursor.close();
        }
    }

    public int updateAlgorithmAlg(long id, String alg) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ALGS, alg);

        // Updating row
        return db.update(TABLE_ALGS, values, KEY_ID + " = ?",
            new String[] { String.valueOf(id) });
    }

    public int updateAlgorithmProgress(long id, int progress) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PROGRESS, progress);

        // Updating row
        return db.update(TABLE_ALGS, values, KEY_ID + " = ?",
            new String[] { String.valueOf(id) });
    }

    /**
     * Returns all solves from puzzle and category
     * @param type
     * @param subtype
     * @return
     */
    public Cursor getAllSolvesFrom(String type, String subtype) {
        SQLiteDatabase db = this.getReadableDatabase();

        String sqlSelection;
        sqlSelection =
            " WHERE type =? AND subtype =? AND penalty!=" + PuzzleUtils.PENALTY_HIDETIME;

        return db.rawQuery("SELECT * FROM times" + sqlSelection, new String[] { type, subtype });
    }

    /**
     * Moves all current solves from puzzle and category to history
     *
     * @param type
     * @param subtype
     *
     * @return
     */
    public int moveAllSolvesToHistory(String type, String subtype) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_HISTORY, true);

        // Updating row
        return db.update(TABLE_TIMES, values, KEY_TYPE + " = ? AND " + KEY_SUBTYPE + " =?",
            new String[] { type, subtype });
    }

    /**
     * Unarchives a select number of the most recent solves
     *
     * @param type
     * @param subtype
     * @param solves number of solves to be unarchived
     *
     * @return
     */
    public int unarchiveSolves(String type, String subtype, int solves) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_HISTORY, false);

        // Updating row
        return db.update(TABLE_TIMES, values,
                         KEY_ID + " IN (SELECT " + KEY_ID + " FROM " + TABLE_TIMES + " WHERE " +
                         KEY_PENALTY + " != " + PuzzleUtils.PENALTY_HIDETIME + " AND " +
                         KEY_HISTORY + " =1 AND " + KEY_TYPE + " =? AND " + KEY_SUBTYPE + " =? ORDER BY " + KEY_DATE + " DESC LIMIT ?)" ,
                         new String[] { type, subtype, String.valueOf(solves) });
    }

    /**
     * Gets the number of archived solves in the given puzzle and category
     * @param type
     * @param subtype
     * @return
     */
    public long getNumArchivedSolves(String type, String subtype) {
        SQLiteDatabase db = this.getReadableDatabase();

        long count = DatabaseUtils.
                queryNumEntries(db, TABLE_TIMES,
                                KEY_PENALTY + " != " + PuzzleUtils.PENALTY_HIDETIME + " AND " +
                                KEY_TYPE + " =? AND " + KEY_SUBTYPE + " =? AND " + KEY_HISTORY + " =1",
                                new String[] { type, subtype });
        db.close();
        return count;
    }

    /**
     * Adds a new solve to the database.
     *
     * @param solve The solve to be added to the database.
     * @return The new ID of the stored solve record.
     */
    public long addSolve(Solve solve) {
        return addSolveInternal(getWritableDatabase(), solve);
    }

    /**
     * Adds a new solve to the given database.
     *
     * @param db    The database to which to add the solve.
     * @param solve The solve to be added to the database.
     * @return The new ID of the stored solve record.
     */
    private long addSolveInternal(SQLiteDatabase db, Solve solve) {
        // Cutting off last digit to fix rounding errors
        long time = solve.getTime();

        ContentValues values = new ContentValues();

        values.put(KEY_TYPE, solve.getPuzzle());
        values.put(KEY_SUBTYPE, solve.getSubtype());
        values.put(KEY_TIME, time);
        values.put(KEY_DATE, solve.getDate());
        values.put(KEY_SCRAMBLE, solve.getScramble());
        values.put(KEY_PENALTY, solve.getRawPenalty());
        values.put(KEY_COMMENT, solve.getComment());
        values.put(KEY_HISTORY, solve.isHistory());
        values.put(KEY_MOVE_COUNT, solve.getMoveCount());
        values.put(KEY_TPS, solve.getTps());
        values.put(KEY_CROSS_TIME, solve.getCrossTime());
        values.put(KEY_CROSS_MOVE_COUNT, solve.getCrossMoveCount());
        values.put(KEY_F2L_TIME, solve.getF2lTime());
        values.put(KEY_F2L_MOVE_COUNT, solve.getF2lMoveCount());
        values.put(KEY_OLL_TIME, solve.getOllTime());
        values.put(KEY_OLL_MOVE_COUNT, solve.getOllMoveCount());
        values.put(KEY_PLL_TIME, solve.getPllTime());
        values.put(KEY_PLL_MOVE_COUNT, solve.getPllMoveCount());
        values.put(KEY_STEP_SPLITS, solve.getStepSplits());

        // Inserting Row
        return db.insert(TABLE_TIMES, null, values);
    }

    /**
     * Adds a collection of new solves to the given database. The solves are added in a single
     * transaction, so this operation is much faster than adding them one-by-one using the
     * {@link #addSolve(Solve)} method. Any given solve that matches a solve already in the
     * database will not be inserted.
     *
     * @param fileFormat
     *      The solve file format, must be {@link ExportImportDialog#EXIM_FORMAT_EXTERNAL}, or
     *          *     {@link ExportImportDialog#EXIM_FORMAT_BACKUP}.
     * @param solves
     *     The collection of solves to be added to the database. Must not be {@code null}, but may
     *     be empty.
     * @param listener
     *     An optional progress listener that will be notified as each new solve is inserted into
     *     the database. Before the first new solve is added, this will be called to report that
     *     zero of the total number of solves have been inserted (even if {@code solves} is empty).
     *     Thereafter, it will be notified after each insertion. May be {@code null} if no progress
     *     updates are required.
     *
     * @return
     *     The number of unique solves inserted. Solves that are duplicates of existing solves
     *     (by {@link #solveExists(Solve)}) are not inserted.
     */
    public int addSolves(int fileFormat, Collection<Solve> solves, ProgressListener listener) {
        final int total = solves.size();
        int numProcessed = 0; // Whether inserted or not (i.e., includes duplicates).

        if (listener != null) {
            listener.onProgress(numProcessed, total);
        }

        int numInserted = 0; // Only those actually inserted (i.e., excludes duplicates).

        if (total > 0) {
            final SQLiteDatabase db = getWritableDatabase();

            try{
                // Wrapping the insertions in a transaction is about 50x faster!
                db.beginTransaction();

                for (Solve solve : solves) {
                    // Do not check for duplicates if importing from external
                    if ((fileFormat == ExportImportDialog.EXIM_FORMAT_EXTERNAL || !solveExists(solve))) {
                        addSolveInternal(db, solve);
                        numInserted++;
                    }

                    if (listener != null) {
                        listener.onProgress(++numProcessed, total);
                    }
                }

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }

        return numInserted;
    }

    public int updateSolve(Solve solve) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Cutting off last digit to fix rounding errors
        long time = solve.getTime();

        ContentValues values = new ContentValues();
        values.put(KEY_TYPE, solve.getPuzzle());
        values.put(KEY_SUBTYPE, solve.getSubtype());
        values.put(KEY_TIME, time);
        values.put(KEY_DATE, solve.getDate());
        values.put(KEY_SCRAMBLE, solve.getScramble());
        values.put(KEY_PENALTY, solve.getRawPenalty());
        values.put(KEY_COMMENT, solve.getComment());
        values.put(KEY_HISTORY, solve.isHistory());
        values.put(KEY_MOVE_COUNT, solve.getMoveCount());
        values.put(KEY_TPS, solve.getTps());
        values.put(KEY_CROSS_TIME, solve.getCrossTime());
        values.put(KEY_CROSS_MOVE_COUNT, solve.getCrossMoveCount());
        values.put(KEY_F2L_TIME, solve.getF2lTime());
        values.put(KEY_F2L_MOVE_COUNT, solve.getF2lMoveCount());
        values.put(KEY_OLL_TIME, solve.getOllTime());
        values.put(KEY_OLL_MOVE_COUNT, solve.getOllMoveCount());
        values.put(KEY_PLL_TIME, solve.getPllTime());
        values.put(KEY_PLL_MOVE_COUNT, solve.getPllMoveCount());
        values.put(KEY_STEP_SPLITS, solve.getStepSplits());

        // Updating row
        return db.update(TABLE_TIMES, values, KEY_ID + " = ?",
            new String[] { String.valueOf(solve.getId()) });
    }

    /**
     * Loads a solve from the database for the given solve ID.
     *
     * @param solveID
     *     The ID of the solve to be loaded.
     *
     * @return
     *     A {@link Solve} object created from the details loaded from the database for the solve
     *     time matching the given ID, or {@code null} if no solve time matching the given ID was
     *     found.
     */
    public Solve getSolve(long solveID) {
        final Cursor cursor = getReadableDatabase().query(TABLE_TIMES,
                new String[] {
                        KEY_ID, KEY_TIME, KEY_TYPE, KEY_SUBTYPE, KEY_DATE, KEY_SCRAMBLE,
                        KEY_PENALTY, KEY_COMMENT, KEY_HISTORY, KEY_MOVE_COUNT, KEY_TPS,
                        KEY_CROSS_TIME, KEY_CROSS_MOVE_COUNT,
                        KEY_F2L_TIME, KEY_F2L_MOVE_COUNT,
                        KEY_OLL_TIME, KEY_OLL_MOVE_COUNT,
                        KEY_PLL_TIME, KEY_PLL_MOVE_COUNT, KEY_STEP_SPLITS },
                KEY_ID + "=?", new String[] { String.valueOf(solveID) }, null, null, null, null);

        try {
            if (cursor.moveToFirst()) {
                Solve solve = new Solve(
                        cursor.getLong(0),
                        cursor.getLong(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getLong(4),
                        cursor.getString(5),
                        cursor.getInt(6),
                        cursor.getString(7),
                        getBoolean(cursor, 8));
                solve.setMoveCount(cursor.getInt(9));
                solve.setTps(cursor.getDouble(10));
                solve.setCrossTime(cursor.getLong(11));
                solve.setCrossMoveCount(cursor.getInt(12));
                solve.setF2lTime(cursor.getLong(13));
                solve.setF2lMoveCount(cursor.getInt(14));
                solve.setOllTime(cursor.getLong(15));
                solve.setOllMoveCount(cursor.getInt(16));
                solve.setPllTime(cursor.getLong(17));
                solve.setPllMoveCount(cursor.getInt(18));
                solve.setStepSplits(cursor.getString(19));
                return solve;
            }

            // No solve matched the given ID.
            return null;
        } finally {
            cursor.close();
        }
    }

    // Preferred display order for detected steps; unknown names are appended in first-seen order.
    private static final String[] STEP_ORDER = {
        "Cross", "F2L", "OLL", "PLL",
        "First layer", "Second layer", "Opposite cross", "Opposite edges",
        "Corners position", "Corners orient",
    };

    /**
     * Builds a full {@link Statistics} object per solve step (cross, F2L, … or the beginner steps),
     * fed with each step's per-solve times in chronological order, so the timer-graph table can be
     * rendered for each step exactly like the overall stats. Reads the generic step breakdown for
     * intermediate/beginner solves and the named cross/F2L/OLL/PLL columns for advanced solves.
     */
    public java.util.LinkedHashMap<String, Statistics> getStepStatistics(String type, String subtype) {
        java.util.LinkedHashMap<String, Statistics> map = new java.util.LinkedHashMap<>();
        final String sql = "SELECT " + KEY_CROSS_TIME + ", " + KEY_F2L_TIME + ", " + KEY_OLL_TIME
                + ", " + KEY_PLL_TIME + ", " + KEY_STEP_SPLITS + ", " + KEY_PENALTY + ", "
                + KEY_HISTORY + ", " + KEY_DATE + " FROM " + TABLE_TIMES
                + " WHERE " + KEY_TYPE + "=? AND " + KEY_SUBTYPE + "=? ORDER BY " + KEY_DATE + " ASC";
        final Cursor cursor = getReadableDatabase().rawQuery(sql, new String[] { type, subtype });

        org.joda.time.DateTime dtNow = new org.joda.time.DateTime();
        org.joda.time.DateTime dtFrom, dtTo;
        if (dtNow.getHourOfDay() < 5) {
            dtTo = new org.joda.time.DateTime(dtNow.getYear(), dtNow.getMonthOfYear(), dtNow.getDayOfMonth(), 5, 0, 0);
            dtNow = dtNow.minusDays(1);
            dtFrom = new org.joda.time.DateTime(dtNow.getYear(), dtNow.getMonthOfYear(), dtNow.getDayOfMonth(), 5, 0, 0);
        } else {
            dtFrom = new org.joda.time.DateTime(dtNow.getYear(), dtNow.getMonthOfYear(), dtNow.getDayOfMonth(), 5, 0, 0);
            dtNow = dtNow.plusDays(1);
            dtTo = new org.joda.time.DateTime(dtNow.getYear(), dtNow.getMonthOfYear(), dtNow.getDayOfMonth(), 5, 0, 0);
        }
        try {
            while (cursor.moveToNext()) {
                if (Solve.getPenalty(cursor.getInt(5)) == PuzzleUtils.PENALTY_DNF) continue;
                boolean session = cursor.getInt(6) == 0; // current session = not archived
                long date = cursor.getLong(7);
                boolean today = dtFrom.getMillis() <= date && date < dtTo.getMillis();
                String splits = cursor.getString(4);
                if (splits != null && !splits.isEmpty()) {
                    for (String part : splits.split(";")) {
                        String[] f = part.split(":");
                        if (f.length < 2) continue;
                        long t;
                        try { t = Long.parseLong(f[1]); } catch (NumberFormatException e) { continue; }
                        if (t >= 0) addStepTime(map, f[0], t, session, today, type);
                    }
                } else {
                    long cross = cursor.getLong(0), f2l = cursor.getLong(1);
                    long oll = cursor.getLong(2), pll = cursor.getLong(3);
                    if (cross >= 0) addStepTime(map, "Cross", cross, session, today, type);
                    if (f2l >= 0) addStepTime(map, "F2L", f2l, session, today, type);
                    if (oll >= 0) addStepTime(map, "OLL", oll, session, today, type);
                    if (pll >= 0) addStepTime(map, "PLL", pll, session, today, type);
                }
            }
        } finally {
            cursor.close();
        }

        // Re-order canonically.
        java.util.LinkedHashMap<String, Statistics> ordered = new java.util.LinkedHashMap<>();
        for (String name : STEP_ORDER) if (map.containsKey(name)) ordered.put(name, map.remove(name));
        ordered.putAll(map);
        return ordered;
    }

    private static void addStepTime(java.util.LinkedHashMap<String, Statistics> map, String name,
                                    long time, boolean session, boolean today, String puzzleType) {
        Statistics st = map.get(name);
        if (st == null) { st = Statistics.newAllTimeStatistics(puzzleType); map.put(name, st); }
        st.addTime(time, session, today);
    }

    public boolean getBoolean(Cursor cursor, int columnIndex) {
        return ! (cursor.isNull(columnIndex) || cursor.getShort(columnIndex) == 0);
    }

    public List<String> getAllSubtypesFromType(String type) {
        List<String> subtypesList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT DISTINCT " + KEY_SUBTYPE + " FROM "
            + TABLE_TIMES + " WHERE " + KEY_TYPE + " ='" + type + "' ORDER BY " + KEY_SUBTYPE + " ASC", null);

        if (cursor.moveToFirst()) {
            do {
                subtypesList.add(cursor.getString(cursor.getColumnIndex(KEY_SUBTYPE)));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return subtypesList;
    }

    /**
     * Populates the collection of statistics (average calculators) with the solve times recorded
     * in the database. The statistics will manage the segregation of solves for the current session
     * only from those from all past and current sessions. If all average calculators are for the
     * current session only, only the times for the current session will be read from the database.
     *
     * @param puzzleType
     *     The name of the puzzle type.
     * @param puzzleSubtype
     *     The name of the puzzle subtype.
     * @param statistics
     *     The statistics in which to record the solve times. This may contain any mix of average
     *     calculators for all sessions or only the current session. The database read will be
     *     adapted automatically to read the minimum number of rows to satisfy the collection of
     *     the required statistics.
     */
    public void populateStatistics(
            String puzzleType, String puzzleSubtype, Statistics statistics) {
        final boolean isStatisticsForCurrentSessionOnly = statistics.isForCurrentSessionOnly();
        final String sql;

        // Sort into ascending order of date (oldest solves first), so that the "current"
        // average is, in the end, calculated to be that of the most recent solves.
        if (isStatisticsForCurrentSessionOnly) {
            sql = "SELECT " + KEY_TIME + ", " + KEY_PENALTY + ", " + KEY_DATE + " FROM " + TABLE_TIMES
                    + " WHERE " + KEY_TYPE + "=? AND " + KEY_SUBTYPE + "=? AND "
                    + KEY_PENALTY + "!=" + PuzzleUtils.PENALTY_HIDETIME + " AND "
                    + KEY_HISTORY + "=0 ORDER BY " + KEY_DATE + " ASC";
        } else {
            sql = "SELECT " + KEY_TIME + ", " + KEY_PENALTY + ", " + KEY_HISTORY + ", " + KEY_DATE
                    + " FROM " + TABLE_TIMES + " WHERE " + KEY_TYPE + "=? AND "
                    + KEY_SUBTYPE + "=? AND " + KEY_PENALTY + "!=" + PuzzleUtils.PENALTY_HIDETIME
                    + " ORDER BY " + KEY_DATE + " ASC";
        }

        final Cursor cursor
                = getReadableDatabase().rawQuery(sql, new String[] { puzzleType, puzzleSubtype });

        try {
            final int timeCol = cursor.getColumnIndex(KEY_TIME);
            final int penaltyCol = cursor.getColumnIndex(KEY_PENALTY);
            final int dateCol = cursor.getColumnIndex(KEY_DATE);
            final int historyCol
                    = isStatisticsForCurrentSessionOnly ? -1 : cursor.getColumnIndex(KEY_HISTORY);

            DateTime dtNow = new DateTime();
            DateTime dtFrom;
            DateTime dtTo;
            //Log.d("DatabaseHandler", "Now  : " + dtNow.toString());
            if (dtNow.getHourOfDay() < 5) {
                dtTo = new DateTime(dtNow.getYear(), dtNow.getMonthOfYear(), dtNow.getDayOfMonth(), 5, 0, 0);
                dtNow = dtNow.minusDays(1);
                dtFrom = new DateTime(dtNow.getYear(), dtNow.getMonthOfYear(), dtNow.getDayOfMonth(), 5, 0, 0);
            } else {
                dtFrom = new DateTime(dtNow.getYear(), dtNow.getMonthOfYear(), dtNow.getDayOfMonth(), 5, 0, 0);
                dtNow = dtNow.plusDays(1);
                dtTo = new DateTime(dtNow.getYear(), dtNow.getMonthOfYear(), dtNow.getDayOfMonth(), 5, 0, 0);
            }
            //Log.d("DatabaseHandler", "From : " + dtFrom.toString());
            //Log.d("DatabaseHandler", "To   : " + dtTo.toString());

            while (cursor.moveToNext()) {
                final boolean isForCurrentSession
                        = isStatisticsForCurrentSessionOnly || cursor.getInt(historyCol) == 0;
                final boolean isToday = (dtFrom.getMillis() <= cursor.getLong(dateCol)
                        && cursor.getLong(dateCol) < dtTo.getMillis());

                if (Solve.getPenalty(cursor.getInt(penaltyCol)) == PuzzleUtils.PENALTY_DNF) {
                    statistics.addDNF(isForCurrentSession, isToday);
                } else {
                    statistics.addTime(cursor.getLong(timeCol), isForCurrentSession, isToday);
                }
            }
        } finally {
            // As elsewhere in this class, assume "cursor" is not null.
            cursor.close();
        }
    }

    /**
     * Populates the chart statistics with the solve times recorded in the database. If all
     * statistics are for the current session only, only the times for the current session will be
     * read from the database.
     *
     * @param puzzleType
     *     The name of the puzzle type.
     * @param puzzleSubtype
     *     The name of the puzzle subtype.
     * @param statistics
     *     The chart statistics in which to record the solve times. This may require solve times for
     *     all sessions or only the current session. The database read will be adapted automatically
     *     to read the minimum number of rows to satisfy the collection of the required statistics.
     */
    public void populateChartStatistics(
            String puzzleType, String puzzleSubtype, ChartStatistics statistics) {
        final boolean isStatisticsForCurrentSessionOnly = statistics.isForCurrentSessionOnly();
        final String sql;

        // Sort into ascending order of date (oldest solves first), so that the "current"
        // average is, in the end, calculated to be that of the most recent solves.
        if (isStatisticsForCurrentSessionOnly) {
            sql = "SELECT " + KEY_TIME + ", " + KEY_PENALTY + ", " + KEY_DATE
                    + " FROM " + TABLE_TIMES + " WHERE " + KEY_TYPE + "=? AND "
                    + KEY_SUBTYPE + "=? AND " + KEY_PENALTY + "!=" + PuzzleUtils.PENALTY_HIDETIME
                    + " AND " + KEY_HISTORY + "=0 ORDER BY " + KEY_DATE + " ASC";
        } else {
            // NOTE: A change from the old approach: the "all time" option include those from the
            // current session, too. This is consistent with the way "all time statistics" are
            // calculated for the table of statistics.
            sql = "SELECT " + KEY_TIME + ", " + KEY_PENALTY + ", " + KEY_DATE
                    + " FROM " + TABLE_TIMES + " WHERE " + KEY_TYPE + "=? AND "
                    + KEY_SUBTYPE + "=? AND " + KEY_PENALTY + "!=" + PuzzleUtils.PENALTY_HIDETIME
                    + " ORDER BY " + KEY_DATE + " ASC";
        }

        final Cursor cursor
                = getReadableDatabase().rawQuery(sql, new String[] { puzzleType, puzzleSubtype });

        try {
            final int timeCol = cursor.getColumnIndex(KEY_TIME);
            final int penaltyCol = cursor.getColumnIndex(KEY_PENALTY);
            final int dateCol = cursor.getColumnIndex(KEY_DATE);

            while (cursor.moveToNext()) {
                if (Solve.getPenalty(cursor.getInt(penaltyCol)) == PuzzleUtils.PENALTY_DNF) {
                    statistics.addDNF(cursor.getLong(dateCol));
                } else {
                    statistics.addTime(cursor.getLong(timeCol), cursor.getLong(dateCol));
                }
            }
        } finally {
            // As elsewhere in this class, assume "cursor" is not null.
            cursor.close();
        }
    }

    /**
     * Deletes a single solve matching the given ID from the database.
     *
     * @param solveID
     *     The ID of the solve record in the "times" table of the database.
     *
     * @return
     *     The number of records deleted. If no record matches {@code solveID}, the result is zero.
     */
    public int deleteSolveByID(long solveID) {
        return deleteSolveByIDInternal(getWritableDatabase(), solveID);
    }

    /**
     * Deletes a single solve from the database.
     *
     * @param solve
     *     The solve to be deleted. The corresponding database record to be deleted from the "times"
     *     table is matched using the ID returned from {@link Solve#getId()}.
     *
     * @return
     *     The number of records deleted. If no record matches the ID of the solve, the result is
     *     zero.
     */
    public int deleteSolve(Solve solve) {
        return deleteSolveByIDInternal(getWritableDatabase(), solve.getId());
    }

    /**
     * Deletes multiple solves from the database that match the solve record IDs in the given
     * collection. The solves are deleted in the context of a single database transaction.
     *
     * @param solveIDs
     *     The IDs of the solve records in the "times" table of the database to be deleted. Must
     *     not be {@code null}, but may be empty.
     * @param listener
     *     An optional progress listener that will be notified as each solve is deleted from the
     *     database. Before the first solve is deleted, this will be called to report that zero of
     *     the total number of solves have been deleted (even if {@code solveIDs} is empty).
     *     Thereafter, it will be notified after each attempted deletion by ID, whether a matching
     *     solve was found or not. May be {@code null} if no progress reports are required.
     *
     * @return
     *     The number of records deleted. If an ID from {@code solveIDs} does not match any record,
     *     or if an ID is a duplicate of an ID that has already been deleted, that ID is ignored,
     *     so the result may be less than the number of solve IDs in the collection.
     */
    public int deleteSolvesByID(Collection<Long> solveIDs, ProgressListener listener) {
        final int total = solveIDs.size();
        int numProcessed = 0; // Whether deleted or not (i.e., includes RNF and duplicates).

        if (listener != null) {
            listener.onProgress(numProcessed, total);
        }

        int numDeleted = 0; // Only those actually deleted (i.e., excludes RNF and duplicates).

        if (total > 0) {
            final SQLiteDatabase db = getWritableDatabase();

            try{
                // Wrap the bulk delete operations in a transaction; it is *much* faster,
                db.beginTransaction();

                for (long id : solveIDs) {
                    // May not change if RNF or if ID is a duplicate and is already deleted.
                    numDeleted += deleteSolveByIDInternal(db, id);

                    if (listener != null) {
                        listener.onProgress(++numProcessed, total);
                    }
                }

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }

        return numDeleted;
    }

    /**
     * Deletes a single solve matching the given ID from the database.
     *
     * @param db
     *     The database from which to delete the solve.
     * @param solveID
     *     The ID of the solve record in the "times" table of the database.
     *
     * @return
     *     The number of records deleted. If no record matches {@code solveID}, the result is zero.
     */
    private int deleteSolveByIDInternal(SQLiteDatabase db, long solveID) {
        return db.delete(TABLE_TIMES, KEY_ID + "=?", new String[] { Long.toString(solveID) });
    }

    // Delete entries from session
    public int deleteAllFromSession(String type, String subtype) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_TIMES, KEY_TYPE + "=? AND " + KEY_SUBTYPE + " = ? AND " + KEY_HISTORY + "=0", new String[] { type, subtype });
    }

    /**
     * Deletes all solves from a subtype, thus removing the subtype
     *
     * @param subtype
     */
    public int deleteSubtype(String type, String subtype) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_TIMES, KEY_TYPE + "=? AND " + KEY_SUBTYPE + " = ?",
            new String[] { type, subtype });
    }

    /**
     * Renames a subtype
     *
     * @param subtype
     */
    public int renameSubtype(String type, String subtype, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_SUBTYPE, newName);
        return db.update(TABLE_TIMES, contentValues, KEY_TYPE + "=? AND " + KEY_SUBTYPE + "=?", new String[] { type, subtype });
    }

    public Cursor getAllSolves() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM times WHERE penalty!=" + PuzzleUtils.PENALTY_HIDETIME, null);
    }

    public boolean solveExists(Solve solve) {
        SQLiteDatabase db = this.getReadableDatabase();

        return DatabaseUtils.queryNumEntries(db, TABLE_TIMES, "type=? AND subtype =? AND time=? AND scramble=? AND date=?", new String[] { solve.getPuzzle(), solve.getSubtype(), String.valueOf(solve.getTime()), solve.getScramble(), String.valueOf(solve.getDate()) }) > 0;
    }

    // TODO: this info should REALLY be in a separate file. I'll get to it when I add other alg sets.

    private void createInitialAlgs(SQLiteDatabase db) {
        // OLL FIXME: the state field is deprecated. use the reference_states xml file
        createAlg(db, SUBSET_OLL, "OLL 01", "NNNNYNNNNNYNYYYNYNYYY", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 01"));
        createAlg(db, SUBSET_OLL, "OLL 02", "NNNNYNNNNNYYNYNYYNYYY", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 02"));
        createAlg(db, SUBSET_OLL, "OLL 03", "NNNNYNYNNYYNYYNYYNNYN", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 03"));
        createAlg(db, SUBSET_OLL, "OLL 04", "NNNNYNNNYNYYNYNNYYNYY", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 04"));
        createAlg(db, SUBSET_OLL, "OLL 05", "NNNNYYNYYYYNYNNNNNYYN", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 05"));
        createAlg(db, SUBSET_OLL, "OLL 06", "NYYNYYNNNNNNNNYNYYNYY", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 06"));
        createAlg(db, SUBSET_OLL, "OLL 07", "NYNYYNYNNYNNYYNYYNNNN", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 07"));
        createAlg(db, SUBSET_OLL, "OLL 08", "NYNNYYNNYNNYNNNNYYNYY", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 08"));
        createAlg(db, SUBSET_OLL, "OLL 09", "NNYYYNNYNNYNNYYNNYNNY", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 09"));
        createAlg(db, SUBSET_OLL, "OLL 10", "NNYYYNNYNYYNNYNYNNYNN", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 10"));
        createAlg(db, SUBSET_OLL, "OLL 11", "NNNNYYYYNYYNYNNYNNNYN", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 11"));
        createAlg(db, SUBSET_OLL, "OLL 12", "NNYNYYNYNNYNNNYNNYNYY", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 12"));
        createAlg(db, SUBSET_OLL, "OLL 13", "NNNYYYYNNYYNYNNYYNNNN", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 13"));
        createAlg(db, SUBSET_OLL, "OLL 14", "NNNYYYNNYNYYNNNNYYNNY", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 14"));
        createAlg(db, SUBSET_OLL, "OLL 15", "NNNYYYNNYYYNYNNNYNYNN", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 15"));
        createAlg(db, SUBSET_OLL, "OLL 16", "NNYYYYNNNNYNNNYNYYNNY", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 16"));
        createAlg(db, SUBSET_OLL, "OLL 17", "YNNNYNNNYNYYNYNNYNYYN", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 17"));
        createAlg(db, SUBSET_OLL, "OLL 18", "YNYNYNNNNNYNNYNYYYNYN", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 18"));
        createAlg(db, SUBSET_OLL, "OLL 19", "YNYNYNNNNNYNNYYNYNYYN", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 19"));
        createAlg(db, SUBSET_OLL, "OLL 20", "YNYNYNYNYNYNNYNNYNNYN", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 20"));
        createAlg(db, SUBSET_OLL, "OLL 21", "NYNYYYNYNNNNYNYNNNYNY", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 21"));
        createAlg(db, SUBSET_OLL, "OLL 22", "NYNYYYNYNNNYNNNYNNYNY", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 22"));
        createAlg(db, SUBSET_OLL, "OLL 23", "YYYYYYNYNNNNNNNYNYNNN", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 23"));
        createAlg(db, SUBSET_OLL, "OLL 24", "NYYYYYNYYYNNNNNNNYNNN", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 24"));
        createAlg(db, SUBSET_OLL, "OLL 25", "YYNYYYNYYNNNYNNNNYNNN", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 25"));
        createAlg(db, SUBSET_OLL, "OLL 26", "YYNYYYNYNNNYNNYNNYNNN", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 26"));
        createAlg(db, SUBSET_OLL, "OLL 27", "NYNYYYYYNYNNYNNYNNNNN", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 27"));
        createAlg(db, SUBSET_OLL, "OLL 28", "YYYYYNYNYNNNNYNNYNNNN", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 28"));
        createAlg(db, SUBSET_OLL, "OLL 29", "YNYYYNNYNNYNNYYNNNYNN", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 29"));
        createAlg(db, SUBSET_OLL, "OLL 30", "YNYNYYNYNNYNNNYNNNYYN", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 30"));
        createAlg(db, SUBSET_OLL, "OLL 31", "NYYNYYNNYYNNNNNNYYNYN", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 31"));
        createAlg(db, SUBSET_OLL, "OLL 32", "NNYNYYNYYYYNNNNNNYNYN", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 32"));
        createAlg(db, SUBSET_OLL, "OLL 33", "NNYYYYNNYYYNNNNNYYNNN", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 33"));
        createAlg(db, SUBSET_OLL, "OLL 34", "YNYYYYNNNNYNNNYNYNYNN", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 34"));
        createAlg(db, SUBSET_OLL, "OLL 35", "YNNNYYNYYNYNYNNNNYNYN", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 35"));
        createAlg(db, SUBSET_OLL, "OLL 36", "YNNYYNNYYNYNYYNNNYNNN", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 36"));
        createAlg(db, SUBSET_OLL, "OLL 37", "YYNYYNNNYNNNYYNNYYNNN", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 37"));
        createAlg(db, SUBSET_OLL, "OLL 38", "NYYYYNYNNYNNNYYNYNNNN", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 38"));
        createAlg(db, SUBSET_OLL, "OLL 39", "YYNNYNNYYNNYNYNNNNYYN", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 39"));
        createAlg(db, SUBSET_OLL, "OLL 40", "NYYNYNYYNNNNNYNYNNNYY", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 40"));
        createAlg(db, SUBSET_OLL, "OLL 41", "YNYNYYNYNNYNNNNYNYNYN", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 41"));
        createAlg(db, SUBSET_OLL, "OLL 42", "YNYYYNNYNNYNNYNYNYNNN", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 42"));
        createAlg(db, SUBSET_OLL, "OLL 43", "YNNYYNYYNNYNYYYNNNNNN", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 43"));
        createAlg(db, SUBSET_OLL, "OLL 44", "NNYNYYNYYNYNNNNNNNYYY", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 44"));
        createAlg(db, SUBSET_OLL, "OLL 45", "NNYYYYNNYNYNNNNNYNYNY", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 45"));
        createAlg(db, SUBSET_OLL, "OLL 46", "YYNNYNYYNNNNYYYNNNNYN", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 46"));
        createAlg(db, SUBSET_OLL, "OLL 47", "NYNNYYNNNYNNYNYNYYNYN", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 47"));
        createAlg(db, SUBSET_OLL, "OLL 48", "NYNYYNNNNNNYNYNYYNYNY", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 48"));
        createAlg(db, SUBSET_OLL, "OLL 49", "NNNYYNNYNYYNYYYNNYNNN", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 49"));
        createAlg(db, SUBSET_OLL, "OLL 50", "NNNNYYNYNNYYNNNYNNYYY", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 50"));
        createAlg(db, SUBSET_OLL, "OLL 51", "NNNYYYNNNNYYNNNYYNYNY", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 51"));
        createAlg(db, SUBSET_OLL, "OLL 52", "NYNNYNNYNYNNYYYNNYNYN", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 52"));
        createAlg(db, SUBSET_OLL, "OLL 53", "NNNNYYNYNNYNYNYNNNYYY", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 53"));
        createAlg(db, SUBSET_OLL, "OLL 54", "NYNNYYNNNNNNYNYNYNYYY", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 54"));
        createAlg(db, SUBSET_OLL, "OLL 55", "NYNNYNNYNNNNYYYNNNYYY", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 55"));
        createAlg(db, SUBSET_OLL, "OLL 56", "NNNYYYNNNNYNYNYNYNYNY", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 56"));
        createAlg(db, SUBSET_OLL, "OLL 57", "YNYYYYYNYNYNNNNNYNNNN", AlgUtils.getDefaultAlgs(SUBSET_OLL, "OLL 57"));

        // PLL
        createAlg(db, SUBSET_PLL, "H", "YYYYYYYYYOROGBGRORBGB", AlgUtils.getDefaultAlgs(SUBSET_PLL, "H"));
        createAlg(db, SUBSET_PLL, "Ua", "YYYYYYYYYOBOGOGRRRBGB", AlgUtils.getDefaultAlgs(SUBSET_PLL, "Ua"));
        createAlg(db, SUBSET_PLL, "Ub", "YYYYYYYYYOGOGBGRRRBOB", AlgUtils.getDefaultAlgs(SUBSET_PLL, "Ub"));
        createAlg(db, SUBSET_PLL, "Z", "YYYYYYYYYOBOGRGRGRBOB", AlgUtils.getDefaultAlgs(SUBSET_PLL, "Z"));
        createAlg(db, SUBSET_PLL, "Aa", "YYYYYYYYYGOGRGBORRBBO", AlgUtils.getDefaultAlgs(SUBSET_PLL, "Aa"));
        createAlg(db, SUBSET_PLL, "Ab", "YYYYYYYYYOORBGOGRGRBB", AlgUtils.getDefaultAlgs(SUBSET_PLL, "Ab"));
        createAlg(db, SUBSET_PLL, "E", "YYYYYYYYYGOBOGRBRGRBO", AlgUtils.getDefaultAlgs(SUBSET_PLL, "E"));
        createAlg(db, SUBSET_PLL, "F", "YYYYYYYYYGOBOBGRRRBGO", AlgUtils.getDefaultAlgs(SUBSET_PLL, "F"));
        createAlg(db, SUBSET_PLL, "Ga", "YYYYYYYYYRBOGGRBOBORG", AlgUtils.getDefaultAlgs(SUBSET_PLL, "Ga"));
        createAlg(db, SUBSET_PLL, "Gb", "YYYYYYYYYBROGGBOBGROR", AlgUtils.getDefaultAlgs(SUBSET_PLL, "Gb"));
        createAlg(db, SUBSET_PLL, "Gc", "YYYYYYYYYOGRBROGOGRBB", AlgUtils.getDefaultAlgs(SUBSET_PLL, "Gc"));
        createAlg(db, SUBSET_PLL, "Gd", "YYYYYYYYYORGRORBGOGBB", AlgUtils.getDefaultAlgs(SUBSET_PLL, "Gd"));
        createAlg(db, SUBSET_PLL, "Ja", "YYYYYYYYYBOOGGGRBBORR", AlgUtils.getDefaultAlgs(SUBSET_PLL, "Ja"));
        createAlg(db, SUBSET_PLL, "Jb", "YYYYYYYYYOOGRROGGRBBB", AlgUtils.getDefaultAlgs(SUBSET_PLL, "Jb"));
        createAlg(db, SUBSET_PLL, "Na", "YYYYYYYYYOORBBGRROGGB", AlgUtils.getDefaultAlgs(SUBSET_PLL, "Na"));
        createAlg(db, SUBSET_PLL, "Nb", "YYYYYYYYYROOGBBORRBGG", AlgUtils.getDefaultAlgs(SUBSET_PLL, "Nb"));
        createAlg(db, SUBSET_PLL, "Ra", "YYYYYYYYYOGOGORBRGRBB", AlgUtils.getDefaultAlgs(SUBSET_PLL, "Ra"));
        createAlg(db, SUBSET_PLL, "Rb", "YYYYYYYYYGOBORGRGRBBO", AlgUtils.getDefaultAlgs(SUBSET_PLL, "Rb"));
        createAlg(db, SUBSET_PLL, "T", "YYYYYYYYYOOGRBOGRRBGB", AlgUtils.getDefaultAlgs(SUBSET_PLL, "T"));
        createAlg(db, SUBSET_PLL, "V", "YYYYYYYYYRGOGOBORRBBG", AlgUtils.getDefaultAlgs(SUBSET_PLL, "V"));
        createAlg(db, SUBSET_PLL, "Y", "YYYYYYYYYRBOGGBORRBOG", AlgUtils.getDefaultAlgs(SUBSET_PLL, "Y"));
    }
}

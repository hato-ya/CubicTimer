package com.hatopigeon.cubictimer.activity;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hatopigeon.cubicify.BuildConfig;
import com.hatopigeon.cubicify.R;
import com.hatopigeon.cubictimer.CubicTimer;
import com.hatopigeon.cubictimer.database.DatabaseHandler;
import com.hatopigeon.cubictimer.fragment.AlgListFragment;
import com.hatopigeon.cubictimer.fragment.TimerFragment;
import com.hatopigeon.cubictimer.fragment.TimerFragmentMain;
import com.hatopigeon.cubictimer.fragment.dialog.ExportImportDialog;
import com.hatopigeon.cubictimer.fragment.dialog.PuzzleChooserDialog;
import com.hatopigeon.cubictimer.fragment.dialog.SchemeSelectDialogMain;
import com.hatopigeon.cubictimer.fragment.dialog.ThemeSelectDialog;
import com.hatopigeon.cubictimer.items.Solve;
import com.hatopigeon.cubictimer.puzzle.TrainerScrambler;
import com.hatopigeon.cubictimer.utils.ExportImportUtils;
import com.hatopigeon.cubictimer.utils.InsetsUtils;
import com.hatopigeon.cubictimer.utils.LocaleUtils;
import com.hatopigeon.cubictimer.utils.Prefs;
import com.hatopigeon.cubictimer.utils.PuzzleUtils;
import com.hatopigeon.cubictimer.utils.StoreUtils;
import com.hatopigeon.cubictimer.utils.ThemeUtils;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.ExpandableDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import butterknife.ButterKnife;

import static com.hatopigeon.cubictimer.database.DatabaseHandler.IDX_COMMENT;
import static com.hatopigeon.cubictimer.database.DatabaseHandler.IDX_DATE;
import static com.hatopigeon.cubictimer.database.DatabaseHandler.IDX_PENALTY;
import static com.hatopigeon.cubictimer.database.DatabaseHandler.IDX_SCRAMBLE;
import static com.hatopigeon.cubictimer.database.DatabaseHandler.IDX_SUBTYPE;
import static com.hatopigeon.cubictimer.database.DatabaseHandler.IDX_TIME;
import static com.hatopigeon.cubictimer.database.DatabaseHandler.IDX_TYPE;
import static com.hatopigeon.cubictimer.database.DatabaseHandler.ProgressListener;
import static com.hatopigeon.cubictimer.fragment.TimerFragmentMain.TIMER_PAGE;
import static com.hatopigeon.cubictimer.utils.PuzzleUtils.FORMAT_SINGLE;
import static com.hatopigeon.cubictimer.utils.PuzzleUtils.convertTimeToString;
import static com.hatopigeon.cubictimer.utils.TTIntent.ACTION_BLUETOOTH_CONNECT;
import static com.hatopigeon.cubictimer.utils.TTIntent.ACTION_TIMES_MODIFIED;
import static com.hatopigeon.cubictimer.utils.TTIntent.CATEGORY_TIME_DATA_CHANGES;
import static com.hatopigeon.cubictimer.utils.TTIntent.CATEGORY_UI_INTERACTIONS;
import static com.hatopigeon.cubictimer.utils.TTIntent.broadcast;

public class MainActivity extends AppCompatActivity
        implements ExportImportDialog.ExportImportCallbacks, PuzzleChooserDialog.PuzzleCallback {
    /**
     * Flag to enable debug logging for this class.
     */
    private static final boolean DEBUG_ME = false;

    /**
     * A "tag" to identify this class in log messages.
     */
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int DEBUG_ID         = 11;
    private static final int TIMER_ID         = 1;
    private static final int THEME_ID         = 2;
    private static final int SCHEME_ID        = 9;
    private static final int OLL_ID           = 6;
    private static final int PLL_ID           = 7;
    private static final int DONATE_ID        = 8;
    private static final int EXPORT_IMPORT_ID = 10;
    private static final int ABOUT_ID         = 4;
    private static final int SETTINGS_ID      = 5;
    private static final int TRAINER_OLL_ID      = 14;
    private static final int TRAINER_PLL_ID      = 15;


    private static final int REQUEST_SETTING           = 42;
    private static final int REQUEST_ABOUT             = 23;
    private static final int STORAGE_PERMISSION_CODE   = 11;

    private static final int EXPORT_BACKUP      = 50;
    private static final int EXPORT_EXTERNAL    = 51;
    private static final int IMPORT_BACKUP      = 60;
    private static final int IMPORT_EXTERNAL    = 61;
    private static final int IMPORT_CSTIMER_EXPORT = 62;
    private static final int IMPORT_CSTIMER_SESSION = 63;

    /**
     * The fragment tag identifying the export/import dialog fragment.
     */
    private static final String FRAG_TAG_EXIM_DIALOG = "export_import_dialog";

    // NOTE: Loader IDs used by fragments need to be unique within the context of an activity that
    // creates those fragments. Therefore, it is safer to define all of the IDs in the same place.

    /**
     * The loader ID for the loader that loads data presented in the statistics table on the timer
     * graph fragment and the summary statistics on the timer fragment.
     */
    public static final int STATISTICS_LOADER_ID = 101;

    /**
     * The loader ID for the loader that loads chart data presented in on the timer graph fragment.
     */
    public static final int CHART_DATA_LOADER_ID = 102;

    /**
     * The loader ID for the loader that loads the list of solve times for the timer list fragment.
     */
    public static final int TIME_LIST_LOADER_ID = 103;

    /**
     * The loader ID for the loader that loads the list of algorithms for the algorithm list
     * fragment.
     */
    public static final int ALG_LIST_LOADER_ID = 104;

    SmoothActionBarDrawerToggle mDrawerToggle;
    FragmentManager             fragmentManager;
    DrawerLayout                mDrawerLayout;

    private Drawer          mDrawer;

    private String mExportPuzzleType = "";
    private String mExportPuzzleCategory = "";
    private boolean mIsToArchive = false;

    /**
     * Sets drawer lock mode
     * {@code DrawerLayout.LOCK_MODE_LOCKED_CLOSED} for force closed and
     * {@code DrawerLayout.LOCK_MODE_LOCKED_UNDEFINED} for default behaviour
     */
    public void setDrawerLock(int lockMode) {
        mDrawer.getDrawerLayout().setDrawerLockMode(lockMode);
    }

    public void openDrawer() {
        mDrawer.openDrawer();
    }

    public void closeDrawer() {
        mDrawer.closeDrawer();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG_ME) Log.d(TAG, "onCreate(savedInstanceState="
                + savedInstanceState + "): " + this);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setTheme(ThemeUtils.getPreferredTheme());

        // Set text styling
        if (!Prefs.getString(R.string.pk_text_style, "default").equals("default")) {
            getTheme().applyStyle(ThemeUtils.getPreferredTextStyle(), true);
        }

        // Set navigation bar tint
        if (Prefs.getBoolean(R.string.pk_tint_navigation_bar, false)) {
            getTheme().applyStyle(R.style.TintedNavigationBar, true);
            // Set navigation bar icon tint
            if (ThemeUtils.fetchAttrBool(this, ThemeUtils.getPreferredTheme(), R.styleable.BaseTwistyTheme_isLightTheme)) {
                getTheme().applyStyle(R.style.LightNavBarIconStyle, true);
            }
        }

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        fragmentManager = getSupportFragmentManager();

        if (savedInstanceState == null) {
            fragmentManager
                .beginTransaction()
                .replace(R.id.main_activity_container, TimerFragmentMain.newInstance(PuzzleUtils.TYPE_333, "Normal", 5, "", TimerFragment.TIMER_MODE_TIMER, TrainerScrambler.TrainerSubset.OLL, TIMER_PAGE), "fragment_main")
                .commit();
        }

        handleDrawer(savedInstanceState);

        // Close drawer on back pressed
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                if (mDrawer.isDrawerOpen()) {
                    mDrawer.closeDrawer();
                    // consume and return
                    return;
                }

                // Not consumed -> temporarily disable this callback, delegate to the default back behavior,
                setEnabled(false);
                getOnBackPressedDispatcher().onBackPressed();
                // then re-enable it afterward so it can handle future back presses again
                setEnabled(true);
            }
        });
    }

    /* NOTE: Leaving this here (commented out) as it may be useful again (probably soon).
    @Override
    protected void onResume() {
        if (DEBUG_ME) Log.d(TAG, "onResume(): " + this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        // Method overridden just for logging. Tracing issues on return from "Settings".
        if (DEBUG_ME) Log.d(TAG, "onPause(): " + this);
        super.onPause();
    }

    @Override
    protected void onStart() {
        // Method overridden just for logging. Tracing issues on return from "Settings".
        if (DEBUG_ME) Log.d(TAG, "onStart(): " + this);
        super.onStart();
    }

    @Override
    protected void onStop() {
        // Method overridden just for logging. Tracing issues on return from "Settings".
        if (DEBUG_ME) Log.d(TAG, "onStop(): " + this);
        super.onStop();
    }
   */

    private void handleDrawer(Bundle savedInstanceState) {
        ImageView headerView = (ImageView) View.inflate(this, R.layout.drawer_header, null);

        //headerView.setImageDrawable(
        //       ThemeUtils.fetchTintedDrawable(this, R.drawable.menu_header, R.attr.colorPrimary));

        // Setup drawer
        DrawerBuilder drawerBuilder = new DrawerBuilder()
                .withActivity(this)
                .withDelayOnDrawerClose(-1)
                .withHeader(headerView)
                .addDrawerItems(
                        new PrimaryDrawerItem()
                                .withName(R.string.drawer_title_timer)
                                .withIcon(R.drawable.ic_outline_timer_24px)
                                .withIconTintingEnabled(true)
                                .withIdentifier(TIMER_ID),

                        new ExpandableDrawerItem()
                                .withName(R.string.drawer_title_trainer)
                                .withIcon(R.drawable.ic_outline_control_camera_24px)
                                .withSelectable(false)
                                .withIconTintingEnabled(true)
                                .withSubItems(
                                        new SecondaryDrawerItem()
                                                .withName(R.string.drawer_title_oll)
                                                .withLevel(2)
                                                .withIcon(R.drawable.ic_oll_black_24dp)
                                                .withIconTintingEnabled(true)
                                                .withIdentifier(TRAINER_OLL_ID),
                                        new SecondaryDrawerItem()
                                                .withName(R.string.drawer_title_pll)
                                                .withLevel(2)
                                                .withIcon(R.drawable.ic_pll_black_24dp)
                                                .withIconTintingEnabled(true)
                                                .withIdentifier(TRAINER_PLL_ID)),

                        new ExpandableDrawerItem()
                                .withName(R.string.title_algorithms)
                                .withIcon(R.drawable.ic_outline_library_books_24px)
                                .withSelectable(false)
                                .withIconTintingEnabled(true)
                                .withSubItems(
                                        new SecondaryDrawerItem()
                                                .withName(R.string.drawer_title_oll)
                                                .withLevel(2)
                                                .withIcon(R.drawable.ic_oll_black_24dp)
                                                .withIconTintingEnabled(true)
                                                .withIdentifier(OLL_ID),
                                        new SecondaryDrawerItem()
                                                .withName(R.string.drawer_title_pll)
                                                .withLevel(2)
                                                .withIcon(R.drawable.ic_pll_black_24dp)
                                                .withIconTintingEnabled(true)
                                                .withIdentifier(PLL_ID)),

                        new SectionDrawerItem()
                                .withName(R.string.drawer_title_other),

                        new PrimaryDrawerItem()
                                .withName(R.string.drawer_title_export_import)
                                .withIcon(R.drawable.ic_outline_folder_24px)
                                .withIconTintingEnabled(true)
                                .withSelectable(false)
                                .withIdentifier(EXPORT_IMPORT_ID),

                        new PrimaryDrawerItem()
                                .withName(R.string.drawer_title_changeTheme)
                                .withIcon(R.drawable.ic_outline_palette_24px)
                                .withIconTintingEnabled(true)
                                .withSelectable(false)
                                .withIdentifier(THEME_ID),

                        new PrimaryDrawerItem()
                                .withName(R.string.drawer_title_changeColorScheme)
                                .withIcon(R.drawable.ic_outline_format_paint_24px)
                                .withIconTintingEnabled(true)
                                .withSelectable(false)
                                .withIdentifier(SCHEME_ID),

                        new DividerDrawerItem(),

                        new PrimaryDrawerItem()
                                .withName(R.string.action_settings)
                                .withIcon(R.drawable.ic_outline_settings_24px)
                                .withIconTintingEnabled(true)
                                .withSelectable(false)
                                .withIdentifier(SETTINGS_ID),

                        new PrimaryDrawerItem()
                                .withName(R.string.drawer_about)
                                .withIcon(R.drawable.ic_outline_help_outline_24px)
                                .withIconTintingEnabled(true)
                                .withSelectable(false)
                                .withIdentifier(ABOUT_ID)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

                        boolean closeDrawer = true;

                        switch ((int) drawerItem.getIdentifier()) {
                            default:
                                closeDrawer = false;
                                break;
                            case TIMER_ID:
                                mDrawerToggle.runWhenIdle(new Runnable() {
                                    @Override
                                    public void run() {
                                        fragmentManager
                                                .beginTransaction()
                                                .replace(R.id.main_activity_container,
                                                         TimerFragmentMain.newInstance(PuzzleUtils.TYPE_333, "Normal", 5, "reset", TimerFragment.TIMER_MODE_TIMER, TrainerScrambler.TrainerSubset.PLL, TIMER_PAGE), "fragment_main")
                                                .commit();
                                    }
                                });
                                break;

                            case TRAINER_OLL_ID:
                                mDrawerToggle.runWhenIdle(new Runnable() {
                                    @Override
                                    public void run() {
                                        fragmentManager
                                                .beginTransaction()
                                                .replace(R.id.main_activity_container,
                                                         TimerFragmentMain.newInstance(TrainerScrambler.TrainerSubset.OLL.name(), "Normal", 5, "reset", TimerFragment.TIMER_MODE_TRAINER, TrainerScrambler.TrainerSubset.OLL, TIMER_PAGE), "fragment_main")
                                                .commit();
                                    }
                                });
                                break;

                            case TRAINER_PLL_ID:
                                mDrawerToggle.runWhenIdle(new Runnable() {
                                    @Override
                                    public void run() {
                                        fragmentManager
                                                .beginTransaction()
                                                .replace(R.id.main_activity_container,
                                                         TimerFragmentMain.newInstance(TrainerScrambler.TrainerSubset.PLL.name(), "Normal", 5, "reset", TimerFragment.TIMER_MODE_TRAINER, TrainerScrambler.TrainerSubset.PLL, TIMER_PAGE), "fragment_main")
                                                .commit();
                                    }
                                });
                                break;

                            case OLL_ID:
                                mDrawerToggle.runWhenIdle(new Runnable() {
                                    @Override
                                    public void run() {
                                        fragmentManager
                                                .beginTransaction()
                                                .replace(R.id.main_activity_container,
                                                         AlgListFragment.newInstance(DatabaseHandler.SUBSET_OLL),
                                                         "fragment_algs_oll")
                                                .commit();
                                    }
                                });
                                break;

                            case PLL_ID:
                                mDrawerToggle.runWhenIdle(new Runnable() {
                                    @Override
                                    public void run() {
                                        fragmentManager
                                                .beginTransaction()
                                                .replace(R.id.main_activity_container,
                                                         AlgListFragment.newInstance(DatabaseHandler.SUBSET_PLL),
                                                         "fragment_algs_pll")
                                                .commit();
                                    }
                                });
                                break;

                            case EXPORT_IMPORT_ID:
                                ExportImportDialog.newInstance()
                                        .show(fragmentManager, FRAG_TAG_EXIM_DIALOG);
                                break;

                            case THEME_ID:
                                ThemeSelectDialog.newInstance().show(fragmentManager, "theme_dialog");
                                break;

                            case SCHEME_ID:
                                SchemeSelectDialogMain.newInstance()
                                        .show(fragmentManager, "scheme_dialog");
                                break;

                            case SETTINGS_ID:
                                mDrawerToggle.runWhenIdle(new Runnable() {
                                    @Override
                                    public void run() {
                                        startActivityForResult(new Intent(
                                                                       getApplicationContext(), SettingsActivity.class),
                                                               REQUEST_SETTING);
                                    }
                                });
                                break;

                            case ABOUT_ID:
                                mDrawerToggle.runWhenIdle(new Runnable() {
                                    @Override
                                    public void run() {
                                        startActivityForResult(new Intent(getApplicationContext(),
                                                                          AboutActivity.class), REQUEST_ABOUT);
                                    }
                                });
                                break;

                            case DEBUG_ID:
                                if (BuildConfig.DEBUG) {
                                    Random rand = new Random();
                                    DatabaseHandler dbHandler = CubicTimer.getDBHandler();
                                    for (int i = 0; i < 10000; i++) {
                                        dbHandler.addSolve(new Solve(30000 + rand.nextInt(6000), "333",
                                                                     "|<<# DEBUG #>>|", 165165l+(i*10), "", 0, "", rand.nextBoolean()));
                                    }
                                }
                                break;
                        }
                        if (closeDrawer)
                            mDrawerLayout.closeDrawers();
                        return false;
                    }
                })
                .withSavedInstance(savedInstanceState);

        if (BuildConfig.DEBUG) {
            drawerBuilder.addDrawerItems(
                    new SectionDrawerItem()
                            .withName("DEBUG"),

                    new PrimaryDrawerItem()
                            .withName("DEBUG OPTION - ADD 10000 SOLVES")
                            .withIcon(R.drawable.ic_outline_help_outline_24px)
                            .withIconTintingEnabled(true)
                            .withSelectable(false)
                            .withIdentifier(DEBUG_ID)
            );
        }

        mDrawer = drawerBuilder.build();

        mDrawerLayout = mDrawer.getDrawerLayout();
        mDrawerToggle = new SmoothActionBarDrawerToggle(
                this, mDrawerLayout, null, R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        // pass through insets to the TimerFragmentMain
        mDrawerLayout.setFitsSystemWindows(false);
        ViewCompat.setOnApplyWindowInsetsListener(mDrawerLayout, (v, insets) -> insets);

        // padding setting for drawer
        InsetsUtils.applySafeInsetsPadding(mDrawer.getSlider(), true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (DEBUG_ME) Log.d(TAG, "onActivityResult(requestCode=" + requestCode
                + ", resultCode=" + resultCode + ", data=" + data + "): " + this);
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SETTING) {
            if (DEBUG_ME) Log.d(TAG, "  Returned from 'Settings'. Will recreate activity.");
            onRecreateRequired();
        } else if ((requestCode == EXPORT_BACKUP || requestCode == EXPORT_EXTERNAL)
                && resultCode == Activity.RESULT_OK) {
            if (data.getData() != null) {
                Uri uri = data.getData();
                Log.d(TAG, "EXPORT : " + uri.toString());
                Log.d(TAG, "EXPORT : " + mExportPuzzleType + "," + mExportPuzzleCategory);

                new ExportSolves(this,
                        (requestCode == EXPORT_BACKUP ? ExportImportDialog.EXIM_FORMAT_BACKUP
                                : ExportImportDialog.EXIM_FORMAT_EXTERNAL),
                        uri, mExportPuzzleType, mExportPuzzleCategory)
                        .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        } else if ((requestCode == IMPORT_BACKUP || requestCode == IMPORT_EXTERNAL
                || requestCode == IMPORT_CSTIMER_EXPORT || requestCode == IMPORT_CSTIMER_SESSION)
                && resultCode == Activity.RESULT_OK) {
            if (data.getData() != null) {
                Uri uri = data.getData();
                Log.d(TAG, "IMPORT : " + uri.toString());
                Log.d(TAG, "IMPORT : " + mExportPuzzleType + "," + mExportPuzzleCategory);

                int fileFormat = ExportImportDialog.EXIM_FORMAT_BACKUP;
                switch(requestCode) {
                    case IMPORT_BACKUP:
                        fileFormat = ExportImportDialog.EXIM_FORMAT_BACKUP;
                        break;
                    case IMPORT_EXTERNAL:
                        fileFormat = ExportImportDialog.EXIM_FORMAT_EXTERNAL;
                        break;
                    case IMPORT_CSTIMER_EXPORT:
                        fileFormat = ExportImportDialog.EXIM_FORMAT_CSTIMER_EXPORT;
                        break;
                    case IMPORT_CSTIMER_SESSION:
                        fileFormat = ExportImportDialog.EXIM_FORMAT_CSTIMER_SESSION;
                        break;
                }

                new ImportSolves(this, fileFormat,
                        uri, mExportPuzzleType, mExportPuzzleCategory, mIsToArchive)
                        .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    }

    /**
     * Handles the need to recreate this activity due to a major change affecting the activity and
     * its fragments. For example, if the theme is changed by {@link ThemeSelectDialog}, or if
     * unknown changes have been made to the preferences in {@link SettingsActivity}.
     */
    public void onRecreateRequired() {
        if (DEBUG_ME) Log.d(TAG, "onRecreationRequired(): " + this);

        // IMPORTANT: If this is not posted to the message queue, i.e., if "recreate()" is simply
        // called directly from "onRecreateRequired()" (or even if a flag is set here and
        // "recreate()" is called later from "onResume()", the recreation goes very wrong. After
        // the newly created activity instance calls "onResume()" it immediately calls "onPause()"
        // for no apparent reason whatsoever. The activity is clearly not "paused", as it the UI is
        // perfectly responsive. However, the next time it actually needs to pause, an exception is
        // logged complaining, "Performing pause of activity that is not resumed".
        //
        // Perhaps the issue is caused by an incorrect synchronisation of the destruction of the
        // old activity and the creation of the new activity. Whatever, simply posting the
        // "recreate()" call here seems to fix this. After posting, the (old) activity will
        // continue on and reach "onResume()" before then going through an orderly shutdown and
        // the new activity will be created and settle properly at "onResume()".
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (DEBUG_ME) Log.d(TAG, "  Activity.recreate() NOW!: " + this);
                recreate();
            }
        });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleUtils.updateLocale(newBase));
    }

    @Override
    protected void onDestroy() {
        if (DEBUG_ME) Log.d(TAG, "onDestroy(): " + this);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_overview, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (DEBUG_ME) Log.d(TAG, "onSaveInstanceState(): " + this);
        outState = mDrawer.saveInstanceState(outState);
        //outState.putBoolean(OPEN_EXPORT_IMPORT_DIALOG, openExportImportDialog);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onImportSolveTimes(int fileFormat, String puzzleType, String puzzleCategory, boolean isToArchive) {

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/*");

        mExportPuzzleType = puzzleType;
        mExportPuzzleCategory = puzzleCategory;
        mIsToArchive = isToArchive;

        if (fileFormat == ExportImportDialog.EXIM_FORMAT_BACKUP) {
            startActivityForResult(intent, IMPORT_BACKUP);
        } else if (fileFormat == ExportImportDialog.EXIM_FORMAT_EXTERNAL) {
            startActivityForResult(intent, IMPORT_EXTERNAL);
        } else if (fileFormat == ExportImportDialog.EXIM_FORMAT_CSTIMER_EXPORT) {
            startActivityForResult(intent, IMPORT_CSTIMER_EXPORT);
        } else if (fileFormat == ExportImportDialog.EXIM_FORMAT_CSTIMER_SESSION) {
            startActivityForResult(intent, IMPORT_CSTIMER_SESSION);
        }
    }

    @Override
    public void onExportSolveTimes(int fileFormat, String puzzleType, String puzzleCategory) {
        if (!StoreUtils.isExternalStorageWritable()) {
            return;
        }

        if (fileFormat == ExportImportDialog.EXIM_FORMAT_BACKUP) {
            // Expect that all other parameters are null, otherwise something is very wrong.
            if (puzzleType != null || puzzleCategory != null) {
                throw new RuntimeException("Bug in the export code for the back-up format!");
            }

            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TITLE, ExportImportUtils.getBackupFileNameForExport());

            mExportPuzzleType = "";
            mExportPuzzleCategory = "";

            startActivityForResult(intent, EXPORT_BACKUP);
        } else if (fileFormat == ExportImportDialog.EXIM_FORMAT_EXTERNAL) {
            // Expect that all other parameters are non-null, otherwise something is very wrong.
            if (puzzleType == null || puzzleCategory == null) {
                throw new RuntimeException("Bug in the export code for the external format!");
            }

            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TITLE,
                    ExportImportUtils.getExternalFileNameForExport(puzzleType, puzzleCategory));

            mExportPuzzleType = puzzleType;
            mExportPuzzleCategory = puzzleCategory;

            startActivityForResult(intent, EXPORT_EXTERNAL);
        } else {
            Log.e(TAG, "Unknown export file format: " + fileFormat);
        }
    }

    /**
     * Handles the call-back from a fragment when a puzzle type and/or category are selected. This
     * is used for communication between the export/import fragments. The "source" fragment should
     * set the {@code tag} to the value of the fragment tag that this activity uses to identify
     * the "destination" fragment. This activity will then forward this notification to that
     * fragment, which is expected to implement this same interface method.
     */
    @Override
    public void onPuzzleSelected(
            @NonNull String tag, @NonNull String puzzleType, @NonNull String puzzleCategory) {
        // This "relay" scheme ensures that this activity is not embroiled in the gory details of
        // what the "destinationFrag" wanted with the puzzle type/category.
        final Fragment destinationFrag = fragmentManager.findFragmentByTag(tag);

        if (destinationFrag instanceof PuzzleChooserDialog.PuzzleCallback) {
            ((PuzzleChooserDialog.PuzzleCallback) destinationFrag)
                    .onPuzzleSelected(tag, puzzleType, puzzleCategory);
        } else {
            // This is not expected unless there is a bug to be fixed.
            Log.e(TAG, "onFileSelection(): Unknown or incompatible fragment: " + tag);
        }
    }

    private static class ExportSolves extends AsyncTask<Void, Integer, Boolean> {

        private final Activity  mContext;
        private final int      mFileFormat;
        private final Uri      mUri;
        private final String   mPuzzleType;
        private final String   mPuzzleCategory;

        private MaterialDialog mProgressDialog;

        /**
         * Creates a new task for exporting solve times to a file.
         *
         * @param context
         *     The context required to access resources and to report progress.
         * @param fileFormat
         *     The solve file format, must be {@link ExportImportDialog#EXIM_FORMAT_EXTERNAL}, or
         *     {@link ExportImportDialog#EXIM_FORMAT_BACKUP}.
         * @param uri
         *     The uri to which to export the solve times.
         * @param puzzleType
         *     The type of the puzzle whose times will be exported. This is required when
         *     {@code fileFormat} is {@code EXIM_FORMAT_EXTERNAL}. For {@code EXIM_FORMAT_BACKUP},
         *     it may be {@code null}, as it will not be used.
         * @param puzzleCategory
         *     The category (subtype) of the puzzle whose times will be exported. Required when
         *     {@code fileFormat} is {@code EXIM_FORMAT_EXTERNAL}. For {@code EXIM_FORMAT_BACKUP},
         *     it may be {@code null}, as it will not be used.
         */
        public ExportSolves(Activity context, int fileFormat, Uri uri,
                            String puzzleType, String puzzleCategory) {
            mContext = context;
            mFileFormat = fileFormat;
            mUri = uri;
            mPuzzleType = puzzleType;
            mPuzzleCategory = puzzleCategory;
        }

        @Override
        protected void onPreExecute() {
            mProgressDialog = ThemeUtils.roundDialog(mContext, new  MaterialDialog.Builder(mContext)
                .content(R.string.export_progress_title)
                .progress(false, 0, true)
                .cancelable(false)
                .build());
            mProgressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (mProgressDialog.isShowing()) {
                if (values.length > 1) {
                    // values[1] is the number of solve times, which could legitimately be zero.
                    // Do not set max. to zero or it will display "NaN".
                    mProgressDialog.setMaxProgress(Math.max(values[1], 1));
                }
                mProgressDialog.setProgress(values[0]);
            }
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            Boolean returnCode;
            int exports = 0;

            try {
                final DatabaseHandler handler = CubicTimer.getDBHandler();
                final OutputStream os = mContext.getContentResolver().openOutputStream(mUri);
                final OutputStreamWriter out = new OutputStreamWriter(os);
                final CSVWriter csvWriter = new CSVWriter(out, ';', '"', '\\');

                if (mFileFormat == ExportImportDialog.EXIM_FORMAT_BACKUP) {
                    Cursor cursor = handler.getAllSolves();

                    try {
                        publishProgress(0, cursor.getCount());
                        csvWriter.writeNext(new String[] {"Puzzle", "Category", "Time(millis)",
                                "Date(millis)", "Scramble", "Penalty", "Comment"});

                        while (cursor.moveToNext()) {
                            csvWriter.writeNext(new String[] {
                                    cursor.getString(IDX_TYPE),
                                    cursor.getString(IDX_SUBTYPE),
                                    String.valueOf(cursor.getLong(IDX_TIME)),
                                    String.valueOf(cursor.getLong(IDX_DATE)),
                                    cursor.getString(IDX_SCRAMBLE),
                                    String.valueOf(cursor.getInt(IDX_PENALTY)),
                                    cursor.getString(IDX_COMMENT)
                            });
                            exports++;
                            publishProgress(exports);
                        }
                    } finally {
                        cursor.close();
                        csvWriter.close();
                        out.close();
                    }
                    returnCode = true;
                } else if (mFileFormat == ExportImportDialog.EXIM_FORMAT_EXTERNAL) {
                    Cursor cursor = handler.getAllSolvesFrom(mPuzzleType, mPuzzleCategory);

                    try {
                        publishProgress(0, cursor.getCount());

                        while (cursor.moveToNext()) {
                            if (Solve.getPenalty(cursor.getInt(IDX_PENALTY)) == PuzzleUtils.PENALTY_DNF) {
                                csvWriter.writeNext(new String[] {
                                        PuzzleUtils.convertTimeToString(cursor.getLong(IDX_TIME), PuzzleUtils.FORMAT_SINGLE, mPuzzleType),
                                        cursor.getString(IDX_SCRAMBLE),
                                        new DateTime(cursor.getLong(IDX_DATE)).toString(),
                                        "DNF"
                                });
                            } else {
                                csvWriter.writeNext(new String[] {
                                        PuzzleUtils.convertTimeToString(cursor.getLong(IDX_TIME), PuzzleUtils.FORMAT_SINGLE, mPuzzleType),
                                        cursor.getString(IDX_SCRAMBLE),
                                        new DateTime(cursor.getLong(IDX_DATE)).toString(),
                                        ""
                                });
                            }
                            exports++;
                            publishProgress(exports);
                        }
                    } finally {
                        cursor.close();
                        csvWriter.close();
                        out.close();
                    }
                    returnCode = true;
                } else {
                    Log.e(TAG, "Unknown export file format: " + mFileFormat);
                    returnCode = false;
                }
            } catch (IOException e) {
                returnCode = false;
                Log.d("ERROR", "IOException: " + e.getMessage());
            }

            return returnCode;
        }

        @Override
        protected void onPostExecute(Boolean isExported) {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.setActionButton(DialogAction.POSITIVE, R.string.action_done);

                if (isExported) {
                    mProgressDialog.setContent(
                            Html.fromHtml(mContext.getString(R.string.export_progress_complete_wo_to)));
                    // Optional share action
                    mProgressDialog.setActionButton(DialogAction.NEUTRAL, R.string.list_options_item_share);
                    mProgressDialog.getBuilder().onNeutral((dialog, which) -> {
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);

                        shareIntent.setAction(Intent.ACTION_SEND);
                        shareIntent.putExtra(Intent.EXTRA_STREAM, mUri);
                        shareIntent.setType("application/octet-stream");

                        // FileProvider can sometimes crash devices lower than Lollipop
                        // due to permission issues, so we have to do some magic to the intent
                        // This is explained in a Medium post by @quiro91
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                            shareIntent.setClipData(ClipData.newRawUri("", mUri));
                            shareIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        }

                        mContext.startActivity(Intent.createChooser(shareIntent, "Share"));
                    });
                } else {
                    mProgressDialog.setContent(R.string.export_progress_error);
                }
            }
        }
    }

    private static class ImportSolves extends AsyncTask<Void, Integer, Void> {

        private final Context  mContext;
        private final int      mFileFormat;
        private final Uri      mUri;
        private final String   mPuzzleType;
        private final String   mPuzzleCategory;
        private final boolean  mIsToArchive;

        private MaterialDialog mProgressDialog;
        private int parseErrors = 0;
        private int duplicates  = 0;
        private int successes   = 0;

        /**
         * Creates a new task for importing solve times from a file.
         *
         * @param context
         *     The context required to access resources and to report progress.
         * @param uri
         *     The file uri from which to import the solve times.
         * @param fileFormat
         *     The solve file format, must be {@link ExportImportDialog#EXIM_FORMAT_EXTERNAL}, or
         *     {@link ExportImportDialog#EXIM_FORMAT_BACKUP}.
         * @param puzzleType
         *     The type of the puzzle whose times will be imported. This is required when
         *     {@code fileFormat} is {@code EXIM_FORMAT_EXTERNAL}. For {@code EXIM_FORMAT_BACKUP},
         *     it may be {@code null}, as it will not be used.
         * @param puzzleCategory
         *     The category (subtype) of the puzzle whose times will be imported. Required when
         *     {@code fileFormat} is {@code EXIM_FORMAT_EXTERNAL}. For {@code EXIM_FORMAT_BACKUP},
         *     it may be {@code null}, as it will not be used.
         * @param isToArchive
         *     True  : import to archive
         *     False : import to current
         */
        public ImportSolves(Context context, int fileFormat, Uri uri,
                            String puzzleType, String puzzleCategory, boolean isToArchive) {
            mContext = context;
            mFileFormat = fileFormat;
            mUri = uri;
            mPuzzleType = puzzleType;
            mPuzzleCategory = puzzleCategory;
            mIsToArchive = isToArchive;
        }

        @Override
        protected void onPreExecute() {
            mProgressDialog = ThemeUtils.roundDialog(mContext, new MaterialDialog.Builder(mContext)
                .content(R.string.import_progress_title)
                .progress(false, 0, true)
                .cancelable(false)
                .build());
            mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (mProgressDialog.isShowing()) {
                if (values.length > 1) {
                    // values[1] is the number of solve times, which could legitimately be zero.
                    // Do not set max. to zero or it will display "NaN".
                    mProgressDialog.setMaxProgress(Math.max(values[1], 1));
                }
                mProgressDialog.setProgress(values[0]);
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            List<Solve> solveList = new ArrayList<>();

            try {
                InputStream is = mContext.getContentResolver().openInputStream(mUri);
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);

                if (mFileFormat == ExportImportDialog.EXIM_FORMAT_BACKUP) {
                    CSVReader csvReader = new CSVReader(br, ';', '"', '\\', 0, true);
                    String[] line;

                    // throw away the header
                    csvReader.readNext();

                    while ((line = csvReader.readNext()) != null) {
                        try {
                            solveList.add(new Solve(
                                Long.parseLong(line[2]), line[0], line[1], Long.parseLong(line[3]),
                                line[4], Integer.parseInt(line[5]), line[6], mIsToArchive));
                        } catch (Exception e) {
                            parseErrors++;
                        }
                    }
                } else if (mFileFormat == ExportImportDialog.EXIM_FORMAT_EXTERNAL) {
                    CSVReader csvReader = new CSVReader(br, ';', '"', '\\', 0, true);
                    String[] line;
                    final long now = DateTime.now().getMillis();

                    while ((line = csvReader.readNext()) != null) {
                        if (line.length <= 4) {
                            try {
                                Log.d("IMPORTING EXTERNAL", "time: " + line[0]);

                                long time;
                                if (mPuzzleType.equals(PuzzleUtils.TYPE_333MBLD)) {
                                    time = PuzzleUtils.parseMbldRecord(line[0]);
                                } else {
                                    time = PuzzleUtils.parseAddedTime(line[0]);
                                }
                                String scramble = "";
                                long date = now;
                                int penalty = PuzzleUtils.NO_PENALTY;

                                if (line.length >= 2) {
                                    scramble = line[1];
                                }
                                if (line.length >= 3) {
                                    try {
                                        date = DateTime.parse(line[2]).getMillis();
                                    } catch (Exception e) {
                                        // "date" remains equal to "now".
                                        e.printStackTrace();
                                    }
                                }
                                // Optional fourth field (index 3) may contain "DNF". If it is
                                // something else, ignore it.
                                if (line.length >= 4 && "DNF".equals(line[3])) {
                                    penalty = PuzzleUtils.PENALTY_DNF;
                                }

                                solveList.add(new Solve(
                                        time, mPuzzleType, mPuzzleCategory,
                                        date, scramble, penalty, "", mIsToArchive));
                            } catch (Exception e) {
                                parseErrors++;
                            }
                        }  else {
                            parseErrors++;
                        }
                    }
                } else if (mFileFormat == ExportImportDialog.EXIM_FORMAT_CSTIMER_EXPORT) {
                    try {
                        String strJSON = br.readLine();
                        JSONObject json = new JSONObject(strJSON);
                        String strSessionData = json.getJSONObject("properties").getString("sessionData");
                        JSONObject jsonSessionDatas = new JSONObject(strSessionData);

                        Iterator<String> keys = jsonSessionDatas.keys();
                        while (keys.hasNext()) {
                            try {
                                // parse session setting (session name, scramble type)
                                String key = keys.next();
                                JSONObject jsonSessionData = jsonSessionDatas.getJSONObject(key);
                                Log.d("IMPORTING EXTERNAL", "SessionData" + key + " : " + jsonSessionData);

                                String strName = jsonSessionData.getString("name");
                                String strScrType;
                                try {
                                    strScrType = jsonSessionData.getJSONObject("opt").getString("scrType");
                                } catch (org.json.JSONException e) {
                                    strScrType = "333";
                                }

                                // convert csTimer scramble type, session name to Cubic Timer puzzleType, puzzleCategory
                                String puzzleType = PuzzleUtils.convertCstimerPuzzleType(strScrType);
                                String puzzleCategory = strName;
                                if (puzzleType == PuzzleUtils.TYPE_OTHER)
                                    puzzleCategory = strScrType + "_" + puzzleCategory;
                                Log.d("IMPORTING EXTERNAL", "SessionData" + key + " : " + strScrType + "," + strName + " -> " + puzzleType + "," + puzzleCategory);

                                // open session record data
                                String strSession = "session" + key;
                                JSONArray jsonSession;
                                try {
                                    jsonSession = json.getJSONArray(strSession);
                                } catch (org.json.JSONException e) {
                                    continue;
                                }

                                for (int i = 0; i < jsonSession.length(); i++) {
                                    try {
                                        JSONArray jsonRecord = jsonSession.getJSONArray(i);

                                        // time in milli-seconds at times[1]
                                        JSONArray jsonTimes = jsonRecord.getJSONArray(0);
                                        long time = jsonTimes.getLong(1);

                                        // penalty is recorded at times[0]
                                        //  2000 : +2
                                        //  -1   : DNF
                                        int rawPenalty = jsonTimes.getInt(0);
                                        int penalty = PuzzleUtils.NO_PENALTY;
                                        if (rawPenalty == 2000) {
                                            penalty = PuzzleUtils.PENALTY_PLUSTWO;
                                            time += 2000;
                                        } else if (rawPenalty == -1) {
                                            penalty = PuzzleUtils.PENALTY_DNF;
                                        }

                                        // multi-phase result is recorded at times[2] - times[length-1] by reverse order
                                        // It is recorded as cumulative time
                                        // Save these phase times as comment
                                        String strPhase = "";
                                        if (jsonTimes.length() > 2) {
                                            jsonTimes.put(0L);
                                            for (int j = jsonTimes.length() - 2; j > 0; j--) {
                                                long phase = jsonTimes.getLong(j) - jsonTimes.getLong(j + 1);
                                                int phaseNum = jsonTimes.length() - j - 1;
                                                if (phaseNum != 1) strPhase += "\n";
                                                strPhase += "P" + phaseNum + ": "
                                                        + convertTimeToString(phase, FORMAT_SINGLE, PuzzleUtils.TYPE_333);
                                                ;
                                            }
                                        }

                                        String scramble = jsonRecord.getString(1);
                                        // adjust scramble text to generate scramble image by tnoodle-lib
                                        if (puzzleType.equals(PuzzleUtils.TYPE_SQUARE1))
                                            scramble = scramble.replaceAll("/", " /").replaceAll("^ ", "");
                                        else if (puzzleType.equals(PuzzleUtils.TYPE_MEGA))
                                            scramble = scramble.replaceAll("  ", "");

                                        String comment = strPhase;
                                        String rawComment = jsonRecord.getString(2);

                                        // concatenate multi-pahse result as comment and comment
                                        if (!comment.isEmpty() && !rawComment.isEmpty())
                                            comment = comment + "\n";
                                        comment = comment + rawComment;

                                        // date is recorded in seconds of unix time
                                        long date = jsonRecord.getLong(3) * 1000;
                                        Log.d("IMPORTING EXTERNAL", "Record : " + i + "," + penalty + "," + time + "," + scramble + "," + comment + "," + date);

                                        solveList.add(new Solve(
                                                time, puzzleType, puzzleCategory, date,
                                                scramble, penalty, comment, mIsToArchive));
                                    } catch (Exception e ) {
                                        parseErrors++;
                                    }
                                }
                            } catch (org.json.JSONException e) {
                                parseErrors++;
                            }
                        }
                    } catch (org.json.JSONException e) {
                        parseErrors++;
                    }
                } else if (mFileFormat == ExportImportDialog.EXIM_FORMAT_CSTIMER_SESSION) {
                    CSVReader csvReader = new CSVReader(br, ';', '"', '\\', 1, false);
                    String[] line;

                    while ((line = csvReader.readNext()) != null) {
                        if (line.length >= 6) {
                            try {
                                Log.d("IMPORTING EXTERNAL", "csTimer: " + line[1]);

                                // csTimer session csv
                                // No.;Time;Comment;Scramble;Date;P.1;
                                int penalty = PuzzleUtils.NO_PENALTY;
                                if (line[1].contains("DNF")) {
                                    penalty = PuzzleUtils.PENALTY_DNF;
                                } else if (line[1].contains("+")) {
                                    penalty = PuzzleUtils.PENALTY_PLUSTWO;
                                }

                                String strTime = line[1].replaceAll("DNF\\(|\\)|\\+", "");
                                long time = PuzzleUtils.parseAddedTime(strTime);

                                String strDate = line[4].replaceAll(" ", "T");
                                long date = DateTime.parse(strDate).getMillis();

                                String comment = "";
                                if (line.length >= 7 && !line[6].isEmpty()) { // if session csv has multi-phase record and this record has at least 2 phases
                                    // parse phase time and add to comment
                                    for (int i = 5; i < line.length; i++) {
                                        if (!line[i].isEmpty()) {
                                            if (i != 5) comment = comment + "\n";
                                            comment = comment + "P" + (i - 4) + ": " + line[i];
                                        }
                                    }
                                }
                                if (!line[2].isEmpty() && !comment.isEmpty())
                                    comment = comment + "\n";
                                comment = comment + line[2];

                                // adjust scramble text to generate scramble image by tnoodle-lib
                                String scramble = line[3];
                                if (mPuzzleType.equals(PuzzleUtils.TYPE_SQUARE1))
                                    scramble = scramble.replaceAll("/", " /").replaceAll("^ ", "");
                                else if (mPuzzleType.equals(PuzzleUtils.TYPE_MEGA))
                                    scramble = scramble.replaceAll("  ", "");

                                solveList.add(new Solve(
                                        time, mPuzzleType, mPuzzleCategory,
                                        date, scramble, penalty, comment, mIsToArchive));
                            } catch (Exception e) {
                                parseErrors++;
                            }
                        } else {
                            parseErrors++;
                        }
                    }
                } else {
                    Log.e(TAG, "Unknown import file format: " + mFileFormat);
                }

                final DatabaseHandler handler = CubicTimer.getDBHandler();

                // Perform a bulk insertion of the solves.
                successes = handler.addSolves(mFileFormat, solveList, new ProgressListener() {
                            @Override
                            public void onProgress(int numCompleted, int total) {
                                publishProgress(numCompleted, total);
                            }
                        });
                duplicates = solveList.size() - successes;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.setActionButton(DialogAction.POSITIVE, R.string.action_done);
                mProgressDialog.setContent(Html.fromHtml(
                        mContext.getString(R.string.import_progress_content)
                        + "<br><br><small><tt>"
                        + "<b>" + successes + "</b> "
                        + mContext.getString(R.string.import_progress_content_successful_imports)
                        + "<br><b>" + duplicates + "</b> "
                        + mContext.getString(R.string.import_progress_content_ignored_duplicates)
                        + "<br><b>" + parseErrors + "</b> "
                        + mContext.getString(R.string.import_progress_content_errors)
                        + "</small></tt>"));
            }
            broadcast(CATEGORY_TIME_DATA_CHANGES, ACTION_TIMES_MODIFIED);
        }
    }

    // So the drawer doesn't lag when closing
    private class SmoothActionBarDrawerToggle extends ActionBarDrawerToggle {

        private Runnable runnable;

        public SmoothActionBarDrawerToggle(Activity activity, DrawerLayout drawerLayout, Toolbar toolbar, int openDrawerContentDescRes, int closeDrawerContentDescRes) {
            super(activity, drawerLayout, toolbar, openDrawerContentDescRes, closeDrawerContentDescRes);
        }

        @Override
        public void onDrawerStateChanged(int newState) {
            super.onDrawerStateChanged(newState);
            if (runnable != null && newState == DrawerLayout.STATE_IDLE) {
                runnable.run();
                runnable = null;
            }
        }

        public void runWhenIdle(Runnable runnable) {
            this.runnable = runnable;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult");
        switch(requestCode) {
            case TimerFragment.REQUEST_BLE_PERMISSION:
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                    broadcast(CATEGORY_UI_INTERACTIONS, ACTION_BLUETOOTH_CONNECT);
                }  else {
                    // Explain to the user that the feature is unavailable because
                    // the feature requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.
                    MaterialDialog dialog = ThemeUtils.roundDialog(this, new MaterialDialog.Builder(this)
                            .title(getString(R.string.ble_permission2_title))
                            .content(getString(R.string.ble_permission2_content))
                            .positiveText(getString(R.string.ble_permission2_OK))
                            .show());
                }
                break;
        }
    }
}

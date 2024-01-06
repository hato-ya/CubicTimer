package com.hatopigeon.cubictimer.fragment;


import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.os.Process;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;

import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hatopigeon.cubicify.R;
import com.hatopigeon.cubicify.BuildConfig;
import com.hatopigeon.cubictimer.CubicTimer;
import com.hatopigeon.cubictimer.database.DatabaseHandler;
import com.hatopigeon.cubictimer.fragment.dialog.AddTimeDialog;
import com.hatopigeon.cubictimer.fragment.dialog.BottomSheetDetailDialog;
import com.hatopigeon.cubictimer.fragment.dialog.CommentDialog;
import com.hatopigeon.cubictimer.items.Solve;
import com.hatopigeon.cubictimer.layout.ChronometerMilli;
import com.hatopigeon.cubictimer.listener.OnBackPressedInFragmentListener;
import com.hatopigeon.cubictimer.puzzle.TrainerScrambler;
import com.hatopigeon.cubictimer.solver.RubiksCubeOptimalCross;
import com.hatopigeon.cubictimer.solver.RubiksCubeOptimalXCross;
import com.hatopigeon.cubictimer.stats.Statistics;
import com.hatopigeon.cubictimer.stats.StatisticsCache;
import com.hatopigeon.cubictimer.utils.CountdownWarning;
import com.hatopigeon.cubictimer.utils.DefaultPrefs;
import com.hatopigeon.cubictimer.utils.Prefs;
import com.hatopigeon.cubictimer.utils.PuzzleUtils;
import com.hatopigeon.cubictimer.utils.ScrambleGenerator;
import com.hatopigeon.cubictimer.utils.TTIntent;
import com.hatopigeon.cubictimer.utils.ThemeUtils;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;
import com.skyfishjy.library.RippleBackground;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.callback.DataReceivedCallback;
import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

import static com.hatopigeon.cubictimer.stats.AverageCalculatorSuper.tr;
import static com.hatopigeon.cubictimer.utils.PuzzleUtils.FORMAT_SINGLE;
import static com.hatopigeon.cubictimer.utils.PuzzleUtils.FORMAT_STATS;
import static com.hatopigeon.cubictimer.utils.PuzzleUtils.NO_PENALTY;
import static com.hatopigeon.cubictimer.utils.PuzzleUtils.PENALTY_DNF;
import static com.hatopigeon.cubictimer.utils.PuzzleUtils.PENALTY_PLUSTWO;
import static com.hatopigeon.cubictimer.utils.PuzzleUtils.TYPE_333;
import static com.hatopigeon.cubictimer.utils.PuzzleUtils.convertTimeToString;
import static com.hatopigeon.cubictimer.utils.PuzzleUtils.isTimeDisabled;
import static com.hatopigeon.cubictimer.utils.TTIntent.ACTION_BLUETOOTH_CONNECT;
import static com.hatopigeon.cubictimer.utils.TTIntent.ACTION_BLUETOOTH_CONNECTED;
import static com.hatopigeon.cubictimer.utils.TTIntent.ACTION_BLUETOOTH_DISCONNECTED;
import static com.hatopigeon.cubictimer.utils.TTIntent.ACTION_COMMENT_ADDED;
import static com.hatopigeon.cubictimer.utils.TTIntent.ACTION_GENERATE_SCRAMBLE;
import static com.hatopigeon.cubictimer.utils.TTIntent.ACTION_SCRAMBLE_MODIFIED;
import static com.hatopigeon.cubictimer.utils.TTIntent.ACTION_SCROLLED_PAGE;
import static com.hatopigeon.cubictimer.utils.TTIntent.ACTION_TIMER_STARTED;
import static com.hatopigeon.cubictimer.utils.TTIntent.ACTION_TIMER_STOPPED;
import static com.hatopigeon.cubictimer.utils.TTIntent.ACTION_TIMES_MODIFIED;
import static com.hatopigeon.cubictimer.utils.TTIntent.ACTION_TIME_ADDED;
import static com.hatopigeon.cubictimer.utils.TTIntent.ACTION_TIME_ADDED_MANUALLY;
import static com.hatopigeon.cubictimer.utils.TTIntent.ACTION_TOOLBAR_RESTORED;
import static com.hatopigeon.cubictimer.utils.TTIntent.BroadcastBuilder;
import static com.hatopigeon.cubictimer.utils.TTIntent.CATEGORY_TIME_DATA_CHANGES;
import static com.hatopigeon.cubictimer.utils.TTIntent.CATEGORY_UI_INTERACTIONS;
import static com.hatopigeon.cubictimer.utils.TTIntent.TTFragmentBroadcastReceiver;
import static com.hatopigeon.cubictimer.utils.TTIntent.broadcast;
import static com.hatopigeon.cubictimer.utils.TTIntent.registerReceiver;
import static com.hatopigeon.cubictimer.utils.TTIntent.unregisterReceiver;

public class TimerFragment extends BaseFragment
        implements OnBackPressedInFragmentListener, StatisticsCache.StatisticsObserver
        , SerialInputOutputManager.Listener {


    // Specifies the timer mode
    // i.e: Trainer mode generates only trainer scrambles, and changes the puzzle select spinner
    // Can be used for other features in the future
    public static final String TIMER_MODE_TIMER = "TIMER_MODE_TIMER";
    public static final String TIMER_MODE_TRAINER = "TIMER_MODE_TRAINER";

    /**
     * Flag to enable debug logging for this class.
     */
    private static final boolean DEBUG_ME = true;

    /**
     * A "tag" to identify this class in log messages.
     */
    private static final String TAG = TimerFragment.class.getSimpleName();

    private static final String PUZZLE = "puzzle";
    private static final String PUZZLE_SUBTYPE = "puzzle_type";
    private static final String MBLD_NUM = "mbld_num";
    private static final String TRAINER_SUBSET = "trainer_subset";
    private static final String TIMER_MODE = "timer_mode";
    private static final String SCRAMBLE = "scramble";
    private static final String HAS_STOPPED_TIMER_ONCE = "has_stopped_timer_once";


    /**
     * The time delay in milliseconds before starting the chronometer if the hold-for-start
     * preference is set.
     */
    private static final long HOLD_FOR_START_DELAY = 500L;

    private String currentPuzzle;
    private String currentPuzzleCategory;
    private TrainerScrambler.TrainerSubset currentSubset;
    private int currentMbldNum = 7;

    /**
     * The last generated scramble, related to the current solve. When the timer is started,
     * the timer will generate a new scramble, but it will be saved in realScramble.
     */
    private String currentScramble = "";

    /**
     * The scramble that is currently being shown to the user. MAY NOT BE currentScramble!
     */
    private String realScramble = null;

    private Solve currentSolve = null;

    CountDownTimer countdown;
    boolean countingDown = false;

    CountdownWarning firstWarning;
    CountdownWarning secondWarning;
    CountdownWarning timeLimitWarning;

    private Context mContext;

    // True If the show toolbar animation is done
    boolean animationDone = true;

    // True if the user has pressed the chronometer for long enough for it to start
    boolean isReady = false;

    // True If the user has holdEnabled and held the DNF at the last second
    boolean holdingDNF;

    // Checks if the chronometer is running. Has to be public so the main fragment can access it
    public boolean isRunning = false;

    // Locks the chronometer so it doesn't start before a scramble sequence is generated
    boolean isLocked = true;

    // True If the chronometer has just been canceled
    private boolean isCanceled;

    // True If the scrambler is done calculating and can calculate a new hint.
    private boolean canShowHint = false;

    // Animation duration for all timer items
    private int mAnimationDuration;

    private ScrambleGenerator generator;

    private GenerateScrambleSequence scrambleGeneratorAsync;
    private GetOptimalCross optimalCrossAsync;

    private int currentPenalty = NO_PENALTY;

    /**
     * Specifies the current TimerMode
     */
    private String currentTimerMode;

    private Animator mCurrentAnimator;

    private Unbinder mUnbinder;

    // Holds the localized strings related to each detail statistic, in order:
    // Ao5, Ao12, Ao50, Ao100, Deviation, Mean, Best, Count
    private String detailTextNamesArray[] = new String[8];

    // Stack Timer support
    private static final String INTENT_ACTION_GRANT_USB = BuildConfig.APPLICATION_ID + ".GRANT_USB";
    private SerialInputOutputManager usbIoManager;
    private UsbSerialPort usbSerialPort;
    private StringBuilder stackTimerString;
    private final Handler mainLooper;
    private boolean isExternalTimer;
    private String previousTimerString = "000000";
    private boolean isSerialConnected = false;

    // Smart Timer support
    public static final int REQUEST_BLE_PERMISSION = 9801;
    public static final int REQUEST_ENABLE_BT = 9802;
    private BleClientManager bleClientManager;
    private MaterialDialog dialogBleScan;
    private ArrayList<BluetoothDevice> bleDevices;
    private long bleScanPeriod;

    // definitions for GAN Smart Timer / GAN Halo Timer
    private static final int GAN_MANUFACTUREID = 0x4147;
    private static final String GANTIMER_TIMER_SERVICE_UUID = "0000fff0-0000-1000-8000-00805f9b34fb";
    private static final String GANTIMER_STATE_CHARACTERISTIC_UUID = "0000fff5-0000-1000-8000-00805f9b34fb";

    private static final int GANTIMER_STATE_IDLE = 5;
    private static final int GANTIMER_STATE_HANDS_ON = 6;
    private static final int GANTIMER_STATE_HANDS_OFF = 2;
    private static final int GANTIMER_STATE_GET_SET = 1;
    private static final int GANTIMER_STATE_RUNNIG = 3;
    private static final int GANTIMER_STATE_STOPPED = 4;
    private static final int GANTIMER_STATE_FINISHED = 7;

    @BindView(R.id.sessionDetailTextAverage)
    TextView detailTextAvg;

    @BindView(R.id.sessionDetailTextOther)
    TextView detailTextOther;

    @BindView(R.id.detail_average_record_message)
    View detailAverageRecordMesssage;

    @BindView(R.id.sessionRecentResultText)
    TextView recentResultText;

    @BindView(R.id.chronometer)
    ChronometerMilli chronometer;
    @BindView(R.id.scramble_box)
    CardView scrambleBox;
    @BindView(R.id.scramble_text)
    AppCompatTextView scrambleText;
    @BindView(R.id.scramble_img)
    ImageView scrambleImg;
    @BindView(R.id.expanded_image)
    ImageView expandedImageView;
    @BindView(R.id.inspection_text)
    TextView inspectionText;
    @BindView(R.id.progressSpinner)
    MaterialProgressBar progressSpinner;
    @BindView(R.id.scramble_progress)
    MaterialProgressBar scrambleProgress;

    @BindView(R.id.scramble_button_hint)
    AppCompatImageView scrambleButtonHint;
    @BindView(R.id.scramble_button_reset)
    AppCompatImageView scrambleButtonReset;
    @BindView(R.id.scramble_button_edit)
    AppCompatImageView scrambleButtonEdit;
    @BindView(R.id.scramble_button_manual_entry)
    AppCompatImageView scrambleButtonManualEntry;

    @BindView(R.id.qa_remove)
    ImageView deleteButton;
    @BindView(R.id.qa_dnf)
    ImageView dnfButton;
    @BindView(R.id.qa_plustwo)
    ImageView plusTwoButton;
    @BindView(R.id.qa_comment)
    ImageView commentButton;
    @BindView(R.id.qa_undo)
    View undoButton;
    @BindView(R.id.qa_layout)
    LinearLayout quickActionButtons;
    @BindView(R.id.rippleBackground)
    RippleBackground rippleBackground;

    @BindView(R.id.root)
    RelativeLayout rootLayout;
    @BindView(R.id.startTimerLayout)
    FrameLayout startTimerLayout;

    @BindView(R.id.congratsText)
    TextView congratsText;

    @BindView(R.id.serial_status_message)
    TextView serialStatusMessage;

    @BindView(R.id.ble_status_message)
    TextView bleStatusMessage;

    private boolean buttonsEnabled;
    private boolean largeQuickActionEnabled;
    private boolean scrambleImgEnabled;
    private boolean sessionStatsEnabled;
    private boolean sessionStatsMo3Enabled;
    private boolean sessionStatsAo1000Enabled;
    private boolean recentResultsEnabled;
    private boolean worstSolveEnabled;
    private boolean bestSolveEnabled;
    private boolean scrambleEnabled;
    private boolean scrambleBackgroundEnabled;
    private boolean holdEnabled;
    private boolean backCancelEnabled;
    private boolean startCueEnabled;
    private boolean showHintsEnabled;
    private boolean showHintsXCrossEnabled;
    private boolean averageRecordsEnabled;
    private boolean stackTimerEnabled;
    private boolean serialStatusEnabled;
    private boolean smartTimerEnabled;
    private boolean bleStatusEnabled;
    private boolean inspectionByResetEnabled;

    /**
     * True if manual entry is enabled
     */
    private boolean manualEntryEnabled;

    private boolean inspectionAlertEnabled;
    private boolean inspectionVibrationAlertEnabled;
    private boolean inspectionSoundAlertEnabled;


    private boolean timeLimitAlertEnabled;
    private boolean timeLimitVibrationAlertEnabled;
    private boolean timeLimitSoundAlertEnabled;
    float scrambleTextSize;

    // True if the user has started (and stopped) the timer at least once. Used to trigger
    // Average highlights, so the user doesn't get a notification when they start the app
    private boolean hasStoppedTimerOnce = false;

    /**
     * The most recently notified solve time statistics. When {@link #addNewSolve()} is called to
     * add a new time, the new time can be compared to these statistics to determine if the new
     * time sets a record.
     */
    private Statistics mRecentStatistics;

    // Receives broadcasts related to changes to the timer user interface.
    private final TTFragmentBroadcastReceiver mUIInteractionReceiver
            = new TTFragmentBroadcastReceiver(this, CATEGORY_UI_INTERACTIONS) {
        @Override
        public void onReceiveWhileAdded(Context context, Intent intent) {
            switch (Objects.requireNonNull(intent.getAction())) {
                case ACTION_SCROLLED_PAGE:
                    if (holdEnabled) {
                        holdHandler.removeCallbacks(holdRunnable);
                    }
                    chronometer.setHighlighted(false);
                    chronometer.cancelHoldForStart();
                    isReady = false;
                    break;

                case ACTION_TIME_ADDED_MANUALLY:
                    currentSolve = TTIntent.getSolve(intent);
                    if (currentSolve != null) {
                        congratsText.setVisibility(View.GONE);
                        hasStoppedTimerOnce = true;
                        chronometer.reset();
                        chronometer.setText(Html.fromHtml(
                                PuzzleUtils.convertTimeToString(currentSolve.getTime(),
                                        PuzzleUtils.FORMAT_SMALL_MILLI, currentPuzzle)));
                        hideButtons(true, true);
                        broadcastNewSolve();
                        declareRecordTimes(currentSolve);
                    }
                    break;

                case ACTION_TOOLBAR_RESTORED:
                    showItems();
                    animationDone = true;
                    // Wait for animations to run before broadcasting solve to avoid UI stuttering
                    Handler handler = new Handler();
                    handler.postDelayed(() -> {
                        if (!isCanceled) {
                            // Only broadcast a new solve if it hasn't been canceled
                            broadcastNewSolve();
                        } else {
                            // The detail stats are triggered by a stats update.
                            // Since the solve has been canceled, there's no new stats
                            // to load, and it must be triggered manually
                            showDetailStats();
                        }

                        // reset isCanceled state
                        isCanceled = false;
                    }, mAnimationDuration + 50);
                    break;

                case ACTION_GENERATE_SCRAMBLE:
                    generateNewScramble();
                    break;

                case ACTION_BLUETOOTH_CONNECT:
                    Log.d(TAG, "BLE : clicked");
                    startBleScan();
                    break;
            }
        }
    };

    private Runnable holdRunnable;
    private Handler holdHandler;
    private CountDownTimer plusTwoCountdown;

    private RubiksCubeOptimalCross optimalCross;
    private RubiksCubeOptimalXCross optimalXCross;
    private BottomSheetDetailDialog scrambleDialog;
    private FragmentManager mFragManager;

    public TimerFragment() {
        // Required empty public constructor

        // Stack Timer support
        mainLooper = new Handler(Looper.getMainLooper());
        stackTimerString = new StringBuilder();
    }

    private final View.OnClickListener buttonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            final DatabaseHandler dbHandler = CubicTimer.getDBHandler();

            // On most of these changes to the current solve, the Statistics and ChartStatistics
            // need to be updated to reflect the change. It would probably be too complicated to
            // add facilities to "AverageCalculator" to handle modification of the last added time
            // or an "undo" facility and then to integrate that into the loaders. Therefore, a full
            // reload will probably be required.

            switch (view.getId()) {
                case R.id.qa_remove:
                    ThemeUtils.roundAndShowDialog(mContext, new MaterialDialog.Builder(mContext)
                            .content(R.string.delete_dialog_confirmation_title)
                            .positiveText(R.string.delete_dialog_confirmation_button)
                            .negativeText(R.string.delete_dialog_cancel_button)
                            .onPositive((dialog, which) -> {
                                if (currentSolve != null) { // FIXME: if solve is null, it should just hide the buttons
                                    dbHandler.deleteSolve(currentSolve);
                                    if (!isRunning)
                                        chronometer.reset(); // Reset to "0.00".
                                    congratsText.setVisibility(View.GONE);
                                    broadcast(CATEGORY_TIME_DATA_CHANGES, ACTION_TIMES_MODIFIED);
                                }
                                hideButtons(true, true);
                            })
                            .build());
                    break;
                case R.id.qa_dnf:
                    currentSolve = PuzzleUtils.applyPenalty(currentSolve, PENALTY_DNF);
                    chronometer.setPenalty(PuzzleUtils.PENALTY_DNF);
                    dbHandler.updateSolve(currentSolve);
                    hideButtons(true, false);
                    broadcast(CATEGORY_TIME_DATA_CHANGES, ACTION_TIMES_MODIFIED);
                    break;
                case R.id.qa_plustwo:
                    if (currentPenalty != PENALTY_PLUSTWO) {
                        currentSolve = PuzzleUtils.applyPenalty(currentSolve, PENALTY_PLUSTWO);
                        chronometer.setPenalty(PuzzleUtils.PENALTY_PLUSTWO);
                        dbHandler.updateSolve(currentSolve);
                        broadcast(CATEGORY_TIME_DATA_CHANGES, ACTION_TIMES_MODIFIED);
                    }
                    hideButtons(true, false);
                    break;
                case R.id.qa_comment:
                    {
                        CommentDialog commentDialog = CommentDialog.newInstance(
                                CommentDialog.COMMENT_DIALOG_TYPE_COMMENT, currentPuzzle,
                                currentSolve.getComment());
                        commentDialog.setCallback((str) -> {
                            currentSolve.setComment(str.toString());
                            dbHandler.updateSolve(currentSolve);

                            broadcast(CATEGORY_TIME_DATA_CHANGES, ACTION_COMMENT_ADDED);
                            Toast.makeText(mContext, getString(R.string.added_comment), Toast.LENGTH_SHORT).show();
                            hideButtons(false, true);
                        });
                        FragmentManager manager = getFragmentManager();
                        if (manager != null)
                            commentDialog.show(manager, "dialog_comment");
                    }
                    break;
                case R.id.qa_undo:
                    // Undo the setting of a DNF or +2 penalty (does not undo a delete or comment).
                    currentSolve = PuzzleUtils.applyPenalty(currentSolve, NO_PENALTY);
                    chronometer.setPenalty(PuzzleUtils.NO_PENALTY);
                    dbHandler.updateSolve(currentSolve);
                    hideButtons(false, true);
                    broadcast(CATEGORY_TIME_DATA_CHANGES, ACTION_TIMES_MODIFIED);
                    break;
                case R.id.scramble_button_reset:
                    broadcast(CATEGORY_UI_INTERACTIONS, ACTION_GENERATE_SCRAMBLE);
                    break;
                case R.id.scramble_button_edit:
                    {
                        CommentDialog commentDialog = CommentDialog.newInstance(
                                CommentDialog.COMMENT_DIALOG_TYPE_SCRAMBLE, currentPuzzle, "");
                        commentDialog.setCallback((str) -> {
                            setScramble(str.toString());

                            // The hint solver will crash if you give it invalid scrambles,
                            // so we shouldn't calculate hints for custom scrambles.
                            // TODO: We can use the scramble image generator (which has a scramble validity checker) to check a scramble before calling a hint
                            canShowHint = false;
                            hideButtons(true, true);
                        });
                        FragmentManager manager = getFragmentManager();
                        if (manager != null)
                            commentDialog.show(manager, "dialog_comment");
                    }
                    break;
                case R.id.scramble_button_manual_entry:
                    addTimeManually();
                    break;
            }
        }
    };

    /**
     * Hides (or shows) the delete/dnf/plus-two quick action buttons and the undo button.
     */
    private void hideButtons(boolean hideQuickActionButtons, boolean hideUndoButton) {
        quickActionButtons.setVisibility(hideQuickActionButtons ? View.GONE : View.VISIBLE);
        undoButton.setVisibility(hideUndoButton ? View.GONE : View.VISIBLE);
    }

    public static TimerFragment newInstance(String puzzle, String puzzleSubType, int mbldNum, String scramble, String timerMode, TrainerScrambler.TrainerSubset subset) {
        TimerFragment fragment = new TimerFragment();
        Bundle args = new Bundle();
        args.putString(PUZZLE, puzzle);
        args.putString(PUZZLE_SUBTYPE, puzzleSubType);
        args.putInt(MBLD_NUM, mbldNum);
        args.putString(SCRAMBLE, scramble);
        Log.d(TAG, "newInstance scramble = " + scramble);
        args.putString(TIMER_MODE, timerMode);
        args.putSerializable(TRAINER_SUBSET, subset);
        fragment.setArguments(args);
        if (DEBUG_ME) Log.d(TAG, "newInstance() -> " + fragment);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (DEBUG_ME) Log.d(TAG, "onCreate updateLocale(savedInstanceState=" + savedInstanceState + ")");
        super.onCreate(savedInstanceState);
        mContext = getContext();
        if (getArguments() != null) {
            currentPuzzle = getArguments().getString(PUZZLE);
            currentPuzzleCategory = getArguments().getString(PUZZLE_SUBTYPE);
            currentMbldNum = getArguments().getInt(MBLD_NUM);
            realScramble = getArguments().getString(SCRAMBLE);
            if (realScramble.isEmpty()) realScramble = null;
            currentSubset = (TrainerScrambler.TrainerSubset) getArguments().getSerializable(TRAINER_SUBSET);
            currentTimerMode = getArguments().getString(TIMER_MODE);
        }

        if (savedInstanceState != null) {
            if (savedInstanceState.getString(PUZZLE) == getArguments().get(PUZZLE)) {
                realScramble = savedInstanceState.getString(SCRAMBLE);
            }
            //hasStoppedTimerOnce = savedInstanceState.getBoolean(HAS_STOPPED_TIMER_ONCE, false);
        }

        detailTextNamesArray = getResources().getStringArray(R.array.timer_detail_stats);

        scrambleGeneratorAsync = new GenerateScrambleSequence();

        getNewOptimalCross();

        mFragManager = getFragmentManager();

        mAnimationDuration = Prefs.getInt(R.string.pk_timer_animation_duration, mContext.getResources().getInteger(R.integer.defaultAnimationDuration));

        generator = new ScrambleGenerator(currentPuzzle);
        // Register a receiver to update if something has changed
        registerReceiver(mUIInteractionReceiver);
    }

    @SuppressLint({"ClickableViewAccessibility", "RestrictedApi"})
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        if (DEBUG_ME) Log.d(TAG, "onCreateView(savedInstanceState=" + savedInstanceState + ")");

        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_timer, container, false);
        mUnbinder = ButterKnife.bind(this, root);

        return root;
    }

    @SuppressLint({"ClickableViewAccessibility", "RestrictedApi"})
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Necessary for the scramble image to show
        scrambleImg.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        expandedImageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        // Set the zoom click listener
        scrambleImg.setOnClickListener(view1 -> zoomImageFromThumb(scrambleImg));

        // Retrieve and cache the system's default "short" animation time.


        deleteButton.setOnClickListener(buttonClickListener);
        dnfButton.setOnClickListener(buttonClickListener);
        plusTwoButton.setOnClickListener(buttonClickListener);
        commentButton.setOnClickListener(buttonClickListener);
        undoButton.setOnClickListener(buttonClickListener);
        scrambleButtonReset.setOnClickListener(buttonClickListener);
        scrambleButtonEdit.setOnClickListener(buttonClickListener);

        // Preferences //
        final boolean inspectionEnabled = Prefs.getBoolean(R.string.pk_inspection_enabled, false)
                && PuzzleUtils.isInspectionEnabled(currentPuzzle);
        final int inspectionTime = Prefs.getInt(R.string.pk_inspection_time, 15);
        final float timerTextSize = Prefs.getInt(R.string.pk_timer_text_size, 100) / 100f;
        float scrambleImageSize = Prefs.getInt(R.string.pk_scramble_image_size, 100) / 100f;
        scrambleTextSize = Prefs.getInt(R.string.pk_scramble_text_size, 100) / 100f;
        final boolean advancedEnabled
                = Prefs.getBoolean(R.string.pk_advanced_timer_settings_enabled, false);

        /*
         *  Scramble text size preference. It doesn't need to be in the "advanced" settings since
         *  it detects if it's clipping and automatically compensates for that by creating a button.
         */
        scrambleText.setTextSize(TypedValue.COMPLEX_UNIT_PX, scrambleText.getTextSize() * scrambleTextSize);

        if (advancedEnabled) {
            chronometer.setAutoSizeTextTypeUniformWithConfiguration(
                    (int) (90 * timerTextSize) / 2,
                    (int) (90 * timerTextSize),
                    2,
                    TypedValue.COMPLEX_UNIT_SP);

            scrambleImg.getLayoutParams().width *= scrambleImageSize;
            scrambleImg.getLayoutParams().height *= calculateScrambleImageHeightMultiplier(scrambleImageSize);
        }

        Resources res = getResources();

        averageRecordsEnabled = Prefs.getBoolean(R.string.pk_show_average_record_enabled,
                DefaultPrefs.getBoolean(R.bool.default_showAverageRecordEnabled));

        backCancelEnabled = Prefs.getBoolean(R.string.pk_back_button_cancel_solve_enabled, res.getBoolean(R.bool.default_backCancelEnabled));

        buttonsEnabled = Prefs.getBoolean(R.string.pk_show_quick_actions, res.getBoolean(R.bool.default_buttonEnabled));
        largeQuickActionEnabled = Prefs.getBoolean(R.string.pk_large_quick_actions_enabled, res.getBoolean(R.bool.default_largeQuickActionEnabled));
        holdEnabled = Prefs.getBoolean(R.string.pk_hold_to_start_enabled, res.getBoolean(R.bool.default_holdEnabled));
        startCueEnabled = Prefs.getBoolean(R.string.pk_start_cue_enabled, res.getBoolean(R.bool.default_startCue));

        sessionStatsEnabled = Prefs.getBoolean(R.string.pk_show_session_stats, true);
        sessionStatsMo3Enabled = Prefs.getBoolean(R.string.pk_show_session_stats_mo3, false)
                || PuzzleUtils.isForceMo3Enabled(currentPuzzle);
        sessionStatsAo1000Enabled = Prefs.getBoolean(R.string.pk_show_session_stats_ao1000, false);
        recentResultsEnabled = Prefs.getBoolean(R.string.pk_show_recent_results, true);
        bestSolveEnabled = Prefs.getBoolean(R.string.pk_show_best_time, true);
        worstSolveEnabled = Prefs.getBoolean(R.string.pk_show_worst_time, false);

        scrambleEnabled = Prefs.getBoolean(R.string.pk_scramble_enabled, true);
        scrambleImgEnabled = Prefs.getBoolean(R.string.pk_show_scramble_image, true);
        showHintsEnabled = Prefs.getBoolean(R.string.pk_show_scramble_hints, true);
        showHintsXCrossEnabled = Prefs.getBoolean(R.string.pk_show_scramble_x_cross_hints, false);

        manualEntryEnabled = Prefs.getBoolean(R.string.pk_enable_manual_entry, false);

        scrambleBackgroundEnabled = Prefs.getBoolean(R.string.pk_show_scramble_background, false);

        stackTimerEnabled = Prefs.getBoolean(R.string.pk_stack_timer_enabled, true);
        serialStatusEnabled = Prefs.getBoolean(R.string.pk_show_serial_status, true);
        smartTimerEnabled = Prefs.getBoolean(R.string.pk_smart_timer_enabled, true);
        bleStatusEnabled = Prefs.getBoolean(R.string.pk_show_ble_status, true);
        inspectionByResetEnabled = Prefs.getBoolean(R.string.pk_inspection_by_reset_enabled, true);

        inspectionAlertEnabled = Prefs.getBoolean(R.string.pk_inspection_alert_enabled, false);
        final String vibrationAlert = getString(R.string.pk_inspection_alert_vibration);
        final String soundAlert = getString(R.string.pk_inspection_alert_sound);
        if (inspectionAlertEnabled) {
            String inspectionAlertType = Prefs.getString(R.string.pk_inspection_alert_type,
                    getString(R.string.pk_inspection_alert_vibration));
            if (inspectionAlertType.equals(vibrationAlert)) {
                inspectionVibrationAlertEnabled = true;
                inspectionSoundAlertEnabled = false;
            } else if (inspectionAlertType.equals(soundAlert)) {
                inspectionVibrationAlertEnabled = false;
                inspectionSoundAlertEnabled = true;
            } else {
                inspectionVibrationAlertEnabled = true;
                inspectionSoundAlertEnabled = true;
            }
        }

        timeLimitAlertEnabled = Prefs.getBoolean(R.string.pk_time_limit_alert_enabled, false);
        if (timeLimitAlertEnabled) {
            String timeLimitAlertType = Prefs.getString(R.string.pk_time_limit_alert_type,
                    getString(R.string.pk_inspection_alert_vibration));
            if (timeLimitAlertType.equals(vibrationAlert)) {
                timeLimitVibrationAlertEnabled = true;
                timeLimitSoundAlertEnabled = false;
            } else if (timeLimitAlertType.equals(soundAlert)) {
                timeLimitVibrationAlertEnabled = false;
                timeLimitSoundAlertEnabled = true;
            } else {
                timeLimitVibrationAlertEnabled = true;
                timeLimitSoundAlertEnabled = true;
            }
        }

        if (!scrambleEnabled) {
            // CongratsText is by default aligned to below the scramble box. If it's missing, we have
            // to add an extra margin to account for the title header
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) congratsText.getLayoutParams();
            params.topMargin = ThemeUtils.dpToPix(mContext, 70); // WARNING: this has to be the same as attr/actionBarPadding
            congratsText.requestLayout();
        }

        if (!scrambleEnabled) {
            // CongratsText is by default aligned to below the scramble box. If it's missing, we have
            // to add an extra margin to account for the title header
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) congratsText.getLayoutParams();
            params.topMargin = ThemeUtils.dpToPix(mContext, 70); // WARNING: this has to be the same as attr/actionBarPadding
            congratsText.requestLayout();
        }

        if (!scrambleBackgroundEnabled) {
            scrambleBox.setBackgroundColor(Color.TRANSPARENT);
            scrambleBox.setCardElevation(0);
            scrambleText.setTextColor(ThemeUtils.fetchAttrColor(mContext, R.attr.colorTimerText));
            scrambleButtonEdit.setColorFilter(ThemeUtils.fetchAttrColor(mContext, R.attr.colorTimerText));
            scrambleButtonReset.setColorFilter(ThemeUtils.fetchAttrColor(mContext, R.attr.colorTimerText));
            scrambleButtonHint.setColorFilter(ThemeUtils.fetchAttrColor(mContext, R.attr.colorTimerText));
            scrambleButtonManualEntry.setColorFilter(ThemeUtils.fetchAttrColor(mContext, R.attr.colorTimerText));
        }

        if (showHintsEnabled && currentPuzzle.equals(PuzzleUtils.TYPE_333) && scrambleEnabled) {
            scrambleButtonHint.setVisibility(View.VISIBLE);
            optimalCross = new RubiksCubeOptimalCross(getString(R.string.optimal_cross));
            optimalXCross = new RubiksCubeOptimalXCross(getString(R.string.optimal_x_cross));
        }

        if (!scrambleEnabled) {
            scrambleBox.setVisibility(View.GONE);
            scrambleImg.setVisibility(View.GONE);
            isLocked = false;
        }

        if (!scrambleImgEnabled)
            scrambleImg.setVisibility(View.GONE);
        if (!sessionStatsEnabled) {
            detailTextAvg.setVisibility(View.INVISIBLE);
            detailTextOther.setVisibility(View.INVISIBLE);
        }
        if (!recentResultsEnabled) {
            recentResultText.setVisibility(View.INVISIBLE);
        }

        if (!serialStatusEnabled) {
            serialStatusMessage.setVisibility(View.GONE);
        }

        if (!bleStatusEnabled) {
            bleStatusMessage.setVisibility(View.GONE);
        }
        if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            bleStatusMessage.setText(getString(R.string.timer_ble_status_message) + getString(R.string.timer_ble_status_no_support_message));
        } else if (!smartTimerEnabled || isTimeDisabled(currentPuzzle)) {
            bleStatusMessage.setText(getString(R.string.timer_ble_status_message) + getString(R.string.timer_ble_status_disabled_message));
        } else {
            bleStatusMessage.setText(getString(R.string.timer_ble_status_message) + getString(R.string.timer_ble_status_disconnect_message));
        }

        // Preferences //

        // Manual entry
        if (manualEntryEnabled) {
            scrambleButtonManualEntry.setVisibility(View.VISIBLE);
        }
        scrambleButtonManualEntry.setOnClickListener(buttonClickListener);

        // Enlarge quick action buttons
        if (largeQuickActionEnabled) {
            ImageView viewRemove = quickActionButtons.findViewById(R.id.qa_remove);
            ImageView viewDnf = quickActionButtons.findViewById(R.id.qa_dnf);
            ImageView viewPlustwo = quickActionButtons.findViewById(R.id.qa_plustwo);
            ImageView viewComment = quickActionButtons.findViewById(R.id.qa_comment);
            ViewGroup.LayoutParams layoutParams = viewRemove.getLayoutParams();
            layoutParams.width = (int)(layoutParams.width * 1.7);
            layoutParams.height = (int)(layoutParams.height * 1.7);
            viewRemove.setLayoutParams(layoutParams);
            viewDnf.setLayoutParams(layoutParams);
            viewPlustwo.setLayoutParams(layoutParams);
            viewComment.setLayoutParams(layoutParams);

            layoutParams = undoButton.getLayoutParams();
            layoutParams.width = (int)(layoutParams.width * 1.7);
            layoutParams.height = (int)(layoutParams.height * 1.7);
            undoButton.setLayoutParams(layoutParams);
        }

        // Time Limit Alert
        if (timeLimitAlertEnabled && PuzzleUtils.isTimeDisabled(currentPuzzle)){
            // For FMC, Time Limit is 60 minutes = 3,600 seconds
            long second = 60*60;
            if (currentPuzzle.equals(PuzzleUtils.TYPE_333MBLD)) {
                // For MBLD, Time Limit is the number of puzzles times 10 minutes (max 60 minutes)
                second = Math.min(currentMbldNum, 6) * 10 * 60;
            }

            timeLimitWarning = new CountdownWarning
                    .Builder(second)
                    .withVibrate(timeLimitVibrationAlertEnabled)
                    .withTone(timeLimitSoundAlertEnabled)
                    .toneCode(ToneGenerator.TONE_CDMA_MED_PBX_S_X4)
                    .toneDuration(2300)
                    .vibrateDuration(2300)
                    .build();
        }

        // Inspection timer
        if (inspectionEnabled) {
            if (inspectionAlertEnabled) {
                // If inspection time is 15 (the official WCA default), first warning should be
                // at 8 seconds in. Else, warn when half the time is up (8 is about 50% of 15)
                firstWarning = new CountdownWarning
                        .Builder(inspectionTime == 15 ? 8 : (int) (inspectionTime * 0.5f))
                        .withVibrate(inspectionVibrationAlertEnabled)
                        .withTone(inspectionSoundAlertEnabled)
                        .toneCode(ToneGenerator.TONE_CDMA_NETWORK_BUSY_ONE_SHOT)
                        .toneDuration(400)
                        .vibrateDuration(300)
                        .build();
                // If inspection time is default, warn at 12 seconds per competition rules, else,
                // warn at when 80% of the time is up (12 is 80% of 15)
                secondWarning = new CountdownWarning
                        .Builder(inspectionTime == 15 ? 12 : (int) (inspectionTime * 0.8f))
                        .withVibrate(inspectionVibrationAlertEnabled)
                        .withTone(inspectionSoundAlertEnabled)
                        .toneCode(ToneGenerator.TONE_CDMA_NETWORK_BUSY)
                        .toneDuration(800)
                        .vibrateDuration(600)
                        .build();
            }
            countdown = new CountDownTimer(inspectionTime * 1000, 500) {
                @Override
                public void onTick(long l) {
                    if (chronometer != null)
                        chronometer.setText(String.valueOf((l / 1000) + 1));
                }

                @Override
                public void onFinish() {
                    if (chronometer != null) {
                        chronometer.setText("+2");
                        // "+2" penalty is applied to "chronometer" when timer is eventually stopped.
                        currentPenalty = PuzzleUtils.PENALTY_PLUSTWO;
                        plusTwoCountdown.start();
                    }
                }
            };

            plusTwoCountdown = new CountDownTimer(2000, 500) {
                @Override
                public void onTick(long l) {
                    // The displayed value remains "+2" for the duration of this countdown.
                }

                @Override
                public void onFinish() {
                    // After counting down the inspection period, a "+2" penalty was counted down
                    // before the solve started, so this is a DNF. If the timer starts before this
                    // countdown ends, then "plusTwoCountdown" is cancelled before this happens.
                    countingDown = false;
                    isReady = false;
                    holdingDNF = true;
                    currentPenalty = PuzzleUtils.PENALTY_DNF;
                    chronometer.setPenalty(PuzzleUtils.PENALTY_DNF);
                    stopChronometer();
                    currentScramble = realScramble;
                    generateNewScramble();
                    addNewSolve();
                    inspectionText.setVisibility(View.GONE);
                }
            };
        }

        // If hold-for-start is enabled, use the "isReady" flag to indicate if the hold was long
        // enough (0.5s) to trigger the starting of the timer.
        if (holdEnabled) {
            holdHandler = new Handler();
            holdRunnable = () -> {
                isReady = true;
                // Indicate to the user that the hold was long enough.
                chronometer.setHighlighted(true);
                if (!inspectionEnabled) {
                    // If inspection is enabled, the toolbar is already hidden.
                    hideToolbar();
                }
            };
        }

        detailAverageRecordMesssage.setBackground(ThemeUtils.createSquareDrawableAttr(mContext, 0, R.attr.colorTimerText, 20, 1.6f));

        // Chronometer
        startTimerLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (!animationDone || isLocked && !isRunning) {
                    // Not ready to start the timer, yet. May be waiting on the animation of the
                    // restoration of the tool-bars after the timer was stopped, or waiting on the
                    // generation of a scramble ("isLocked" flag).
                    // To compensate for long generating times, the timer generates a scramble
                    // while it is counting down. In this case, it's necessary to check if the timer
                    // is running, so the user can stop the it.
                    return false;
                }

                if (countingDown) { // "countingDown == true" => "inspectionEnabled == true"
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            // During inspection, touching down changes the text highlight color
                            // to indicate readiness to start timing. If the hold-for-start delay
                            // is enabled, that color change will be delayed. The timer will not
                            // start until the touch is lifted up, but the inspection countdown
                            // will still continue in the meantime.
                            if (holdEnabled) {
                                isReady = false;
                                holdHandler.postDelayed(holdRunnable, HOLD_FOR_START_DELAY);
                            } else if (startCueEnabled) {
                                chronometer.setHighlighted(true);
                            }
                            // "chronometer.holdForStart" is not called here; it displays "0.00",
                            // which would interfere with the continuing countdown of the
                            // inspection
                            // period and, anyway, be overwritten by the next countdown "tick".
                            return true;

                        case MotionEvent.ACTION_UP:
                            // Counting down inspection period. User has already touched down after
                            // starting the inspection, so start the timer unless "hold-to-start"
                            // is enabled and the hold delay was not long enough.
                            if (holdEnabled && !isReady) {
                                holdHandler.removeCallbacks(holdRunnable);
                            } else {
                                stopInspectionCountdown();
                                startChronometer(); // Tool-bar is already hidden and remains so.
                            }
                            return false;
                    }
                } else if (!isRunning) { // Not running and not counting down.
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:

                            if (holdingDNF) {
                                holdingDNF = false;
                                chronometer.setHighlighted(false);
                            }

                            if (!inspectionEnabled) {
                                if (holdEnabled) {
                                    isReady = false;
                                    holdHandler.postDelayed(holdRunnable, HOLD_FOR_START_DELAY);
                                } else if (startCueEnabled) {
                                    chronometer.setHighlighted(true);
                                }
                                // Display "0.00" while holding in readiness for a new solve.
                                // This is not used above when inspection is enabled, as it would
                                // interfere with the countdown display.
                                chronometer.holdForStart();
                            }
                            return true;

                        case MotionEvent.ACTION_UP:

                            if (holdingDNF) {
                                // Checks if the user was holding the screen when the inspection
                                // timed out and saved a DNF
                                holdingDNF = false;
                            } else if (inspectionEnabled) {
                                hideToolbar();
                                startInspectionCountdown(inspectionTime);
                            } else if (holdEnabled && !isReady) {
                                // Not held for long enough. Replace "0.00" with previous value.
                                chronometer.cancelHoldForStart();
                                holdHandler.removeCallbacks(holdRunnable);
                            } else {
                                // Inspection disabled. Hold-for-start disabled, or hold-for-start
                                // enabled, but the hold time was long enough. In the latter case,
                                // the tool-bar will already have been hidden. Start timing!
                                if (!holdEnabled) {
                                    hideToolbar();
                                }
                                startChronometer();
                            }
                            return false;
                    }
                } else if (motionEvent.getAction() == MotionEvent.ACTION_DOWN
                        && chronometer.getElapsedTime() >= 80) { // => "isRunning == true"
                    // Chronometer is timing a solve (running, not counting down inspection period).
                    // Stop the timer if it has been running for long enough (80 ms) for this not to
                    // be an accidental touch as the user lifted up the touch to start the timer.
                    animationDone = false;
                    stopChronometer();
                    if (currentPenalty == PuzzleUtils.PENALTY_PLUSTWO) {
                        // If a user has inspection on and went past his inspection time, he has
                        // two extra seconds do start his time, but with a +2 penalty. This penalty
                        // is recorded above (see plusTwoCountdown), and the timer checks if it's true here.
                        chronometer.setPenalty(PuzzleUtils.PENALTY_PLUSTWO);
                    }
                    if (!isTimeDisabled(currentPuzzle)) {
                        addNewSolve();
                    } else {
                        // For FMC and MBLD, use isCanceld flag not to show quick action button and not update solves
                        isCanceled = true;
                        addTimeManually();
                    }
                }
                return false;
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // If the statistics are already loaded, the update notification will have been missed,
        // so fire that notification now. If the statistics are non-null, they will be displayed.
        // If they are null (i.e., not yet loaded), nothing will be displayed until this fragment,
        // as a registered observer, is notified when loading is complete. Post the firing of the
        // event, so that it is received after "onCreateView" returns.
        onStatisticsUpdated(StatisticsCache.getInstance().getStatistics());
        StatisticsCache.getInstance().registerObserver(this); // Unregistered in "onDestroyView".
    }

    @Override
    public void onResume() {
        if (DEBUG_ME) Log.d(TAG, "onResume()");
        super.onResume();
        if (scrambleEnabled) {
            if (realScramble == null) {
                generateNewScramble();
            } else {
                setScramble(realScramble);
            }
        }
        mainLooper.post(this::connect);
        bleClientManager = new BleClientManager(mContext);
    }

    @Override
    public void onPause() {
        if (DEBUG_ME) Log.d(TAG, "onPause()");
        super.onPause();
        disconnect();
        disconnectBle();
        if (bleClientManager != null)
            bleClientManager = null;
    }

    /**
     * Stops the chronometer on back press.
     *
     * @return
     *     {@code true} if the "Back" button press was consumed to hide the scramble or stop the
     *     timer; or {@code false} if neither was necessary and the "Back" button press was ignored.
     */
    @Override
    public boolean onBackPressedInFragment() {
        if (DEBUG_ME) Log.d(TAG, "onBackPressedInFragment()");

        if (isResumed()) {
            if (isRunning || countingDown) {
                cancelChronometer();
                return true;
            }
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TimerFragment.REQUEST_ENABLE_BT) {
            Log.d(TAG,"BLE : onActivityResult " + resultCode);
            if (resultCode == RESULT_OK) {
                startBleScan();
            }
        }
    }

    /**
     * Stops the inspection period countdown, and its warnings (if it is active). This cancels the
     * inspection countdown timer and associated "+2" countdown timer and hides the inspection text.
     */
    private void stopInspectionCountdown() {
        // These timers may be null if inspection was not enabled when "updateLocale" was called.
        if (countdown != null) {
            countdown.cancel();
        }

        if (plusTwoCountdown != null) {
            plusTwoCountdown.cancel();
        }

        if (firstWarning != null) {
            firstWarning.cancel();
        }

        if (secondWarning != null) {
            secondWarning.cancel();
        }

        inspectionText.setVisibility(View.GONE);
        countingDown = false;
    }

    /**
     * Starts the inspection period countdown.
     *
     * @param inspectionTime
     *     The inspection time in seconds.
     */
    private void startInspectionCountdown(int inspectionTime) {
        // The "countdown" timer may be null if inspection was not enabled when "updateLocale" was
        // called. In that case this method will not be called from the touch listener.

        // So it doesn't flash the old time when the inspection starts
        chronometer.setText(String.valueOf(inspectionTime));
        inspectionText.setVisibility(View.VISIBLE);
        countdown.start();
        if (firstWarning != null) {
            firstWarning.start();
        }
        if (secondWarning != null) {
            secondWarning.start();
        }
        countingDown = true;
    }

    /**
     * Calculates scramble image height multiplier to respect aspect ratio
     *
     * @param multiplier the height multiplier (must be the same multiplier as the width)
     *
     * @return the height in px
     */
    private float calculateScrambleImageHeightMultiplier(float multiplier) {
        switch (currentPuzzle) {
            case PuzzleUtils.TYPE_777:
            case PuzzleUtils.TYPE_666:
            case PuzzleUtils.TYPE_555:
            case PuzzleUtils.TYPE_222:
            case PuzzleUtils.TYPE_444:
            case PuzzleUtils.TYPE_333:
            case PuzzleUtils.TYPE_333BLD:
            case PuzzleUtils.TYPE_444BLD:
            case PuzzleUtils.TYPE_555BLD:
            case PuzzleUtils.TYPE_333FMC:
                // 3 faces of the cube vertically divided by 4 faces horizontally (it draws the cube like a cross)
                return (multiplier / 4) * 3;
            case PuzzleUtils.TYPE_CLOCK:
                return multiplier / 2;
            case PuzzleUtils.TYPE_MEGA:
                return (multiplier / 2);
            case PuzzleUtils.TYPE_PYRA:
                // Just pythagoras. Height of an equilateral triangle
                return (float) (multiplier / Math.sqrt(1.25));
            case PuzzleUtils.TYPE_SKEWB:
                // This one is the same as the NxN cubes
                return (multiplier / 4) * 3;
            case PuzzleUtils.TYPE_SQUARE1: // Square-1
            case PuzzleUtils.TYPE_333MBLD:
            case PuzzleUtils.TYPE_OTHER:
                return multiplier;
        }
        return multiplier;
    }

    private void addNewSolve() {
        currentSolve = new Solve(
                chronometer.getElapsedTime(), // Includes any "+2" penalty. Is zero for "DNF".
                currentPuzzle, currentPuzzleCategory,
                System.currentTimeMillis(), currentScramble, currentPenalty, "", false);

        if (currentPenalty != PENALTY_DNF) {
            declareRecordTimes(currentSolve);
        }

        currentSolve.setId(CubicTimer.getDBHandler().addSolve(currentSolve));
        currentPenalty = NO_PENALTY;
    }

    public void addTimeManually() {
        long time = chronometer.getElapsedTime();
        time = (time + 500) / 1000 * 1000;  // rounding to the nearest second

        AddTimeDialog addTimeDialog = AddTimeDialog.newInstance(currentPuzzle, currentPuzzleCategory, currentMbldNum, realScramble, time);
        FragmentManager manager = getFragmentManager();
        if (manager != null)
            addTimeDialog.show(manager, "dialog_add_time");
    }

    private void broadcastNewSolve() {
        // The receiver might be able to use the new solve and avoid accessing the database, so
        // parcel it up in the intent.
        new BroadcastBuilder(CATEGORY_TIME_DATA_CHANGES, ACTION_TIME_ADDED)
                .solve(currentSolve)
                .broadcast();
    }

    /**
     * Declares a new all-time best or worst solve time, if the new solve time sets a record. The
     * first valid solve time will not set any records; it is itself the best and worst time and
     * only later times will be compared to it. If the solve time is not greater than zero, or if
     * the solve is a DNF, the solve will be ignored and no new records will be declared.
     *
     * @param solve The solve (time) to be tested.
     */
    private void declareRecordTimes(Solve solve) {
        // NOTE: The old approach did not check for PB/record solves until at least 4 previous
        // solves had been recorded for the *current session*. This seemed a bit arbitrary. Perhaps
        // it had to do with waiting for the best and worst times to be loaded. If a user records
        // their *first* solve for the current session and it beats the best time from *any* past
        // session, it should be reported *immediately*, not ignored just because the session has
        // only started. However, the limit should perhaps have been 4 previous solves in the full
        // history of all past and current sessions. If this is the first ever session, then it
        // would be annoying if each of the first few times were reported as a record of some sort.
        // Therefore, do not report PB records until at least 4 previous *non-DNF* times have been
        // recorded in the database across all sessions, including the current session.

        final long newTime = solve.getTime();

        if (solve.getPenalty() == PENALTY_DNF || newTime <= 0
                || mRecentStatistics == null
                || mRecentStatistics.getAllTimeNumSolves()
                - mRecentStatistics.getAllTimeNumDNFSolves() < 4) {
            // Not a valid time, or there are no previous statistics, or not enough previous times
            // to make reporting meaningful (or non-annoying), so cannot check for a new PB.
            return;
        }

        if (bestSolveEnabled) {
            final long previousBestTime = mRecentStatistics.getAllTimeBestTime();

            // If "previousBestTime" is a DNF or UNKNOWN, it will be less than zero, so the new
            // solve time cannot better (i.e., lower).
            if (newTime < previousBestTime) {
                rippleBackground.startRippleAnimation();
                String strDiff = getDifferenceString(previousBestTime, newTime, currentPuzzle);
                congratsText.setText(getString(R.string.personal_best_message, strDiff));

                congratsText.setVisibility(View.VISIBLE);

                new Handler().postDelayed(() -> {
                    if (rippleBackground != null)
                        rippleBackground.stopRippleAnimation();
                }, 2900);
            }
        }

        if (worstSolveEnabled) {
            final long previousWorstTime = mRecentStatistics.getAllTimeWorstTime();
            Drawable poopDrawable = ThemeUtils.tintDrawable(mContext, R.drawable.ic_emoticon_poop, R.attr.colorTimerText);
            // If "previousWorstTime" is a DNF or UNKNOWN, it will be less than zero. Therefore,
            // make sure it is at least greater than zero before testing against the new time.
            if (previousWorstTime > 0 && newTime > previousWorstTime) {
                String strDiff = getDifferenceString(newTime, previousWorstTime, currentPuzzle);
                congratsText.setText(getString(R.string.personal_worst_message, strDiff));

                congratsText.setCompoundDrawablesWithIntrinsicBounds(
                        poopDrawable, null,
                        poopDrawable, null);

                congratsText.setVisibility(View.VISIBLE);
            }
        }
    }

    public String getDifferenceString(long t1, long t2, String puzzleType) {
        String str;
        if (puzzleType.equals(PuzzleUtils.TYPE_333MBLD)) {
            long diff = PuzzleUtils.MbldRecord.getPointDiff(t2, t1);
            if (diff != 0) {
                str = getString(R.string.personal_best_points, String.valueOf(diff/1000));
            } else {
                diff = PuzzleUtils.MbldRecord.getSecondDiff(t1, t2);
                if (diff != 0) {
                    str = getString(R.string.personal_best_seconds, PuzzleUtils.formatTime(diff*1000, PuzzleUtils.FORMAT_SECOND));
                } else {
                    diff = PuzzleUtils.MbldRecord.getFailedDiff(t1, t2);
                    str = getString(R.string.personal_best_failed, String.valueOf(diff));
                }
            }
        } else {
            str = convertTimeToString(t1 - t2, PuzzleUtils.FORMAT_SINGLE, puzzleType);
        }

        return str;
    }

    /**
     * Refreshes the display of the statistics. If this fragment has no view, or if the given
     * statistics are {@code null}, no update will be attempted.
     *
     * @param stats
     *     The updated statistics. These will not be modified.
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void onStatisticsUpdated(Statistics stats) {
        if (DEBUG_ME) Log.d(TAG, "onStatisticsUpdated(" + stats + ")");

        if (getView() == null) {
            // Must have arrived after "onDestroyView" was called, so do nothing.
            return;
        }

        // Save these for later. The best and worst times can be retrieved and compared to the next
        // new solve time to be added via "addNewSolve".
        mRecentStatistics = stats; // May be null.

        if (stats == null) {
            return;
        }

        if (sessionStatsEnabled) {
            String sessionDeviation = convertTimeToString(tr(stats.getSessionStdDeviation()),
                    PuzzleUtils.FORMAT_STATS, currentPuzzle);
            String sessionCount = String.format(Locale.getDefault(), "%,d", stats.getSessionNumSolves());
            String sessionBestTime = convertTimeToString(tr(stats.getSessionBestTime()),
                    PuzzleUtils.FORMAT_SINGLE, currentPuzzle);
            String sessionMean = convertTimeToString(tr(stats.getSessionMeanTime()),
                    PuzzleUtils.FORMAT_STATS, currentPuzzle);

            long sessionCurrentAvg[] = new long[6];
            boolean isAllTimeBestAvgUpdate[] = new boolean[6];

            isAllTimeBestAvgUpdate[0] = stats.getAverageOf(3, false).getIsBestAverageUpdated();
            isAllTimeBestAvgUpdate[1] = stats.getAverageOf(5, false).getIsBestAverageUpdated();
            isAllTimeBestAvgUpdate[2] = stats.getAverageOf(12, false).getIsBestAverageUpdated();
            isAllTimeBestAvgUpdate[3] = stats.getAverageOf(50, false).getIsBestAverageUpdated();
            isAllTimeBestAvgUpdate[4] = stats.getAverageOf(100, false).getIsBestAverageUpdated();
            isAllTimeBestAvgUpdate[5] = stats.getAverageOf(1000, false).getIsBestAverageUpdated();

            sessionCurrentAvg[0] = tr(stats.getAverageOf(3, true).getCurrentAverage());
            sessionCurrentAvg[1] = tr(stats.getAverageOf(5, true).getCurrentAverage());
            sessionCurrentAvg[2] = tr(stats.getAverageOf(12, true).getCurrentAverage());
            sessionCurrentAvg[3] = tr(stats.getAverageOf(50, true).getCurrentAverage());
            sessionCurrentAvg[4] = tr(stats.getAverageOf(100, true).getCurrentAverage());
            sessionCurrentAvg[5] = tr(stats.getAverageOf(1000, true).getCurrentAverage());

            // detailTextNamesArray should be in the same order as shown in the timer
            // (keep R.arrays.timer_detail_stats in sync with the order!)
            StringBuilder stringDetailOther = new StringBuilder();
            stringDetailOther.append(detailTextNamesArray[6]).append(": ").append(sessionDeviation).append("\n");
            stringDetailOther.append(detailTextNamesArray[7]).append(": ").append(sessionMean).append("\n");
            stringDetailOther.append(detailTextNamesArray[8]).append(": ").append(sessionBestTime).append("\n");
            stringDetailOther.append(detailTextNamesArray[9]).append(": ").append(sessionCount);

            detailTextOther.setText(stringDetailOther.toString());

            // To prevent the record message being animated more than once in case the user sets
            // two or more average records at the same time.
            boolean hasShownRecordMessage = false;

            // reset card visibility
            detailAverageRecordMesssage.setVisibility(View.GONE);

            StringBuilder stringDetailAvg = new StringBuilder();
            // Iterate through averages and set respective TextViews

            String[] avgNums = {"3", "5", "12", "50", "100", "1000"};
            for (int i = 0; i < 6; i++) {
                if ((i == 0 && sessionStatsMo3Enabled) || (1 <= i && i <= 4)
                        || (i == 5 && sessionStatsAo1000Enabled)) {
                    if (sessionStatsEnabled && averageRecordsEnabled && hasStoppedTimerOnce &&
                            sessionCurrentAvg[i] > 0 && isAllTimeBestAvgUpdate[i]) {
                        // Create string.
                        stringDetailAvg.append("<u><b>").append(detailTextNamesArray[i]).append(avgNums[i]).append(": ")
                                .append(convertTimeToString(sessionCurrentAvg[i], FORMAT_STATS, currentPuzzle)).append("</b></u>");

                        // Show record message, if it was not shown before
                        if (!hasShownRecordMessage && !isRunning && !countingDown) {
                            detailAverageRecordMesssage.setVisibility(View.VISIBLE);
                            detailAverageRecordMesssage
                                    .animate()
                                    .alpha(1)
                                    .setDuration(mAnimationDuration);
                            hasShownRecordMessage = true;
                        }
                    } else if (sessionStatsEnabled) {
                        stringDetailAvg.append(detailTextNamesArray[i]).append(avgNums[i]).append(": ")
                                .append(convertTimeToString(sessionCurrentAvg[i], FORMAT_STATS, currentPuzzle));
                    }
                    stringDetailAvg.append("<br>");
                }
            }
            // remove last "<br>"
            stringDetailAvg.setLength(stringDetailAvg.length() - 4);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                detailTextAvg.setText(Html.fromHtml(stringDetailAvg.toString(), Html.FROM_HTML_MODE_LEGACY));
            } else {
                detailTextAvg.setText(Html.fromHtml(stringDetailAvg.toString()));
            }
        }

        if (recentResultsEnabled) {
            // show recent result
            StringBuilder stringRecentResult = new StringBuilder();
            stringRecentResult.append(getString(R.string.timer_recent_results) + "\n");
            long recentTimes[] = stats.getAverageOf(5, true).getRecentTimes();
            for (int i = 0; i < recentTimes.length; i++) {
                stringRecentResult.append(convertTimeToString(recentTimes[i], FORMAT_SINGLE, currentPuzzle) + "\n");
            }
            recentResultText.setText(stringRecentResult.toString());
        }

        if (!isRunning && !countingDown)
            showDetailStats();

    }

    private void generateScrambleImage() {
        new GenerateScrambleImage().execute();
    }

    private void showToolbar() {
        unlockOrientation(getActivity());
        // Resize startTimerLayout to have the little margin
        // on the left that allows the user to open the side menu.
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) startTimerLayout.getLayoutParams();
        params.leftMargin = ThemeUtils.dpToPix(mContext, 16);
        startTimerLayout.requestLayout();
        broadcast(CATEGORY_UI_INTERACTIONS, ACTION_TIMER_STOPPED);
    }

    private void showItems() {

        // reset chronometer position
        chronometer.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(mAnimationDuration);
        inspectionText.animate()
                .translationY(0)
                .setDuration(mAnimationDuration);

        if (scrambleEnabled) {
            scrambleBox.setVisibility(View.VISIBLE);
            scrambleBox.animate()
                    .alpha(1)
                    .translationY(0)
                    .setDuration(mAnimationDuration);
            scrambleBox.setEnabled(true);
            if (scrambleImgEnabled) {
                scrambleImg.setEnabled(true);
                showImage();
            }
        }
        if (buttonsEnabled && !isCanceled) {
            quickActionButtons.setEnabled(true);
            quickActionButtons.setVisibility(View.VISIBLE);
            quickActionButtons.animate()
                    .alpha(.9f)
                    .setDuration(mAnimationDuration);
        }
    }

    private void showDetailStats() {
        if (sessionStatsEnabled) {
            detailTextAvg.setVisibility(View.VISIBLE);
            detailTextAvg.animate()
                    .alpha(1)
                    .translationY(0)
                    .setDuration(mAnimationDuration);

            detailTextOther.setVisibility(View.VISIBLE);
            detailTextOther.animate()
                    .alpha(1)
                    .translationY(0)
                    .setDuration(mAnimationDuration);
        }

        if (recentResultsEnabled) {
            recentResultText.setVisibility(View.VISIBLE);
            recentResultText.animate()
                    .alpha(1)
                    .translationY(0)
                    .setDuration(mAnimationDuration);
        }
    }

    private void showImage() {
        scrambleImg.setVisibility(View.VISIBLE);
        scrambleImg.setEnabled(true);
        scrambleImg.animate()
                .alpha(1)
                .translationY(0)
                .setDuration(mAnimationDuration);
    }

    private void hideImage() {
        scrambleImg.animate()
                .alpha(0)
                .translationY(scrambleImg.getHeight())
                .setDuration(mAnimationDuration)
                .withEndAction(() -> {
                    if (scrambleImg != null) {
                        scrambleImg.setVisibility(View.GONE);
                        scrambleImg.setEnabled(false);
                    }
                });
    }

    private void hideToolbar() {
        lockOrientation(getActivity());
        broadcast(CATEGORY_UI_INTERACTIONS, ACTION_TIMER_STARTED);

        congratsText.setVisibility(View.GONE);
        congratsText.setCompoundDrawables(null, null, null, null);

        // bring chronometer up a bit
        chronometer.animate()
                .scaleX(1.15f)
                .scaleY(1.15f)
                .setDuration(mAnimationDuration);
        inspectionText.animate()
                .translationY(-getActionBarSize())
                .setDuration(mAnimationDuration);

        // Resize startTimerLayout to fill the entire screen. This removes the little margin
        // on the left that allows the user to open the side menu.
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) startTimerLayout.getLayoutParams();
        params.leftMargin = 0;
        startTimerLayout.requestLayout();

        if (scrambleEnabled) {
            scrambleBox.setEnabled(false);
            scrambleBox.animate()
                    .alpha(0)
                    .translationY(-scrambleBox.getHeight())
                    .setDuration(mAnimationDuration)
                    .withEndAction(() -> {if(scrambleBox!=null) scrambleBox.setVisibility(View.INVISIBLE);}); // FIXME: Attempt to invoke virtual method 'void androidx.cardview.widget.CardView.setVisibility(int)' on a null object reference
            if (scrambleImgEnabled) {
                scrambleImg.setEnabled(false);
                hideImage();
            }
        }
        if (sessionStatsEnabled) {
            detailTextAvg.animate()
                    .alpha(0)
                    .translationY(detailTextAvg.getHeight())
                    .setDuration(mAnimationDuration)
                    .withEndAction(() -> {if(detailTextAvg!=null) detailTextAvg.setVisibility(View.INVISIBLE);});
            detailTextOther.animate()
                    .alpha(0)
                    .translationY(detailTextOther.getHeight())
                    .setDuration(mAnimationDuration)
                    .withEndAction(() -> {if(detailTextOther!=null) detailTextOther.setVisibility(View.INVISIBLE);});
        }
        if (recentResultsEnabled) {
            recentResultText.animate()
                    .alpha(0)
                    .translationY(recentResultText.getHeight())
                    .setDuration(mAnimationDuration)
                    .withEndAction(() -> {if(recentResultText!=null) recentResultText.setVisibility(View.INVISIBLE);});
        }
        if (buttonsEnabled) {
            undoButton.setVisibility(View.GONE);
            quickActionButtons.setEnabled(false);
            quickActionButtons.animate()
                    .alpha(0)
                    .setDuration(mAnimationDuration)
                    .withEndAction(() -> {if(quickActionButtons!=null) quickActionButtons.setVisibility(View.GONE);});
        }
        if (averageRecordsEnabled) {
            detailAverageRecordMesssage
                    .animate()
                    .alpha(0)
                    .setDuration(mAnimationDuration)
                    .withEndAction(() -> {if(detailAverageRecordMesssage!=null) detailAverageRecordMesssage.setVisibility(View.GONE);});
        }
    }

    /**
     * Starts the chronometer from zero and removes any color highlight.
     */
    private void startChronometer() {
        chronometer.reset(); // Start from "0.00"; do not resume from the previous time.
        chronometer.start();
        chronometer.setHighlighted(false); // Clear any start cue or hold-for-start highlight.

        if (timeLimitWarning != null)
            timeLimitWarning.start();

        // isRunning should be set before generateNewScramble so the loading spinner doesn't appear
        // during a solve, since generateNewScramble checks if isRunning is false before setting
        // the spinner to visible.
        isRunning = true;

        if (scrambleEnabled && !isTimeDisabled(currentPuzzle)) {
            currentScramble = realScramble;
            generateNewScramble();
        }
    }

    /**
     * Stops the chronometer
     */
    private void stopChronometer() {
        chronometer.stop();
        chronometer.setHighlighted(false);
        if (timeLimitWarning != null)
            timeLimitWarning.cancel();
        isRunning = false;
        hasStoppedTimerOnce = true;
        showToolbar();
    }

    /**
     * Cancels the chronometer and any inspection countdown. Nothing is saved and the timer is
     * reset to zero.
     */
    private void cancelChronometer() {
        if (backCancelEnabled) {
            stopInspectionCountdown();
            stopChronometer();

            chronometer.reset(); // Show "0.00".
            isCanceled = true;
            currentPenalty = NO_PENALTY;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (DEBUG_ME) Log.d(TAG, "onSaveInstanceState()");
        super.onSaveInstanceState(outState);
        outState.putString(SCRAMBLE, realScramble);
        outState.putString(PUZZLE, currentPuzzle);
        outState.putBoolean(HAS_STOPPED_TIMER_ONCE, hasStoppedTimerOnce);
    }

    @Override
    public void onDetach() {
        if (DEBUG_ME) Log.d(TAG, "onDetach()");
        super.onDetach();
        // To fix memory leaks
        unregisterReceiver(mUIInteractionReceiver);
        scrambleGeneratorAsync.cancel(true);
    }

    @Override
    public void onDestroy() {
        if (DEBUG_ME) Log.d(TAG, "onDestroy()");
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        if (DEBUG_ME) Log.d(TAG, "onDestroyView()");
        super.onDestroyView();
        mUnbinder.unbind();
        StatisticsCache.getInstance().unregisterObserver(this);
        mRecentStatistics = null;
    }

    private static void lockOrientation(Activity activity) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        } else {
            Display display = ((WindowManager) activity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            int rotation = display.getRotation();
            int tempOrientation = activity.getResources().getConfiguration().orientation;
            int orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

            switch (tempOrientation) {
                case Configuration.ORIENTATION_LANDSCAPE:
                    if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_90)
                        orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    else
                        orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                case Configuration.ORIENTATION_PORTRAIT:
                    if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_270)
                        orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    else
                        orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
            }
            activity.setRequestedOrientation(orientation);
        }
    }

    private static void unlockOrientation(Activity activity) {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    private void getNewOptimalCross() {
        if (showHintsEnabled) {
            if (optimalCrossAsync != null)
                optimalCrossAsync.cancel(true);
            optimalCrossAsync = new GetOptimalCross(realScramble,
                    optimalCross, optimalXCross,
                    showHintsXCrossEnabled,
                    isRunning,
                    scrambleDialog);
            optimalCrossAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    /**
     * Generates a new scramble and handles everything.
     */
    private void generateNewScramble() {
        if (scrambleEnabled && currentTimerMode.equals(TIMER_MODE_TIMER)) {
            scrambleGeneratorAsync.cancel(true);
            scrambleGeneratorAsync = new GenerateScrambleSequence();
            scrambleGeneratorAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else if (currentTimerMode.equals(TIMER_MODE_TRAINER)) {
            setScramble(TrainerScrambler.generateTrainerCase(getContext(), currentSubset, currentPuzzleCategory));
            canShowHint = false;
            hideButtons(true, true);
        }
    }


    private static class GetOptimalCross extends AsyncTask<Void, Void, String> {

        private String scramble;
        private RubiksCubeOptimalCross optimalCross;
        private RubiksCubeOptimalXCross optimalXCross;
        private boolean showHintsXCrossEnabled;
        private boolean isRunning;
        private BottomSheetDetailDialog scrambleDialog;

        GetOptimalCross(String scramble,
                        RubiksCubeOptimalCross optimalCross,
                        RubiksCubeOptimalXCross optimalXCross,
                        boolean showHintsXCrossEnabled,
                        boolean isRunning,
                        BottomSheetDetailDialog scrambleDialog) {
            this.scramble = scramble;
            this.optimalCross = optimalCross;
            this.optimalXCross = optimalXCross;
            this.showHintsXCrossEnabled = showHintsXCrossEnabled;
            this.isRunning = isRunning;
            this.scrambleDialog = scrambleDialog;
        }

        @Override
        protected void onPreExecute() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_MORE_FAVORABLE);
            Log.d("OptimalCross onPre:", System.currentTimeMillis() + "");
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... voids) {
            String text = "";
            text += optimalCross.getTip(scramble);
            if (showHintsXCrossEnabled) {
                text += "\n\n";
                text += optimalXCross.getTip(scramble);
            }
            return text;
        }

        @Override
        protected void onPostExecute(String text) {
            super.onPostExecute(text);
            if (!isRunning) {
                // Set the hint text
                if (scrambleDialog != null) {
                    scrambleDialog.setHintText(text);
                    scrambleDialog.setHintVisibility(View.VISIBLE);
                }
            }
        }
    }

    private class GenerateScrambleSequence extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_MORE_FAVORABLE);
            if (showHintsEnabled && currentPuzzle.equals(PuzzleUtils.TYPE_333) && scrambleEnabled && scrambleDialog != null) {
                scrambleDialog.setHintVisibility(View.GONE);
                scrambleDialog.dismiss();
            }
            canShowHint = false;
            scrambleText.setText(R.string.generating_scramble);
            scrambleText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            scrambleText.setClickable(false);

            scrambleButtonHint.setVisibility(View.GONE);
            scrambleButtonEdit.setVisibility(View.GONE);
            scrambleButtonReset.setVisibility(View.GONE);
            scrambleButtonManualEntry.setVisibility(View.GONE);
            scrambleProgress.setVisibility(View.VISIBLE);

            hideImage();
            if (!isRunning)
                progressSpinner.setVisibility(View.VISIBLE);
            isLocked = true;
        }

        @Override
        protected String doInBackground(String... params) {
            if (currentPuzzle.equals(PuzzleUtils.TYPE_OTHER))
                return " ";

            if (currentPuzzle.equals(PuzzleUtils.TYPE_333MBLD)) {
                int i;
                String scramble = "";
                for (i = 0; i < currentMbldNum; i++) {
                    if (i != 0) scramble = scramble + "\n";
                    scramble = scramble + (i+1) + ") " + generator.getPuzzle().generateScramble();
                }

                return scramble;
            }

            try {
                return generator.getPuzzle().generateScramble();
            } catch (Exception e) {
                Log.e(TAG, "Invalid puzzle for generator");
            }
            return "An error has ocurred";
        }

        @Override
        protected void onProgressUpdate(Void... values) {

        }

        @Override
        protected void onPostExecute(String scramble) {
            setScramble(scramble);
        }
    }

    /**
     * Updates everything related to displaying the current scramble
     * Ex. scramble image, box, text, dialogs
     */
    private void setScramble(final String scramble) {
        realScramble = scramble;

        if (currentTimerMode.equals(TIMER_MODE_TIMER)) {
            // save the scramble especially for MBLD
            Prefs.edit().putString(R.string.pk_last_used_scramble, realScramble).apply();
        }

        scrambleText.setText(scramble);
        scrambleText.post(() -> {
            if (chronometer != null)
                chronometer.post(() -> {
                    if (scrambleText != null) {
                        // Calculate surrounding layouts to make sure the scramble text doesn't intersect any element
                        // If it does, show only a "tap here to see more" hint instead of the scramble
                        Rect scrambleRect = new Rect(scrambleBox.getLeft(), scrambleBox.getTop(), scrambleBox.getRight(), scrambleBox.getBottom());
                        // The top line calculation is a bit tricky
                        // We first get the top of the bounding box (which isn't necessarily
                        // the top of the actual, visible text. To that, we add the baseline,
                        // which is the measure from the top of the box to the actual baseline
                        // of the text. Then, we add the text size, which gets us to the visible
                        // top.
                        Rect chronometerRect = new Rect(chronometer.getLeft(),
                                (int) (chronometer.getTop()
                                        + chronometer.getBaseline()
                                        - chronometer.getTextSize()
                                        + ThemeUtils.dpToPix(mContext, 28)),
                                chronometer.getRight(),
                                chronometer.getBottom());
                        Rect congratsRect = new Rect(congratsText.getLeft(), congratsText.getTop(), congratsText.getRight(), congratsText.getBottom());

                        if ((Rect.intersects(scrambleRect, chronometerRect)) ||
                                (congratsText.getVisibility() == View.VISIBLE && Rect.intersects(chronometerRect, congratsRect))) {
                            scrambleText.setText("[ " + getString(R.string.scramble_text_tap_hint) + " ]");
                            scrambleBox.setClickable(true);
                            scrambleBox.setOnClickListener(scrambleDetailClickListener);
                        } else {
                            scrambleBox.setOnClickListener(null);
                            scrambleBox.setClickable(false);
                            scrambleBox.setFocusable(false);
                        }
                        scrambleButtonHint.setOnClickListener(scrambleDetailClickListener);
                    }
                });
        });

        if (showHintsEnabled && currentPuzzle.equals(PuzzleUtils.TYPE_333))
            scrambleButtonHint.setVisibility(View.VISIBLE);
        if (manualEntryEnabled || isTimeDisabled(currentPuzzle))
            scrambleButtonManualEntry.setVisibility(View.VISIBLE);
        scrambleProgress.setVisibility(View.GONE);
        scrambleButtonEdit.setVisibility(View.VISIBLE);
        scrambleButtonReset.setVisibility(View.VISIBLE);

        if (scrambleImgEnabled)
            generateScrambleImage();
        else
            progressSpinner.setVisibility(View.INVISIBLE);
        isLocked = false;

        if (showHintsEnabled)
            canShowHint = true;

        // Broadcast the new scramble
        new BroadcastBuilder(CATEGORY_UI_INTERACTIONS, ACTION_SCRAMBLE_MODIFIED)
                .scramble(realScramble)
                .broadcast();
    }

    private View.OnClickListener scrambleDetailClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            scrambleDialog = new BottomSheetDetailDialog();
            scrambleDialog.setDetailText(realScramble);
            scrambleDialog.setDetailTextSize(scrambleTextSize);
            if (canShowHint && showHintsEnabled && currentPuzzle.equals(TYPE_333)) {
                getNewOptimalCross();
                scrambleDialog.hasHints(true);
            }
            if (mFragManager != null)
                scrambleDialog.show(mFragManager, "fragment_dialog_scramble_detail");
        }
    };

    private class GenerateScrambleImage extends AsyncTask<Void, Void, Drawable> {

        @Override
        protected Drawable doInBackground(Void... voids) {
            return generator.generateImageFromScramble(
                    PreferenceManager.getDefaultSharedPreferences(CubicTimer.getAppContext()),
                    realScramble);
        }

        @Override
        protected void onPostExecute(Drawable drawable) {
            super.onPostExecute(drawable);
            if (!isRunning) {
                if (scrambleImg != null)
                    showImage();
            }
            if (progressSpinner != null)
                progressSpinner.setVisibility(View.INVISIBLE);
            if (scrambleImg != null)
                scrambleImg.setImageDrawable(drawable);
            if (expandedImageView != null)
                expandedImageView.setImageDrawable(drawable);
        }
    }

    private void zoomImageFromThumb(final View thumbView) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        rootLayout.getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        globalOffset.y -= scrambleBox.getHeight();
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
        expandedImageView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                        startScale, 1f)).with(ObjectAnimator.ofFloat(expandedImageView,
                        View.SCALE_Y, startScale, 1f));
        set.setDuration(mAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        expandedImageView.setOnClickListener(view -> {
            if (mCurrentAnimator != null) {
                mCurrentAnimator.cancel();
            }

            // Animate the four positioning/sizing properties in parallel,
            // back to their original values.
            AnimatorSet set1 = new AnimatorSet();
            set1.play(ObjectAnimator
                            .ofFloat(expandedImageView, View.X, startBounds.left))
                    .with(ObjectAnimator
                            .ofFloat(expandedImageView,
                                    View.Y, startBounds.top))
                    .with(ObjectAnimator
                            .ofFloat(expandedImageView,
                                    View.SCALE_X, startScaleFinal))
                    .with(ObjectAnimator
                            .ofFloat(expandedImageView,
                                    View.SCALE_Y, startScaleFinal));
            set1.setDuration(mAnimationDuration);
            set1.setInterpolator(new DecelerateInterpolator());
            set1.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    thumbView.setAlpha(1f);
                    expandedImageView.setVisibility(View.GONE);
                    mCurrentAnimator = null;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    thumbView.setAlpha(1f);
                    expandedImageView.setVisibility(View.GONE);
                    mCurrentAnimator = null;
                }
            });
            set1.start();
            mCurrentAnimator = set1;
        });
    }

    // Stack Timer Support
    private void connect() {
        // to avoid IllegalStateException on getString that reported by Android Vitals
        if (getActivity() == null) {
            return;
        }

        Log.d(TAG, "UART connect : start");
        if (!stackTimerEnabled || isTimeDisabled(currentPuzzle)) {
            Log.d(TAG, "UART connect : disabled");
            serialStatusMessage.setText(getString(R.string.timer_serial_status_message)
                    + getString(R.string.timer_serial_status_disabled_message));
            return;
        }

        // In general, TimerFragment goes onPause->onResume when USB connected and bluetooth is disconnected.
        // So, following condition is not taken.
        if (bleClientManager != null && bleClientManager.isConnected()) {
            Log.d(TAG, "UART connect : BLE connected");
            return;
        }

        serialStatusMessage.setText(getString(R.string.timer_serial_status_message)
                + getString(R.string.timer_serial_status_disconnect_message));
        // Find all available drivers from attached devices.
        UsbManager usbManager = (UsbManager) getActivity().getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager);
        if (availableDrivers.isEmpty()) {
            Log.d(TAG, "UART connect : empty");
            serialStatusMessage.setText(getString(R.string.timer_serial_status_message)
                    + getString(R.string.timer_serial_status_disconnect_no_connection_message));
            return;
        }

        // Open a connection to the first available driver.
        UsbSerialDriver driver = availableDrivers.get(0);
        if (driver == null) {
            Log.d(TAG, "UART connect : driver null");
            serialStatusMessage.setText(getString(R.string.timer_serial_status_message)
                    + getString(R.string.timer_serial_status_disconnect_null_message));
            return;
        }
        UsbDeviceConnection usbConnection = usbManager.openDevice(driver.getDevice());
        if (usbConnection == null && !usbManager.hasPermission(driver.getDevice())) {
            Log.d(TAG, "UART connect : no permission");
            serialStatusMessage.setText(getString(R.string.timer_serial_status_message)
                    + getString(R.string.timer_serial_status_disconnect_no_permission_message));
            int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_MUTABLE : 0;
            PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(getActivity(), 0, new Intent(INTENT_ACTION_GRANT_USB), flags);
            usbManager.requestPermission(driver.getDevice(), usbPermissionIntent);
            return;
        }
        if (usbConnection == null) {
            if (!usbManager.hasPermission(driver.getDevice())) {
                Log.d(TAG, "UART connect : permission denied");
                serialStatusMessage.setText(getString(R.string.timer_serial_status_message)
                        + getString(R.string.timer_serial_status_disconnect_permission_denied_message));
            } else {
                Log.d(TAG, "UART connect : open failed");
                serialStatusMessage.setText(getString(R.string.timer_serial_status_message)
                        + getString(R.string.timer_serial_status_disconnect_open_failed_message));
            }
            return;
        }

        usbSerialPort = driver.getPorts().get(0); // Most devices have just one port (port 0)
        try {
            usbSerialPort.open(usbConnection);
            usbSerialPort.setParameters(1200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            usbIoManager = new SerialInputOutputManager(usbSerialPort, this);
            usbIoManager.start();
            Log.d(TAG, "UART connect : connected");
            serialStatusMessage.setText(getString(R.string.timer_serial_status_message)
                    + getString(R.string.timer_serial_status_connect_message));
            isSerialConnected = true;
        } catch (Exception e) {
            Log.d(TAG, "UART connect : open exception");
            serialStatusMessage.setText(getString(R.string.timer_serial_status_message)
                    + getString(R.string.timer_serial_status_disconnect_open_exception_message));
            disconnect();
        }
    }

    private void disconnect() {
        isSerialConnected = false;
        if (!stackTimerEnabled) {
            serialStatusMessage.setText(getString(R.string.timer_serial_status_message)
                    + getString(R.string.timer_serial_status_disabled_message));
            return;
        }

        serialStatusMessage.setText(getString(R.string.timer_serial_status_message)
                + getString(R.string.timer_serial_status_disconnect_message));

        if (usbIoManager != null) {
            usbIoManager.setListener(null);
            usbIoManager.stop();
        }
        usbIoManager = null;

        if (usbSerialPort != null) {
            try {
                usbSerialPort.close();
            } catch (IOException ignored) {
            }
        }
        usbSerialPort = null;
    }

    private void updateStackTimer(String str) {
        final boolean inspectionEnabled = Prefs.getBoolean(R.string.pk_inspection_enabled, false)
                && PuzzleUtils.isInspectionEnabled(currentPuzzle);
        final int inspectionTime = Prefs.getInt(R.string.pk_inspection_time, 15);

        // Sometimes, this function is dispatched after view recreated and in that time
        // serialStatusMessage is null. So, checking serial is connected or not to avoid null
        // pointer exception
        if (!isSerialConnected) {
            Log.d(TAG, "UART update after closed");
            return;
        }

        // update debug string
        serialStatusMessage.setText(str);

        // for state detection
        String strTime = str.substring(1, 7);

        // detect timer start
        boolean isStart = (previousTimerString.equals("000000") && !strTime.equals("000000"));

        // detect timer stop
        boolean isStop = (strTime.equals(previousTimerString) && !strTime.equals("000000"));

        // detect reset trigger
        boolean isReset = (!previousTimerString.equals("000000") && strTime.equals("000000"));

        previousTimerString = strTime;

        if (isStart || isReset) {
            Log.d(TAG, "UART detect : (start, reset) = (" + isStart + ", " + isReset + ")");
        }

        // set time from stack timer to chronometer
        if (!isReset && isRunning && isExternalTimer) {
            chronometer.setExternalTime(str);
        }

        // start/stop timer
        if (isStart) {
            if (countingDown) { // "countingDown == true" => "inspectionEnabled == true"
                stopInspectionCountdown();
                isExternalTimer = true;
                startChronometer(); // Tool-bar is already hidden and remains so.
            } else if (!isRunning) { // Not running and not counting down.
                if (holdingDNF) {
                    // Checks if the user was holding the screen when the inspection
                    // timed out and saved a DNF
                    holdingDNF = false;
                }

                if (!inspectionEnabled) {
                    // Inspection disabled. Hold-for-start disabled, or hold-for-start
                    // enabled, but the hold time was long enough. In the latter case,
                    // the tool-bar will already have been hidden. Start timing!
                    hideToolbar();
                    isExternalTimer = true;
                    startChronometer();
                }
            }
        } else if (isStop) {
            if (isRunning && isExternalTimer) { // => "isRunning == true"
                // stop timer
                animationDone = false;
                isExternalTimer = false;
                stopChronometer();
                if (currentPenalty == PuzzleUtils.PENALTY_PLUSTWO) {
                    // If a user has inspection on and went past his inspection time, he has
                    // two extra seconds do start his time, but with a +2 penalty. This penalty
                    // is recorded above (see plusTwoCountdown), and the timer checks if it's true here.
                    chronometer.setPenalty(PuzzleUtils.PENALTY_PLUSTWO);
                }
                addNewSolve();
            }
        } else if (isReset) {
            if (isRunning && isExternalTimer) { // => "isRunning == true"
                Log.d(TAG, "UART reset detected : cancel chronometer");
                cancelChronometer();
            } else if (!isRunning && inspectionEnabled && !countingDown && inspectionByResetEnabled) {
                Log.d(TAG, "UART reset detected : start inspection");
                hideToolbar();
                startInspectionCountdown(inspectionTime);
            } else {
                Log.d(TAG, "UART reset detected : (isRunning, isExternalTimer, inspectionEnabled, countingDown, inspectionByResetEnabled) = ("
                        + isRunning + ", " + isExternalTimer + ", " + inspectionEnabled + ", "
                        + countingDown + ", " + inspectionByResetEnabled + ")");
            }
        }
    }

    @Override
    public void onNewData(byte[] data) {
        for (int i = 0; i < data.length; i++) {
            if (' ' <= data[i] && data[i] <= '~') {
                stackTimerString.append(new String(data, i, 1));
            } else if (data[i] == 0x0A) {
                if (stackTimerString.length() > 8) {
                    String org = stackTimerString.toString();
                    stackTimerString.delete(0, stackTimerString.length() - 8);
                    Log.d(TAG, "UART str       : " + org + " -> " + stackTimerString.toString());
                }

                String str = stackTimerString.toString();
                stackTimerString.setLength(0);

                String valid_status = "IA SLRC";
                if (str.length() == 8) {
                    byte[] bytes = str.getBytes(StandardCharsets.US_ASCII);
                    int check_sum = 64;
                    for (int j = 1; j < 7; j++) {
                        check_sum += bytes[j] - '0';
                    }
                    if (bytes[7] == check_sum && valid_status.contains(str.substring(0, 1))) {
                        mainLooper.post(() -> {
                            updateStackTimer(str);
                        });
                    } else {
                        Log.d(TAG, "UART str       : " + str);
                        Log.d(TAG, "UART raw       : " + bytes[0] + bytes[1] + bytes[2] + bytes[3] + bytes[4] + bytes[5] + bytes[6] + bytes[7]);
                        Log.d(TAG, "UART Check sum : " + check_sum + " " + bytes[7]);
                    }
                } else {
                    Log.d(TAG, "UART str       : " + str);
                }
            } else if (data[i] == 0x0D) {
            } else {
                stackTimerString.append("_");
            }
        }
    }

    @Override
    public void onRunError(Exception e) {
        Log.d(TAG, "onRunError");
        mainLooper.post(() -> {
            disconnect();
        });
    }

    private void startBleScan() {
        if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.d(TAG, "BLE Scan : " + "No support");
            return;
        }

        if (isSerialConnected) {
            Log.d(TAG, "BLE Scan : " + "serial connected");
            return;
        }

        if (!smartTimerEnabled || isTimeDisabled(currentPuzzle)) {
            Log.d(TAG, "BLE Scan : " + "disabled");
            return;
        }

        if (bleClientManager != null && bleClientManager.isConnected()) {
            disconnectBle();
            return;
        }

        ArrayList<String> requestPermissions = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // BLUETOOTH_CONNECT and BLUETOOTH_SCAN only for upper API31(S)
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "BLE Scan : " + "No BLUETOOTH_CONNECT");
                requestPermissions.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "BLE Scan : " + "No BLUETOOTH_SCAN");
                requestPermissions.add(Manifest.permission.BLUETOOTH_SCAN);
            }
        } else {
            // ACCESS_FINE_LOCATION only for lower API30(R)
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "BLE Scan : " + "No ACCESS_FINE_LOCATION");
                MaterialDialog dialog = ThemeUtils.roundDialog(mContext, new MaterialDialog.Builder(mContext)
                        .title(getString(R.string.ble_permission_title))
                        .content(getString(R.string.ble_permission_content))
                        .positiveText(getString(R.string.ble_permission_next))
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        REQUEST_BLE_PERMISSION);
                            }
                        })
                        .show());
                return;
            }
        }

        // request permissions. onRequestPermissionsResult is at MainActivity
        if (!requestPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(getActivity(),
                    requestPermissions.toArray(new String[0]), REQUEST_BLE_PERMISSION);
            return;
        }

        // check whether Bluetooth is enabled or not
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Log.d(TAG, "BLE Scan : " + "No BluetoothAdapter");
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {
            Log.d(TAG, "BLE Scan : " + "Bluetooth disabled");
            // if Bluetooth is disabled by setting send enable request
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        // First, start scanning with short period
        startBleScanInternal(500);

        dialogBleScan = ThemeUtils.roundDialog(mContext, new MaterialDialog.Builder(mContext)
                .title(getString(R.string.ble_scan_title))
                .content(getString(R.string.ble_scan_content))
                .items(new ArrayList<CharSequence>())
                .itemsCallback((dialog, view, which, text) -> {
                    BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
                    scanner.stopScan(mLeScanCallback);
                    Log.d(TAG, "BLE Scan selected : " + which + ", " + text);
                    bleClientManager.connect(bleDevices.get(which)).enqueue();
                })
                .negativeText(getString(R.string.ble_scan_cancel))
                .onAny((dialog, which) -> {
                    BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
                    scanner.stopScan(mLeScanCallback);
                })
                .show());
        dialogBleScan.setOnCancelListener(dialog -> BluetoothLeScannerCompat.getScanner().stopScan(mLeScanCallback));
    }

    private void startBleScanInternal(long reportDelayMillis) {
        bleScanPeriod = reportDelayMillis;

        // scan bluetooth devices
        BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        ScanSettings settings = new ScanSettings.Builder()
                .setLegacy(false)
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setReportDelay(reportDelayMillis)
                .setUseHardwareBatchingIfSupported(false)   // The use of hardware batch sometimes results in larger delays
                .build();
        List<ScanFilter> filters = new ArrayList<>();
        filters.add(new ScanFilter.Builder()
                .setServiceUuid(ParcelUuid.fromString(GANTIMER_TIMER_SERVICE_UUID))
                .build());
        filters.add(new ScanFilter.Builder()
                .setManufacturerData(GAN_MANUFACTUREID, new byte[]{}, new byte[]{})
                .build());
        scanner.startScan(filters, settings, mLeScanCallback);
    }

    private ScanCallback mLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Log.d(TAG, "BLE Scan : " + "onBatchScanResults");

            ArrayList<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
            for (ScanResult result : results) {
                devices.add(result.getDevice());
                //Log.d(TAG, "BLE Scan : Name = " + result.getDevice().getName());
                //Log.d(TAG, "BLE Scan : Name = " + result.getScanRecord().getDeviceName());
                //Log.d(TAG, "BLE Scan : Addr = " + result.getDevice().getAddress());
            }
            Collections.sort(devices, new Comparator<BluetoothDevice>() {
                @SuppressLint("MissingPermission")
                @Override
                public int compare(BluetoothDevice personFirst, BluetoothDevice personSecond) {
                    return personFirst.getAddress().compareTo(personSecond.getAddress());
                }});

            bleDevices = devices;

            ArrayList<CharSequence> items = new ArrayList<CharSequence>();
            for (BluetoothDevice device : bleDevices) {
                if (device.getName() != null)
                    items.add(device.getName() + " (" + device.getAddress() + ")");
                else
                    items.add(device.getAddress());
            }
            String[] array = items.toArray(new String[items.size()]);
            dialogBleScan.setItems(array);

            // Slow down scanning period after first device found
            if (bleScanPeriod == 500 && !results.isEmpty()) {
                BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
                scanner.stopScan(mLeScanCallback);
                startBleScanInternal(5000);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.d(TAG, "BLE Scan : " + "onScanFailed");
            BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
            scanner.stopScan(mLeScanCallback);
        }
    };

    private void disconnectBle() {
        if (bleClientManager != null && bleClientManager.isConnected()) {
            Log.d(TAG, "BLE disconnect");
            bleClientManager.disconnect().enqueue();
        }
    }

    class BleClientManager extends BleManager {
        private static final String TAG = "BleClientManager";

        public BleClientManager(@NonNull final Context context) {
            super(context);
        }

        // ==== Logging =====

        @Override
        public int getMinLogPriority() {
            // Use to return minimal desired logging priority.
            return Log.VERBOSE;
        }

        @Override
        public void log(int priority, @NonNull String message) {
            // Log from here.
            Log.println(priority, TAG, message);
        }

        // ==== Required implementation ====

        // This is a reference to a characteristic that the manager will use internally.
        private BluetoothGattCharacteristic stateCharacteristic;

        @Override
        protected boolean isRequiredServiceSupported(@NonNull BluetoothGatt gatt) {
            // Here obtain instances of your characteristics.
            // Return false if a required service has not been discovered.
            BluetoothGattService timerService = gatt.getService(UUID.fromString(GANTIMER_TIMER_SERVICE_UUID));
            if (timerService != null) {
                stateCharacteristic = timerService.getCharacteristic(UUID.fromString(GANTIMER_STATE_CHARACTERISTIC_UUID));
            }
            return stateCharacteristic != null;
        }

        @Override
        protected void initialize() {
            // Initialize your device.
            // This means e.g. enabling notifications, setting notification callbacks, or writing
            // something to a Control Point characteristic.
            // Kotlin projects should not use suspend methods here, as this method does not suspend.
            requestMtu(517)
                    .enqueue();

            // GAN Smart/Halo Timer state characteristic notification
            setNotificationCallback(stateCharacteristic)
                    .with(
                            new DataReceivedCallback() {
                                @Override
                                public void onDataReceived(@NonNull BluetoothDevice device, @NonNull Data data) {
                                    final int header = data.getIntValue(Data.FORMAT_UINT8, 0);
                                    final int length = data.getIntValue(Data.FORMAT_UINT8, 1);
                                    final int prefix = data.getIntValue(Data.FORMAT_UINT8, 2);
                                    final int state =  data.getIntValue(Data.FORMAT_UINT8, 3);
                                    int min = 0;
                                    int sec = 0;
                                    int milli = 0;
                                    String timerStr = "";

                                    if ((state == GANTIMER_STATE_STOPPED || state == GANTIMER_STATE_IDLE)
                                            && length == 0x08) {
                                        min = data.getIntValue(Data.FORMAT_UINT8, 4);
                                        sec = data.getIntValue(Data.FORMAT_UINT8, 5);
                                        milli = data.getIntValue(Data.FORMAT_UINT16_LE, 6);
                                        timerStr = String.format(" %01d%02d%03d", min, sec, milli);
                                    }
                                    if (header == 0xFE && prefix == 0x01) {
                                        Log.d(TAG, "BLE data notify OK : " + state + ", " + min + ":" + sec + ":" + milli);
                                        updateGanTimer(state, timerStr);
                                    } else {
                                        Log.d(TAG, "BLE data notify NG : " + data.toString());
                                    }

                                }
                            }
                    );
            enableNotifications(stateCharacteristic).enqueue();

            bleStatusMessage.setText(getString(R.string.timer_ble_status_message) + getString(R.string.timer_ble_status_connect_message));
            broadcast(CATEGORY_UI_INTERACTIONS, ACTION_BLUETOOTH_CONNECTED);
        }

        @Override
        protected void onServicesInvalidated() {
            // This method is called when the services get invalidated, i.e. when the device
            // disconnects.
            // References to characteristics should be nullified here.
            stateCharacteristic = null;
            // sometime this callback called after fragment detached, so checking bleStatusMessage
            if (bleStatusMessage != null)
                bleStatusMessage.setText(getString(R.string.timer_ble_status_message) + getString(R.string.timer_ble_status_disconnect_message));
            Log.d(TAG, "BLE disconnected");
            broadcast(CATEGORY_UI_INTERACTIONS, ACTION_BLUETOOTH_DISCONNECTED);
        }

        // ==== Public API ====

        // Here you may add some high level methods for your device:
    }

    private void updateGanTimer(int state, String str) {
        final boolean inspectionEnabled = Prefs.getBoolean(R.string.pk_inspection_enabled, false)
                && PuzzleUtils.isInspectionEnabled(currentPuzzle);
        final int inspectionTime = Prefs.getInt(R.string.pk_inspection_time, 15);

        // update debug string
        String[] strState = getResources().getStringArray(R.array.timer_gan_timer_state);
        if (state < strState.length)
            bleStatusMessage.setText(getString(R.string.timer_ble_status_message) + strState[state] + str);

        // detect timer start
        boolean isStart = (state == GANTIMER_STATE_RUNNIG);

        // detect timer stop
        boolean isStop = (state == GANTIMER_STATE_STOPPED);

        // detect reset trigger
        boolean isReset = (state == GANTIMER_STATE_IDLE);

        // detect timer ready
        boolean isReady = (state == GANTIMER_STATE_GET_SET);

        // start/stop timer
        if (isStart) {
            if (countingDown) { // "countingDown == true" => "inspectionEnabled == true"
                stopInspectionCountdown();
                isExternalTimer = true;
                startChronometer(); // Tool-bar is already hidden and remains so.
            } else if (!isRunning) { // Not running and not counting down.
                if (holdingDNF) {
                    // Checks if the user was holding the screen when the inspection
                    // timed out and saved a DNF
                    holdingDNF = false;
                }

                if (!inspectionEnabled) {
                    // Inspection disabled. Hold-for-start disabled, or hold-for-start
                    // enabled, but the hold time was long enough. In the latter case,
                    // the tool-bar will already have been hidden. Start timing!
                    hideToolbar();
                    isExternalTimer = true;
                    startChronometer();
                }
            }
        } else if (isStop) {
            if (isRunning && isExternalTimer) { // => "isRunning == true"
                // stop timer
                animationDone = false;
                isExternalTimer = false;
                chronometer.setExternalTime(str);
                stopChronometer();
                if (currentPenalty == PuzzleUtils.PENALTY_PLUSTWO) {
                    // If a user has inspection on and went past his inspection time, he has
                    // two extra seconds do start his time, but with a +2 penalty. This penalty
                    // is recorded above (see plusTwoCountdown), and the timer checks if it's true here.
                    chronometer.setPenalty(PuzzleUtils.PENALTY_PLUSTWO);
                }
                addNewSolve();
            }
        } else if (isReset) {
            if (isRunning && isExternalTimer) { // => "isRunning == true"
                Log.d(TAG, "GAN Timer reset detected : cancel chronometer");
                cancelChronometer();
            } else if (!isRunning && inspectionEnabled && !countingDown && inspectionByResetEnabled) {
                Log.d(TAG, "GAN Timer reset detected : start inspection");
                hideToolbar();
                startInspectionCountdown(inspectionTime);
            } else {
                Log.d(TAG, "GAN Timer reset detected : (isRunning, isExternalTimer, inspectionEnabled, countingDown, inspectionByResetEnabled) = ("
                        + isRunning + ", " + isExternalTimer + ", " + inspectionEnabled + ", "
                        + countingDown + ", " + inspectionByResetEnabled + ")");
            }
        } else if (isReady) {
            if (countingDown) { // "countingDown == true" => "inspectionEnabled == true"
                // Indicate to the user that the hold was long enough.
                chronometer.setHighlighted(true);
            }
        }
    }
}

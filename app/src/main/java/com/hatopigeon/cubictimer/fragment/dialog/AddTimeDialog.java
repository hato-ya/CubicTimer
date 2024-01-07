package com.hatopigeon.cubictimer.fragment.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.hatopigeon.cubicify.R;
import com.hatopigeon.cubictimer.CubicTimer;
import com.hatopigeon.cubictimer.items.Solve;
import com.hatopigeon.cubictimer.listener.DialogListener;
import com.hatopigeon.cubictimer.utils.PuzzleUtils;
import com.hatopigeon.cubictimer.utils.TTIntent;
import com.hatopigeon.cubictimer.utils.ThemeUtils;
import com.hatopigeon.cubictimer.watcher.SolveTimeNumberTextWatcher;
import com.hatopigeon.cubictimer.watcher.SolveTimeNumberTextWatcherSecond;

import org.joda.time.DateTime;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.hatopigeon.cubictimer.utils.TTIntent.ACTION_GENERATE_SCRAMBLE;
import static com.hatopigeon.cubictimer.utils.TTIntent.ACTION_TIMES_MODIFIED;
import static com.hatopigeon.cubictimer.utils.TTIntent.ACTION_TIME_ADDED_MANUALLY;
import static com.hatopigeon.cubictimer.utils.TTIntent.CATEGORY_TIME_DATA_CHANGES;
import static com.hatopigeon.cubictimer.utils.TTIntent.CATEGORY_UI_INTERACTIONS;
import static com.hatopigeon.cubictimer.utils.TTIntent.broadcast;

/**
 * Shows the algList dialog
 */
public class AddTimeDialog extends DialogFragment {

    private Unbinder mUnbinder;

    @BindView(R.id.nameText)
    AppCompatTextView nameText;

    @BindView(R.id.edit_text_time)
    AppCompatEditText timeEditText;

    @BindView(R.id.button_more)
    View moreButton;

    @BindView(R.id.solvedText)
    AppCompatTextView solvedText;

    @BindView(R.id.edit_text_solved)
    AppCompatEditText solvedEditText;

    @BindView(R.id.penaltyText)
    AppCompatTextView penaltyText;

    @BindView(R.id.edit_text_penalty)
    AppCompatEditText penaltyEditText;

    @BindView(R.id.check_scramble)
    AppCompatCheckBox useCurrentScramble;

    @BindView(R.id.button_save)
    View saveButton;

    private DialogListener  dialogListener;

    private String currentPuzzle;
    private String currentScramble;
    private String currentPuzzleSubtype;
    private int currentMbldNum;
    private int currentMbldPenaltyNum;
    private long currentTime;
    private Solve currentSolve;

    private int mCurrentPenalty = PuzzleUtils.NO_PENALTY;
    private String mCurrentComment = "";

    private static final int MODE_ADD = 0;
    private static final int MODE_EDIT = 1;
    private int mMode;

    private Context mContext;

    public interface AddTimeCallback {
        void onAddTime(Solve solve);
    }

    protected AddTimeCallback addTimeCallback;

    @SuppressLint("RestrictedApi")
    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {
                case R.id.button_save:
                    if (timeEditText.getText().toString().length() > 0) {
                        long time = 0;
                        if (currentPuzzle.equals(PuzzleUtils.TYPE_333FMC)) {
                            // set move count as second (time is milli-second units)
                            time = Integer.parseInt(timeEditText.getText().toString()) * 1000;
                        } else if (currentPuzzle.equals(PuzzleUtils.TYPE_333MBLD)) {
                            if (solvedEditText.getText().toString().length() > 0
                                    && penaltyEditText.getText().toString().length() > 0) {
                                // set number of puzzles solved as second and number of puzzles as milli-second
                                time = PuzzleUtils.parseAddedTime(timeEditText.getText().toString() + ".00");
                                int solved = Integer.parseInt(solvedEditText.getText().toString());
                                currentMbldPenaltyNum = Math.min(Integer.parseInt(penaltyEditText.getText().toString()), solved);

                                PuzzleUtils.MbldRecord record =
                                        new PuzzleUtils.MbldRecord(solved, currentMbldNum,
                                                time / 1000 + currentMbldPenaltyNum * 2);

                                time = record.getLong();
                                if (mMode == MODE_ADD && record.isDNF())
                                    mCurrentPenalty = PuzzleUtils.PENALTY_DNF;
                            } else {
                                dismiss();
                                return;
                            }
                        } else {
                            time = PuzzleUtils.parseAddedTime(timeEditText.getText().toString());
                            time = mCurrentPenalty == PuzzleUtils.PENALTY_PLUSTWO ? time + 2_000 : time;
                        }

                        if (mMode == MODE_ADD) {
                            final Solve solve = new Solve(
                                    time,
                                    currentPuzzle,
                                    currentPuzzleSubtype,
                                    new DateTime().getMillis(),
                                    useCurrentScramble.isChecked() ? currentScramble : "",
                                    mCurrentPenalty,
                                    currentMbldPenaltyNum,
                                    mCurrentComment,
                                    false);

                            CubicTimer.getDBHandler().addSolve(solve);
                            // The receiver might be able to use the new solve and avoid
                            // accessing the database.
                            new TTIntent.BroadcastBuilder(CATEGORY_UI_INTERACTIONS, ACTION_TIME_ADDED_MANUALLY)
                                    .solve(solve)
                                    .broadcast();

                            // Generate new scramble
                            broadcast(CATEGORY_UI_INTERACTIONS, ACTION_GENERATE_SCRAMBLE);
                        } else {
                            if (addTimeCallback != null) {
                                currentSolve.setTime(time);
                                currentSolve.setPenalty(mCurrentPenalty);
                                currentSolve.setComment(mCurrentComment);
                                currentSolve.setMbldPenaltyNum(currentMbldPenaltyNum);
                                addTimeCallback.onAddTime(currentSolve);
                            }
                        }

                        dismiss();
                    } else {
                        dismiss();
                    }
                    break;
                case R.id.button_more:
                    PopupMenu popupMenu = new PopupMenu(mContext, moreButton);
                    popupMenu.getMenuInflater().inflate(R.menu.menu_add_time_options, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(item -> {
                        switch (item.getItemId()) {
                            case R.id.penalty:
                                if (PuzzleUtils.isTimeDisabled(currentPuzzle)) {
                                    int selectedPenalty = mCurrentPenalty == PuzzleUtils.PENALTY_DNF ? 1 : 0;
                                    ThemeUtils.roundAndShowDialog(mContext, new MaterialDialog.Builder(mContext)
                                            .title(R.string.select_penalty)
                                            .items(R.array.array_penalties_wo_plus2)
                                            .itemsCallbackSingleChoice(selectedPenalty, (dialog, itemView, which, text) -> {
                                                switch (which) {
                                                    case 0: // No penalty
                                                        mCurrentPenalty = PuzzleUtils.NO_PENALTY;
                                                        break;
                                                    case 1: // DNF
                                                        mCurrentPenalty = PuzzleUtils.PENALTY_DNF;
                                                        break;
                                                }
                                                return true;
                                            })
                                            .negativeText(R.string.action_cancel)
                                            .build());
                                } else {
                                    ThemeUtils.roundAndShowDialog(mContext, new MaterialDialog.Builder(mContext)
                                            .title(R.string.select_penalty)
                                            .items(R.array.array_penalties)
                                            .itemsCallbackSingleChoice(mCurrentPenalty, (dialog, itemView, which, text) -> {
                                                switch (which) {
                                                    case 0: // No penalty
                                                        mCurrentPenalty = PuzzleUtils.NO_PENALTY;
                                                        break;
                                                    case 1: // +2
                                                        mCurrentPenalty = PuzzleUtils.PENALTY_PLUSTWO;
                                                        break;
                                                    case 2: // DNF
                                                        mCurrentPenalty = PuzzleUtils.PENALTY_DNF;
                                                        break;
                                                }
                                                return true;
                                            })
                                            .negativeText(R.string.action_cancel)
                                            .build());
                                }
                                break;
                            case R.id.comment:
                                {
                                    CommentDialog commentDialog = CommentDialog.newInstance(CommentDialog.COMMENT_DIALOG_TYPE_COMMENT, currentPuzzle, mCurrentComment);
                                    commentDialog.setCallback((str) -> {
                                        mCurrentComment = str.toString();
                                    });
                                    FragmentManager manager = getFragmentManager();
                                    if (manager != null)
                                        commentDialog.show(manager, "dialog_comment");
                                }
                                break;
                        }
                        return true;
                    });

                    MenuPopupHelper popupHelper = new MenuPopupHelper(mContext, (MenuBuilder) popupMenu.getMenu(), moreButton);
                    popupHelper.setForceShowIcon(true);
                    popupHelper.show();
                    break;
            }
        }
    };

    public static AddTimeDialog newInstance(String currentPuzzle, String currentPuzzleSubtype, int currentMbldNum, String currentScramble) {
        return newInstance(MODE_ADD, currentPuzzle, currentPuzzleSubtype, currentMbldNum, currentScramble, 0, null);
    }

    public static AddTimeDialog newInstance(String currentPuzzle, String currentPuzzleSubtype, int currentMbldNum, String currentScramble, long currentTime) {
        return newInstance(MODE_ADD, currentPuzzle, currentPuzzleSubtype, currentMbldNum, currentScramble, currentTime, null);
    }

    public static AddTimeDialog newInstance(Solve currentSolve) {
        return newInstance(MODE_EDIT, "", "", 0, "", 0, currentSolve);
    }

    public static AddTimeDialog newInstance(int mode, String currentPuzzle, String currentPuzzleSubtype, int currentMbldNum, String currentScramble, long currentTime, Solve currentSolve) {
        AddTimeDialog timeDialog = new AddTimeDialog();
        Bundle args = new Bundle();
        args.putInt("mode", mode);
        args.putString("puzzle", currentPuzzle);
        args.putString("category", currentPuzzleSubtype);
        args.putInt("mbldnum", currentMbldNum);
        args.putString("scramble", currentScramble);
        args.putLong("time", currentTime);
        args.putParcelable("solve", currentSolve);
        timeDialog.setArguments(args);
        return timeDialog;
    }

    public void setCallback(AddTimeCallback callback) {
        addTimeCallback = callback;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMode = getArguments().getInt("mode");
        currentPuzzle = getArguments().getString("puzzle");
        currentPuzzleSubtype = getArguments().getString("category");
        currentMbldNum = getArguments().getInt("mbldnum");
        currentMbldPenaltyNum = 0;
        currentScramble = getArguments().getString("scramble");
        currentTime = getArguments().getLong("time");
        currentSolve = getArguments().getParcelable("solve");
        if (currentSolve != null) {
            currentPuzzle = currentSolve.getPuzzle();
            // currentPuzzleSubtype = currentSolve.getSubtype();
            if (currentPuzzle.equals(PuzzleUtils.TYPE_333MBLD)) {
                currentMbldNum = (int) (new PuzzleUtils.MbldRecord(currentSolve.getTime()).getAttempted());
                currentMbldPenaltyNum = currentSolve.getMbldPenaltyNum();
            }
            // currentScramble = currentSolve.getScramble();
            currentTime = currentSolve.getTime();
            mCurrentComment = currentSolve.getComment();
            mCurrentPenalty = currentSolve.getPenalty();
        }
        mContext = getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View dialogView = inflater.inflate(R.layout.dialog_add_time, container);
        mUnbinder = ButterKnife.bind(this, dialogView);

        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        saveButton.setOnClickListener(clickListener);
        moreButton.setOnClickListener(clickListener);

        if (currentPuzzle.equals(PuzzleUtils.TYPE_333FMC)) {
            // input move counts instead of time
            nameText.setText(R.string.add_move_count);
            timeEditText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(2)});

            if (mMode == MODE_EDIT) {
                timeEditText.setText(String.valueOf(currentTime));
            }
        } else if (currentPuzzle.equals(PuzzleUtils.TYPE_333MBLD)) {
            // format input time
            timeEditText.addTextChangedListener(new SolveTimeNumberTextWatcherSecond());
            timeEditText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(8)});

            if (mMode == MODE_ADD) {
                if (currentTime != 0)
                    timeEditText.setText(PuzzleUtils.formatTime(currentTime, PuzzleUtils.FORMAT_SECOND));
            } else {
                PuzzleUtils.MbldRecord record = new PuzzleUtils.MbldRecord(currentTime);
                long time = record.getSecond() - currentMbldPenaltyNum*2;
                time = Math.max(Math.min(time, 99*3600+59*60+59), 0)*1000;
                timeEditText.setText(PuzzleUtils.formatTime(time, PuzzleUtils.FORMAT_SECOND));
                solvedEditText.setText(String.valueOf(record.getSolved()));
            }
            penaltyEditText.setText(String.valueOf(currentMbldPenaltyNum));

            solvedText.setVisibility(View.VISIBLE);
            solvedEditText.setVisibility(View.VISIBLE);
            penaltyText.setVisibility(View.VISIBLE);
            penaltyEditText.setVisibility(View.VISIBLE);
        } else {
            // format input time
            timeEditText.addTextChangedListener(new SolveTimeNumberTextWatcher());

            if (mMode == MODE_EDIT) {
                long time = currentTime - (mCurrentPenalty == PuzzleUtils.PENALTY_PLUSTWO ? 2000 : 0);
                time = Math.max(Math.min(time, (99*3600+59*60+59)*1000+990), 0);
                timeEditText.setText(PuzzleUtils.formatTime(time, PuzzleUtils.FORMAT_MILLI));
            }
        }

        if (mMode == MODE_EDIT) {
            useCurrentScramble.setVisibility(View.GONE);
        }

        // Focus on editText and request keyboard
        timeEditText.requestFocus();

        try {
            timeEditText.postDelayed(() ->
                    ((InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE))
                        .showSoftInput(timeEditText, InputMethodManager.SHOW_IMPLICIT), 400);
        } catch (Exception e) {
            Log.e("AddTimeDialog", "Error showing keyboard: " + e);
        }

        return dialogView;
    }

    public void setDialogListener(DialogListener listener) {
        dialogListener = listener;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);

        // Hide keyboard
        try {
            ((InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(timeEditText.getWindowToken(), 0);
        } catch (Exception e) {
            Log.e("AddTimeDialog", "Error hiding keyboard: " + e);
        }

        if (dialogListener != null)
            dialogListener.onDismissDialog();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }
}

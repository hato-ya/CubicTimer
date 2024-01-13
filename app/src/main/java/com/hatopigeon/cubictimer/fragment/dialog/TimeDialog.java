package com.hatopigeon.cubictimer.fragment.dialog;

import static android.text.format.DateUtils.FORMAT_ABBREV_ALL;
import static android.text.format.DateUtils.FORMAT_SHOW_DATE;
import static android.text.format.DateUtils.FORMAT_SHOW_TIME;
import static android.text.format.DateUtils.FORMAT_SHOW_YEAR;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.FragmentManager;

import android.text.Html;
import android.text.InputType;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.hatopigeon.cubicify.R;
import com.hatopigeon.cubictimer.CubicTimer;
import com.hatopigeon.cubictimer.database.DatabaseHandler;
import com.hatopigeon.cubictimer.items.Solve;
import com.hatopigeon.cubictimer.listener.DialogListener;
import com.hatopigeon.cubictimer.utils.AnimUtils;
import com.hatopigeon.cubictimer.utils.PuzzleUtils;
import com.hatopigeon.cubictimer.utils.ScrambleGenerator;
import com.hatopigeon.cubictimer.utils.TTIntent;
import com.hatopigeon.cubictimer.utils.ThemeUtils;

import org.joda.time.DateTime;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Shows the timeList dialog
 */
public class TimeDialog extends DialogFragment {

    private Unbinder mUnbinder;

    @BindView(R.id.timeText)          TextView  timeText;
    @BindView(R.id.puzzlePenaltyText) TextView  penaltyText;
    @BindView(R.id.dateText)          TextView  dateText;
    @BindView(R.id.scrambleText)      TextView  scrambleText;
    @BindView(R.id.editButton)        ImageView editButton;
    @BindView(R.id.commentButton)     ImageView commentButton;
    @BindView(R.id.editRecordButton)  ImageView editRecordButton;
    @BindView(R.id.commentText)       TextView  commentText;
    @BindView(R.id.overflowButton)    ImageView overflowButton;
    @BindView(R.id.scramble_image)    ImageView scrambleImage;

    private long            mId;
    private Solve           solve;
    private DialogListener  dialogListener;

    @SuppressLint("RestrictedApi")
    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            final DatabaseHandler dbHandler = CubicTimer.getDBHandler();

            switch (view.getId()) {
                case R.id.overflowButton:
                    PopupMenu popupMenu = new PopupMenu(getActivity(), overflowButton);
                    if (solve.isHistory())
                        popupMenu.getMenuInflater().inflate(R.menu.menu_list_detail_history, popupMenu.getMenu());
                    else
                        popupMenu.getMenuInflater().inflate(R.menu.menu_list_detail, popupMenu.getMenu());

                    MenuPopupHelper popupHelper = new MenuPopupHelper(mContext, (MenuBuilder) popupMenu.getMenu(), overflowButton);
                    popupHelper.setForceShowIcon(true);

                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.share:
                                    Intent shareIntent = new Intent();
                                    shareIntent.setAction(Intent.ACTION_SEND);
                                    if (!PuzzleUtils.isTimeDisabled(solve.getPuzzle())) {
                                        shareIntent.putExtra(Intent.EXTRA_TEXT, PuzzleUtils.convertTimeToString(solve.getTime(), PuzzleUtils.FORMAT_SINGLE, solve.getPuzzle()) + "s.\n" + solve.getComment() + "\n" + solve.getScramble());
                                    } else {
                                        shareIntent.putExtra(Intent.EXTRA_TEXT, PuzzleUtils.convertTimeToString(solve.getTime(), PuzzleUtils.FORMAT_SINGLE, solve.getPuzzle()) + ".\n" + solve.getComment() + "\n" + solve.getScramble());
                                    }
                                    shareIntent.setType("text/plain");
                                    getContext().startActivity(shareIntent);
                                    break;
                                case R.id.remove:
                                    // do not close popup
                                    item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
                                    item.setActionView(new View(mContext));
                                    ThemeUtils.roundAndShowDialog(mContext, new MaterialDialog.Builder(mContext)
                                            .content(R.string.delete_dialog_confirmation_title)
                                            .positiveText(R.string.delete_dialog_confirmation_button)
                                            .negativeText(R.string.delete_dialog_cancel_button)
                                            .onPositive((dialog, which) -> {
                                                dbHandler.deleteSolveByID(mId);
                                                updateList();
                                            })
                                            .build());
                                    return false;
                                case R.id.history_to:
                                    solve.setHistory(true);
                                    Toast.makeText(getContext(), getString(R.string.sent_to_history), Toast.LENGTH_SHORT).show();
                                    dbHandler.updateSolve(solve);
                                    updateList();
                                    dismiss();
                                    break;
                                case R.id.history_from:
                                    solve.setHistory(false);
                                    Toast.makeText(getContext(), getString(R.string.sent_to_session), Toast.LENGTH_SHORT).show();
                                    dbHandler.updateSolve(solve);
                                    updateList();
                                    dismiss();
                                    break;
                            }
                            return true;
                        }
                    });
                    popupHelper.show();
                    break;
                case R.id.editButton:
                    if (PuzzleUtils.isTimeDisabled(solve.getPuzzle())) {
                        int selectedPenalty = solve.getPenalty() == PuzzleUtils.PENALTY_DNF ? 1 : 0;
                        ThemeUtils.roundAndShowDialog(mContext, new MaterialDialog.Builder(mContext)
                                .title(R.string.select_penalty)
                                .items(R.array.array_penalties_wo_plus2)
                                .itemsCallbackSingleChoice(selectedPenalty, (dialog, itemView, which, text) -> {
                                    switch (which) {
                                        case 0: // No penalty
                                            solve = PuzzleUtils.applyPenalty(solve, PuzzleUtils.NO_PENALTY);
                                            break;
                                        case 1: // DNF
                                            solve = PuzzleUtils.applyPenalty(solve, PuzzleUtils.PENALTY_DNF);
                                            break;
                                    }
                                    dbHandler.updateSolve(solve);
                                    // dismiss dialog
                                    updateList();
                                    return true;
                                })
                                .negativeText(R.string.action_cancel)
                                .build());
                    } else {
                        ThemeUtils.roundAndShowDialog(mContext, new MaterialDialog.Builder(mContext)
                                .title(R.string.select_penalty)
                                .items(R.array.array_penalties)
                                .itemsCallbackSingleChoice(solve.getPenalty(), (dialog, itemView, which, text) -> {
                                    switch (which) {
                                        case 0: // No penalty
                                            solve = PuzzleUtils.applyPenalty(solve, PuzzleUtils.NO_PENALTY);
                                            break;
                                        case 1: // +2
                                            solve = PuzzleUtils.applyPenalty(solve, PuzzleUtils.PENALTY_PLUSTWO);
                                            break;
                                        case 2: // DNF
                                            solve = PuzzleUtils.applyPenalty(solve, PuzzleUtils.PENALTY_DNF);
                                            break;
                                    }
                                    dbHandler.updateSolve(solve);
                                    // dismiss dialog
                                    updateList();
                                    return true;
                                })
                                .negativeText(R.string.action_cancel)
                                .build());
                    }
                    break;
                case R.id.commentButton:
                    {
                        CommentDialog commentDialog = CommentDialog.newInstance(CommentDialog.COMMENT_DIALOG_TYPE_COMMENT, solve.getPuzzle(), solve.getComment());
                        commentDialog.setCallback((str) -> {
                            solve.setComment(str.toString());
                            dbHandler.updateSolve(solve);
                            Toast.makeText(getContext(), getString(R.string.added_comment), Toast.LENGTH_SHORT).show();
                            updateList();
                        });
                        FragmentManager manager = getFragmentManager();
                        if (manager != null)
                            commentDialog.show(manager, "dialog_comment");
                    }
                    break;
                case R.id.editRecordButton:
                    AddTimeDialog addTimeDialog = AddTimeDialog.newInstance(solve);
                    addTimeDialog.setCallback((solve) -> {
                        dbHandler.updateSolve(solve);
                        updateList();
                    });
                    FragmentManager manager = getFragmentManager();
                    if (manager != null)
                        addTimeDialog.show(manager, "dialog_add_time");
                    break;
                case R.id.scrambleText:
                    AnimUtils.toggleContentVisibility(scrambleImage);
                    break;
            }
        }
    };
    private Context mContext;

    public static TimeDialog newInstance(long id) {
        TimeDialog timeDialog = new TimeDialog();
        Bundle args = new Bundle();
        args.putLong("id", id);
        timeDialog.setArguments(args);
        return timeDialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View dialogView = inflater.inflate(R.layout.dialog_time_details, container);
        //this.setEnterTransition(R.anim.activity_slide_in);
        mUnbinder = ButterKnife.bind(this, dialogView);
        mContext = getContext();

        mId = getArguments().getLong("id");

        //Log.d("TIME DIALOG", "mId: " + mId + "\nexists: " + handler.idExists(mId));

        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //getDialog().getWindow().setWindowAnimations(R.style.DialogAnimationScale);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        final Solve matchedSolve = CubicTimer.getDBHandler().getSolve(mId);

        if (matchedSolve != null) {
            solve = matchedSolve;

            timeText.setText(Html.fromHtml(PuzzleUtils.convertTimeToString(solve.getTime(), PuzzleUtils.FORMAT_SMALL_MILLI, solve.getPuzzle())));
            dateText.setText(DateUtils.formatDateTime(mContext, solve.getDate(),
                    FORMAT_SHOW_YEAR|FORMAT_SHOW_DATE|FORMAT_ABBREV_ALL) + "\n"
                    + DateUtils.formatDateTime(mContext, solve.getDate(), FORMAT_SHOW_TIME));

            scrambleText.setText(solve.getScramble());

            if (solve.getPenalty() == PuzzleUtils.PENALTY_DNF)
                penaltyText.setText("DNF");
            else if (solve.getPenalty() == PuzzleUtils.PENALTY_PLUSTWO)
                penaltyText.setText("+2");
            else if (solve.getMbldPenaltyNum() > 0)
                penaltyText.setText("+" + solve.getMbldPenaltyNum()*2);
            else
                penaltyText.setVisibility(View.GONE);

            if (solve.getComment() != null) {
                if (! solve.getComment().equals("")) {
                    commentText.setText(solve.getComment());
                    commentText.setVisibility(View.VISIBLE);
                }
            }

            if (solve.getScramble() != null) {
                if (solve.getScramble().equals(""))
                    scrambleText.setVisibility(View.GONE);
            }

            scrambleText.setOnClickListener(clickListener);
            overflowButton.setOnClickListener(clickListener);
            editButton.setOnClickListener(clickListener);
            commentButton.setOnClickListener(clickListener);
            editRecordButton.setOnClickListener(clickListener);

        }

        return dialogView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new GenerateScrambleImage().execute();
    }

    public void setDialogListener(DialogListener listener) {
        dialogListener = listener;
    }

    private void updateList() {
        if (dialogListener != null) {
            dialogListener.onUpdateDialog();
        } else {
            TTIntent.broadcast(TTIntent.CATEGORY_TIME_DATA_CHANGES, TTIntent.ACTION_TIMES_MODIFIED);
        }
        dismiss();
    }

    @Override
    public void onDestroyView() {
        mUnbinder.unbind();
        if (dialogListener != null)
            dialogListener.onDismissDialog();
        super.onDestroyView();
    }

    private class GenerateScrambleImage extends AsyncTask<Void, Void, Drawable> {

        @Override
        protected Drawable doInBackground(Void... voids) {
            ScrambleGenerator generator = new ScrambleGenerator(solve.getPuzzle());
            return generator.generateImageFromScramble(
                    PreferenceManager.getDefaultSharedPreferences(CubicTimer.getAppContext()),
                    solve.getScramble());
        }

        @Override
        protected void onPostExecute(Drawable drawable) {
            super.onPostExecute(drawable);
            if (scrambleImage != null)
                scrambleImage.setImageDrawable(drawable);
        }
    }

}

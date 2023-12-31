package com.hatopigeon.cubictimer.fragment.dialog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.hatopigeon.cubicify.R;
import com.hatopigeon.cubictimer.listener.DialogListener;
import com.hatopigeon.cubictimer.utils.PuzzleUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class CommentDialog  extends DialogFragment {
    private Unbinder mUnbinder;
    private Context mContext;

    @BindView(R.id.commentDlgTitle) TextView commentDlgTitle;
    @BindView(R.id.commentDlgText)  EditText commentDlgText;
    @BindView(R.id.commentDlgCancel)    TextView commentDlgCancel;
    @BindView(R.id.commentDlgDone)      TextView commentDlgDone;


    @BindView(R.id.commentDlgNxNxN) RelativeLayout swkbNxNxN;
    @BindView(R.id.notation_prime)  AppCompatButton notationPrime;
    @BindView(R.id.notation_B)  AppCompatButton notationB;
    @BindView(R.id.notation_U)  AppCompatButton notationU;
    @BindView(R.id.notation_y)  AppCompatButton notationY;
    @BindView(R.id.notation_two)    AppCompatButton notationTwo;
    @BindView(R.id.notation_L)  AppCompatButton notationL;
    @BindView(R.id.notation_space)  AppCompatButton notationSpace;
    @BindView(R.id.notation_R)  AppCompatButton notationR;
    @BindView(R.id.notation_x)  AppCompatButton notationX;
    @BindView(R.id.notation_w)  AppCompatButton notationW;
    @BindView(R.id.notation_D)  AppCompatButton notationD;
    @BindView(R.id.notation_F)  AppCompatButton notationF;
    @BindView(R.id.notation_z)  AppCompatButton notationZ;
    @BindView(R.id.notation_three)    AppCompatButton notationThree;
    @BindView(R.id.notation_M)  AppCompatButton notationM;
    @BindView(R.id.notation_E)  AppCompatButton notationE;
    @BindView(R.id.notation_S)  AppCompatButton notationS;
    @BindView(R.id.notation_LP)  AppCompatButton notationLP;
    @BindView(R.id.notation_RP)  AppCompatButton notationRP;
    @BindView(R.id.notation_COMMENT)  AppCompatButton notationComment;
    @BindView(R.id.notation_BS) AppCompatButton notationBS;

    public static final int COMMENT_DIALOG_TYPE_COMMENT = 0;
    public static final int COMMENT_DIALOG_TYPE_SCRAMBLE = 1;

    protected static final String ARG_TYPE = "ARG_TYPE";
    protected static final String ARG_PUZZLE = "ARG_PUZZLE";
    protected static final String ARG_PREFILL = "ARG_PREFILL";

    protected InputCallback inputCallback;

    public interface InputCallback {
        void onInput(CharSequence str);
    }

    public static CommentDialog newInstance(int type, String puzzle, String prefill) {
        CommentDialog commentDialog = new CommentDialog();
        Bundle args = new Bundle();
        args.putInt(ARG_TYPE, type);
        args.putString(ARG_PUZZLE, puzzle);
        args.putString(ARG_PREFILL, prefill);
        commentDialog.setArguments(args);
        return commentDialog;
    }

    public void setCallback(InputCallback callback) {
        inputCallback = callback;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View dialogView = inflater.inflate(R.layout.dialog_comment, container);
        mUnbinder = ButterKnife.bind(this, dialogView);

        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        mContext = getContext();

        commentDlgText.setText(getArguments().getString(ARG_PREFILL));
        commentDlgText.setSelection(commentDlgText.getText().length());

        if (getArguments().getInt(ARG_TYPE) == COMMENT_DIALOG_TYPE_SCRAMBLE) {
            commentDlgTitle.setText(R.string.edit_scramble);
            notationM.setVisibility(View.GONE);
            notationE.setVisibility(View.GONE);
            notationS.setVisibility(View.GONE);
            notationLP.setVisibility(View.GONE);
            notationRP.setVisibility(View.GONE);
            notationComment.setVisibility(View.GONE);
        }

        switch(getArguments().getString(ARG_PUZZLE).toString()){
            case PuzzleUtils.TYPE_222:
            case PuzzleUtils.TYPE_333:
            case PuzzleUtils.TYPE_333BLD:
            case PuzzleUtils.TYPE_333FMC:
                notationThree.setVisibility(View.GONE);
                break;
            case PuzzleUtils.TYPE_MEGA:
            case PuzzleUtils.TYPE_PYRA:
            case PuzzleUtils.TYPE_SKEWB:
            case PuzzleUtils.TYPE_CLOCK:
            case PuzzleUtils.TYPE_SQUARE1:
                swkbNxNxN.setVisibility(View.GONE);
                break;

        }

        commentDlgCancel.setOnClickListener(clickListener);
        commentDlgDone.setOnClickListener(clickListener);

        notationPrime.setOnClickListener(clickListener);
        notationB.setOnClickListener(clickListener);
        notationU.setOnClickListener(clickListener);
        notationY.setOnClickListener(clickListener);

        notationTwo.setOnClickListener(clickListener);
        notationL.setOnClickListener(clickListener);
        notationSpace.setOnClickListener(clickListener);
        notationR.setOnClickListener(clickListener);
        notationX.setOnClickListener(clickListener);

        notationW.setOnClickListener(clickListener);
        notationD.setOnClickListener(clickListener);
        notationF.setOnClickListener(clickListener);
        notationZ.setOnClickListener(clickListener);

        notationThree.setOnClickListener(clickListener);
        notationM.setOnClickListener(clickListener);
        notationE.setOnClickListener(clickListener);
        notationS.setOnClickListener(clickListener);

        notationLP.setOnClickListener(clickListener);
        notationRP.setOnClickListener(clickListener);
        notationComment.setOnClickListener(clickListener);
        notationBS.setOnClickListener(clickListener);

        return dialogView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    public void insertText(TextView editText, String str, boolean isAddSpace) {
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        int cursor = Math.min(start, end);

        Editable editable = editText.getEditableText();

        String lastchar = " ";
        if (cursor > 0 && editable.length() > 0)
            lastchar = editable.subSequence(cursor-1, cursor).toString();

        if (isAddSpace&& !lastchar.equals(" ") && !lastchar.equals("\n") && !lastchar.equals("3")
                && !lastchar.equals("("))
            editable.replace(cursor, Math.max(start, end), " " + str);
        else
            editable.replace(cursor, Math.max(start, end), str);
    }

    public void backspaceText(TextView editText) {
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        int from = Math.min(start, end);
        int to = Math.max(start, end);

        Editable editable = editText.getEditableText();

        if (from == to && from > 0 && editable.length() > 0)
            editable.replace(from-1, from, "");
        else
            editable.replace(from, to, "");
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.commentDlgCancel:
                    dismiss();
                    break;
                case R.id.commentDlgDone:
                    if (inputCallback != null)
                        inputCallback.onInput(commentDlgText.getText());
                    dismiss();
                    break;
                case R.id.notation_prime:
                    insertText(commentDlgText, "'", false);
                    break;
                case R.id.notation_B:
                    insertText(commentDlgText, "B", true);
                    break;
                case R.id.notation_U:
                    insertText(commentDlgText, "U", true);
                    break;
                case R.id.notation_y:
                    insertText(commentDlgText, "y", true);
                    break;
                case R.id.notation_two:
                    insertText(commentDlgText, "2", false);
                    break;
                case R.id.notation_L:
                    insertText(commentDlgText, "L", true);
                    break;
                case R.id.notation_space:
                    insertText(commentDlgText, " ", false);
                    break;
                case R.id.notation_R:
                    insertText(commentDlgText, "R", true);
                    break;
                case R.id.notation_x:
                    insertText(commentDlgText, "x", true);
                    break;
                case R.id.notation_w:
                    insertText(commentDlgText, "w", false);
                    break;
                case R.id.notation_D:
                    insertText(commentDlgText, "D", true);
                    break;
                case R.id.notation_F:
                    insertText(commentDlgText, "F", true);
                    break;
                case R.id.notation_z:
                    insertText(commentDlgText, "z", true);
                    break;
                case R.id.notation_three:
                    insertText(commentDlgText, "3", true);
                    break;
                case R.id.notation_M:
                    insertText(commentDlgText, "M", true);
                    break;
                case R.id.notation_E:
                    insertText(commentDlgText, "E", true);
                    break;
                case R.id.notation_S:
                    insertText(commentDlgText, "S", true);
                    break;
                case R.id.notation_LP:
                    insertText(commentDlgText, "(", true);
                    break;
                case R.id.notation_RP:
                    insertText(commentDlgText, ")", false);
                    break;
                case R.id.notation_COMMENT:
                    insertText(commentDlgText, "//", true);
                    break;
                case R.id.notation_BS:
                    backspaceText(commentDlgText);
                    break;
            }
        }
    };
}

package com.hatopigeon.cubictimer.watcher;

import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;

/**
 * A TextWatcher to format a number input into a valid time input for the user
 */
public class SolveTimeNumberTextWatcherSecond implements TextWatcher {

    private boolean isFormatting;
    private int mLen;
    private String mUnformatted;

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (isFormatting)
            return;

        isFormatting = true;

        // Since the keyboard input type is "number", we can't punctuation with the actual
        // filters, so we clear them and restore once we finish formatting
        InputFilter[] filters = s.getFilters(); // save filters
        s.setFilters(new InputFilter[] {});     // clear filters


        // Clear all formatting from editable
        // Regex matches the characters ':', '.', 'h' and a leading zero, if present
        mUnformatted = s.toString().replaceAll("^0+|[h]|:|\\.", "");
        mLen = mUnformatted.length();

        s.clear();
        s.insert(0, mUnformatted);

        switch (mLen) {
            case 3:
                s.insert(1, ":");
                break;
            case 4:
                s.insert(2, ":");
                break;
            case 5:
                s.insert(1, ":");
                s.insert(4, ":");
                break;
            case 6:
                s.insert(2, ":");
                s.insert(5, ":");
                break;
            default:
                break;
        }

        isFormatting = false;

        // Restore filters
        s.setFilters(filters);
    }
}

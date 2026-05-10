package com.hatopigeon.cubictimer.watcher;

import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;

/**
 * A TextWatcher to format a number input into a valid time input for the user
 */
public class SolveTimeNumberTextWatcherThousandth implements TextWatcher {

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
        mUnformatted = s.toString().replaceAll("^0\\.0|^0+|[h]|:|\\.", "");
        mLen = mUnformatted.length();

        s.clear();
        s.insert(0, mUnformatted);

        if (mLen == 1) { // 1 -> 0.001
            s.insert(0, "0.00");
        } else if (mLen == 2) { // 12 -> 0.012
            s.insert(0, "0.0");
        } else if (mLen == 3) { // 123 -> 0.123
            s.insert(0, "0.");
        } else if (mLen == 4) { // 1234 -> 1.234
            s.insert(1, ".");
        } else if (mLen == 5) { // 12345 -> 12.345
            s.insert(2, ".");
        } else if (mLen == 6) { // 123456 -> 1:23.456
            s.insert(1, ":");
            s.insert(4, ".");
        } else if (mLen == 7) { // 1234567 -> 12:34.567
            s.insert(2, ":");
            s.insert(5, ".");
        } else if (mLen == 8) { // 12345678 -> 1:23:45.678
            s.insert(1, ":");
            s.insert(4, ":");
            s.insert(7, ".");
        } else if (mLen == 9) { // 123456789 -> 12:34:56.789
            s.insert(2, ":");
            s.insert(5, ":");
            s.insert(8, ".");
        }

        isFormatting = false;

        // Restore filters
        s.setFilters(filters);
    }
}

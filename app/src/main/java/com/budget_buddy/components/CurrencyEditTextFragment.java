package com.budget_buddy.components;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class CurrencyEditTextFragment extends AppCompatEditText{

    private String previousNumber;

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // do nothing dood
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // do nothing dood
        }

        @Override
        public void afterTextChanged(Editable s) {
            // Get string to work with
            String str = s.toString();
            if (str.equals(".")){
                return;
            }
            if (str.contains(".")){
                DecimalFormat format = new DecimalFormat("##.00");
                format.setRoundingMode(RoundingMode.DOWN);
                Log.i("oh jeez", format.format(str).toString());
                CurrencyEditTextFragment.super.setText(format.format(str).toString());
            }
            // Prevent recursive call for afterTextChanged
            if (str.equals(previousNumber) || str.isEmpty()){
                return;
            }
            previousNumber = str;
        }
    };

    public CurrencyEditTextFragment(Context context) {
        super(context);
        this.addTextChangedListener(textWatcher);
    }

    public CurrencyEditTextFragment(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.addTextChangedListener(textWatcher);
    }

    public CurrencyEditTextFragment(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.addTextChangedListener(textWatcher);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);

        if (focused) {
            this.addTextChangedListener(textWatcher);
        } else {
            this.removeTextChangedListener(textWatcher);
        }
    }


}

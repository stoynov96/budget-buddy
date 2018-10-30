package com.budget_buddy.components;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class CurrencyEditTextFragment extends AppCompatEditText{

    private final int MAX_LENGTH = 20;
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
            // Prevent recursive call for afterTextChanged
            if (str.equals(previousNumber) || str.isEmpty()){
                return;
            }
            previousNumber = str;
            if (str.equals(".")){
                return;
            }
            if (str.contains(".")){
                BigDecimal parsed = new BigDecimal(str);
                DecimalFormat format = new DecimalFormat("#.#");
                format.setRoundingMode(RoundingMode.DOWN);
                Log.i("oh jeez", format.format(parsed));
                // Remove before we set the text
                CurrencyEditTextFragment.super.removeTextChangedListener(this);
                CurrencyEditTextFragment.super.setText(format.format(parsed));
                setSelection();
                // Add it back
                CurrencyEditTextFragment.super.addTextChangedListener(this);
            }



        }
    };

    public CurrencyEditTextFragment(Context context) {
        super(context);
        this.addTextChangedListener(textWatcher);
        //this.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
    }

    public CurrencyEditTextFragment(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.addTextChangedListener(textWatcher);
        //this.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
    }

    public CurrencyEditTextFragment(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.addTextChangedListener(textWatcher);
        //this.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
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


    private void setSelection(){
        if (CurrencyEditTextFragment.super.getText().length() <= MAX_LENGTH) {
            CurrencyEditTextFragment.super.setSelection(CurrencyEditTextFragment.super.getText().length());
        } else {
            CurrencyEditTextFragment.super.setSelection(MAX_LENGTH);
        }
    }
}

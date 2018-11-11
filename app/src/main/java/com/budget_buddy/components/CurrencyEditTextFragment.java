package com.budget_buddy.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.v7.widget.AppCompatEditText;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;

import com.budget_buddy.R;

import java.text.NumberFormat;


public class CurrencyEditTextFragment extends AppCompatEditText{

    private int MAX_LENGTH = 20;

    private NumberFormat numberFormat = NumberFormat.getCurrencyInstance();

    public CurrencyEditTextFragment(Context context) {
        super(context);
        this.setInputType(EditorInfo.TYPE_CLASS_NUMBER | EditorInfo.TYPE_NUMBER_FLAG_DECIMAL);
        this.setFilters(new InputFilter[] {new InputFilter.LengthFilter(MAX_LENGTH)});
    }

    public CurrencyEditTextFragment(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    public CurrencyEditTextFragment(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);

        if (!focused) {
            validate();
        } else {
            String str = this.getText().toString();
            if (str.contains(numberFormat.getCurrency().getSymbol())){
                this.setText(CleanString());
                this.setSelection(this.length());
            }
        }
    }

    @Override
    public void onEditorAction(int actionCode) {
        super.onEditorAction(actionCode);
        if (actionCode == EditorInfo.IME_ACTION_DONE) {
            validate();
        }
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getAction() != KeyEvent.ACTION_DOWN){
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK ){
                dispatchKeyEvent(event);
                validate();
                this.clearFocus();
                return  false;
            }
        }
        return super.onKeyPreIme(keyCode, event);
    }

    private void init(Context context, AttributeSet attrs){
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CurrencyEditTextFragment);

        int maxLength = array.getIndex((R.styleable.AppCompatTextView.length));
        this.setInputType(EditorInfo.TYPE_CLASS_NUMBER | EditorInfo.TYPE_NUMBER_FLAG_DECIMAL);
        if (maxLength == 0) {
            this.setFilters(new InputFilter[] {new InputFilter.LengthFilter(MAX_LENGTH)});
        } else {
            this.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLength)});
        }

        array.recycle();
    }

    private String Currency() {
        return numberFormat.format(getValue());
    }

    /**
     * This function return's the string in the field as a decimal
     * @return value of string as a double in CurrencyEditTextFragment
     */
    public String CleanString(){
        return this.getText().toString().replaceAll("[^\\d.]+", "");
    }

    /**
     * This function validate's the text set in the field as currency for the current locale
     */
    public void validate(){
        String str = this.getText().toString();
        if (str.matches("\\.")){
            this.setText( numberFormat.format(0));
        } else if (str.isEmpty()) {
            // do nothing
        } else {
            this.setText(Currency());
            this.setSelection(this.length());
        }
    }

    /**
     * This function return's the value of the string in the field as a double
     * @return value of string as a double in CurrencyEditTextFragment
     */
    public Double getValue(){ return Double.valueOf(CleanString()); }
}

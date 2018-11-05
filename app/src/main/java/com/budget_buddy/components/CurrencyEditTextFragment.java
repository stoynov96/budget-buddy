package com.budget_buddy.components;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import java.text.NumberFormat;

public class CurrencyEditTextFragment extends AppCompatEditText{

    private NumberFormat f = NumberFormat.getCurrencyInstance();

    public CurrencyEditTextFragment(Context context) {
        super(context);
    }

    public CurrencyEditTextFragment(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CurrencyEditTextFragment(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);

        if (!focused) {
            validate();
        } else {
            String str = this.getText().toString();
            if (str.contains(f.getCurrency().getSymbol())){
                this.setText(StripCurrency());
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
                return  false;
            }
        }
        return super.onKeyPreIme(keyCode, event);
    }

    private String Currency() {
        return f.format(getValue());
    }

    private String CleanString(){
        return this.getText().toString().replaceAll("[^\\d.]+", "");
    }


    private String StripCurrency() {
        String pattern = "\\" + f.getCurrency().getSymbol();
        return this.getText().toString().replaceAll(pattern, "");
    }

    private void validate(){
        String str = this.getText().toString();
        if (str.matches("\\.")){
            this.setText( f.format(0));
        } else if (str.isEmpty()) {
            // do nothing
        } else {
            this.setText(Currency());
            this.setSelection(this.length());
        }
    }

    /**
     * Return's the value of the string in the field as a double
     * @return value of string as a double in CurrencyEditTextFragment
     */
    public Double getValue(){
        return Double.valueOf(CleanString());
    }
}

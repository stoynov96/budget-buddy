package com.budget_buddy.components;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;

import java.text.DecimalFormat;
import java.text.Format;
import java.text.NumberFormat;

public class CurrencyEditTextFragment extends AppCompatEditText{

    private NumberFormat f = NumberFormat.getCurrencyInstance();

    public CurrencyEditTextFragment(Context context) {
        super(context);
        //this.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
    }

    public CurrencyEditTextFragment(Context context, AttributeSet attrs) {
        super(context, attrs);
        //this.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
    }

    public CurrencyEditTextFragment(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //this.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);

        if (!focused) {
            String str = this.getText().toString();
            if (str.matches(".")){
                this.setText( f.format(0));
            } else if (str.isEmpty()) {
                // do nothing
            } else {
                this.setText(Currency());
            }
        }
    }

//    @Override
//    public void onEditorAction(int actionCode) {
//        super.onEditorAction(actionCode);
//    }
// TODO FINISH KEYBOARD CLOSES
    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getAction() != KeyEvent.ACTION_DOWN){
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK ){
                dispatchKeyEvent(event);
                this.setText(Currency());
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

    private Double getValue(){
        return Double.valueOf(CleanString());
    }
}

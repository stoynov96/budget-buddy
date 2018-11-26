package com.budget_buddy.components;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.budget_buddy.R;

public class BBToast {

    private Toast bbToast;

    public BBToast(Context context, String message) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View customToast = inflater.inflate(R.layout.bb_toast, null);
        // Set message in toast
        TextView textView = (TextView)((LinearLayout)customToast).getChildAt(1);
        textView.setText(message);

        // Set current context for new Toast
        bbToast = new Toast(context);

        // Set custom toast
        bbToast.setView(customToast);
        bbToast.setDuration(Toast.LENGTH_SHORT); // to appear
        bbToast.setGravity(Gravity.BOTTOM, 0, 1);
        bbToast.show();
        //Toast toast = new Toast(getApplicationContext());
    }

    private void setMessage(String message){
        //BBToast.
    }
    private void setStyle(){
        //BBToast.setView()
    }
}

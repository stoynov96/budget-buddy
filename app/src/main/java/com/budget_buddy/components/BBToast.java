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

        hideExpToast(customToast);

        // Set message in toast
        TextView textView = customToast.findViewById(R.id.customToastText);
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

    public BBToast(Context context, String message, int gravityLocation) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View customToast = inflater.inflate(R.layout.bb_toast, null);
        hideExpToast(customToast);

        // Set message in toast
        TextView textView = customToast.findViewById(R.id.customToastText);
        textView.setText(message);

        // Set current context for new Toast
        bbToast = new Toast(context);

        // Set custom toast
        bbToast.setView(customToast);
        bbToast.setDuration(Toast.LENGTH_SHORT); // to appear
        bbToast.setGravity(gravityLocation, 0, 1);
        bbToast.show();
    }

    public BBToast(Context context, String message, int exp, int gravityLocation) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View customToast = inflater.inflate(R.layout.bb_toast, null);

        // Set message in toast
        TextView textView = customToast.findViewById(R.id.customToastText);
        TextView textView2 = customToast.findViewById(R.id.expToastText);
        textView.setText(message);
        textView2.setText("+" + exp);

        // Set current context for new Toast
        bbToast = new Toast(context);

        // Set custom toast
        bbToast.setView(customToast);
        bbToast.setDuration(Toast.LENGTH_SHORT); // to appear
        bbToast.setGravity(gravityLocation, 0, 1);
        bbToast.show();
    }

    private void hideExpToast(View customToast){
        // Hide exp toast box
        LinearLayout expToastBox = customToast.findViewById(R.id.expToastBox);
        expToastBox.setVisibility(View.GONE);
    }
}

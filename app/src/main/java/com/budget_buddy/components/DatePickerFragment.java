package com.budget_buddy.components;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.app.DatePickerDialog;
import android.util.Log;
import android.widget.DatePicker;

import java.util.Calendar;

// https://developer.android.com/guide/topics/ui/controls/pickers
public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    public interface OnDateSetListener {
        void onDateSet(DatePicker view, int year, int month, int day);
    }

    private OnDateSetListener onDateSetListener;

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        // https://stackoverflow.com/a/42889134/10417393

        try {
            onDateSetListener = (OnDateSetListener) activity;
        } catch (ClassCastException e) {
            Log.d("Date Listener", "activity does not implement OnDateSetListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // use current date as default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        if (onDateSetListener != null) {
            onDateSetListener.onDateSet(view, year, month, day);
        }
    }
}

package com.budget_buddy;

import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import com.budget_buddy.components.DatePickerFragment;
import com.budget_buddy.exception.InvalidDataLabelException;

import java.text.DateFormat;
import java.util.Calendar;

public class ManualEntry extends AppCompatActivity implements DatePickerFragment.OnDateSetListener {

    BBUser user = BBUser.GetInstance();

    private EditText purchaseDateField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_entry);

        purchaseDateField = findViewById(R.id.purchaseDate);
        final Calendar calendar = Calendar.getInstance();
        final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
        purchaseDateField.setText(dateFormat.format(calendar.getTime()));
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        final Calendar calendar = Calendar.getInstance();
        final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);

        calendar.set(year, month, day);
        purchaseDateField.setText(dateFormat.format(calendar.getTime()));
    }

    /**
     * This function is called when pressing the button on the manual data entry screen. Sends the
     * fields to the WriteNewExpenditure function in the BBUser class to add the purchase to the
     * database.
     * @param view
     * @throws InvalidDataLabelException
     */
    public void DataEntry(View view) throws InvalidDataLabelException {
        EditText name = findViewById(R.id.purchaseName);
        EditText date = findViewById(R.id.purchaseDate);
        EditText amount = findViewById(R.id.purchaseAmount);
        EditText note = findViewById(R.id.purchaseNote);
        user.WriteNewExpenditure(name.getText().toString(), date.getText().toString(), amount.getText().toString(), note.getText().toString());
    }

    public void displayDatepicker(View view) {
        DialogFragment datePickerFragment = new DatePickerFragment();
        datePickerFragment.show(getSupportFragmentManager(), "datePicker");
    }
}
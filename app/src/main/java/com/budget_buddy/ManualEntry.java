package com.budget_buddy;

import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.budget_buddy.components.DatePickerFragment;
import com.budget_buddy.exception.InvalidDataLabelException;

public class ManualEntry extends AppCompatActivity {

    BBUser user = BBUser.GetInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_entry);
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
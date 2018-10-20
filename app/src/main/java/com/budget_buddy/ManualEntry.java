package com.budget_buddy;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

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
        EditText nameField = findViewById(R.id.purchaseName);
        EditText dateField = findViewById(R.id.purchaseDate);
        EditText amountField = findViewById(R.id.purchaseAmount);
        EditText notesField = findViewById(R.id.purchaseNote);
        String name = nameField.getText().toString();
        String date = dateField.getText().toString();
        String amount = amountField.getText().toString();
        String notes = notesField.getText().toString();

        if(name == "" || date == "" || amount == "") {
            Log.i("Purchase attempt", "Invalid input");
            return;
        }
        user.WriteNewExpenditure(name, date, amount, notes);
    }
}
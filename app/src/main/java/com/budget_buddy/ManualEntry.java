package com.budget_buddy;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import com.budget_buddy.components.CurrencyEditTextFragment;
import com.budget_buddy.exception.InvalidDataLabelException;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.budget_buddy.components.DatePickerFragment;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ManualEntry extends AppCompatActivity implements DatePickerFragment.OnDateSetListener {

    BBUser user = BBUser.GetInstance();

    private EditText purchaseDateField;
    // For Toast
    private Context context;
    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_entry);

        purchaseDateField = findViewById(R.id.purchaseDate);
        final Calendar calendar = Calendar.getInstance();
        final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
        purchaseDateField.setText(dateFormat.format(calendar.getTime()));
        context = getApplicationContext();

        // This contains the parsed lines from the receipt. Used to make a drop-down selector for
        // the item name. Could possibly do some fancy stuff here to make the options better, like
        // various regular expressions to make better options (like removing the restaurant number
        // from a chain name, for example).
        // This can (probably should) be modified to split the array into two arrays: one of text, one of numbers matching a price format.
        // Then we can allow for both title and price correction
        ArrayList<String> options = getIntent().getStringArrayListExtra("options");
        if(options != null) {
            // split the array into two: one of values, and one of possible titles
            ArrayList<String> values = new ArrayList<String>();
            ArrayList<String> titles = new ArrayList<String>();
            // pattern to search the lines for those that contain a valid price
            Pattern validPrice = Pattern.compile("\\d+\\.\\d\\d");
            Matcher matcher;
            String string;
            for(int i = 0; i < options.size(); i++) {
                string = options.get(i);
                matcher = validPrice.matcher(string);
                if(matcher.find()) {
                    values.add(matcher.group(0));
                }
                else {
                    titles.add(string);
                }
            }

            ConstraintLayout layout = findViewById(R.id.manualEntryLayout);
            ConstraintSet set = new ConstraintSet();
            set.clone(layout);

            final EditText editText = (EditText)findViewById(R.id.purchaseName);
            final Spinner spinner = new Spinner(this);
            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, titles);

            final EditText editText2 = (EditText)findViewById(R.id.purchaseAmount);
            final Spinner spinner2 = new Spinner(this);
            ArrayAdapter<String> spinnerAdapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, values);

            spinner.setAdapter(spinnerAdapter);
            spinner.setId(View.generateViewId());
            spinner.setTag("nameSpinner");
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    editText.setText(spinner.getSelectedItem().toString());
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            spinner2.setAdapter(spinnerAdapter2);
            spinner2.setId(View.generateViewId());
            spinner2.setTag("priceSpinner");
            spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    editText2.setText(spinner2.getSelectedItem().toString());
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            // initialize the boxes with what makes the most sense
            // for title, we may want to try the tallest box; this will have to be checked/passed in in the PhotoEntry class
            // for now, just use the first entry in the titles array
            // for price, we will pick the largest value
            editText.setText(titles.get(0));
            layout.addView(spinner);

            String value = "0";
            int index = 0;
            for(int i = 0; i < values.size(); i++) {
                String newValue = values.get(i);
                if (Double.valueOf(newValue) > Double.valueOf(value)) {
                    value = newValue;
                    index = i;
                }
            }

            // swap the first item in the values array with the largest value found
            // this makes the price default to the highest value
            String tempString = values.get(0);
            values.set(0, value);
            values.set(index, tempString);
            layout.addView(spinner2);

            TextView optionsText = new TextView(this);
            optionsText.setText("Purchase Title Correction Options:");
            optionsText.setId(View.generateViewId());
            optionsText.setTag("optionsHeader");
            layout.addView(optionsText);

            TextView priceText = new TextView(this);
            priceText.setText("Purchase Price Correction Options:");
            priceText.setId(View.generateViewId());
            priceText.setTag("priceHeader");
            layout.addView(priceText);

            set.constrainHeight(optionsText.getId(), 50);
            set.constrainHeight(spinner.getId(), 50);
            set.constrainHeight(priceText.getId(), 50);
            set.constrainHeight(spinner2.getId(), 50);
            set.connect(optionsText.getId(), ConstraintSet.TOP, R.id.purchaseAmount, ConstraintSet.BOTTOM, 12);
            set.connect(optionsText.getId(), ConstraintSet.RIGHT, spinner.getId(), ConstraintSet.LEFT, 0);
            set.connect(optionsText.getId(), ConstraintSet.BOTTOM, spinner.getId(), ConstraintSet.BOTTOM, 0);
            set.connect(optionsText.getId(), ConstraintSet.START, R.id.purchaseAmountTitle, ConstraintSet.START, 0);
            set.connect(spinner.getId(), ConstraintSet.TOP, R.id.purchaseAmount, ConstraintSet.BOTTOM, 12);
            set.connect(spinner.getId(), ConstraintSet.LEFT, optionsText.getId(), ConstraintSet.RIGHT, 0);
            set.connect(spinner.getId(), ConstraintSet.BOTTOM, priceText.getId(), ConstraintSet.TOP, 0);
            set.connect(spinner.getId(), ConstraintSet.RIGHT, R.id.purchaseAmount, ConstraintSet.RIGHT, 0);
            set.connect(priceText.getId(), ConstraintSet.TOP, optionsText.getId(), ConstraintSet.BOTTOM, 12);
            set.connect(priceText.getId(), ConstraintSet.RIGHT, spinner2.getId(), ConstraintSet.LEFT, 0);
            set.connect(priceText.getId(), ConstraintSet.BOTTOM, spinner2.getId(), ConstraintSet.BOTTOM, 0);
            set.connect(priceText.getId(), ConstraintSet.START, R.id.purchaseAmountTitle, ConstraintSet.START, 0);
            set.connect(spinner2.getId(), ConstraintSet.TOP, optionsText.getId(), ConstraintSet.BOTTOM, 12);
            set.connect(spinner2.getId(), ConstraintSet.LEFT, priceText.getId(), ConstraintSet.RIGHT, 0);
            set.connect(spinner2.getId(), ConstraintSet.BOTTOM, R.id.purchaseDescriptionTitle, ConstraintSet.TOP, 0);
            set.connect(spinner2.getId(), ConstraintSet.RIGHT, R.id.purchaseAmount, ConstraintSet.RIGHT, 0);
            set.connect(R.id.purchaseDescriptionTitle, ConstraintSet.TOP, spinner2.getId(), ConstraintSet.BOTTOM, 12);

            set.applyTo(layout);
       }
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
        EditText nameField = findViewById(R.id.purchaseName);
        EditText dateField = findViewById(R.id.purchaseDate);
        CurrencyEditTextFragment amountField = findViewById(R.id.purchaseAmount);
        EditText notesField = findViewById(R.id.purchaseNote);
        String name = nameField.getText().toString();
        String date = dateField.getText().toString();
        String amount = amountField.getText().toString();
        String notes = notesField.getText().toString();

        if(name.matches("") || date.matches("") || amount.matches("") ) {
            toast = Toast.makeText(context, "Invalid Input", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.BOTTOM, 0, 1);
            toast.show();
            nameField.getText().clear();
            amountField.getText().clear();
            notesField.getText().clear();
            return;
        }

        amountField.validate();
        amount = amountField.CleanString();
        user.WriteNewExpenditure(name, date, amount, notes);
        toast = Toast.makeText(context, "Added!", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM, 0, 1);
        toast.show();
        nameField.getText().clear();
        amountField.getText().clear();
        notesField.getText().clear();
    }

    /**
     * This function is called when pressing the finish button on the manual data entry screen. Closes
     * the manual entry activity.
     * @param view
     */
    public void FinishDataEntry(View view) {
        // end purchase entry
        finish();
    }

    /**
     * This function is called when tapping the date field. Instead of focusing on the field,
     * a date picker dialog pops up.
     * @param view
     */
    public void displayDatepicker(View view) {
        DialogFragment datePickerFragment = new DatePickerFragment();
        datePickerFragment.show(getSupportFragmentManager(), "datePicker");
    }
}
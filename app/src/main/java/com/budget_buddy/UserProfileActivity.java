package com.budget_buddy;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import com.budget_buddy.exception.InvalidDataLabelException;
import com.budget_buddy.components.CurrencyEditTextFragment;
import com.budget_buddy.utils.Data.MyCallback;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import com.budget_buddy.components.CurrencyEditTextFragment;
import com.budget_buddy.utils.Data.MyCallback;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.Map;

public class UserProfileActivity extends AppCompatActivity {

    BBUser currentUser = BBUser.GetInstance();

    CurrencyEditTextFragment monthlyIncomeField;
    CurrencyEditTextFragment rentField;
    CurrencyEditTextFragment otherMonthlyExpensesField;
    CurrencyEditTextFragment monthlySavingsGoalField;

    double monthlyIncome;
    double rent;
    double otherMonthlyExpenses;
    double monthlySavingsGoal;
    HashMap<String, String> spendingCategories;
    ArrayAdapter<String> adapter;
    ArrayList<String> names = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Spinner spinner = findViewById(R.id.categoriesSpinner);

        spendingCategories = new HashMap<String, String>();
        adapter = new ArrayAdapter<String> (this, android.R.layout.simple_spinner_dropdown_item, names);
        spinner.setAdapter(adapter);

        monthlyIncomeField = findViewById(R.id.total_monthy_income_field);
        rentField = findViewById(R.id.total_rent_field);
        otherMonthlyExpensesField = findViewById(R.id.total_other_expenses_field);
        monthlySavingsGoalField = findViewById(R.id.monthly_savings_goal_field);

        currentUser.addUICallback(profileCallback);
    }

    public void saveEntry(View view) {
        Map<String, Object> profileMap = new HashMap<>();
        monthlyIncome = monthlyIncomeField.getValue();
        rent = rentField.getValue();
        otherMonthlyExpenses = otherMonthlyExpensesField.getValue();
        monthlySavingsGoal = monthlySavingsGoalField.getValue();

        currentUser.setPrimaryIncome(monthlyIncome);
        currentUser.setRent(rent);
        currentUser.setOtherExpenses(otherMonthlyExpenses);
        currentUser.setSavingsGoal(monthlySavingsGoal);
        currentUser.SetSpendingCategories(spendingCategories);
        try {
            currentUser.WriteUserInfo();
        } catch (Exception e) {
            Log.i("Error", "" + e);
        }

        fromLogin();
        //Intent dashboardIntent = new Intent(this, Dashboard.class);
        //startActivity(dashboardIntent);
        finish();
    }

    public void AddCategory(View view) {
        AlertDialog.Builder newCategory = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        ConstraintLayout layout = (ConstraintLayout)inflater.inflate(R.layout.create_spending_category, null);
        final EditText name = layout.findViewById(R.id.categoryNameTag);
        final CurrencyEditTextFragment value = layout.findViewById(R.id.categoryNameLimit);
        name.requestFocus();
        newCategory.setView(layout);
        newCategory.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                value.validate();
                spendingCategories.put(name.getText().toString(), value.getText().toString());
                names.add(name.getText().toString());
                adapter.notifyDataSetChanged();
            }
        });
        AlertDialog alert = newCategory.create();
        alert.show();
        alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    public void cancelEntry(View view) {
        finish();
    }

    MyCallback profileCallback = new MyCallback() {
        @Override
        public void OnCallback(float[] weeklySpending) { }

        @Override
        public void OnPurchases(HashMap<String, ArrayList<Expenditure>> purchases) { }

        @Override
        public void OnCallback(HashMap<String, Object> map) { }

        @Override
        public void OnProfileSet() {
            monthlyIncome = currentUser.getPrimaryIncome();
            rent = currentUser.getRent();
            otherMonthlyExpenses = currentUser.getOtherExpenses();
            monthlySavingsGoal = currentUser.getSavingsGoal();
            monthlyIncomeField.setText("$" + (monthlyIncome == -1 ? "0.00" : monthlyIncome));
            rentField.setText("$" + (rent == -1 ? "0.00" : rent));
            otherMonthlyExpensesField.setText("$" + (otherMonthlyExpenses == -1 ? "0.00" : otherMonthlyExpenses));
            monthlySavingsGoalField.setText("$" + (monthlySavingsGoal == -1 ? "0.00" : monthlySavingsGoal));

            monthlyIncomeField.validate();
            rentField.validate();
            otherMonthlyExpensesField.validate();
            monthlySavingsGoalField.validate();
        }

        @Override
        public void CreateNewUser() {

        }

        @Override
        public void UserExists() {

        }

        @Override
        public void OnIncrement(int value) {

        }
    };

    private void fromLogin(){
        // Check if we are coming from login activity
        Bundle extras = getIntent().getExtras();
        if(extras != null && extras.getBoolean("login", false)){
            try {
                currentUser.IncStat(UserStats.Counters.LOGIN_COUNT);
            } catch (InvalidDataLabelException e) {
                Log.i("Error", "" + e);
            }
        }
    }
}

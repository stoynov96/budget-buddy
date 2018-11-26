package com.budget_buddy;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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
    ArrayList<String> spendingCategories;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        spendingCategories = new ArrayList<>();
        spendingCategories.add("Rent");
        spendingCategories.add("Food");
        spendingCategories.add("Entertainment");
        Spinner spinner = findViewById(R.id.categoriesSpinner);
        adapter = new ArrayAdapter<String> (this, android.R.layout.simple_spinner_dropdown_item, spendingCategories);
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

        finish();
    }

    public void AddCategory(View view) {
        AlertDialog.Builder newCategory = new AlertDialog.Builder(this);
        newCategory.setMessage("Enter the new category name: ");
        final EditText input = new EditText(this);
        input.requestFocus();
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setId(View.generateViewId());
        input.setTag("categoryInput");
        newCategory.setView(input);
        newCategory.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                spendingCategories.add(input.getText().toString());
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
        }

        @Override
        public void CreateNewUser() {

        }

        @Override
        public void UserExists() {

        }
    };
}

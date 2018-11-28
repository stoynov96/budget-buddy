package com.budget_buddy;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import com.budget_buddy.exception.InvalidDataLabelException;
import com.budget_buddy.components.CurrencyEditTextFragment;
import com.budget_buddy.utils.Data.MyCallback;
import com.budget_buddy.utils.Data.UserParameters;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

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
        profileMap.put("Monthly Income", monthlyIncome);
        profileMap.put("Rent", rent);
        profileMap.put("Other Monthly Expenses", otherMonthlyExpenses);
        profileMap.put("Monthly Savings Goal", monthlySavingsGoal);
        currentUser.GetFromMap(profileMap);
        try {
            currentUser.WriteUserInfo();
        } catch (Exception e) {
            Log.i("Error", "" + e);
        }
        Intent dashboardIntent = new Intent(this, Dashboard.class);
        startActivity(dashboardIntent);
        finish();
    }

    public void cancelEntry(View view) {
        Intent dashboardIntent = new Intent(this, Dashboard.class);
        startActivity(dashboardIntent);
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
        public void StatsChanged(int loginDebug) {

        }
    };
}

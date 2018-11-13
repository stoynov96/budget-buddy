package com.budget_buddy;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.budget_buddy.exception.InvalidDataLabelException;
import com.budget_buddy.utils.Data.MyCallback;
import com.budget_buddy.utils.Data.UserParameters;

import java.util.HashMap;

public class UserProfileActivity extends AppCompatActivity {

    BBUser currentUser = BBUser.GetInstance();

    EditText monthlyIncomeField;
    EditText rentField;
    EditText otherMonthlyExpensesField;
    EditText monthlySavingsGoalField;

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

    public void saveEntry(View view) throws InvalidDataLabelException {
        UserParameters newParameters = new UserParameters(rentField.getText().toString(), monthlySavingsGoalField.getText().toString(), otherMonthlyExpensesField.getText().toString(), monthlyIncomeField.getText().toString());
        currentUser.WriteUserParameters(newParameters);
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
            long monthlyIncome, rent, otherExpenses, savingsGoal;
            monthlyIncome = currentUser.getPrimaryIncome();
            rent = currentUser.getRent();
            otherExpenses = currentUser.getOtherExpenses();
            savingsGoal = currentUser.getSavingsGoal();
            monthlyIncomeField.setText("$" + (monthlyIncome == -1 ? "0.00" : monthlyIncome));
            rentField.setText("$" + (rent == -1 ? "0.00" : rent));
            otherMonthlyExpensesField.setText("$" + (otherExpenses == -1 ? "0.00" : otherExpenses));
            monthlySavingsGoalField.setText("$" + (savingsGoal == -1 ? "0.00" : savingsGoal));
        }

        @Override
        public void CreateNewUser() {

        }

        @Override
        public void UserExists() {

        }
    };
}

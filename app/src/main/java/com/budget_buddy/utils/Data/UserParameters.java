package com.budget_buddy.utils.Data;

import java.util.HashMap;
import java.util.Map;

public class UserParameters implements DataNode {
    private String rent;
    private String monthlySavingsGoal;
    private String otherMonthlyExpenses;
    private String monthlyIncome;

    public UserParameters(String newRent, String newGoal, String newExpenses, String newIncome) {
        rent = newRent;
        monthlySavingsGoal = newGoal;
        otherMonthlyExpenses = newExpenses;
        monthlyIncome = newIncome;
    }

    @Override
    public Map<String, Object> ToMap() {
        return new HashMap<String, Object>() {{
            put("Rent", rent);
            put("Other Monthly Expenses", otherMonthlyExpenses);
            put("Monthly Savings Goal", monthlySavingsGoal);
            put("Monthly Income", monthlyIncome);
        }};
    }

    @Override
    public void GetFromMap(Map<String, Object> map) {

    }

    @Override
    public void OnDataChange() {

    }
}

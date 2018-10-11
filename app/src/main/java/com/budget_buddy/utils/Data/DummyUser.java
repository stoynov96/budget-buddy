package com.budget_buddy.utils.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is only here because firebase wants some class to write data. Disregard
 */
public class DummyUser implements DataNode {
    public String username;
    public double budget;

    public DummyUser() {
        // Default constructor required for calls to DataSnapshot.getValue(DummyUser.class)
    }

    public DummyUser(String username, double budget) {
        this.username = username;
        this.budget = budget;
    }

    @Override
    public HashMap<String, Object> ToMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("mapLabelun", username);
        map.put("mapLabelB", budget);
        return map;
    }

    @Override
    public void GetFromMap(Map<String, Object> map) {
        username = (String) map.get("mapLabelun");

        Object budgetO = map.get("mapLabelB");
        //  This handles errors with Long conversion to double
        if (budgetO instanceof Long) {
            budget = ((Long) budgetO).doubleValue();
        }
        else {
            budget = (double) budgetO;
        }
    }
}

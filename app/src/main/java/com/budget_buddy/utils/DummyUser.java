package com.budget_buddy.utils;

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

    public HashMap<String, Object> ToMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("mapLabelun", username);
        map.put("mapLabelB", budget);
        return map;
    }
}

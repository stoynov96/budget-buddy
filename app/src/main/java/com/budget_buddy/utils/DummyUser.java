package com.budget_buddy.utils;

/**
 * This class is only here because firebase wants some class to write data. Disregard
 */
public class DummyUser {
    public String username;
    public double budget;

    public DummyUser() {
        // Default constructor required for calls to DataSnapshot.getValue(DummyUser.class)
    }

    public DummyUser(String username, double budget) {
        this.username = username;
        this.budget = budget;
    }
}

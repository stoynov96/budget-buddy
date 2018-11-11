package com.budget_buddy.utils.Data;

import java.util.HashMap;

// Primitive callback interface to help with reading from the database. Since Firebase is asynchronous
// and we don't want to force waiting/make it synchronous, we need to send a message back when the
// action is complete.

public interface MyCallback {
    /**
     * This function is called in BBUser after it processes all of the weekly expenditure data pulled
     * from Firebase. Returns an array of integers representing how much a user spent on a given day
     * (stored from 7 days ago to 1 day ago). In theory this should also be usable for any kind of
     * callback when we need to return an array of data from Firebase.
     * @param weeklySpending The array of integers representing how much a user spent on each of the
     *                       last 7 days.
     */
    void OnCallback(float [] weeklySpending);

    /**
     * This function is called in TableReader after getting the requested data from Firebase. Returns
     * a HashMap of (key, value) pairs. The key is always a string but the value may be a string, int,
     * etc.
     * @param map The HashMap of (key, value) pairs from Firebase.
     */
    void OnCallback(HashMap<String, Object> map);

    /**
     * This function is called when all of the properties in the BBUser singleton are set.
     */
    void OnProfileSet();
}

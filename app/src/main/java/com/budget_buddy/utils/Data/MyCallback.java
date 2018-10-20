package com.budget_buddy.utils.Data;

import com.budget_buddy.Expenditure;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface MyCallback {
    void onCallback(int [] weeklySpending);
    void onCallback(HashMap<String, Object> map);
}

package com.budget_buddy;

import com.budget_buddy.exception.InvalidDataLabelException;
import com.budget_buddy.utils.Data.DataNode;
import com.budget_buddy.utils.Data.TableReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserStats implements DataNode {

    String loginCount;

    // Achievements
    boolean FirstLogin = false;

    // Constructor
    UserStats(String newLoginCount) {
        int theLoginCount = Integer.valueOf(newLoginCount);
        theLoginCount++;
        loginCount = String.valueOf(theLoginCount);
    }

    @Override
    public Map<String, Object> ToMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("login count", loginCount);

        return map;
    }

    @Override
    public void GetFromMap(Map<String, Object> map) {
        loginCount = (String) map.get("login count");
    }

    @Override
    public void OnDataChange() {

    }

    public void LatchToDatabase(List<String> latchPath) throws InvalidDataLabelException {
        new TableReader().Latch(latchPath, this);
    }

    public void checkAchievements(BBUser currentUser){

    }
}

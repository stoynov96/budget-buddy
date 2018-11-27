package com.budget_buddy;

import com.budget_buddy.utils.Data.DataNode;

import java.util.HashMap;
import java.util.Map;

public class UserStats implements DataNode {

    String loginCount;

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
}

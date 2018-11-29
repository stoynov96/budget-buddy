package com.budget_buddy;

import android.util.Log;
import android.view.Gravity;

import com.budget_buddy.components.BBToast;
import com.budget_buddy.exception.InvalidDataLabelException;
import com.budget_buddy.utils.Data.DataNode;
import com.budget_buddy.utils.Data.MyCallback;
import com.budget_buddy.utils.Data.TableReader;
import com.budget_buddy.utils.Data.TableWriter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserStats implements DataNode {

    private final String userPath;
    private final TableWriter tableWriter;
    public MyCallback statCallBack;

    // Counters
    public int loginCount;

    // Achievements
    boolean FirstLogin = false;

    enum Counters {
        LOGINCOUNT;
    }

    // Constructor
    UserStats(final String uid) {
        tableWriter = new TableWriter();
        loginCount = 0;
        userPath = "Users/" + uid + "/User Stats/";
    }

    @Override
    public Map<String, Object> ToMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("login count", loginCount);

        return map;
    }

    @Override
    public void GetFromMap(Map<String, Object> map) {
        loginCount = (int) map.get("login count");
    }

    @Override
    public void OnDataChange() {

    }

    public void LatchToDatabase(List<String> latchPath) throws InvalidDataLabelException {
        new TableReader().Latch(latchPath, this);
    }

    public void checkAchievements(BBUser currentUser){
        new BBToast(currentUser.currentContext, "First Login!", 100,Gravity.TOP);
        currentUser.IncBudgetScore(100);
        // TODO EXPERIENCE SET UP
        //currentUser.ex
        switch (loginCount) {
            case 1:
                currentUser.IncBudgetScore(100);
                //display junk
                break;
            case 5:
                //display junk
                break;

            default:
                break;
        }
    }

    public MyCallback loginCallBack(final BBUser user){
        // Here's a more permanent home for the callback
        MyCallback statsChanged = new MyCallback() {
            @Override
            public void OnCallback(float [] weeklySpending) {
            }

            @Override
            public void OnCallback(HashMap<String, Object> map) {

            }

            @Override
            public void OnProfileSet() {
            }

            @Override
            public void CreateNewUser() {

            }

            @Override
            public void UserExists() {

            }

            @Override
            public void StatsChanged(int count) {
                    loginCount = count;
                    checkAchievements(user);
                }
            };

        return statsChanged;
    }


}

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
    public int purchaseCount;



    enum Counters {
        LOGIN_COUNT, PURCHASE_COUNT;
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

    public void checkAchievements(BBUser currentUser){

        //currentUser.IncBudgetScore(100);
        //currentUser.ex
        switch (loginCount) {
            case 1:
                new BBToast(currentUser.currentContext, "First Login!", 100,Gravity.TOP);
                //Log.i("FUCK", "checkAchievements: IT WORKED");
                currentUser.IncBudgetScore(100);
                // TODO set up link between AchievementActivity
                //currentUser.
                break;
            case 5:
                break;

            default:
                break;
        }

        // TODO move FINISH
        switch (purchaseCount) {
            case 1:
                currentUser.IncBudgetScore(100);
                new BBToast(currentUser.currentContext, "Very First Purchase!", 100,Gravity.TOP);
                break;
            case 2:
                break;
                default:
                    break;

        }
    }

    public MyCallback loginCountCallBack(final BBUser user){
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
            public void OnIncrement(int value) {
                    loginCount = value;
                    checkAchievements(user);
                    // TODO move display achievement and exp allocation here
                }
            };

        return statsChanged;
    }

    public MyCallback purchaseCountCallBack(final BBUser user){
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
            public void OnIncrement(int value) {
                purchaseCount = value;
                checkAchievements(user);
                // TODO move display achievement and exp allocation here
            }
        };

        return statsChanged;
    }

}

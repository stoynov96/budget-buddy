package com.budget_buddy;

import android.util.Log;
import android.view.Gravity;

import com.budget_buddy.components.BBToast;
import com.budget_buddy.utils.Data.DataNode;
import com.budget_buddy.utils.Data.MyCallback;
import com.budget_buddy.utils.Data.TableWriter;

import java.util.HashMap;
import java.util.Map;

public class UserStats implements DataNode {

    private final String userPath;
    private final TableWriter tableWriter;
    public MyCallback statCallBack;

    // Counters
    public int loginCount;
    public int purchaseCount;



    enum Counters {
        LOGIN_COUNT, PURCHASE_COUNT
    }

    enum Dailies {
        FIRST_PURCHASE
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

    private void checkProgressEXP(BBUser currentUser) {
        Log.i("FUCK", "checkProgressEXP: IT WORKED");
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

    private void checkLoginEXP(BBUser currentUser){
        //Log.i("FUCK", "checkLoginEXP: IT WORKED");
        // Daily exp check


        // Lifetime exp check
        switch (loginCount) {
            case 1:
                new BBToast(currentUser.currentContext, "First Login!", 100,Gravity.TOP);

                currentUser.IncBudgetScore(100);
                break;
            case 5:
                break;

            default:
                break;
        }


    }

    public void loginCountCallBack(final BBUser user){
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
                    checkLoginEXP(user);
                    // TODO move display achievement and exp allocation here
                }
            };

        statCallBack =  statsChanged;
    }

    public void purchaseCountCallBack(final BBUser user){
        MyCallback statsChanged = new MyCallback() {
            @Override
            public void OnCallback(float [] weeklySpending) {
            }

            @Override
            public void OnCallback(HashMap<String, Object> map) {
                // do first purchase check here?

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
                checkProgressEXP(user);
                Log.i("FUCK", "INSIDE purchaseCountCallBack");
                // TODO move display achievement and exp allocation here
            }
        };

        statCallBack =  statsChanged;
    }

    private void checkFirstPurchaseDaily(HashMap<String, Object> map) {
        if (map.isEmpty()) {

        }
    }

}

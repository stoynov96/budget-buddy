package com.budget_buddy;

import android.util.Log;
import android.view.Gravity;

import com.budget_buddy.components.BBToast;
import com.budget_buddy.utils.Data.DataNode;
import com.budget_buddy.utils.Data.MyCallback;
import com.budget_buddy.utils.Data.TableWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UserStats implements DataNode {

    public MyCallback statCallBack;

    // Counters
    public int loginCount;
    public int purchaseCount;

    // Tags to set path
    enum Counters {
        LOGIN_COUNT, PURCHASE_COUNT
    }

    enum Dailies {
        FIRST_PURCHASE
    }

    // Constructor
    UserStats() {
        loginCount = 0;
        purchaseCount = 0;
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
        //Log.i("FUCK", "checkProgressEXP: IT WORKED");
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

    /***
     * Creates and sets a callback in regards to keeping track of logins
     * @param user holds reference for callback
     ***/
    public void loginCountCallBack(final BBUser user){
        MyCallback statsChanged = new MyCallback() {
            @Override
            public void OnCallback(float [] weeklySpending) { }

            @Override
            public void OnPurchases(HashMap<String, ArrayList<Expenditure>> purchases) { }

            @Override
            public void OnCallback(HashMap<String, Object> map) { }

            @Override
            public void OnProfileSet() { }

            @Override
            public void CreateNewUser() { }

            @Override
            public void UserExists() { }

            @Override
            public void OnIncrement(int value) {
                    loginCount = value;
                    checkLoginEXP(user);
                    // TODO move display achievement and exp allocation here
                }
            };

        statCallBack =  statsChanged;
    }

    /***
     * Creates and sets a callback in regards to keeping track of purchases
     * @param user holds reference for callback
     ***/
    public void purchaseCountCallBack(final BBUser user){
        MyCallback statsChanged = new MyCallback() {
            @Override
            public void OnCallback(float [] weeklySpending) { }

            @Override
            public void OnPurchases(HashMap<String, ArrayList<Expenditure>> purchases) { }
            @Override

            public void OnCallback(HashMap<String, Object> map) {
                checkFirstPurchaseDaily(map, user);
            }

            @Override
            public void OnProfileSet() { }

            @Override
            public void CreateNewUser() { }

            @Override
            public void UserExists() { }

            @Override
            public void OnIncrement(int value) {
                purchaseCount = value;
                checkProgressEXP(user);
                Log.i("FUCK", "INSIDE purchaseCountCallBack");
            }
        };

        statCallBack =  statsChanged;
    }

    private void checkFirstPurchaseDaily(HashMap<String, Object> map, BBUser currentUser) {
        //Log.i("FUCK", "checkFirstPurchaseDaily: " + map.size());
        if (map.size() == 1) {
            new BBToast(currentUser.currentContext, "First Purchase of the Day!", 100,Gravity.TOP);
            currentUser.IncBudgetScore(100);
        }
    }

}

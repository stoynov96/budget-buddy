package com.budget_buddy;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;

import com.budget_buddy.components.BBToast;
import com.budget_buddy.utils.Data.DataNode;
import com.budget_buddy.utils.Data.MyCallback;

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

    // Dailies
    boolean FirstDailyPurchase = false;

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

    private void checkPurchaseExp(BBUser currentUser) {
        //Log.i("FUCK", "checkPurchaseExp: IT WORKED");
        switch (purchaseCount) {
            case 1:
                currentUser.IncBudgetScore(100);
                LayerDrawable ld = GetAchievementBadge(currentUser.currentContext, R.drawable.one_purchase_milestone);
                new BBToast(ld, currentUser.currentContext, "Very First Purchase!", 100,Gravity.TOP);
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
                currentUser.IncBudgetScore(100);
                LayerDrawable ld = GetAchievementBadge(currentUser.currentContext, R.drawable.one_login_milestone);
                new BBToast(ld, currentUser.currentContext, "First Login!", 100,Gravity.TOP);
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
                checkFirstPurchaseDaily(map, user, true);
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
                checkPurchaseExp(user);
                Log.i("FUCK", "INSIDE purchaseCountCallBack");
            }
        };

        statCallBack =  statsChanged;
    }

    /***
     * Creates and sets a callback in regards to doing a read
     * @param user holds reference for callback
     ***/
    public void readCallBack(final BBUser user){
        MyCallback statsChanged = new MyCallback() {
            @Override
            public void OnCallback(float [] weeklySpending) { }

            @Override
            public void OnPurchases(HashMap<String, ArrayList<Expenditure>> purchases) { }
            @Override

            public void OnCallback(HashMap<String, Object> map) {
                checkFirstPurchaseDaily(map, user, false);
            }

            @Override
            public void OnProfileSet() { }

            @Override
            public void CreateNewUser() { }

            @Override
            public void UserExists() { }

            @Override
            public void OnIncrement(int value) { }
        };

        statCallBack =  statsChanged;
    }

    private void checkFirstPurchaseDaily(HashMap<String, Object> map, BBUser currentUser, boolean giving) {
        Log.i("FUCK", "checkFirstPurchaseDaily: " + map.size());
        if (map.size() == 1) {
            if (giving) {

                LayerDrawable ld = (LayerDrawable) ContextCompat.getDrawable(currentUser.currentContext, R.drawable.achievement_base);
                new BBToast(ld, currentUser.currentContext, "First Purchase of the Day!", 100, Gravity.TOP);
                currentUser.IncBudgetScore(100);
            }
            FirstDailyPurchase = true;
        }
    }

    public LayerDrawable GetAchievementBadge(Context context, int RDrawableID){
        LayerDrawable ld = (LayerDrawable) ContextCompat.getDrawable(context, R.drawable.achievement_base);
        Drawable replace =  ContextCompat.getDrawable(context, RDrawableID);
        ld.setDrawableByLayerId(R.id.achievementImage, replace);
        return  ld;
    }
}

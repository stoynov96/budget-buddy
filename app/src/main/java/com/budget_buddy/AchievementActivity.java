package com.budget_buddy;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;

public class AchievementActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    BBUser currentUser = BBUser.GetInstance();


    // Dailies
    ImageButton firstDailyPurchase;

    // Milestone Achievements
    ImageButton firstPurchase;
    ImageButton firstLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);
        setUpDrawer();

        setUpAchievements();

        checkMilestones();
        checkDailies();

        getSupportActionBar().setTitle("Achievements");
    }

    private void setUpAchievements(){
        firstDailyPurchase = findViewById(R.id.daily_purchase_achievement);
        firstLogin = findViewById(R.id.firstLogin);
        firstPurchase = findViewById(R.id.firstPurchase);

        firstDailyPurchase.setColorFilter(0x99000000, PorterDuff.Mode.SRC_ATOP);
        firstDailyPurchase.invalidate();

        LayerDrawable ld = (LayerDrawable) firstLogin.getDrawable();
        Drawable replace = getResources().getDrawable(R.drawable.one_login_milestone);
        ld.setDrawableByLayerId(R.id.achievementImage, replace);
        firstLogin.setColorFilter(0x99000000, PorterDuff.Mode.SRC_ATOP);
        firstLogin.invalidate();

        ld = (LayerDrawable) firstPurchase.getDrawable();
        replace = getResources().getDrawable(R.drawable.one_purchase_milestone);
        ld.setDrawableByLayerId(R.id.achievementImage, replace);
        firstPurchase.setColorFilter(0x99000000, PorterDuff.Mode.SRC_ATOP);
        firstPurchase.invalidate();
    }

    private void checkMilestones(){
        int count2Check = currentUser.userStats.loginCount;
        switch (count2Check) {
            case 10:
                // achievement here
                // TODO stuff to display achievement ~ set button image spot not greyed out?
                // TODO also make pretty achievement icons
            case 5:

            case 1:
                firstLogin.setColorFilter(null);
                firstLogin.invalidate();
                break;
        }
        count2Check = currentUser.userStats.purchaseCount;
        switch (count2Check){
            case 10:
            case 5:
            case 1:
                firstPurchase.setColorFilter(null);
                firstPurchase.invalidate();
                break;
        }

        //int purchaseCount = currentUser.userStats.purchaseCount
    }

    private void checkDailies(){
        if (currentUser.userStats.FirstDailyPurchase) {
            //Log.i("FUCK", "checkDailies: ITS GRAY");
            firstDailyPurchase.setColorFilter(null);
            firstDailyPurchase.invalidate();
        }
    }



    private void setUpDrawer(){
        drawerLayout = findViewById(R.id.drawer_layout);

        // Set up Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);

        // Remove AchievementItem
        NavigationView navigationView = findViewById(R.id.nav_view);
        Menu menuNav = navigationView.getMenu();
        menuNav.findItem(R.id.achievementItem).setEnabled(false);
        menuNav.removeItem(R.id.achievementItem);

        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // close drawer when item is tapped
                        drawerLayout.closeDrawers();

                        switch(menuItem.getItemId()) {
                            case R.id.logoutButton:
                                goToLogin();
                                break;
                            case R.id.achievementItem:
                                // TODO: Other navigation items
                                break;
                        }
                        return true;
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(Gravity.LEFT);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void goToLogin(){
        Intent loginIntent = new Intent(this, Login.class);
        loginIntent.putExtra("dashboard", true);
        startActivity(loginIntent);
    }
}

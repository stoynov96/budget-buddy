package com.budget_buddy;

import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.Group;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class AchievementActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    BBUser currentUser = BBUser.GetInstance();


    // Dailies ??? probs dont really need?
    boolean FirstDailyPurchase = false;

    // Milestone Achievements ???
    boolean FirstLogin = false;
    boolean FifthLogin = false;
    boolean TenthLogin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);
        setUpDrawer();
        checkAchievements();

    }


    public void checkAchievements(){
        int loginCount = currentUser.userStats.loginCount;
        switch (loginCount) {
            case 10:
                TenthLogin = true;
                // TODO stuff to display achievement ~ set button image spot not greyed out?
                // TODO also make pretty achievement icons
            case 5:
                FifthLogin = true;
            case 1:
                FirstLogin = true;
                break;
        }

        //int purchaseCount = currentUser.userStats.purchaseCount
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

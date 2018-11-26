package com.budget_buddy;

import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.Group;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;

public class AchievementActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);

        setUpDrawer();
    }
    /*
    AchievementActivity - Nice way to handle collections besides shoving it in a class?
     - set the thing to the alpha channel to insure stuff
     - a collection of private achievements
        - has a property: public boolean GET ~ if already gotten
     - a function to check the collection
        - has a property of when to check it based on what activity we are in

     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        this.menu = menu;
        return true;
    }


    private void setUpDrawer(){
        drawerLayout = findViewById(R.id.drawer_layout);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);

//        MenuItem achievementItem = menu.getItem(0); // item 0 = achievement item ~ TODO make work haha
//        achievementItem.setVisible(false);
        //MenuItem item = menu.findItem(R.id.achievementItem);
        //item.setVisible(false);

        NavigationView navigationView = findViewById(R.id.nav_view);
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

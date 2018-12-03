package com.budget_buddy;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;

import com.budget_buddy.animations.ExperienceBarAnimation;
import com.budget_buddy.charts.GoalProgressBar;

import com.budget_buddy.charts.SpendingChart;
import com.budget_buddy.components.BBToast;
import com.budget_buddy.utils.Data.MyCallback;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.charts.BarChart;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Dashboard extends AppCompatActivity {

    BBUser currentUser = BBUser.GetInstance();

    ProgressBar experienceBar;
    TextView experienceProgressText;
    ExperienceBarAnimation experienceBarAnimation;

    GoalProgressBar progressBar;
    SpendingChart spendingChart;
    final List<BarEntry> entries = new ArrayList<BarEntry>();
    TextView progressBarDescription;
	DrawerLayout drawerLayout;
	Dashboard This = this; // a hack to reference this in the callback class, java's the best

    // Here's a more permanent home for the callback
    MyCallback callback = new MyCallback() {
        @Override
        public void OnCallback(float [] weeklySpending) {
            spendingChart.clear();
            entries.clear();
            for(int i = 0; i < 7; i++) {
                entries.add(new BarEntry(i, weeklySpending[6-i]));
            }
            spendingChart.setEntries(entries);
        }

        @Override
        public void OnPurchases(HashMap<String, ArrayList<Expenditure>> purchases) {
            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            spendingChart.clear();
            entries.clear();
            for (long i = 0; i < 7; i++) {
                LocalDate date = LocalDate.now().minusDays(i);
                String dateStr = date.format(dateFormat);

                ArrayList<Expenditure> expenditures = purchases.get(dateStr);
                float dayTotal = 0.0f;
                if(expenditures != null) {
                    for(Expenditure e: expenditures) {
                        dayTotal += (new Float(e.amount).floatValue());
                    }
                }
                BarEntry entry = new BarEntry(6 - i, dayTotal);

                entries.add(entry);
            }
            spendingChart.setEntries(entries);

            LimitLine aveLine = new LimitLine(currentUser.GetAveMonthSpent());
            aveLine.setLineColor(getResources().getColor(R.color.colorAccent, This.getTheme()));
            aveLine.setLineWidth(1f);
            aveLine.setTextSize(12f);
            aveLine.setTextColor(Color.BLACK);

            spendingChart.getAxisLeft().removeAllLimitLines();
            spendingChart.getAxisLeft().addLimitLine(aveLine);

            progressBar.setGoal((int)currentUser.getSavingsGoal());
            int progress = (int)currentUser.GetGoalProgress();
            if (progress > 0)
                progressBar.setProgress(progress,  This);
            else
                progressBar.setProgress(0, This);
            if((int) currentUser.getSavingsGoal() - progress > 0)
                progressBarDescription.setText(This.getString(R.string.goal, (int)currentUser.getSavingsGoal() - progress));

            TextView suggestedSpendingText = findViewById(R.id.suggested_spending_text);
            TextView averageSpendingText = findViewById(R.id.average_spending_text);
            suggestedSpendingText.setText(This.getString(R.string.suggested_spending,(int) currentUser.GetSuggestedDailySpendingAmount()));
            averageSpendingText.setText(This.getString(R.string.average,(int) currentUser.GetAveMonthSpent()));
        }

        @Override
        public void OnCallback(HashMap<String, Object> map) {

        }

        @Override
        public void OnProfileSet() {
            if(ProfileNeedsSetup()) {
                // display profile setup dialog
                Log.i("Profile", "needs set up");
            } else {
                //Log.i("FUCK", "OnProfileSet: EXP FROM USER = " + currentUser.getBudgetScore());
                experienceBarAnimation.setProgress((int)currentUser.getBudgetScore());

                SetExperience(currentUser.getBudgetScore());
                SetSavingsGoal(currentUser.getSavingsGoal());
            }
        }

        @Override
        public void CreateNewUser() {

        }

        @Override
        public void UserExists() {

        }

        @Override
        public void OnIncrement(int value) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentUser.currentContext = getApplicationContext();

        setContentView(R.layout.activity_dashboard);
        currentUser.CheckDaily(UserStats.Dailies.FIRST_PURCHASE);
        currentUser.setUserInterfaceCallback(callback);
        setUpDrawer();
        setupExperienceBar();
        addChart();
        addProgressBar();
        TextView suggestedSpendingText = findViewById(R.id.suggested_spending_text);
        TextView averageSpendingText = findViewById(R.id.average_spending_text);
        suggestedSpendingText.setText("");
        averageSpendingText.setText("");

        getSupportActionBar().setTitle("Dashboard");
    }

    private void setupExperienceBar() {
        experienceBar = findViewById(R.id.experienceBar);
        experienceBar.setMax(2500);
        experienceProgressText = findViewById(R.id.experienceProgessFraction);
        experienceBarAnimation = new ExperienceBarAnimation(experienceBar, experienceProgressText);
        experienceBarAnimation.setProgress((int)currentUser.getBudgetScore());
    }

    public void gotoEntryMethodFor(View view) {
        if(view.getId() == R.id.manualEntry) {
            Intent manualEntryIntent = new Intent(this, ManualEntry.class);
            startActivity(manualEntryIntent);
        } else if (view.getId() == R.id.cameraEntry) {
            Intent cameraEntryIntent = new Intent(this, PhotoEntry.class);
            startActivity(cameraEntryIntent);
        } else if (view.getId() == R.id.profileImageButton) {
            Intent userProfileViewIntent = new Intent(this, UserProfileActivity.class);
            startActivity(userProfileViewIntent);
        } else {
            return;
        }
    }

    private void SetExperience(double experience) {

    }

    private void SetSavingsGoal(double goal) {

    }

    private boolean ProfileNeedsSetup() {
        // check if any one of these is negative, for now rent is required
        if(currentUser.getPrimaryIncome() < 0 || currentUser.getRent() < 0 || currentUser.getSavingsGoal() < 0) {
            return true;
        }
        // might add other checks later

        return false;
    }

    private void addProgressBar() {
        // create description view
        progressBarDescription = new TextView(this);
        progressBarDescription.setId(R.id.progress_bar_description);
        progressBarDescription.setText(this.getString(R.string.goal, 0));

        progressBar = new GoalProgressBar(this);
        progressBar.setId(R.id.progress_bar_view);

        ConstraintLayout cl = findViewById(R.id.dataBreakdownLayout);
        cl.addView(progressBar, 0, 200);
        cl.addView(progressBarDescription, 0, 50);

        ConstraintSet constraintSet = new ConstraintSet();

        constraintSet.clone(cl);
        // constrain bar to bottom and sides
        constraintSet.connect(progressBar.getId(), ConstraintSet.LEFT, cl.getId(),ConstraintSet.LEFT, 8);
        constraintSet.connect(progressBar.getId(), ConstraintSet.RIGHT, cl.getId(),ConstraintSet.RIGHT, 8);
        constraintSet.connect(progressBar.getId(),ConstraintSet.BOTTOM, cl.getId(),ConstraintSet.BOTTOM, 0);
        // constrain description to bar and sides
        constraintSet.connect(progressBarDescription.getId(),ConstraintSet.LEFT, progressBar.getId(), ConstraintSet.LEFT,0);
        constraintSet.connect(progressBarDescription.getId(),ConstraintSet.RIGHT, progressBar.getId(), ConstraintSet.RIGHT,0);
        constraintSet.connect(progressBarDescription.getId(), ConstraintSet.BOTTOM, progressBar.getId(), ConstraintSet.TOP, 0);
        constraintSet.applyTo(cl);

        //progressBar.setProgress(200, this);

        //progressBar.setGoal(300);

        // remove legend
        progressBar.getLegend().setEnabled(false);
        // remove description
        progressBar.getDescription().setEnabled(false);

        progressBar.animateY(getResources().getInteger(R.integer.dashboard_animation_time), Easing.EasingOption.EaseInOutExpo);
        progressBar.setNoDataText("");
    }

    private void addChart() {

        spendingChart = new SpendingChart(this, getTheme());
        spendingChart.setId(R.id.bar_graph_view);
        spendingChart.setNoDataText("");

        ConstraintLayout cl = (ConstraintLayout) findViewById(R.id.dataGraphLayout);
        cl.addView(spendingChart,0,0);

        ConstraintSet constraintSet = new ConstraintSet();

        constraintSet.clone(cl);
        constraintSet.connect(spendingChart.getId(), ConstraintSet.LEFT, cl.getId(),ConstraintSet.LEFT, 8);
        constraintSet.connect(spendingChart.getId(), ConstraintSet.RIGHT, cl.getId(),ConstraintSet.RIGHT, 8);
        constraintSet.connect(spendingChart.getId(),ConstraintSet.BOTTOM, cl.getId(),ConstraintSet.BOTTOM, 0);
        constraintSet.connect(spendingChart.getId(), ConstraintSet.TOP, cl.getId(), ConstraintSet.TOP, 0);
        constraintSet.applyTo(cl);

        //currentUser.GetWeeklySpending(callback);
        currentUser.AcquireAllPurchases(callback);

    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(Gravity.LEFT);
        } else {
            drawerLayout.openDrawer(Gravity.LEFT);
        }
        return;
    }

    private void setUpDrawer(){
        drawerLayout = findViewById(R.id.drawer_layout);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);

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
                                goToAchievements();
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

    private void goToAchievements() {
        Intent achievementIntent = new Intent(this, AchievementActivity.class);
        startActivity(achievementIntent);
    }


}

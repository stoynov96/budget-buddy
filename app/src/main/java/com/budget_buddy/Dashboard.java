package com.budget_buddy;

import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.budget_buddy.animations.ExperienceBarAnimation;
import com.budget_buddy.charts.GoalProgressBar;

import com.budget_buddy.utils.Data.MyCallback;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.firebase.database.DataSnapshot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Dashboard extends AppCompatActivity {

    BBUser currentUser = BBUser.GetInstance();

    ProgressBar experienceBar;
    TextView experienceProgressText;
    ExperienceBarAnimation experienceBarAnimation;

    HorizontalBarChart progressBar;
    TextView progressBarDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);


        setupExperienceBar();
        addChart();
        addProgressBar();
    }

    private void setupExperienceBar() {
        experienceBar = findViewById(R.id.experienceBar);
        experienceBar.setMax(2500);
        experienceProgressText = findViewById(R.id.experienceProgessFraction);
        experienceBarAnimation = new ExperienceBarAnimation(experienceBar, experienceProgressText);
        experienceBarAnimation.setProgress(1675);
    }

    public void gotoEntryMethodFor(View view) {
        if(view.getId() == R.id.manualEntry) {
            Intent manualEntryIntent = new Intent(this, ManualEntry.class);
            startActivity(manualEntryIntent);
        } else if (view.getId() == R.id.cameraEntry) {
            Intent cameraEntryIntent = new Intent(this, PhotoEntry.class);
            startActivity(cameraEntryIntent);
        } else {
            return;
        }
    }

    private void addProgressBar() {
        // create description view
        progressBarDescription = new TextView(this);
        progressBarDescription.setId(R.id.progress_bar_description);
        progressBarDescription.setText(this.getString(R.string.goal, 300 - 235));

        GoalProgressBar progressBar = new GoalProgressBar(this);
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

        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, 235));
        BarDataSet barDataSet = new BarDataSet(entries, "");

        // set colors
        barDataSet.setColor(getResources().getColor(R.color.colorPrimary, this.getTheme()));
        barDataSet.setBarBorderColor(getResources().getColor(R.color.colorPrimaryDark, this.getTheme()));
        barDataSet.setBarBorderWidth(2.5f);

        BarData barData = new BarData(barDataSet);
        barData.setDrawValues(false);

        progressBar.setData(barData);
        progressBar.setFitBars(true);

        progressBar.setGoal(300);

        // remove legend
        progressBar.getLegend().setEnabled(false);
        // remove description
        progressBar.getDescription().setEnabled(false);

        progressBar.animateY(getResources().getInteger(R.integer.dashboard_animation_time), Easing.EasingOption.EaseInOutExpo);
    }

    private void addChart() {
        final BarChart chart = new BarChart(this);
        chart.setId(R.id.bar_graph_view);

        ConstraintLayout cl = (ConstraintLayout) findViewById(R.id.dataGraphLayout);
        cl.addView(chart,0,0);

        ConstraintSet constraintSet = new ConstraintSet();

        constraintSet.clone(cl);
        constraintSet.connect(chart.getId(), ConstraintSet.LEFT, cl.getId(),ConstraintSet.LEFT, 8);
        constraintSet.connect(chart.getId(), ConstraintSet.RIGHT, cl.getId(),ConstraintSet.RIGHT, 8);
        constraintSet.connect(chart.getId(),ConstraintSet.BOTTOM, cl.getId(),ConstraintSet.BOTTOM, 0);
        constraintSet.connect(chart.getId(), ConstraintSet.TOP, cl.getId(), ConstraintSet.TOP, 0);
        constraintSet.applyTo(cl);

        final List<BarEntry> entries = new ArrayList<BarEntry>();

        // This is initializing the bars to 0 since we do not have data from Firebase yet.
        for(int i = 0; i < 7; i++) {
            entries.add(new BarEntry(i, 0));
        }

        // Idea for how a callback can be used on the data to update/draw the graph. Aside from
        // using a callback for this, I think we may want to consider an update callback somewhere in Dashboard
        // to make the database reading better, and to make this update as data is added
        MyCallback callback = new MyCallback() {
            @Override
            public void OnCallback(int [] weeklySpending) {
                for(int i = 0; i < 7; i++) {
                    entries.add(new BarEntry(i, weeklySpending[6-i]));
                }
                BarDataSet dataSet = new BarDataSet(entries, "");
                BarData barData = new BarData(dataSet);
                barData.setBarWidth(0.85f);
                chart.setData(barData);
                chart.setFitBars(true);

            }

            @Override
            public void OnCallback(HashMap<String, Object> map) {

            }
        };

        currentUser.GetWeeklySpending(callback);

        BarDataSet dataSet = new BarDataSet(entries, "");

        // set colors
        dataSet.setColor(getResources().getColor(R.color.colorPrimary, this.getTheme()));
        dataSet.setBarBorderColor(getResources().getColor(R.color.colorPrimaryDark, this.getTheme()));
        dataSet.setBarBorderWidth(2.5f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.85f);

        chart.setData(barData);
        chart.setFitBars(true);
        IValueFormatter valueFormatter = new IValueFormatter() {

            private DecimalFormat mFormat = new DecimalFormat("###,###,##0.00");

            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return "$" + mFormat.format(value);
            }
        };

        barData.setValueFormatter(valueFormatter);

        // disable touch gestures
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);
        chart.setTouchEnabled(false);

        // remove label
        chart.getDescription().setEnabled(false);

        // make the daily allowance line
        LimitLine dailyAllowance = new LimitLine(4.0f, "Daily allowance");
        dailyAllowance.setLineColor(getResources().getColor(R.color.colorAccent, this.getTheme()));
        chart.getAxisLeft().addLimitLine(dailyAllowance);

        // don't show the grid
        chart.getXAxis().setDrawGridLines(false);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getAxisRight().setEnabled(false);

        // draw labels on bottom
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        IAxisValueFormatter axisValueFormatter = new IAxisValueFormatter() {

            private String[] days = getResources().getStringArray(R.array.day_abbreviations);

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                Calendar calendar = Calendar.getInstance();
                // this graph will only show previous 7 days
                int today = calendar.get(Calendar.DAY_OF_WEEK);
                return days[((int) value + today - 1) % 7];
            }
        };

        chart.getXAxis().setValueFormatter(axisValueFormatter);
        // set the bottom of the window to y=0
        chart.getAxisLeft().setAxisMinimum(0);

        chart.getLegend().setEnabled(false);

        //chart.animateX(2000);
        chart.animateY( 2000, Easing.EasingOption.EaseInOutExpo);
    }
}

package com.budget_buddy.charts;

import android.content.Context;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;

public class GoalProgressBar extends HorizontalBarChart {

    public GoalProgressBar(Context context) {
        super(context);

        // get axes
        XAxis xAxis = getXAxis();
        YAxis topAxis = getAxisLeft();

        // don't show the grid
        topAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);
        topAxis.setDrawGridLines(false);

        // don't draw line along xAxis
        xAxis.setDrawAxisLine(false);
        // don't draw xAxis labels
        xAxis.setDrawLabels(false);

        // TODO: just set this on the dashboard
        setGoal(300);

        // take care of top and bottom labels
        topAxis.setDrawLabels(false);
    }

    public void setGoal(int max) {
        YAxis bottomAxis = getAxisRight();
        YAxis topAxis = getAxisLeft();
        // set axis scale to the max price of the item
        bottomAxis.setAxisMinimum(0);
        bottomAxis.setAxisMaximum(max);
        topAxis.setAxisMinimum(0);
        topAxis.setAxisMaximum(max);
    }

    public void setProgress(int progress) {
        // TODO: move the bar data set stuff in here
    }

}

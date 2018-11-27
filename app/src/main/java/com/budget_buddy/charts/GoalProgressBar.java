package com.budget_buddy.charts;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import com.budget_buddy.R;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.List;

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

        // take care of top and bottom labels
        topAxis.setDrawLabels(false);

        setTouchEnabled(false);
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

    public void setProgress(int progress, AppCompatActivity activity) {
        clear();
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, progress));
        BarDataSet barDataSet = new BarDataSet(entries, "");

        // set colors
        barDataSet.setColor(getResources().getColor(R.color.colorPrimary, activity.getTheme()));
        barDataSet.setBarBorderColor(getResources().getColor(R.color.colorPrimaryDark, activity.getTheme()));
        barDataSet.setBarBorderWidth(2.5f);

        BarData barData = new BarData(barDataSet);
        barData.setDrawValues(false);

        setData(barData);
        setFitBars(true);
    }

}

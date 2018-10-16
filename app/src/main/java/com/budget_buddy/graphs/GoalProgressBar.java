package com.budget_buddy.graphs;

import android.content.Context;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

public class GoalProgressBar extends HorizontalBarChart {

    public GoalProgressBar(Context context) {
        super(context);

        setFitBars(true);
        // remove legend
        getLegend().setEnabled(false);
        // remove description
        getDescription().setEnabled(false);

        // get axes
        XAxis xAxis = getXAxis();
        YAxis topAxis = getAxisLeft();
        YAxis bottomAxis = getAxisRight();
        // don't show the grid
        topAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);
        topAxis.setDrawGridLines(false);

        // don't draw line along xAxis
        xAxis.setDrawAxisLine(false);
        // don't draw xAxis labels
        xAxis.setDrawLabels(false);

        // set axis scale to the max price of the item
        bottomAxis.setAxisMinimum(0);
        bottomAxis.setAxisMaximum(300);
        topAxis.setAxisMinimum(0);
        topAxis.setAxisMaximum(300);
        // take care of top and bottom labels
        topAxis.setDrawLabels(false);
    }
}

package com.budget_buddy.charts;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.budget_buddy.R;
import com.budget_buddy.utils.Data.MyCallback;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class SpendingChart extends BarChart {

    Resources.Theme theme;

    public SpendingChart(Context context, Resources.Theme theme) {
        super(context);

        this.theme = theme;
        // don't show the grid
        getXAxis().setDrawGridLines(false);
        getAxisLeft().setDrawGridLines(false);
        getAxisRight().setEnabled(false);

        // draw labels on bottom
        getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        IAxisValueFormatter axisValueFormatter = new IAxisValueFormatter() {

            private String[] days = getResources().getStringArray(R.array.day_abbreviations);

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                Calendar calendar = Calendar.getInstance();
                // this graph will only show previous 7 days
                int today = calendar.get(Calendar.DAY_OF_WEEK);
                return days[((int) value + today) % 7];
            }
        };

        // remove label
        getDescription().setEnabled(false);

        getXAxis().setValueFormatter(axisValueFormatter);
        // set the bottom of the window to y=0
        getAxisLeft().setAxisMinimum(0);

        getLegend().setEnabled(false);

        //chart.animateX(2000);
        animateY( 2000, Easing.EasingOption.EaseInOutExpo);

        setTouchEnabled(false);
    }

    public void setEntries(List<BarEntry> entries) {
        BarDataSet dataSet = new BarDataSet(entries, "");
        // set colors
        dataSet.setColor(getResources().getColor(R.color.colorPrimary, theme));
        dataSet.setBarBorderColor(getResources().getColor(R.color.colorPrimaryDark, theme));
        dataSet.setBarBorderWidth(2.5f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.85f);
        setData(barData);
        setFitBars(true);

        IValueFormatter valueFormatter = new IValueFormatter() {

            private DecimalFormat mFormat = new DecimalFormat("###,###,##0.00");

            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return "$" + mFormat.format(value);
            }
        };

        barData.setValueFormatter(valueFormatter);
        animateY( 2000, Easing.EasingOption.EaseInOutExpo);
    }
}

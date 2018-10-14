package com.budget_buddy;

import android.graphics.Color;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.budget_buddy.animations.ExperienceBarAnimation;

import com.github.mikephil.charting.animation.Easing;
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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Dashboard extends AppCompatActivity {

    BBUser currentUser = BBUser.GetInstance();

    ProgressBar experienceBar;
    TextView experienceProgressText;
    ExperienceBarAnimation experienceBarAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);


        setupExperienceBar();
        addChart();
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

        } else if (view.getId() == R.id.cameraEntry) {

        } else {
            return;
        }
    }

    private void addChart() {
        BarChart chart = new BarChart(this);
        chart.setId(R.id.bar_graph_view);

        ConstraintLayout cl = (ConstraintLayout) findViewById(R.id.dataDigestLayout);
        cl.addView(chart,0,500);

        ConstraintSet constraintSet = new ConstraintSet();

        constraintSet.clone(cl);
        constraintSet.connect(chart.getId(), ConstraintSet.LEFT, cl.getId(),ConstraintSet.LEFT, 8);
        constraintSet.connect(chart.getId(), ConstraintSet.RIGHT, cl.getId(),ConstraintSet.RIGHT, 8);
        constraintSet.connect(chart.getId(),ConstraintSet.BOTTOM, cl.getId(),ConstraintSet.BOTTOM, 8);
        constraintSet.applyTo(cl);

        List<BarEntry> entries = new ArrayList<BarEntry>();
        entries.add(new BarEntry(1, 5));
        entries.add(new BarEntry(2, 3));
        entries.add(new BarEntry(3, 8));
        entries.add(new BarEntry(4, 7));
        entries.add(new BarEntry(5, 2));
        entries.add(new BarEntry(6, 1));
        entries.add(new BarEntry(7, 5));

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

        chart.getLegend().setEnabled(false);

        //chart.animateX(2000);
        chart.animateY( 2000, Easing.EasingOption.EaseInOutExpo);


    }

}

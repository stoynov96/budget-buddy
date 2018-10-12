package com.budget_buddy;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.budget_buddy.animations.ExperienceBarAnimation;

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


}

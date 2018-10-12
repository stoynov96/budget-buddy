package com.budget_buddy;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ProgressBar;
import com.budget_buddy.animations.ExperienceBarAnimation;

public class Dashboard extends AppCompatActivity {

    BBUser currentUser = BBUser.GetInstance();

    ProgressBar experienceBar;
    ExperienceBarAnimation experienceBarAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        experienceBar = findViewById(R.id.experienceBar);
        experienceBarAnimation = new ExperienceBarAnimation(experienceBar);
        experienceBarAnimation.setProgress(90);
        //setExperienceBar(90);
    }

    private void setExperienceBar(int experience) {
        //experienceBarAnimation.setProgress(90);
    }
}

package com.budget_buddy.animations;

import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ProgressBar;

// https://stackoverflow.com/a/23156398/10417393

public class ExperienceBarAnimation extends Animation {
    private ProgressBar progressBar;
    private long animationDuration = 2000;
    private long stepDuration;
    private int moveTo;
    private int moveFrom;

    public ExperienceBarAnimation(ProgressBar progressBar) {
        super();
        this.progressBar = progressBar;
        stepDuration = animationDuration / progressBar.getMax();
    }

    public void setProgress(int progress) {
        if (progress < 0) {
            progress = 0;
        } else if(progress > progressBar.getMax()) {
            progress = progressBar.getMax();
        }

        moveTo = progress;
        moveFrom = progressBar.getProgress();

        //animationDuration = (Math.abs(moveTo - moveFrom) * stepDuration);
        setDuration(Math.abs(moveTo - moveFrom) * stepDuration);
        progressBar.startAnimation(this);
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);
        float value = moveFrom + (moveTo - moveFrom) * interpolatedTime;
        progressBar.setProgress((int) value);
    }
}

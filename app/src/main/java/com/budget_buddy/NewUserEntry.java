package com.budget_buddy;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class NewUserEntry extends AppCompatActivity {
    BBUser user = BBUser.GetInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user_entry);
    }
}

package com.sdunk.jiraestimator;

import android.content.Intent;
import android.os.Bundle;

import com.sdunk.jiraestimator.data.db.DBExecutor;
import com.sdunk.jiraestimator.data.db.user.UserDatabase;
import com.sdunk.jiraestimator.data.model.User;
import com.sdunk.jiraestimator.ui.login.LoginActivity;

import androidx.appcompat.app.AppCompatActivity;

public class InitialActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DBExecutor.getInstance().diskIO().execute(this::startAppIntent);
    }

    private void startAppIntent() {
        User user = UserDatabase.getInstance(getApplicationContext()).userDao().getLoggedInUser();

        Intent intent = new Intent();

        if (user == null) {
            intent.setClass(getApplicationContext(), LoginActivity.class);
        } else {
            if (user.getProjectKey() == null) {
                intent.setClass(getApplicationContext(), ProjectSelectActivity.class);
            } else {
                intent.setClass(getApplicationContext(), LoginActivity.class);
            }
        }

        startActivity(intent);
        finish();
    }
}
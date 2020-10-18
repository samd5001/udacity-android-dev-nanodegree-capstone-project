package com.sdunk.jiraestimator;

import android.content.Intent;
import android.os.Bundle;

import com.sdunk.jiraestimator.db.DBExecutor;
import com.sdunk.jiraestimator.db.user.UserDatabase;
import com.sdunk.jiraestimator.model.User;
import com.sdunk.jiraestimator.view.issues.IssueListActivity;
import com.sdunk.jiraestimator.view.login.LoginActivity;
import com.sdunk.jiraestimator.view.project.ProjectSelectActivity;

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
                intent.setClass(getApplicationContext(), IssueListActivity.class);
            }
        }

        startActivity(intent);
        finish();
    }
}
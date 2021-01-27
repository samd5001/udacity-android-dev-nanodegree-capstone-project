package com.sdunk.jiraestimator.db;

import android.content.Context;

import com.sdunk.jiraestimator.BuildConfig;
import com.sdunk.jiraestimator.R;
import com.sdunk.jiraestimator.db.issue.IssueDatabase;
import com.sdunk.jiraestimator.db.project.ProjectDatabase;
import com.sdunk.jiraestimator.db.user.UserDatabase;

import static android.content.Context.MODE_PRIVATE;

public class DBUtils {

    public static void clearLoggedInData(Context context) {
        UserDatabase.getInstance(context.getApplicationContext()).userDao().logoutUser();
        ProjectDatabase.getInstance(context.getApplicationContext()).projectDao().clearProjects();
        IssueDatabase.getInstance(context.getApplicationContext()).issueDAO().clearIssues();
        context.getSharedPreferences(BuildConfig.APPLICATION_ID, MODE_PRIVATE).edit().remove(context.getString(R.string.widget_pref)).apply();
    }
}

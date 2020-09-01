package com.sdunk.jiraestimator.db;

import android.content.Context;
import android.util.Log;

import com.sdunk.jiraestimator.model.JiraIssue;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {JiraIssue.class}, version = 1, exportSchema = false)
public abstract class IssueDatabase extends RoomDatabase {

    private static final String LOG_TAG = IssueDatabase.class.getSimpleName();
    private static final Object LOCK = new Object();
    private static final String DB_NAME = "jira_data";
    private static IssueDatabase instance;

    public static IssueDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (LOCK) {
                Log.d(LOG_TAG, "Creating db instance");
                instance = Room.databaseBuilder(context.getApplicationContext(), IssueDatabase.class, DB_NAME).build();
            }
        }
        Log.d(LOG_TAG, "Getting db instance");
        return instance;
    }

    public abstract IssueDAO issueDAO();
}

package com.sdunk.jiraestimator.db.issue;

import android.content.Context;

import com.sdunk.jiraestimator.db.Converters;
import com.sdunk.jiraestimator.model.JiraIssue;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import timber.log.Timber;

@Database(entities = {JiraIssue.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class IssueDatabase extends RoomDatabase {

    private static final Object LOCK = new Object();
    private static final String DB_NAME = "jira_data";
    private static IssueDatabase instance;

    public static IssueDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (LOCK) {
                Timber.d("Creating db instance");
                instance = Room.databaseBuilder(context.getApplicationContext(), IssueDatabase.class, DB_NAME).build();
            }
        }
        Timber.d("Getting db instance");
        return instance;
    }

    public abstract IssueDAO issueDAO();
}

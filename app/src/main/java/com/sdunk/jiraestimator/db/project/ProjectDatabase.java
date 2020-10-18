package com.sdunk.jiraestimator.db.user;

import android.content.Context;

import com.sdunk.jiraestimator.db.project.ProjectDao;
import com.sdunk.jiraestimator.model.Project;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import timber.log.Timber;

@Database(entities = {Project.class}, version = 1, exportSchema = false)
public abstract class ProjectDatabase extends RoomDatabase {

    private static final Object LOCK = new Object();
    private static final String DB_NAME = "project_data";
    private static ProjectDatabase instance;

    public static ProjectDatabase getInstance(Context context) {

        if (instance == null) {
            synchronized (LOCK) {
                Timber.d("Creating db instance");
                instance = Room.databaseBuilder(context.getApplicationContext(), ProjectDatabase.class, DB_NAME).build();
            }
        }
        Timber.d("Getting db instance");
        return instance;
    }

    public abstract ProjectDao projectDao();

}

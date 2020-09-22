package com.sdunk.jiraestimator;

import android.app.Application;

import timber.log.Timber;

public class JiraEstimatorApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }

}

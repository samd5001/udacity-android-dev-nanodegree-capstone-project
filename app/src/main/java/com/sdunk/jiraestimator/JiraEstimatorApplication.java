package com.sdunk.jiraestimator;

import android.app.Application;

import com.google.android.gms.nearby.Nearby;
import com.sdunk.jiraestimator.nearby.EstimateNearbyService;

import timber.log.Timber;

public class JiraEstimatorApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        EstimateNearbyService estimateNearbyService = EstimateNearbyService.getInstance();
        estimateNearbyService.setConnectionsClient(Nearby.getConnectionsClient(this));

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }

}

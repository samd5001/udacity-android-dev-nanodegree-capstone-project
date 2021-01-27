package com.sdunk.jiraestimator.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.RemoteViews;

import com.sdunk.jiraestimator.BuildConfig;
import com.sdunk.jiraestimator.R;
import com.sdunk.jiraestimator.SplashScreenActivity;
import com.sdunk.jiraestimator.db.DBExecutor;
import com.sdunk.jiraestimator.db.issue.IssueDatabase;
import com.sdunk.jiraestimator.model.JiraIssue;

import static android.content.Context.MODE_PRIVATE;

public class IssueWidgetProvider extends AppWidgetProvider {

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                 int appWidgetId) {
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_issue);

        SharedPreferences prefs = context.getSharedPreferences(BuildConfig.APPLICATION_ID, MODE_PRIVATE);
        String key = prefs.getString(context.getString(R.string.widget_pref), null);

        DBExecutor.getInstance().diskIO().execute(() -> {
            JiraIssue issue = key == null ? null : IssueDatabase.getInstance(context).issueDAO().loadIssueByKey(key);

            if (issue != null) {
                populateIssueWidget(context, views, issue);
            } else {
                populateEmptyWidget(context, views);
            }

            Intent appIntent = new Intent(context, SplashScreenActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, appIntent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        });
    }

    private void populateIssueWidget(Context context, RemoteViews views, JiraIssue issue) {
        String storyDescription = issue.getDescription() == null || issue.getDescription().isEmpty() ? context.getString(R.string.no_description) : issue.getDescription();
        views.setTextViewText(R.id.widget_story_name, issue.getKey() + " - " + issue.getName());
        views.setViewVisibility(R.id.widget_header, View.VISIBLE);
        views.setTextViewText(R.id.widget_story_description, storyDescription);
        views.setTextViewText(R.id.widget_story_points, issue.getStoryPoints() != null ? issue.getStoryPoints().toString() : "");
        views.setViewVisibility(R.id.widget_story_point_container, View.VISIBLE);
    }

    private void populateEmptyWidget(Context context, RemoteViews views) {
        views.setViewVisibility(R.id.widget_header, View.GONE);
        views.setViewVisibility(R.id.widget_story_point_container, View.GONE);
        views.setTextViewText(R.id.widget_story_description, context.getString(R.string.widget_empty_text));
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }
}
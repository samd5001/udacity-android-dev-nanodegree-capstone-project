package com.sdunk.jiraestimator.api;

import android.content.Context;

import com.google.gson.JsonObject;
import com.sdunk.jiraestimator.db.DBExecutor;
import com.sdunk.jiraestimator.db.user.UserDatabase;
import com.sdunk.jiraestimator.model.GenericResponse;
import com.sdunk.jiraestimator.model.JiraIssue;
import com.sdunk.jiraestimator.model.User;

import org.jetbrains.annotations.NotNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class APIUtils {

    public static void updateIssueCache(Context context) {

        DBExecutor.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                User loggedInUser = UserDatabase.getInstance(context).userDao().getLoggedInUser();
                if (loggedInUser != null && loggedInUser.getProjectKey() != null) {
                    JiraServiceFactory.buildService(loggedInUser.getJiraUrl()).searchIssues(loggedInUser.getAuthHeader(), formatProjectJQL(loggedInUser.getProjectKey())).enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                            response.body();
                        }

                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {

                        }
                    });
                }
            }
        });
    }

    private static String formatProjectJQL(String projectKey) {
        return "project=" + projectKey;
    }
}

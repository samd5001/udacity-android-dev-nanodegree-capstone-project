package com.sdunk.jiraestimator.api;

import android.content.Context;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sdunk.jiraestimator.db.DBExecutor;
import com.sdunk.jiraestimator.db.issue.IssueDAO;
import com.sdunk.jiraestimator.db.issue.IssueDatabase;
import com.sdunk.jiraestimator.db.user.UserDatabase;
import com.sdunk.jiraestimator.model.Field;
import com.sdunk.jiraestimator.model.JiraIssue;
import com.sdunk.jiraestimator.model.User;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
                    JiraService service = JiraServiceFactory.buildService(loggedInUser.getJiraUrl());
                    if (service != null) {
                        service.getFields(loggedInUser.getAuthHeader()).enqueue(new Callback<List<Field>>() {
                            @Override
                            public void onResponse(@NotNull Call<List<Field>> call, @NotNull Response<List<Field>> response) {
                                List<Field> fields = response.body();

                                if (fields != null) {
                                    fields.stream().filter(field -> field.getName().equalsIgnoreCase("Story Points"))
                                            .findFirst()
                                            .ifPresent(storyPointField ->
                                                    service.searchIssues(loggedInUser.getAuthHeader(), formatProjectJQL(loggedInUser.getProjectKey()), formatReturnFields(storyPointField.getId()))
                                                            .enqueue(new Callback<JsonObject>() {
                                        @Override
                                        public void onResponse(@NotNull Call<JsonObject> call, @NotNull Response<JsonObject> response) {
                                            JsonObject issuesResponse = response.body();

                                            JsonArray issuesResponseArray = issuesResponse.getAsJsonArray("issues");

                                            List<JiraIssue> issues = StreamSupport.stream(issuesResponseArray.spliterator(), false)
                                                    .map(issueJson -> convertJsonObjectToPOJO(
                                                            issueJson.getAsJsonObject(),
                                                            storyPointField.getId()))
                                                    .collect(Collectors.toList());

                                            DBExecutor.getInstance().diskIO().execute(new Runnable() {
                                                @Override
                                                public void run() {
                                                    IssueDAO issueDAO =  IssueDatabase.getInstance(context).issueDAO();
                                                    issueDAO.clearIssues();
                                                    issueDAO.insertIssues(issues);
                                                }
                                            });
                                        }

                                        @Override
                                        public void onFailure(@NotNull Call<JsonObject> call, @NotNull Throwable t) {

                                        }
                                    }));

                                }
                            }

                            @Override
                            public void onFailure(@NotNull Call<List<Field>> call, @NotNull Throwable t) {

                            }
                        });
                    }
                }
            }
        });
    }

    private static JiraIssue convertJsonObjectToPOJO(JsonObject issueJson, String storyPointField) {
        JsonObject fieldsJson = issueJson.getAsJsonObject("fields");
        JsonElement storyPoints = fieldsJson.get(storyPointField);
        return new JiraIssue(issueJson.get("id").getAsString(),
                issueJson.get("self").getAsString(),
                issueJson.get("key").getAsString(),
                fieldsJson.get("summary").getAsString(),
                getDescriptionFromJson(fieldsJson),
                storyPoints.isJsonNull() ? null : storyPoints.getAsDouble());
    }

    private static String getDescriptionFromJson(JsonObject issueJson) {
        JsonElement descriptionJson = issueJson.get("description");

        if (!descriptionJson.isJsonNull()) {
            return StreamSupport.stream(descriptionJson.getAsJsonObject().getAsJsonArray("content").spliterator(), false)
                    .map(paragraph -> StreamSupport
                            .stream(paragraph.getAsJsonObject().get("content").getAsJsonArray().spliterator(), false)
                            .map(paragraphContent -> paragraphContent.getAsJsonObject().get("text").getAsString().isEmpty() ? "\n" : paragraphContent.getAsJsonObject().get("text").getAsString() + "\n").collect(Collectors.joining()))
                    .collect(Collectors.joining());
        }
        return null;
    }

    private static String formatProjectJQL(String projectKey) {
        return "project=" + projectKey;
    }

    private static String formatReturnFields(String storyPointField) {
        return "summary,description," + storyPointField;
    }
}

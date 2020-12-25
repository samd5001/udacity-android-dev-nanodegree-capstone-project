package com.sdunk.jiraestimator.api;

import android.content.Context;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.sdunk.jiraestimator.db.DBExecutor;
import com.sdunk.jiraestimator.db.issue.IssueDAO;
import com.sdunk.jiraestimator.db.issue.IssueDatabase;
import com.sdunk.jiraestimator.db.user.UserDatabase;
import com.sdunk.jiraestimator.model.Field;
import com.sdunk.jiraestimator.model.JiraIssue;
import com.sdunk.jiraestimator.model.User;
import com.sdunk.jiraestimator.view.estimate.EstimateActivity;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import lombok.AllArgsConstructor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AllArgsConstructor
public class APIUtils {

    private final Context context;

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

    public static void updateIssuePoints(User user, String key, String points, EstimateActivity activity) {
        JiraService service = JiraServiceFactory.buildService(user.getJiraUrl());
        if (service != null) {
            service.getFields(user.getAuthHeader()).enqueue(new Callback<List<Field>>() {
                @Override
                public void onResponse(@NotNull Call<List<Field>> call, @NotNull Response<List<Field>> response) {
                    List<Field> fields = response.body();

                    if (fields != null) {
                        fields.stream().filter(field -> field.getName().equalsIgnoreCase("Story Points"))
                                .findFirst()
                                .ifPresent(storyPointField -> {

                                    JsonObject fieldsObject = new JsonObject();

                                    if (points.equals("?")) {
                                        fieldsObject.add(storyPointField.getId(), JsonNull.INSTANCE);
                                    } else {
                                        fieldsObject.addProperty(storyPointField.getId(), Double.valueOf(points));
                                    }

                                    JsonObject requestBody = new JsonObject();
                                    requestBody.add("fields", fieldsObject);

                                    service.updateStoryPoints(user.getAuthHeader(), key, requestBody).enqueue(new Callback<JsonObject>() {
                                        @Override
                                        public void onResponse(@NotNull Call<JsonObject> call, @NotNull Response<JsonObject> response) {
                                            if (response.code() == 204) {
                                                new APIUtils(activity).updateIssueCache();
                                                activity.handleSuccessfulVote(points);
                                            } else {
                                                activity.handleVoteError(points);
                                            }
                                        }

                                        @Override
                                        public void onFailure(@NotNull Call<JsonObject> call, @NotNull Throwable t) {
                                            activity.handleVoteError(points);
                                        }
                                    });
                                });
                    }
                }

                @Override
                public void onFailure(@NotNull Call<List<Field>> call, @NotNull Throwable t) {

                }
            });
        }
    }

    // This method was static taking a Context parameter but was causing a VerifyError when called
    public void updateIssueCache() {

        DBExecutor.getInstance().diskIO().execute(() -> {
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

                                                                if (issuesResponse != null) {
                                                                    JsonArray issuesResponseArray = issuesResponse.getAsJsonArray("issues");

                                                                    List<JiraIssue> issues = StreamSupport.stream(issuesResponseArray.spliterator(), false)
                                                                            .map(issueJson -> convertJsonObjectToPOJO(
                                                                                    issueJson.getAsJsonObject(),
                                                                                    storyPointField.getId()))
                                                                            .collect(Collectors.toList());

                                                                    DBExecutor.getInstance().diskIO().execute(() -> {
                                                                        IssueDAO issueDAO = IssueDatabase.getInstance(context).issueDAO();
                                                                        issueDAO.clearIssues();
                                                                        issueDAO.insertIssues(issues);
                                                                    });
                                                                }
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
        });
    }
}

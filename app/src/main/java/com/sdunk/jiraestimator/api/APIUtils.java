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
import com.sdunk.jiraestimator.model.GenericResponse;
import com.sdunk.jiraestimator.model.JiraIssue;
import com.sdunk.jiraestimator.model.Project;
import com.sdunk.jiraestimator.model.User;
import com.sdunk.jiraestimator.nearby.EstimateNearbyService;
import com.sdunk.jiraestimator.view.login.LoginUser;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import androidx.lifecycle.MutableLiveData;
import lombok.AllArgsConstructor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

@AllArgsConstructor
public class APIUtils {

    private static APIIdlingResource apiIdlingResource;
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

    /**
     * Updates an issue with new story points using the Jira REST API.
     *  @param user     Logged in user.
     * @param key      Key of issue to be updated,
     * @param points   New points to be set.
     */
    public static void updateIssuePoints(User user, String key, String points) {
        JiraService service = JiraServiceFactory.buildService(user.getJiraUrl());
        EstimateNearbyService estimateNearbyService = EstimateNearbyService.getInstance();
        if (service != null) {
            Timber.d("Sending GET request to /rest/api/3/field");

            service.getFields(user.getAuthHeader()).enqueue(new Callback<List<Field>>() {
                @Override
                public void onResponse(@NotNull Call<List<Field>> call, @NotNull Response<List<Field>> response) {
                    Timber.d("Response received from /rest/api/3/field");

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

                                    Timber.d("Sending PUT request to /rest/api/3/issue/%s", key);
                                    service.updateStoryPoints(user.getAuthHeader(), key, requestBody).enqueue(new Callback<JsonObject>() {
                                        @Override
                                        public void onResponse(@NotNull Call<JsonObject> call, @NotNull Response<JsonObject> response) {
                                            if (response.code() == 204) {
                                                Timber.d("Issue with key %s success response received", key);
                                                estimateNearbyService.handleSuccessfulVote(points);
                                            } else {
                                                Timber.d("Issue with key %s fail response received", key);
                                                estimateNearbyService.handleFailedVote(points);
                                            }
                                            setIdling();
                                        }

                                        @Override
                                        public void onFailure(@NotNull Call<JsonObject> call, @NotNull Throwable t) {
                                            Timber.d("Error occurred when calling update on issue with key %s", key);
                                            estimateNearbyService.handleFailedVote(points);
                                            setIdling();
                                        }
                                    });
                                });
                    }
                }

                @Override
                public void onFailure(@NotNull Call<List<Field>> call, @NotNull Throwable t) {
                    Timber.d("Request to /rest/api/3/field failed");
                    estimateNearbyService.handleFailedVote(points);
                    setIdling();
                }
            });
        }
    }

    public static void getUserProjects(LoginUser loginUser, MutableLiveData<LoginUser> userMutableLiveData) {

        JiraService service = JiraServiceFactory.buildService(loginUser.getUrl());
        if (service != null) {
            Call<GenericResponse<Project>> projectCall = service.getProjects(loginUser.getAuthToken());

            projectCall.enqueue(new Callback<GenericResponse<Project>>() {
                @Override
                public void onResponse(@NotNull Call<GenericResponse<Project>> call, @NotNull Response<GenericResponse<Project>> response) {
                    Timber.d("Login response received");

                    if (response.body() != null) {
                        Timber.d("Login successful");
                        loginUser.setProjectList(response.body().getValues());
                    } else {
                        loginUser.setApiError(response.message());
                    }
                    if (userMutableLiveData != null) {
                        userMutableLiveData.setValue(loginUser);
                    }
                    setIdling();
                }

                @Override
                public void onFailure(@NotNull Call<GenericResponse<Project>> call, @NotNull Throwable t) {
                    loginUser.setApiError(t.getMessage());
                    if (userMutableLiveData != null) {
                        userMutableLiveData.setValue(loginUser);
                    }
                    setIdling();
                }
            });
        }
    }

    /**
     * Set idling resource to idle for tests.
     */
    private static void setIdling() {
        if (apiIdlingResource == null) {
            apiIdlingResource = new APIIdlingResource();
        }
        apiIdlingResource.setIdleState();
    }

    public static APIIdlingResource getApiIdlingResource() {
        if (apiIdlingResource == null) {
            apiIdlingResource = new APIIdlingResource();
        }

        return apiIdlingResource;
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
                                                                        setIdling();
                                                                    });
                                                                }
                                                            }

                                                            @Override
                                                            public void onFailure(@NotNull Call<JsonObject> call, @NotNull Throwable t) {
                                                                setIdling();
                                                            }
                                                        }));

                            }
                        }

                        @Override
                        public void onFailure(@NotNull Call<List<Field>> call, @NotNull Throwable t) {
                            setIdling();
                        }
                    });
                }
            }
        });
    }
}

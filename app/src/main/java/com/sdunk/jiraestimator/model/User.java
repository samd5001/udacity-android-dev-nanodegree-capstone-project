package com.sdunk.jiraestimator.model;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import okhttp3.Credentials;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
@Entity(tableName = "users")
public class User {

    private String jiraUrl;

    @Nullable
    private String projectKey;

    @PrimaryKey
    @NonNull
    private String email;

    private String token;

    public User(String jiraUrl, @Nullable String projectKey, String email, String token) {
        this.jiraUrl = jiraUrl;
        this.projectKey = projectKey;
        this.email = email;
        this.token = token;
    }

    @Ignore
    public User(String jiraUrl, @NotNull String email, String token) {
        this.jiraUrl = jiraUrl;
        this.email = email;
        this.token = token;
    }

    public String getJiraUrl() {
        return jiraUrl;
    }

    public void setJiraUrl(String jiraUrl) {
        this.jiraUrl = jiraUrl;
    }

    public @Nullable
    String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(@Nullable String projectKey) {
        this.projectKey = projectKey;
    }


    public @NonNull
    String getEmail() {
        return email;
    }

    public void setEmail(@NonNull String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getAuthHeader() {
        return Credentials.basic(email, token);
    }
}
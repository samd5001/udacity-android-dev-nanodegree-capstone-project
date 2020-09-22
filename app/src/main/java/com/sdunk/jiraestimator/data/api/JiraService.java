package com.sdunk.jiraestimator.data.api;

import com.sdunk.jiraestimator.data.model.GenericResponse;
import com.sdunk.jiraestimator.data.model.Project;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface JiraService {

    @GET("rest/api/3/project/search?maxResults=10000")
    public Call<GenericResponse<Project>> getProjects(@Header("Authorization") String token);
}

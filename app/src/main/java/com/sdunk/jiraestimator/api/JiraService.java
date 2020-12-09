package com.sdunk.jiraestimator.api;

import com.google.gson.JsonObject;
import com.sdunk.jiraestimator.model.Field;
import com.sdunk.jiraestimator.model.GenericResponse;
import com.sdunk.jiraestimator.model.Project;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface JiraService {

    @GET("rest/api/3/project/search?maxResults=100000")
    Call<GenericResponse<Project>> getProjects(@Header("Authorization") String token);

    @GET("rest/api/3/search?maxResults=100000")
    Call<JsonObject> searchIssues(@Header("Authorization") String token, @Query("jql") String jqlQuery, @Query("fields") String returnFields);

    @GET("rest/api/3/field")
    Call<List<Field>> getFields(@Header("Authorization") String token);

    @PUT("/rest/api/3/issue/{issueKey}")
    Call<JsonObject> updateStoryPoints(@Header("Authorization") String token, @Path("issueKey") String key, @Body JsonObject body);


}

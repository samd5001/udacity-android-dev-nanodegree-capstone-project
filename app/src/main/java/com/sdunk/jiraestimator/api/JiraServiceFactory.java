package com.sdunk.jiraestimator.api;

import android.util.Patterns;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class JiraServiceFactory {

    private static Retrofit retrofit;


    public static JiraService buildService(String url) {
        if (url != null && !url.isEmpty() && Patterns.WEB_URL.matcher(url).matches() && (retrofit == null || !retrofit.baseUrl().toString().equalsIgnoreCase(url))) {
            retrofit = new retrofit2.Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit == null ? null : retrofit.create(JiraService.class);
    }
}

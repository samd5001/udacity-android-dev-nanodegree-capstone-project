package com.sdunk.jiraestimator.api;

import android.util.Patterns;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class JiraServiceFactory {

    private static Retrofit retrofit;

    public static OkHttpClient getHttpClient() {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.level(HttpLoggingInterceptor.Level.BODY);


        //TODO : remove logging interceptors as it is to be used for development purpose
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(300, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS).
                        addInterceptor(logging).
                        build();

        return client;
    }

    public static JiraService buildService(String url) {
        if (url != null && !url.isEmpty() && Patterns.WEB_URL.matcher(url).matches() && (retrofit == null || !retrofit.baseUrl().toString().equalsIgnoreCase(url))) {
            retrofit = new retrofit2.Retrofit.Builder()
                    .baseUrl(url)
                    .client(getHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit == null ? null : retrofit.create(JiraService.class);
    }
}

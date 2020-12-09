package com.sdunk.jiraestimator.db;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import androidx.room.TypeConverter;

public class Converters {

    @TypeConverter
    public List<String> stringToList(String stringList) {
        return new Gson().fromJson(stringList, new TypeToken<List<String>>() {
        }.getType());
    }

    @TypeConverter
    public String listToString(List<String> list) {
        return new Gson().toJson(list);
    }
}

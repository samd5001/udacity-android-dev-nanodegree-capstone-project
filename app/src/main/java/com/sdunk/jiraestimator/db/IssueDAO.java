package com.sdunk.jiraestimator.db;

import com.sdunk.jiraestimator.model.JiraIssue;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface IssueDAO {

    @Query("SELECT * FROM issues WHERE id = :movieId")
    JiraIssue loadByKey(Integer movieId);

    @Query("SELECT * FROM issues")
    LiveData<List<JiraIssue>> loadFavourites();

    @Insert
    void insertFavourite(JiraIssue issue);

    @Delete
    void deleteFavourite(JiraIssue issue);


}

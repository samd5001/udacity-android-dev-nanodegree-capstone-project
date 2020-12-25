package com.sdunk.jiraestimator.db.issue;

import com.sdunk.jiraestimator.model.JiraIssue;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface IssueDAO {

    @Query("SELECT * FROM issues WHERE `key` = :key")
    LiveData<JiraIssue> loadIssueByKey(String key);

    @Query("SELECT * FROM issues")
    LiveData<List<JiraIssue>> loadIssues();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertIssue(JiraIssue issue);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertIssues(List<JiraIssue> issues);

    @Update
    void updateIssue(JiraIssue issue);

    @Update
    void updateIssues(List<JiraIssue> issues);

    @Delete
    void deleteIssue(JiraIssue issue);

    @Query("DELETE FROM issues")
    void clearIssues();


}

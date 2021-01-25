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
    LiveData<JiraIssue> loadLiveIssueByKey(String key);

    @Query("SELECT * FROM issues WHERE `key` = :key")
    JiraIssue loadIssueByKey(String key);

    @Query("SELECT * FROM issues")
    LiveData<List<JiraIssue>> loadIssues();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertIssues(List<JiraIssue> issues);

    @Query("DELETE FROM issues")
    void clearIssues();
}

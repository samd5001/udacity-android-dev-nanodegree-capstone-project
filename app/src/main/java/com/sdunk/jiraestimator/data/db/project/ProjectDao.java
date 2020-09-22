package com.sdunk.jiraestimator.data.db.project;

import com.sdunk.jiraestimator.data.model.JiraIssue;
import com.sdunk.jiraestimator.data.model.Project;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface ProjectDao {

    @Query("SELECT * FROM projects WHERE `key` = :key")
    Project loadByKey(String key);

    @Query("SELECT * FROM projects")
    LiveData<List<Project>> loadProjects();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProjects(List<Project> projects);

    @Query("DELETE FROM projects")
    void clearProjects();

}

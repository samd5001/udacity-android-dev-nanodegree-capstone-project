package com.sdunk.jiraestimator.view.issues;

import android.app.Application;

import com.sdunk.jiraestimator.db.issue.IssueDatabase;
import com.sdunk.jiraestimator.model.JiraIssue;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import lombok.Getter;

@Getter
public class IssueListViewModel extends AndroidViewModel {

    private final LiveData<List<JiraIssue>> issues;

    public IssueListViewModel(@NonNull Application application) {
        super(application);
        issues = IssueDatabase.getInstance(getApplication()).issueDAO().loadIssues();
    }
}

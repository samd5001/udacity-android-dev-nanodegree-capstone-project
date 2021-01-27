package com.sdunk.jiraestimator.view.issues;

import android.app.Application;

import com.sdunk.jiraestimator.db.issue.IssueDatabase;
import com.sdunk.jiraestimator.model.JiraIssue;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import lombok.Getter;

@Getter
public class IssueViewModel extends AndroidViewModel {

    private String issueKey;

    private LiveData<JiraIssue> issue;

    public IssueViewModel(@NonNull Application application) {
        super(application);
    }

    public void setIssueKey(String issueKey) {
        this.issueKey = issueKey;
        this.issue = IssueDatabase.getInstance(getApplication()).issueDAO().loadLiveIssueByKey(issueKey);
    }
}

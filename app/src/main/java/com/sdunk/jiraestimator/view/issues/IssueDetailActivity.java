package com.sdunk.jiraestimator.view.issues;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBar;

import android.view.MenuItem;

import com.sdunk.jiraestimator.R;
import com.sdunk.jiraestimator.model.JiraIssue;

public class IssueDetailActivity extends AppCompatActivity {

    private JiraIssue issue;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(IssueDetailFragment.ARG_ISSUE, issue);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_detail);

        if (savedInstanceState == null) {
            issue = getIntent().getParcelableExtra(IssueDetailFragment.ARG_ISSUE);
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putParcelable(IssueDetailFragment.ARG_ISSUE, issue);
            IssueDetailFragment fragment = new IssueDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.issue_detail_container, fragment)
                    .commit();
        } else {
            issue = savedInstanceState.getParcelable(IssueDetailFragment.ARG_ISSUE);
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(issue.getName());
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            navigateUpTo(new Intent(this, IssueListActivity.class));

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
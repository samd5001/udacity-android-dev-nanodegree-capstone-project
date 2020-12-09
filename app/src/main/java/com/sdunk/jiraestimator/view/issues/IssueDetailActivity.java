package com.sdunk.jiraestimator.view.issues;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.sdunk.jiraestimator.R;
import com.sdunk.jiraestimator.model.JiraIssue;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import static com.sdunk.jiraestimator.view.issues.IssueDetailFragment.ARG_ISSUE;

public class IssueDetailActivity extends AppCompatActivity {

    private JiraIssue issue;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(ARG_ISSUE, issue);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_detail);

        if (savedInstanceState == null) {
            issue = getIntent().getParcelableExtra(ARG_ISSUE);
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle args = new Bundle();
            args.putParcelable(ARG_ISSUE, issue);
            IssueDetailFragment fragment = new IssueDetailFragment();
            fragment.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.issue_detail_container, fragment)
                    .commit();
        } else {
            issue = savedInstanceState.getParcelable(ARG_ISSUE);
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(issue.getKey());
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
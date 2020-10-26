package com.sdunk.jiraestimator.view.issues;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.sdunk.jiraestimator.R;
import com.sdunk.jiraestimator.adapters.GenericRVAdapter;
import com.sdunk.jiraestimator.api.APIUtils;
import com.sdunk.jiraestimator.databinding.IssueListBinding;
import com.sdunk.jiraestimator.databinding.IssueListItemBinding;
import com.sdunk.jiraestimator.model.JiraIssue;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

public class IssueListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    private final ArrayList<JiraIssue> issues = new ArrayList<>();

    private IssueListBinding listBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        APIUtils.updateIssueCache(getApplicationContext());

        com.sdunk.jiraestimator.databinding.ActivityIssueListBinding activityBinding = DataBindingUtil.setContentView(this, R.layout.activity_issue_list);
        listBinding = DataBindingUtil.getBinding(activityBinding.issueListLayout.issueList);

        if (listBinding != null && listBinding.issueDetailContainer != null) {
            mTwoPane = true;
        }

        setSupportActionBar(activityBinding.toolbar);
        activityBinding.toolbar.setTitle(getTitle());
        listBinding.issueList.setLayoutManager(new LinearLayoutManager(this));
        listBinding.issueList.setAdapter(new GenericRVAdapter<JiraIssue, IssueListItemBinding>(this, issues) {
            @Override
            public int getLayoutResId() {
                return R.layout.issue_list_item;
            }

            @Override
            public void onBindData(JiraIssue issue, int position, IssueListItemBinding dataBinding) {
                dataBinding.setIssue(issue);
            }

            @Override
            public void onItemClick(JiraIssue issue, int position) {
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putParcelable(IssueDetailFragment.ARG_ISSUE, issue);
                    IssueDetailFragment fragment = new IssueDetailFragment();
                    fragment.setArguments(arguments);
                    IssueListActivity.this.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.issue_detail_container, fragment)
                            .commit();
                } else {
                    Context context = IssueListActivity.this.getApplicationContext();
                    Intent intent = new Intent(context, IssueDetailActivity.class);
                    intent.putExtra(IssueDetailFragment.ARG_ISSUE, issue);
                    startActivity(intent);
                }
            }
        });

        setupDBObserver();
    }

    private void setupDBObserver() {
        new ViewModelProvider(this).get(IssueListViewModel.class).getIssues().observe(this, dbProjects -> {
            issues.clear();
            issues.addAll(dbProjects);
            if (listBinding.issueList.getAdapter() != null) {
                listBinding.issueList.getAdapter().notifyDataSetChanged();
            }
        });
    }
}
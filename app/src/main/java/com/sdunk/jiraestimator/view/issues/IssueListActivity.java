package com.sdunk.jiraestimator.view.issues;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sdunk.jiraestimator.R;
import com.sdunk.jiraestimator.adapters.GenericRVAdapter;
import com.sdunk.jiraestimator.api.APIUtils;
import com.sdunk.jiraestimator.api.JiraService;
import com.sdunk.jiraestimator.databinding.ActivityIssueListBinding;
import com.sdunk.jiraestimator.databinding.IssueListBinding;
import com.sdunk.jiraestimator.databinding.IssueListContentBinding;
import com.sdunk.jiraestimator.databinding.IssueListItemBinding;
import com.sdunk.jiraestimator.model.JiraIssue;
import com.sdunk.jiraestimator.view.issues.dummy.DummyContent;
import com.sdunk.jiraestimator.view.project.ProjectViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * An activity representing a list of Issues. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link IssueDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class IssueListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    private ArrayList<JiraIssue> issues = new ArrayList<>();

    private ActivityIssueListBinding activityBinding;
    private IssueListBinding listBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        APIUtils.updateIssueCache(getApplicationContext());

        activityBinding = DataBindingUtil.setContentView(this, R.layout.activity_issue_list);
        listBinding = DataBindingUtil.getBinding(activityBinding.issueListLayout.issueList);

        if (listBinding.issueDetailContainer != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
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
                    arguments.putParcelable(IssueDetailFragment.ARG_ITEM_ID, issue);
                    IssueDetailFragment fragment = new IssueDetailFragment();
                    fragment.setArguments(arguments);
                    IssueListActivity.this.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.issue_detail_container, fragment)
                            .commit();
                } else {
                    Context context = IssueListActivity.this.getApplicationContext();
                    Intent intent = new Intent(context, IssueDetailActivity.class);
                    intent.putExtra(IssueDetailFragment.ARG_ITEM_ID, issue);

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
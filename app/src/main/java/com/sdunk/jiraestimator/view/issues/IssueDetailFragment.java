package com.sdunk.jiraestimator.view.issues;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sdunk.jiraestimator.R;
import com.sdunk.jiraestimator.databinding.FragmentIssueDetailBinding;
import com.sdunk.jiraestimator.db.issue.IssueDatabase;
import com.sdunk.jiraestimator.model.JiraIssue;
import com.sdunk.jiraestimator.view.estimate.EstimateActivity;

import org.jetbrains.annotations.NotNull;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class IssueDetailFragment extends Fragment {

    public static final String ARG_ISSUE = "issue_arg";

    private LiveData<JiraIssue> issue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentIssueDetailBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_issue_detail, container, false);

        if (savedInstanceState == null) {
            Bundle args = getArguments();
            if (args != null && args.containsKey(ARG_ISSUE)) {
                issue = IssueDatabase.getInstance(getContext()).issueDAO().loadIssueByKey(args.getString(ARG_ISSUE));

                issue.observe(getViewLifecycleOwner(), jiraIssue -> {
                    if (issue.getValue() != null) {
                        binding.setIssue(issue.getValue());
                        binding.estimateButton.setEnabled(true);
                    } else {
                        binding.estimateButton.setEnabled(false);
                    }
                });
            }
        }


        binding.estimateButton.setOnClickListener(view -> {
            if (issue != null && issue.getValue() != null) {
                Intent intent = new Intent(getContext(), EstimateActivity.class);
                intent.putExtra(ARG_ISSUE, issue.getValue().getKey());
                startActivity(intent);
            }
        });

        return binding.getRoot();
    }

}
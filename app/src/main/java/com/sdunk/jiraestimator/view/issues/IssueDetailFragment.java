package com.sdunk.jiraestimator.view.issues;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sdunk.jiraestimator.R;
import com.sdunk.jiraestimator.databinding.FragmentIssueDetailBinding;
import com.sdunk.jiraestimator.model.JiraIssue;
import com.sdunk.jiraestimator.view.estimate.EstimateActivity;

import org.jetbrains.annotations.NotNull;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class IssueDetailFragment extends Fragment {

    public static final String ARG_ISSUE = "issue_arg";

    private JiraIssue issue;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(ARG_ISSUE, issue);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentIssueDetailBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_issue_detail, container, false);

        if (savedInstanceState != null && savedInstanceState.getParcelable(ARG_ISSUE) != null) {
            issue = savedInstanceState.getParcelable(ARG_ISSUE);
        } else {
            Bundle args = getArguments();

            if (args != null && args.containsKey(ARG_ISSUE)) {
                issue = args.getParcelable(ARG_ISSUE);
            }
        }

        if (issue != null) {
            binding.setIssue(issue);
        }

        binding.estimateButton.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), EstimateActivity.class);
            intent.putExtra(ARG_ISSUE, issue.getKey());
            startActivity(intent);
        });

        return binding.getRoot();
    }

}
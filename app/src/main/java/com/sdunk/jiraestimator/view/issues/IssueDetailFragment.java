package com.sdunk.jiraestimator.view.issues;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sdunk.jiraestimator.BuildConfig;
import com.sdunk.jiraestimator.R;
import com.sdunk.jiraestimator.databinding.FragmentIssueDetailBinding;
import com.sdunk.jiraestimator.view.estimate.EstimateActivity;
import com.sdunk.jiraestimator.widget.WidgetUtils;

import org.jetbrains.annotations.NotNull;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import lombok.NoArgsConstructor;

import static android.content.Context.MODE_PRIVATE;

@NoArgsConstructor
public class IssueDetailFragment extends Fragment {

    public static final String ARG_ISSUE = "issue_arg";

    private FragmentIssueDetailBinding binding;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        SharedPreferences prefs = requireActivity().getSharedPreferences(BuildConfig.APPLICATION_ID, MODE_PRIVATE);

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_issue_detail, container, false);

        Bundle args = getArguments();
        if (args != null && args.containsKey(ARG_ISSUE)) {
            IssueViewModel viewModel = new ViewModelProvider(requireActivity()).get(args.getString(ARG_ISSUE), IssueViewModel.class);
            if (viewModel.getIssueKey() == null) {
                viewModel.setIssueKey(args.getString(ARG_ISSUE));
            }

            viewModel.getIssue().observe(requireActivity(), jiraIssue -> {
                if (jiraIssue != null) {
                    binding.setIssue(jiraIssue);
                    binding.estimateButton.setEnabled(true);
                    binding.setIssueInWidget(jiraIssue.getKey().equals(prefs.getString(getString(R.string.widget_pref), "NOTSET")));
                }
            });
        }

        binding.showInWidgetCheckbox.setOnCheckedChangeListener((compoundButton, checked) -> {
            if (compoundButton.isPressed()) {
                if (checked && binding.getIssue() != null) {
                    prefs.edit().putString(getString(R.string.widget_pref), binding.getIssue().getKey()).apply();
                } else {
                    prefs.edit().remove(getString(R.string.widget_pref)).apply();
                }
                WidgetUtils.triggerWidgetUpdates(requireActivity());
            }
        });

        binding.estimateButton.setOnClickListener(view -> {
            if (binding.getIssue() != null) {
                Intent intent = new Intent(getContext(), EstimateActivity.class);
                intent.putExtra(ARG_ISSUE, binding.getIssue().getKey());
                startActivity(intent);
            }

        });

        return binding.getRoot();
    }
}
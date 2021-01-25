package com.sdunk.jiraestimator.view.issues;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.sdunk.jiraestimator.BuildConfig;
import com.sdunk.jiraestimator.R;
import com.sdunk.jiraestimator.databinding.FragmentIssueDetailBinding;
import com.sdunk.jiraestimator.db.issue.IssueDatabase;
import com.sdunk.jiraestimator.model.JiraIssue;
import com.sdunk.jiraestimator.view.estimate.EstimateActivity;
import com.sdunk.jiraestimator.widget.IssueWidgetProvider;

import org.jetbrains.annotations.NotNull;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import lombok.NoArgsConstructor;

import static android.content.Context.MODE_PRIVATE;

@NoArgsConstructor
public class IssueDetailFragment extends Fragment {

    public static final String ARG_ISSUE = "issue_arg";

    private LiveData<JiraIssue> issue;

    private boolean showInWidgetSelected = false;

    private SharedPreferences prefs;

    private FragmentIssueDetailBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        prefs = requireActivity().getSharedPreferences(BuildConfig.APPLICATION_ID, MODE_PRIVATE);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_issue_detail, container, false);

        if (savedInstanceState == null) {
            Bundle args = getArguments();
            if (args != null && args.containsKey(ARG_ISSUE)) {
                issue = IssueDatabase.getInstance(getContext()).issueDAO().loadLiveIssueByKey(args.getString(ARG_ISSUE));

                issue.observe(getViewLifecycleOwner(), jiraIssue -> {
                    if (issue.getValue() != null) {
                        binding.setIssue(issue.getValue());
                        binding.estimateButton.setEnabled(true);
                        setShowInWidgetCheckbox();
                        binding.showInWidgetCheckbox.setOnCheckedChangeListener((compoundButton, checked) -> {
                            if (checked) {
                                prefs.edit().putString(getString(R.string.widget_pref), issue.getValue().getKey()).apply();
                            } else {
                                prefs.edit().remove(getString(R.string.widget_pref)).apply();
                            }
                            Intent intent = new Intent(getContext(), IssueWidgetProvider.class);
                            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                            int[] ids = AppWidgetManager.getInstance(requireActivity().getApplication()).getAppWidgetIds(new ComponentName(requireActivity().getApplication(), IssueWidgetProvider.class));
                            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
                            requireActivity().sendBroadcast(intent);
                        });
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

    @Override
    public void onResume() {
        if (binding != null && issue.getValue() != null) {
            setShowInWidgetCheckbox();
        }
        super.onResume();
    }

    private void setShowInWidgetCheckbox() {
        showInWidgetSelected = issue.getValue().getKey().equals(prefs.getString(getString(R.string.widget_pref), "NOTSET"));
        binding.showInWidgetCheckbox.setChecked(showInWidgetSelected);
    }
}
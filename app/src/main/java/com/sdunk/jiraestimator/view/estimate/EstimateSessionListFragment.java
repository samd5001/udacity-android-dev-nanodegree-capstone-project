package com.sdunk.jiraestimator.view.estimate;

import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import lombok.NoArgsConstructor;
import timber.log.Timber;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sdunk.jiraestimator.R;
import com.sdunk.jiraestimator.databinding.FragmentEstimateSessionListBinding;
import com.sdunk.jiraestimator.databinding.FragmentIssueDetailBinding;

import org.jetbrains.annotations.NotNull;

@NoArgsConstructor
public class EstimateSessionListFragment extends Fragment {

    private FragmentEstimateSessionListBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_estimate_session_list, container, false);

        binding.hostButton.setOnClickListener(view -> ((EstimateActivity) getActivity()).startHostingSession());
        return binding.getRoot();
    }


}
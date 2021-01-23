package com.sdunk.jiraestimator.view.estimate;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sdunk.jiraestimator.R;
import com.sdunk.jiraestimator.databinding.FragmentEstimateVoteDeciderBinding;

import org.jetbrains.annotations.NotNull;

import androidx.databinding.DataBindingUtil;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class EstimateVoteDeciderFragment extends AbstractEstimateVoteFragment {

    private static final String ARG_OPTIONA = "arg_optiona";
    private static final String ARG_OPTIONB = "arg_optionb";

    public static EstimateVoteDeciderFragment newInstance(String optionA, String optionB) {
        EstimateVoteDeciderFragment fragment = new EstimateVoteDeciderFragment();
        Bundle args = new Bundle();
        args.putString(ARG_OPTIONA, optionA);
        args.putString(ARG_OPTIONB, optionB);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        hideAppBar();

        FragmentEstimateVoteDeciderBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_estimate_vote_decider, container, false);

        if (getArguments() != null) {
            String optionA = getArguments().getString(ARG_OPTIONA);
            String optionB = getArguments().getString(ARG_OPTIONB);
            binding.setVoteOptionA(optionA);
            binding.optionA.setOnClickListener(getVoteOnClickListener(optionA));
            binding.setVoteOptionB(optionB);
            binding.optionB.setOnClickListener(getVoteOnClickListener(optionB));
        }

        return binding.getRoot();
    }

    private View.OnClickListener getVoteOnClickListener(String option) {
        return view -> estimateNearbyService.submitVote(option);
    }
}
package com.sdunk.jiraestimator.view.estimate;

import android.os.Bundle;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sdunk.jiraestimator.R;
import com.sdunk.jiraestimator.databinding.FragmentEstimateVoteDeciderBinding;

import org.jetbrains.annotations.NotNull;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class EstimateVoteDeciderFragment extends Fragment {

    private static final String ARG_OPTIONA = "arg_optiona";
    private static final String ARG_OPTIONB = "arg_optionb";

    private String optionA;
    private String optionB;

    public static EstimateVoteDeciderFragment newInstance(String optionA, String optionB) {
        EstimateVoteDeciderFragment fragment = new EstimateVoteDeciderFragment();
        Bundle args = new Bundle();
        args.putString(ARG_OPTIONA, optionA);
        args.putString(ARG_OPTIONB, optionB);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TransitionInflater inflater = TransitionInflater.from(requireContext());
        setEnterTransition(inflater.inflateTransition(R.transition.fade));
        setExitTransition(inflater.inflateTransition(R.transition.fade));
        if (getArguments() != null) {
            optionA = getArguments().getString(ARG_OPTIONA);
            optionB = getArguments().getString(ARG_OPTIONB);
        }
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        FragmentEstimateVoteDeciderBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_estimate_vote_decider, container, false);

        binding.setVoteOptionA(optionA);
        binding.optionA.setOnClickListener(getVoteOnClickListener(optionA));
        binding.setVoteOptionB(optionB);
        binding.optionB.setOnClickListener(getVoteOnClickListener(optionB));

        return binding.getRoot();
    }

    private View.OnClickListener getVoteOnClickListener(String option) {
        return view -> ((EstimateActivity) requireActivity()).submitVote(option);
    }
}
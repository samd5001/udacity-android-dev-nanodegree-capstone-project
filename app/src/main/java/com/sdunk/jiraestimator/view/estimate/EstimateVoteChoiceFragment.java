package com.sdunk.jiraestimator.view.estimate;

import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sdunk.jiraestimator.R;
import com.sdunk.jiraestimator.databinding.FragmentEstimateVoteChoiceBinding;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EstimateVoteChoiceFragment extends Fragment {

    private static final String ARG_CHOICE = "arg_choice";

    public static EstimateVoteChoiceFragment newInstance(String vote) {
        EstimateVoteChoiceFragment fragment = new EstimateVoteChoiceFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CHOICE, vote);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentEstimateVoteChoiceBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_estimate_vote_choice, container, false);

        if (getArguments() != null && getArguments().containsKey(ARG_CHOICE)) {
            binding.setVoteOption(getArguments().getString(ARG_CHOICE));
            binding.voteChoice.setOnClickListener((v) -> ((EstimateActivity) requireActivity()).resetVote());
        }

        return binding.getRoot();
    }
}
package com.sdunk.jiraestimator.view.estimate;

import android.os.Bundle;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sdunk.jiraestimator.R;
import com.sdunk.jiraestimator.adapters.GenericRVAdapter;
import com.sdunk.jiraestimator.databinding.FragmentEstimateVoteBinding;
import com.sdunk.jiraestimator.databinding.VoteCardItemBinding;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class EstimateVoteFragment extends AbstractEstimateVoteFragment {


    public static EstimateVoteFragment newInstance() {
        EstimateVoteFragment fragment = new EstimateVoteFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEnterTransition(TransitionInflater.from(requireContext()).inflateTransition(R.transition.explode));
    }



    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        hideAppBar();

        FragmentEstimateVoteBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_estimate_vote, container, false);

        ArrayList<String> voteOptions = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.estimate_options)));
        binding.voteCardGrid.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        binding.voteCardGrid.setAdapter(new GenericRVAdapter<String, VoteCardItemBinding>(voteOptions) {

            @Override
            public int getLayoutResId() {
                return R.layout.vote_card_item;
            }

            @Override
            public void onBindData(String vote, int position, VoteCardItemBinding binding) {
                binding.setVoteOption(vote);
            }

            @Override
            public void onItemClick(String vote, int position) {
                estimateNearbyService.submitVote(vote);
            }
        });
        binding.voteCardGrid.setHasFixedSize(true);

        return binding.getRoot();
    }
}
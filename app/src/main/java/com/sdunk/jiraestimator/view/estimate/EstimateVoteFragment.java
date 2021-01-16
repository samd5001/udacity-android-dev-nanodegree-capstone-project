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

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EstimateVoteFragment extends Fragment {

    private EstimateActivity activity;

    private GenericRVAdapter<String, VoteCardItemBinding> gridAdapter;

    public static EstimateVoteFragment newInstance(GenericRVAdapter<String, VoteCardItemBinding> gridAdapter) {
        EstimateVoteFragment fragment = new EstimateVoteFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);

        fragment.gridAdapter = gridAdapter;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TransitionInflater inflater = TransitionInflater.from(requireContext());
        setEnterTransition(inflater.inflateTransition(R.transition.explode));
        setExitTransition(inflater.inflateTransition(R.transition.fade));

        activity = ((EstimateActivity) getActivity());
        if (activity != null) {
            activity.getBinding().estimateAppBar.setVisibility(GONE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        activity.getBinding().estimateAppBar.setVisibility(VISIBLE);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentEstimateVoteBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_estimate_vote, container, false);


        binding.voteCardGrid.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        binding.voteCardGrid.setAdapter(gridAdapter);
        binding.voteCardGrid.setHasFixedSize(true);

        return binding.getRoot();
    }
}
package com.sdunk.jiraestimator.view.estimate;

import android.os.Bundle;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sdunk.jiraestimator.R;
import com.sdunk.jiraestimator.adapters.GenericRVAdapter;
import com.sdunk.jiraestimator.databinding.FragmentEstimateVoteNineCardsBinding;
import com.sdunk.jiraestimator.databinding.VoteCardItemBinding;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.sdunk.jiraestimator.view.estimate.EstimateSessionHostFragment.ARG_IS_HOST;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EstimateVoteFragment extends Fragment {

    public static final String FRAGMENT_VOTE = "vote";

    private boolean isHost;

    private EstimateActivity activity;

    private GenericRVAdapter<String, VoteCardItemBinding> gridAdapter;

    private FragmentEstimateVoteNineCardsBinding binding;

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
        setEnterTransition(inflater.inflateTransition(R.transition.slide_up));
        setExitTransition(inflater.inflateTransition(R.transition.slide_up));
        if (getArguments() != null) {
            isHost = getArguments().getBoolean(ARG_IS_HOST);
        }

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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_estimate_vote_nine_cards, container, false);


        binding.voteCardGrid.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        binding.voteCardGrid.setAdapter(gridAdapter);

        return binding.getRoot();
    }
}
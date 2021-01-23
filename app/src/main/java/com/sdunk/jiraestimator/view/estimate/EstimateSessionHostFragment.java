package com.sdunk.jiraestimator.view.estimate;

import android.os.Bundle;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sdunk.jiraestimator.R;
import com.sdunk.jiraestimator.databinding.FragmentEstimateSessionListBinding;
import com.sdunk.jiraestimator.nearby.EstimateNearbyService;

import org.jetbrains.annotations.NotNull;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static android.view.View.INVISIBLE;

@NoArgsConstructor
public class EstimateSessionHostFragment extends Fragment {

    public static final String ARG_IS_HOST = "is_host_arg";

    private EstimateNearbyService estimateNearbyService;

    public static EstimateSessionHostFragment newInstance(Boolean isHost) {

        EstimateSessionHostFragment fragment = new EstimateSessionHostFragment();

        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_HOST, isHost);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        estimateNearbyService = EstimateNearbyService.getInstance();
        TransitionInflater inflater = TransitionInflater.from(requireContext());
        setExitTransition(inflater.inflateTransition(R.transition.explode));
        setEnterTransition(inflater.inflateTransition(R.transition.explode));
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        com.sdunk.jiraestimator.databinding.FragmentEstimateSessionListBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_estimate_session_list, container, false);
        binding.sessionListTitle.setText(R.string.session_participants_title);

        binding.sessionList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.sessionList.setAdapter(estimateNearbyService.createUserListAdapter());

        boolean isHost = getArguments() != null && getArguments().getBoolean(ARG_IS_HOST);

        if (isHost) {
            binding.hostButton.setText(R.string.estimate_start_voting);
            binding.hostButton.setOnClickListener((View view) -> {
                estimateNearbyService.startUserVoteSessions();
                ((EstimateActivity) requireActivity()).switchToVoteFragment();
            });
        } else {
            binding.hostButton.setVisibility(INVISIBLE);
        }

        return binding.getRoot();
    }
}
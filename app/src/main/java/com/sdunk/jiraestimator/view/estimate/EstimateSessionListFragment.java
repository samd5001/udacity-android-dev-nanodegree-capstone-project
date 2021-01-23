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
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class EstimateSessionListFragment extends Fragment {

    public static final String FRAGMENT_SESSION_LIST = "session_list";

    private EstimateNearbyService estimateNearbyService;

    FragmentEstimateSessionListBinding binding;

    public static EstimateSessionListFragment newInstance() {
        return new EstimateSessionListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        estimateNearbyService = EstimateNearbyService.getInstance();
        setExitTransition(TransitionInflater.from(requireContext())
                .inflateTransition(R.transition.explode));
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_estimate_session_list, container, false);

        binding.sessionList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.sessionList.setAdapter(estimateNearbyService.createSessionListAdapter());
        binding.hostButton.setOnClickListener(view -> estimateNearbyService.startHostingSession());
        return binding.getRoot();
    }
}
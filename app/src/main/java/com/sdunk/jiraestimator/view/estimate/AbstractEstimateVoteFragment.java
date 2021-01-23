package com.sdunk.jiraestimator.view.estimate;

import android.os.Bundle;
import android.transition.TransitionInflater;

import com.sdunk.jiraestimator.R;
import com.sdunk.jiraestimator.nearby.EstimateNearbyService;

import androidx.fragment.app.Fragment;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public abstract class AbstractEstimateVoteFragment extends Fragment {

    private EstimateActivity activity;

    protected EstimateNearbyService estimateNearbyService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        estimateNearbyService = EstimateNearbyService.getInstance();
        TransitionInflater inflater = TransitionInflater.from(requireContext());
        setReenterTransition(inflater.inflateTransition(R.transition.fade));
        setReenterTransition(inflater.inflateTransition(R.transition.fade));
        setExitTransition(inflater.inflateTransition(R.transition.fade));
        activity = ((EstimateActivity) requireActivity());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        activity.getBinding().estimateAppBar.setVisibility(VISIBLE);
    }

    protected void hideAppBar() {
        if (activity.getBinding() != null) {
            activity.getBinding().estimateAppBar.setVisibility(GONE);
        }
    }
}

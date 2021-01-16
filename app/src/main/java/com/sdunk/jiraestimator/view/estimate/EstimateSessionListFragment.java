package com.sdunk.jiraestimator.view.estimate;

import android.os.Bundle;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sdunk.jiraestimator.R;
import com.sdunk.jiraestimator.adapters.GenericRVAdapter;
import com.sdunk.jiraestimator.databinding.EstimateSessionItemBinding;
import com.sdunk.jiraestimator.model.EstimateUser;

import org.jetbrains.annotations.NotNull;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class EstimateSessionListFragment extends Fragment {

    public static final String FRAGMENT_LIST = "list";

    private GenericRVAdapter<EstimateUser, EstimateSessionItemBinding> listAdapter;

    public static EstimateSessionListFragment newInstance(GenericRVAdapter<EstimateUser, EstimateSessionItemBinding> listAdapter) {
        EstimateSessionListFragment fragment = new EstimateSessionListFragment();

        fragment.listAdapter = listAdapter;

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setExitTransition(TransitionInflater.from(requireContext())
                .inflateTransition(R.transition.explode));
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        com.sdunk.jiraestimator.databinding.FragmentEstimateSessionListBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_estimate_session_list, container, false);

        binding.sessionList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.sessionList.setAdapter(listAdapter);

        binding.hostButton.setOnClickListener(view -> ((EstimateActivity) requireActivity()).startHostingSession());
        return binding.getRoot();
    }

}
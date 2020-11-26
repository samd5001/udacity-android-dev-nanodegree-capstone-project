package com.sdunk.jiraestimator.view.estimate;

import android.os.Bundle;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sdunk.jiraestimator.R;
import com.sdunk.jiraestimator.adapters.GenericRVAdapter;
import com.sdunk.jiraestimator.databinding.EstimateSessionItemBinding;
import com.sdunk.jiraestimator.databinding.FragmentEstimateSessionListBinding;

import org.jetbrains.annotations.NotNull;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static android.view.View.INVISIBLE;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EstimateSessionHostFragment extends Fragment {

    public static final String FRAGMENT_HOST = "host";

    public static final String ARG_IS_HOST = "is_host_arg";

    private EstimateActivity activity;

    @Getter
    private FragmentEstimateSessionListBinding binding;

    private GenericRVAdapter<String, EstimateSessionItemBinding> listAdapter;

    private boolean isHost;

    public static EstimateSessionHostFragment newInstance(Boolean isHost, GenericRVAdapter<String, EstimateSessionItemBinding> listAdapter) {

        EstimateSessionHostFragment fragment = new EstimateSessionHostFragment();

        fragment.listAdapter = listAdapter;

        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_HOST, isHost);
        fragment.setArguments(args);

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



    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_estimate_session_list, container, false);
        binding.sessionListTitle.setText(R.string.session_participants_title);

        binding.sessionList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.sessionList.setAdapter(listAdapter);

        if (isHost) {
            binding.hostButton.setText(R.string.estimate_start_voting);
            binding.hostButton.setOnClickListener((View view) -> activity.startVoting());
        } else {
            binding.hostButton.setVisibility(INVISIBLE);
        }

        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (activity != null) {
            activity.handleHostSessionFragmentClose();
        }
    }
}
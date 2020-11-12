package com.sdunk.jiraestimator.view.estimate;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sdunk.jiraestimator.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EstimateSessionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EstimateSessionFragment extends Fragment {

    private static final String ARG_IS_HOST = "is_host_arg";

    private boolean isHost;

    public static EstimateSessionFragment newInstance(Boolean isHost) {
        EstimateSessionFragment fragment = new EstimateSessionFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_HOST, isHost);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isHost = getArguments().getBoolean(ARG_IS_HOST);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_estimate_session, container, false);
    }
}
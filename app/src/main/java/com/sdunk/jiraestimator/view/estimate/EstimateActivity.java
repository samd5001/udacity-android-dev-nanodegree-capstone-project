package com.sdunk.jiraestimator.view.estimate;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.sdunk.jiraestimator.R;
import com.sdunk.jiraestimator.databinding.ActivityEstimateBinding;
import com.sdunk.jiraestimator.db.DBExecutor;
import com.sdunk.jiraestimator.db.user.UserDatabase;
import com.sdunk.jiraestimator.model.User;
import com.sdunk.jiraestimator.nearby.EstimateNearbyService;

import java.util.Collections;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;
import lombok.Getter;

import static com.sdunk.jiraestimator.view.issues.IssueDetailFragment.ARG_ISSUE;

public class EstimateActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_REQUIRED_PERMISSIONS = 1;

    private static final String[] REQUIRED_PERMISSIONS =
            new String[]{
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
            };

    @Getter
    private String issueKey;

    @Getter
    private User user;

    private EstimateNearbyService estimateNearbyService;


    @Getter
    private ActivityEstimateBinding binding;

    /**
     *
     */
    private static boolean hasPermissions(Context context) {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     *
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(ARG_ISSUE, issueKey);
        super.onSaveInstanceState(outState);
    }

    /**
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estimate);
        estimateNearbyService = EstimateNearbyService.getInstance();
        estimateNearbyService.setEstimateActivity(this);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_estimate);

        setSupportActionBar(binding.estimateToolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {

            loadAndShowInterstitialAd();

            if (getIntent().hasExtra(ARG_ISSUE)) {
                issueKey = getIntent().getStringExtra(ARG_ISSUE);
            }

            switchToSessionListFragment();
        } else {
            issueKey = savedInstanceState.getString(ARG_ISSUE);
        }
        getUserFromDB();
    }

    /**
     *
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            if (fragmentManager.getBackStackEntryCount() > 0) {
                handleBackPress(fragmentManager);
            } else {
                finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() == 1) {
            new AlertDialog.Builder(this)
                    .setTitle("End Session")
                    .setMessage("Do you want to disconnect from this session?")
                    .setPositiveButton("Yes", (dialog, whichButton) -> handleBackPress(fragmentManager))
                    .setNegativeButton("No", null).show();
        } else if (fragmentManager.getBackStackEntryCount() == 2) {
            fragmentManager.popBackStack();
            estimateNearbyService.resetVote();
        } else {
            super.onBackPressed();
        }
    }

    private void handleBackPress(FragmentManager fragmentManager) {
        fragmentManager.popBackStack();
        if (fragmentManager.getBackStackEntryCount() == 1) {
            estimateNearbyService.resetNearbyConnections();
        }
    }

    /**
     *
     */
    @Override
    protected void onStart() {
        super.onStart();

        if (!hasPermissions(this)) {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_REQUIRED_PERMISSIONS);
        }
    }

    @Override
    protected void onDestroy() {
        if (isFinishing()) {
            estimateNearbyService.stopConnections();
        }
        super.onDestroy();
    }


    /**
     *
     */
    public void switchToSessionListFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.estimate_fragment_container, EstimateSessionListFragment.newInstance())
                .commit();
    }

    /**
     *
     */
    public void switchToSessionHostFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.estimate_fragment_container, EstimateSessionHostFragment.newInstance(estimateNearbyService.isHosting()))
                .addToBackStack("session_list")
                .commit();
    }

    /**
     *
     */
    public void switchToVoteFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStack();
        fragmentManager
                .beginTransaction()
                .replace(R.id.estimate_fragment_container, EstimateVoteFragment.newInstance())
                .addToBackStack("session_list")
                .commit();
    }

    /**
     *
     */
    public void switchToChoiceFragment(String vote) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.estimate_fragment_container, EstimateVoteChoiceFragment.newInstance(vote))
                .addToBackStack("vote")
                .commit();
    }

    /**
     *
     */
    public void switchToDeciderFragment(String optionA, String optionB) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStack();
        fragmentManager.popBackStack();
        fragmentManager
                .beginTransaction()
                .replace(R.id.estimate_fragment_container, EstimateVoteDeciderFragment.newInstance(optionA, optionB))
                .addToBackStack("session_list")
                .commit();
    }

    /**
     *
     */
    private void getUserFromDB() {
        DBExecutor.getInstance().diskIO().execute(() -> {
            user = UserDatabase.getInstance(EstimateActivity.this).userDao().getLoggedInUser();
            estimateNearbyService.startDiscovery();
        });
    }

    /**
     *
     */
    private void loadAndShowInterstitialAd() {
        MobileAds.initialize(this);
        RequestConfiguration requestConfiguration = new RequestConfiguration.Builder().setTestDeviceIds(Collections.singletonList("34C1F882D6CC2F3D16893974B7CEEC6C")).build();
        MobileAds.setRequestConfiguration(requestConfiguration);

        InterstitialAd interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getString(R.string.test_interstitial_ad_id));
        interstitialAd.loadAd(new AdRequest.Builder().build());
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                interstitialAd.show();
            }
        });
    }
}
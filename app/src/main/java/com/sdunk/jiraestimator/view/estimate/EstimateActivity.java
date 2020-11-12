package com.sdunk.jiraestimator.view.estimate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import timber.log.Timber;

import android.Manifest;
import android.app.ActionBar;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.sdunk.jiraestimator.R;
import com.sdunk.jiraestimator.db.DBExecutor;
import com.sdunk.jiraestimator.db.user.UserDatabase;
import com.sdunk.jiraestimator.model.User;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

import static com.sdunk.jiraestimator.view.issues.IssueDetailFragment.ARG_ISSUE;

public class EstimateActivity extends FragmentActivity {

    private static final String STATE_FRAGMENT = "state_fragment";

    private String issueKey;

    private ArrayList<String> endpointIds;

    private HashMap<String, String> endpointResults;

    private User user;

    private ConnectionsClient connectionsClient;

    // Callbacks for receiving payloads
    private final PayloadCallback payloadCallback =
            new PayloadCallback() {
                @Override
                public void onPayloadReceived(@NotNull String endpointId, @NotNull Payload payload) {
                    endpointResults.replace(endpointId, payload.toString());

                    if (endpointResults.entrySet().stream().allMatch(result -> result.getValue() != null && !result.getValue().isEmpty())) {
                        // DO ESTIMATION LOGIC
                        connectionsClient.stopAdvertising();
                        EstimateActivity.this.finish();
                    }
                }

                @Override
                public void onPayloadTransferUpdate(@NotNull String endpointId, @NotNull PayloadTransferUpdate update) {

                }
            };

    // Callbacks for finding other devices
    private final EndpointDiscoveryCallback endpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {
                @Override
                public void onEndpointFound(@NotNull String endpointId, @NotNull DiscoveredEndpointInfo info) {
                    Timber.i("onEndpointFound: endpoint found, adding to endpoint list");
                    endpointIds.add(endpointId);
                }

                @Override
                public void onEndpointLost(@NotNull String endpointId) {
                    Timber.i("onEndpointLost: endpoint lost, removing from endpoint list");
                    endpointIds.remove(endpointId);
                }
            };

    private static final String[] REQUIRED_PERMISSIONS =
            new String[] {
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
            };

    private static final int REQUEST_CODE_REQUIRED_PERMISSIONS = 1;

    // Callbacks for connections to other devices
    private final ConnectionLifecycleCallback connectionLifecycleCallback =
            new ConnectionLifecycleCallback() {
                @Override
                public void onConnectionInitiated(@NotNull String endpointId, @NotNull ConnectionInfo connectionInfo) {
                    Timber.i("onConnectionInitiated: accepting connection");
                    connectionsClient.acceptConnection(endpointId, payloadCallback);
                }

                @Override
                public void onConnectionResult(@NotNull String endpointId, ConnectionResolution result) {
                    if (result.getStatus().isSuccess()) {
                        Timber.i( "onConnectionResult: connection successful");

                        endpointIds.add(endpointId);

                    } else {
                        Timber.i("onConnectionResult: connection failed");
                    }
                }

                @Override
                public void onDisconnected(@NotNull String endpointId) {
                    Timber.i("onDisconnected: endpoint connected");
                    endpointIds.remove(endpointId);
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estimate);
        connectionsClient = Nearby.getConnectionsClient(this);

        if (savedInstanceState == null) {
            if (getIntent().hasExtra(ARG_ISSUE)) {
                issueKey = getIntent().getStringExtra(ARG_ISSUE);
            }

            Bundle args = new Bundle();
            EstimateSessionListFragment fragment = new EstimateSessionListFragment();
            fragment.setArguments(args);
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.estimate_fragment_container, fragment)
                    .commit();
        } else {
            issueKey = savedInstanceState.getString(ARG_ISSUE);
        }

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


        getUserFromDB();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getUserFromDB() {
        DBExecutor.getInstance().diskIO().execute(() -> {
            user = UserDatabase.getInstance(EstimateActivity.this).userDao().getLoggedInUser();
            startDiscovery();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!hasPermissions(this, REQUIRED_PERMISSIONS)) {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_REQUIRED_PERMISSIONS);
        }
    }

    private static boolean hasPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void startDiscovery() {
        DiscoveryOptions discoveryOptions =
                new DiscoveryOptions.Builder().setStrategy(Strategy.P2P_STAR).build();

       connectionsClient
                .startDiscovery(getPackageName(), endpointDiscoveryCallback, discoveryOptions)
                .addOnSuccessListener(
                        (unused) -> Timber.d("Nearby Discovery started"))
                .addOnFailureListener(
                        (e) -> Timber.e("Nearby Discovery failed to start"));
    }

    private void startAdvertising() {
        AdvertisingOptions advertisingOptions =
                new AdvertisingOptions.Builder().setStrategy(Strategy.P2P_STAR).build();
        connectionsClient
                .startAdvertising(
                        user.getEmail(), getPackageName(), connectionLifecycleCallback, advertisingOptions)
                .addOnSuccessListener(
                        (unused) -> {
                            // We're advertising!
                        })
                .addOnFailureListener(
                        (e) -> {
                            // We were unable to start advertising.
                        });
    }

    public void startHostingSession() {
        startAdvertising();
        switchToSessionFragment(true);
    }

    private void switchToSessionFragment(boolean isHost) {

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.estimate_fragment_container, EstimateSessionFragment.newInstance(isHost))
                .addToBackStack("session_list")
                .commit();
    }
}
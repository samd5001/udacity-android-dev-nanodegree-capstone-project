package com.sdunk.jiraestimator.view.estimate;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

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
import com.sdunk.jiraestimator.adapters.GenericRVAdapter;
import com.sdunk.jiraestimator.databinding.ActivityEstimateBinding;
import com.sdunk.jiraestimator.databinding.EstimateSessionItemBinding;
import com.sdunk.jiraestimator.databinding.VoteCardItemBinding;
import com.sdunk.jiraestimator.db.DBExecutor;
import com.sdunk.jiraestimator.db.user.UserDatabase;
import com.sdunk.jiraestimator.model.EstimateUser;
import com.sdunk.jiraestimator.model.User;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import lombok.Getter;
import timber.log.Timber;

import static com.sdunk.jiraestimator.view.issues.IssueDetailFragment.ARG_ISSUE;

public class EstimateActivity extends AppCompatActivity {


    private static final String STATE_FRAGMENT = "state_fragment";

    private static final String STATE_HOST_LIST = "state_host_list";

    private static final String STATE_USER_LIST = "state_user_list";

    private static final String STATE_USER_RESULTS = "state_user_results";

    private static final String STATE_USER_NAMES = "state_user_names";

    @Getter
    private  ActivityEstimateBinding binding;

    private static final Strategy NEARBY_STRATEGY = Strategy.P2P_STAR;
    private static final String[] REQUIRED_PERMISSIONS =
            new String[]{
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
            };
    private static final int REQUEST_CODE_REQUIRED_PERMISSIONS = 1;
    private boolean isAdvertising = false;
    private boolean isDiscovering = false;
    private String currentFragment;
    private String issueKey;
    private ArrayList<EstimateUser> hosts;
    private ArrayList<EstimateUser> users;
    private ArrayList<String> userNames;
    private HashMap<String, String> userEstimates;
    private GenericRVAdapter<EstimateUser, EstimateSessionItemBinding> sessionListAdapter;
    private GenericRVAdapter<String, EstimateSessionItemBinding> hostListAdapter;
    private User user;

    private final PayloadCallback userPayloadCallback =
            new PayloadCallback() {
                @Override
                public void onPayloadReceived(@NotNull String endpointId, @NotNull Payload payload) {
                    String userName = new String(payload.asBytes(), StandardCharsets.UTF_8);

                    if (userNames.contains(userName)) {
                        userNames.remove(userName);
                    } else {
                        userNames.add(userName);
                    }
                    hostListAdapter.notifyDataSetChanged();
                }

                @Override
                public void onPayloadTransferUpdate(@NotNull String endpointId, @NotNull PayloadTransferUpdate update) {
                }
            };

    private final EndpointDiscoveryCallback endpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {
                @Override
                public void onEndpointFound(@NotNull String endpointId, @NotNull DiscoveredEndpointInfo info) {
                    Timber.i("onEndpointFound: endpoint found, adding to host list");

                    String hostName = getHostName(info.getEndpointName());
                    if (hostName != null) {
                        hosts.add(new EstimateUser(endpointId, hostName));
                        sessionListAdapter.notifyDataSetChanged();
                    }

                }

                @Override
                public void onEndpointLost(@NotNull String endpointId) {
                    Timber.i("onEndpointLost: endpoint lost, removing from endpoint list");
                    removeUserFromList(hosts, endpointId);
                }
            };
    private ConnectionsClient connectionsClient;


    private final PayloadCallback hostPayloadCallback =
            new PayloadCallback() {
                @Override
                public void onPayloadReceived(@NotNull String endpointId, @NotNull Payload payload) {
                    userEstimates.replace(endpointId, payload.toString());

                    estimate();
                }

                @Override
                public void onPayloadTransferUpdate(@NotNull String endpointId, @NotNull PayloadTransferUpdate update) {
                }
            };

    private void estimate() {
        if (userEstimates.entrySet().stream().allMatch(result -> result.getValue() != null && !result.getValue().isEmpty())) {
            // DO ESTIMATION LOGIC
            connectionsClient.stopAdvertising();
            connectionsClient.stopAllEndpoints();
            finish();
        }
    }

    private final ConnectionLifecycleCallback hostConnectionLifecycleCallback =
            new ConnectionLifecycleCallback() {
                @Override
                public void onConnectionInitiated(@NotNull String endpointId, @NotNull ConnectionInfo connectionInfo) {
                    Timber.d("onConnectionInitiated: accepting connection");
                    connectionsClient.acceptConnection(endpointId, hostPayloadCallback);

                    Timber.d("onConnectionInitiated: accepting connection");
                    users.add(new EstimateUser(endpointId, connectionInfo.getEndpointName()));
                }

                @Override
                public void onConnectionResult(@NotNull String endpointId, ConnectionResolution result) {
                    if (result.getStatus().isSuccess()) {
                        Timber.i("onConnectionResult: connection successful");

                        EstimateUser newUser = getUserForEndpointID(endpointId);

                        if (newUser != null) {

                            userNames.add(newUser.getEmail());
                            userEstimates.put(endpointId, null);

                            sendExistingUsersPayloads(newUser);
                            sendUserPayload(newUser);

                            hostListAdapter.notifyDataSetChanged();
                        }
                    } else {
                        removeUserFromList(users, endpointId);
                        Timber.i("onConnectionResult: connection failed");
                    }
                }

                @Override
                public void onDisconnected(@NotNull String endpointId) {
                    Timber.i("onDisconnected: endpoint connected");
                    EstimateUser disconnectedUser = getUserForEndpointID(endpointId);

                    sendUserPayload(disconnectedUser);
                    userNames.remove(disconnectedUser.getEmail());
                    userEstimates.remove(endpointId);
                    removeUserFromList(users, endpointId);
                }
            };

    private EstimateUser getUserForEndpointID(@NotNull String endpointId) {
        return users.stream().filter(user -> user.getEndpointId().equals(endpointId)).findFirst().orElse(null);
    }


    private final ConnectionLifecycleCallback userLifecycleCallback =
            new ConnectionLifecycleCallback() {
                @Override
                public void onConnectionInitiated(@NotNull String endpointId, @NotNull ConnectionInfo connectionInfo) {
                    Timber.i("onConnectionInitiated: accepting connection");
                    connectionsClient.acceptConnection(endpointId, userPayloadCallback);
                    userNames.add(getHostName(connectionInfo.getEndpointName()));
                }

                @Override
                public void onConnectionResult(@NotNull String endpointId, ConnectionResolution result) {
                    if (result.getStatus().isSuccess()) {
                        Timber.i("onConnectionResult: connection successful");
                        switchToSessionHostFragment(false);
                    } else {
                        Timber.i("onConnectionResult: connection failed");
                        userNames.clear();
                    }
                }

                @Override
                public void onDisconnected(@NotNull String endpointId) {
                    Timber.i("onDisconnected: endpoint disconnected");
                    resetActivity();
                    Toast.makeText(EstimateActivity.this, R.string.error_host_disconnect, Toast.LENGTH_LONG).show();
                }
            };

    private void resetActivity() {
        stopConnections();
        startDiscovery();
        setupSessionListFragment();
    }

    private void sendExistingUsersPayloads(EstimateUser newUser) {
        users.stream().filter(user -> !user.equals(newUser)).forEach(u -> sendUserPayload(newUser.getEndpointId(), u));
    }

    private void sendUserPayload(EstimateUser newUser) {
        Payload newUserPayload = Payload.fromBytes(newUser.getEmail().getBytes());
        connectionsClient.sendPayload(
                users
                    .stream()
                    .map(EstimateUser::getEndpointId)
                    .collect(Collectors.toList()),
                newUserPayload
        );
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
    
    private void sendUserPayload(String endpointId, EstimateUser user) {
        Payload payload = Payload.fromBytes(user.getEmail().getBytes());
        connectionsClient.sendPayload(endpointId, payload);
    }

    private String getHostName(String endpointName) {
        String[] userData = endpointName.split("%");
        if (userData.length == 2 && userData[0].equals(issueKey) && !userData[1].equalsIgnoreCase(user.getEmail())) {
            return userData[1];
        }

        return null;
    }

    private void removeUserFromList(List<EstimateUser> users, @NotNull String endpointId) {
        users.removeIf((user) -> user.getEndpointId().equals(endpointId));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(ARG_ISSUE, issueKey);
        outState.putString(STATE_FRAGMENT, currentFragment);
        outState.putParcelableArrayList(STATE_HOST_LIST, hosts);
        outState.putParcelableArrayList(STATE_USER_LIST, users);
        outState.putSerializable(STATE_USER_RESULTS, userEstimates);
        outState.putStringArrayList(STATE_USER_NAMES, userNames);
        super.onSaveInstanceState(outState);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estimate);
        connectionsClient = Nearby.getConnectionsClient(this);

        if (savedInstanceState == null) {
            if (getIntent().hasExtra(ARG_ISSUE)) {
                issueKey = getIntent().getStringExtra(ARG_ISSUE);
            }

            hosts = new ArrayList<>();
            users = new ArrayList<>();
            userEstimates = new HashMap<>();
            userNames = new ArrayList<>();

            setupSessionListFragment();
        } else {
            issueKey = savedInstanceState.getString(ARG_ISSUE);
            hosts = savedInstanceState.getParcelableArrayList(STATE_HOST_LIST);
            users = savedInstanceState.getParcelableArrayList(STATE_USER_LIST);
            userEstimates = (HashMap<String, String>) savedInstanceState.getSerializable(STATE_USER_RESULTS);
            userNames = savedInstanceState.getStringArrayList(STATE_USER_NAMES);
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_estimate);

        setSupportActionBar(binding.estimateToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getUserFromDB();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (getSupportFragmentManager().getBackStackEntryCount() != 0) {
                getSupportFragmentManager().popBackStack();
            } else {
                finish();
            }
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

    private void startDiscovery() {

        connectionsClient
                .startDiscovery(getPackageName(), endpointDiscoveryCallback, new DiscoveryOptions.Builder().setStrategy(NEARBY_STRATEGY).build())
                .addOnSuccessListener((unused) -> {
                    Timber.d("Nearby Discovery started");
                    isDiscovering = true;
                })
                .addOnFailureListener((e) -> Timber.e(e, "Nearby Discovery failed to start"));
    }

    private void startAdvertising() {
        users.clear();
        userEstimates.clear();
        userNames.clear();

        userEstimates.put(user.getEmail() + 20, null);
        userNames.add(user.getEmail() + 20);

        connectionsClient
                .startAdvertising(getHostEndpointName(), getPackageName(), hostConnectionLifecycleCallback, new AdvertisingOptions.Builder().setStrategy(NEARBY_STRATEGY).build())
                .addOnSuccessListener((unused) -> {
                    Timber.d("Nearby Advertising started");
                    isAdvertising = true;
                })
                .addOnFailureListener((e) -> Timber.e(e, "Nearby Advertising failed to start"));

    }

    private void stopDiscovery() {
        if (isDiscovering) {
            connectionsClient.stopDiscovery();
        }
        hosts.clear();
    }

    private void stopAdvertising() {
        if (isAdvertising) {
            connectionsClient.stopAdvertising();
        }
    }

    @NotNull
    private String getHostEndpointName() {
        return issueKey + "%" + user.getEmail() + 20;
    }

    public void startHostingSession() {
        startAdvertising();
        switchToSessionHostFragment(true);
    }

    private void setupSessionListFragment() {

        sessionListAdapter = new GenericRVAdapter<EstimateUser, EstimateSessionItemBinding>(this, hosts) {
            @Override
            public int getLayoutResId() {
                return R.layout.estimate_session_item;
            }

            @Override
            public void onBindData(EstimateUser hostName, int position, EstimateSessionItemBinding binding) {
                binding.setUserName(hostName.getEmail());
            }

            @Override
            public void onItemClick(EstimateUser hostName, int position) {
                connectionsClient.requestConnection(user.getEmail(), hostName.getEndpointId(), userLifecycleCallback);
            }
        };

        EstimateSessionListFragment fragment = EstimateSessionListFragment.newInstance(sessionListAdapter);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.estimate_fragment_container, fragment)
                .commit();

        currentFragment = EstimateSessionListFragment.FRAGMENT_LIST;
    }

    private void stopConnections() {
        stopDiscovery();
        stopAdvertising();
        connectionsClient.stopAllEndpoints();

    }

    private void clearUserData() {
        userNames.clear();
        users.clear();
        userEstimates.clear();
    }

    private void switchToSessionHostFragment(boolean isHost) {

        stopDiscovery();

        hostListAdapter = new GenericRVAdapter<String, EstimateSessionItemBinding>(this, userNames) {
            @Override
            public int getLayoutResId() {
                return R.layout.estimate_session_item;
            }

            @Override
            public void onBindData(String user, int position, EstimateSessionItemBinding binding) {
                binding.setUserName(user);
            }

            @Override
            public void onItemClick(String hostName, int position) {
            }
        };

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.estimate_fragment_container, EstimateSessionHostFragment.newInstance(isHost, hostListAdapter))
                .addToBackStack("session_list")
                .commit();

        currentFragment = EstimateSessionHostFragment.FRAGMENT_HOST;
    }

    public void startVoting() {
        ArrayList<String> voteOptions = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.estimate_options)));

        GenericRVAdapter<String, VoteCardItemBinding> voteOptionAdapter = new GenericRVAdapter<String, VoteCardItemBinding>(this, voteOptions) {
            @Override
            public int getLayoutResId() {
                return R.layout.vote_card_item;
            }

            @Override
            public void onBindData(String vote, int position, VoteCardItemBinding binding) {
                binding.setVoteOption(vote);
            }

            @Override
            public void onItemClick(String vote, int position) {
                //TODO: do vote
            }
        };

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.estimate_fragment_container, EstimateVoteFragment.newInstance(voteOptionAdapter))
                .addToBackStack("session_host")
                .commit();

        currentFragment = EstimateSessionHostFragment.FRAGMENT_HOST;
    }

    public void handleHostSessionFragmentClose() {
        stopConnections();
        startDiscovery();
    }
}
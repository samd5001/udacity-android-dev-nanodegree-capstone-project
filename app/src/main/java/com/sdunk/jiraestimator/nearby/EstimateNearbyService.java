package com.sdunk.jiraestimator.nearby;

import android.widget.Toast;

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
import com.sdunk.jiraestimator.api.APIUtils;
import com.sdunk.jiraestimator.databinding.EstimateSessionItemBinding;
import com.sdunk.jiraestimator.model.EstimateUser;
import com.sdunk.jiraestimator.model.User;
import com.sdunk.jiraestimator.view.estimate.EstimateActivity;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import timber.log.Timber;

import static com.sdunk.jiraestimator.api.APIUtils.updateIssuePoints;

@AllArgsConstructor
@NoArgsConstructor
public class EstimateNearbyService {

    private static final EstimateNearbyService instance = new EstimateNearbyService();

    private static final String START_VOTE = "START_VOTE";

    private static final String START_DECIDER = "START_DECIDER";

    private static final String VOTE_SUCCESS = "VOTE_SUCCESS";

    private static final String VOTE_ERROR = "VOTE_ERROR";

    private static final Strategy NEARBY_STRATEGY = Strategy.P2P_STAR;
    @NonNull
    @Getter
    private final ArrayList<EstimateUser> hosts = new ArrayList<>();
    @NonNull
    @Getter
    private final ArrayList<EstimateUser> users = new ArrayList<>();
    @NonNull
    @Getter
    private final ArrayList<String> userNames = new ArrayList<>();
    @NonNull
    @Getter
    private final HashMap<String, String> userEstimates = new HashMap<>();
    @Setter
    private ConnectionsClient connectionsClient;

    @Setter
    private EstimateActivity estimateActivity;
    /**
     * PayloadCallback for handling received payloads as a host.
     */
    private final PayloadCallback hostPayloadCallback =
            new PayloadCallback() {
                @Override
                public void onPayloadReceived(@NotNull String endpointId, @NotNull Payload payload) {
                    updateUserEstimates(endpointId, payload);
                    doEstimation();
                }

                @Override
                public void onPayloadTransferUpdate(@NotNull String endpointId, @NotNull PayloadTransferUpdate update) {
                }
            };
    @Getter
    private boolean hosting = false;
    private boolean isAdvertising = false;
    private boolean isDiscovering = false;
    private String hostEndpoint;
    @Setter
    private boolean isVoting = false;
    private boolean votingFinished = false;
    /**
     * ConnectionLifecycleCallback for handling connecting to a host devices
     */
    private final ConnectionLifecycleCallback userLifecycleCallback =
            new ConnectionLifecycleCallback() {
                @Override
                public void onConnectionInitiated(@NotNull String endpointId, @NotNull ConnectionInfo connectionInfo) {
                    Timber.i("onConnectionInitiated: accepting connection");
                    connectionsClient.acceptConnection(endpointId, userPayloadCallback);
                    userNames.add(getHostName(connectionInfo.getEndpointName()));
                    hostEndpoint = endpointId;
                }

                @Override
                public void onConnectionResult(@NotNull String endpointId, ConnectionResolution result) {
                    if (result.getStatus().isSuccess()) {
                        Timber.i("onConnectionResult: connection successful");
                        estimateActivity.switchToSessionHostFragment();
                    } else {
                        Timber.i("onConnectionResult: connection failed");
                        clearUserData();
                    }
                }

                @Override
                public void onDisconnected(@NotNull String endpointId) {
                    if (!votingFinished) {
                        Timber.i("onDisconnected: endpoint disconnected");
                        resetActivity();
                        Toast.makeText(estimateActivity, R.string.error_host_disconnect, Toast.LENGTH_LONG).show();
                    }
                }
            };
    private GenericRVAdapter<EstimateUser, EstimateSessionItemBinding> sessionListAdapter;
    /**
     * EndpointDiscoveryCallback for updating the session list on the first fragment.
     */
    private final EndpointDiscoveryCallback endpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {
                @Override
                public void onEndpointFound(@NotNull String endpointId, @NotNull DiscoveredEndpointInfo info) {
                    handleEndpointFound(endpointId, info);
                }

                @Override
                public void onEndpointLost(@NotNull String endpointId) {
                    handleEndpointLost(endpointId);
                }


            };
    private GenericRVAdapter<String, EstimateSessionItemBinding> userListAdapter;
    /**
     * ConnectionLifecycleCallback for handling connecting to a user device as a host
     */
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

                            sendExistingUserInfoPayloadsToNewUser(newUser);
                            sendNewUserInfoPayloadToExistingUsers(newUser);

                            userListAdapter.notifyDataSetChanged();
                            Toast.makeText(estimateActivity, estimateActivity.getString(R.string.user_connected_message, newUser.getEmail()), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        removeUserFromList(users, endpointId);
                        Timber.i("onConnectionResult: connection failed");
                    }
                }

                @Override
                public void onDisconnected(@NotNull String endpointId) {
                    Timber.i("onDisconnected: endpoint disconnected");
                    if (!estimateActivity.isDestroyed()) {
                        EstimateUser disconnectedUser = getUserForEndpointID(endpointId);

                        sendNewUserInfoPayloadToExistingUsers(disconnectedUser);
                        userNames.remove(disconnectedUser.getEmail());
                        userEstimates.remove(endpointId);
                        removeUserFromList(users, endpointId);
                        if (userListAdapter != null) {
                            userListAdapter.notifyDataSetChanged();
                        }
                        Toast.makeText(estimateActivity, estimateActivity.getString(R.string.user_disconnected_message, disconnectedUser.getEmail()), Toast.LENGTH_SHORT).show();
                        doEstimation();
                    }
                }
            };
    /**
     * PayloadCallback for handling received payloads as a user
     */
    private final PayloadCallback userPayloadCallback =
            new PayloadCallback() {
                @Override
                public void onPayloadReceived(@NotNull String endpointId, @NotNull Payload payload) {
                    handlePayloadFromHost(payload);
                }

                @Override
                public void onPayloadTransferUpdate(@NotNull String endpointId, @NotNull PayloadTransferUpdate update) {
                }
            };

    public static EstimateNearbyService getInstance() {
        return instance;
    }

    /**
     *
     */
    private void removeUserFromList(List<EstimateUser> users, @NonNull String endpointId) {
        users.removeIf((user) -> user.getEndpointId().equals(endpointId));
    }

    /**
     *
     */
    public GenericRVAdapter<EstimateUser, EstimateSessionItemBinding> createSessionListAdapter() {
        sessionListAdapter = new GenericRVAdapter<EstimateUser, EstimateSessionItemBinding>(hosts) {
            @Override
            public int getLayoutResId() {
                return R.layout.estimate_session_item;
            }

            @Override
            public void onBindData(EstimateUser hostName, EstimateSessionItemBinding binding) {
                binding.setUserName(hostName.getEmail());
            }

            @Override
            public void onItemClick(EstimateUser hostName) {
                connectionsClient.requestConnection(estimateActivity.getUser().getEmail(), hostName.getEndpointId(), userLifecycleCallback);
            }
        };
        return sessionListAdapter;
    }

    /**
     *
     */
    public GenericRVAdapter<String, EstimateSessionItemBinding> createUserListAdapter() {
        userListAdapter = new GenericRVAdapter<String, EstimateSessionItemBinding>(userNames) {
            @Override
            public int getLayoutResId() {
                return R.layout.estimate_session_item;
            }

            @Override
            public void onBindData(String user, EstimateSessionItemBinding binding) {
                binding.setUserName(user);
            }

            @Override
            public void onItemClick(String hostName) {
            }
        };
        return userListAdapter;
    }

    /**
     * Updates the stored estimate for a user from a payload.
     *
     * @param endpointId Endpoint id for user.
     * @param payload    Received payload message.
     */
    private void updateUserEstimates(@NotNull String endpointId, @NotNull Payload payload) {
        userEstimates.replace(endpointId, new String(payload.asBytes(), StandardCharsets.UTF_8));
    }

    /**
     * Parses a host payload and handles the payload based on the value
     */
    private void handlePayloadFromHost(@NotNull Payload payload) {
        String[] payloadArray = new String(payload.asBytes(), StandardCharsets.UTF_8).split("%");

        switch (payloadArray[0]) {
            case START_VOTE:
                estimateActivity.switchToVoteFragment();
                break;
            case START_DECIDER:
                estimateActivity.switchToDeciderFragment(payloadArray[1], payloadArray[2]);
                break;
            case VOTE_SUCCESS:
                handleSuccessfulVote(payloadArray[1]);
                break;
            case VOTE_ERROR:
                handleFailedVote(payloadArray[1]);
                break;
            default:
                updateUsers(payloadArray[0]);
        }
    }

    /**
     * Adds or removes a user from the userName list
     */
    private void updateUsers(String userName) {
        if (userNames.contains(userName)) {
            userNames.remove(userName);
        } else {
            userNames.add(userName);
        }
        userListAdapter.notifyDataSetChanged();
    }

    /**
     *
     */
    private void clearUserData() {
        userNames.clear();
        users.clear();
        userEstimates.clear();
    }

    /**
     * Resets the activity to the first fragment and resets Nearby Connections
     */
    public void resetActivity() {
        stopConnections();
        startDiscovery();
        clearUserData();
        estimateActivity.switchToSessionListFragment();
    }

    /**
     * Checks if all user votes have been collected and will either send the most common value to the API.
     * <b/>
     * If there is not a clear winner, the another round of voting will start.
     */
    private void doEstimation() {
        if (!userEstimates.isEmpty() && userEstimates.entrySet().stream().allMatch(result -> result.getValue() != null && !result.getValue().isEmpty())) {

            HashMap<String, Integer> choiceCountMap = new HashMap<>();
            userEstimates.values().forEach(value -> {
                Integer currentCount = choiceCountMap.get(value);
                if (currentCount == null) {
                    choiceCountMap.put(value, 0);
                } else {
                    choiceCountMap.put(value, currentCount + 1);
                }
            });

            if (choiceCountMap.values().size() == 1) {
                choiceCountMap
                        .keySet()
                        .stream()
                        .findFirst()
                        .ifPresent((choice) -> updateIssuePoints(estimateActivity.getUser(), estimateActivity.getIssueKey(), choice));
            } else {
                choiceCountMap
                        .entrySet()
                        .stream()
                        .max(Map.Entry.comparingByValue())
                        .ifPresent((topChoice) -> {
                            List<String> topVoteChoices = choiceCountMap.entrySet().stream().filter(entry -> entry.getValue().equals(topChoice.getValue())).map(Map.Entry::getKey).collect(Collectors.toList());
                            if (topVoteChoices.size() == 2) {
                                userEstimates.replaceAll((key, value) -> null);
                                topVoteChoices.sort(String::compareToIgnoreCase);
                                sendDeciderOptionsPayload(topVoteChoices.get(0), topVoteChoices.get(1));
                                estimateActivity.switchToDeciderFragment(topVoteChoices.get(0), topVoteChoices.get(1));
                            }
                        });
            }

        }
    }

    private EstimateUser getUserForEndpointID(@NotNull String endpointId) {
        return users.stream().filter(user -> user.getEndpointId().equals(endpointId)).findFirst().orElse(null);
    }

    /**
     * Sends details for every existing user to a new user.
     *
     * @param newUser User to send existing users to
     */
    private void sendExistingUserInfoPayloadsToNewUser(EstimateUser newUser) {
        users.stream().filter(user -> !user.equals(newUser)).forEach(u -> sendUserInfoPayloadToEndpoint(u, newUser.getEndpointId()));
    }

    /**
     * Sends a new user email to all connected endpoints.
     *
     * @param newUser User to be sent
     */
    private void sendNewUserInfoPayloadToExistingUsers(EstimateUser newUser) {
        sendPayloadToAllUsers(newUser.getEmail());
    }

    private void sendPayloadToAllUsers(String email) {
        Payload newUserPayload = Payload.fromBytes(email.getBytes());
        connectionsClient.sendPayload(
                users
                        .stream()
                        .map(EstimateUser::getEndpointId)
                        .collect(Collectors.toList()),
                newUserPayload
        );
    }

    /**
     *
     */
    private void sendUserInfoPayloadToEndpoint(EstimateUser user, String userEndpoint) {
        Payload payload = Payload.fromBytes(user.getEmail().getBytes());
        connectionsClient.sendPayload(userEndpoint, payload);
    }

    /**
     *
     */
    private void sendVotePayload(String vote) {
        Payload payload = Payload.fromBytes(vote.getBytes());
        connectionsClient.sendPayload(hostEndpoint, payload);
    }

    /**
     *
     */
    private void sendDeciderOptionsPayload(String optionA, String optionB) {
        sendPayloadToAllUsers(START_DECIDER + "%" + optionA + "%" + optionB);
    }

    /**
     *
     */
    public void startUserVoteSessions() {
        sendPayloadToAllUsers(START_VOTE);
        stopAdvertising();
    }

    /**
     *
     */
    private void sendVoteResultPayloads(String voteStatus, String voteValue) {
        sendPayloadToAllUsers(voteStatus + "%" + voteValue);
    }


    /**
     *
     */
    private String getHostName(String endpointName) {
        String[] userData = endpointName.split("%");
        if (userData.length == 2 && userData[0].equals(estimateActivity.getIssueKey()) && !userData[1].equalsIgnoreCase(estimateActivity.getUser().getEmail())) {
            return userData[1];
        }

        return null;
    }

    /**
     *
     */
    public void startDiscovery() {

        if (!isDiscovering) {
            connectionsClient
                    .startDiscovery(estimateActivity.getPackageName(), endpointDiscoveryCallback, new DiscoveryOptions.Builder().setStrategy(NEARBY_STRATEGY).build())
                    .addOnSuccessListener((unused) -> {
                        Timber.d("Nearby Discovery started");
                        isDiscovering = true;
                    })
                    .addOnFailureListener((e) -> Timber.e(e, "Nearby Discovery failed to start"));
        }
    }

    /**
     *
     */
    public void stopDiscovery() {
        if (isDiscovering) {
            connectionsClient.stopDiscovery();
            isDiscovering = false;
            hosts.clear();
        }
    }

    /**
     *
     */
    private void startAdvertising() {
        if (!isAdvertising) {
            clearUserData();

            userEstimates.put(estimateActivity.getUser().getEmail(), null);
            userNames.add(estimateActivity.getUser().getEmail());

            connectionsClient
                    .startAdvertising(getHostEndpointName(), estimateActivity.getPackageName(), hostConnectionLifecycleCallback, new AdvertisingOptions.Builder().setStrategy(NEARBY_STRATEGY).build())
                    .addOnSuccessListener((unused) -> {
                        Timber.d("Nearby Advertising started");
                        isAdvertising = true;
                    })
                    .addOnFailureListener((e) -> Timber.e(e, "Nearby Advertising failed to start"));
        }
    }

    /**
     *
     */
    private void stopAdvertising() {
        if (isAdvertising) {
            connectionsClient.stopAdvertising();
            isAdvertising = false;
        }
    }

    /**
     *
     */
    @NonNull
    private String getHostEndpointName() {
        return estimateActivity.getIssueKey() + "%" + estimateActivity.getUser().getEmail();
    }

    /**
     *
     */
    public void startHostingSession() {
        stopDiscovery();
        startAdvertising();
        hosting = true;
        estimateActivity.switchToSessionHostFragment();
    }

    /**
     *
     */
    public void stopConnections() {
        stopDiscovery();
        stopAdvertising();
        hosting = false;
        connectionsClient.stopAllEndpoints();
        clearUserData();
    }

    /**
     *
     */
    public void submitVote(String vote) {
        if (hosting) {
            userEstimates.put(estimateActivity.getUser().getEmail(), vote);
            doEstimation();
        } else {
            sendVotePayload(vote);
        }

        estimateActivity.switchToChoiceFragment(vote);
    }

    /**
     *
     */
    public void resetVote() {
        boolean sendStartVote = false;
        if (!hosting) {
            sendVotePayload("");
        } else {
            HashMap<String, String> estimates = userEstimates;
            User user = estimateActivity.getUser();
            if (!estimates.containsKey(user.getEmail())) {
                sendStartVote = true;
            }
            estimates.put(user.getEmail(), null);
        }

        if (sendStartVote) {
            startUserVoteSessions();
        }
    }

    /**
     *
     */
    public void resetNearbyConnections() {
        stopConnections();
        startDiscovery();
    }

    /**
     *
     */
    public void handleSuccessfulVote(String vote) {
        if (isHosting()) {
            sendVoteResultPayloads(VOTE_SUCCESS, vote);
        }
        new APIUtils(estimateActivity).updateIssueCache();
        String toastMessage = vote.equals("?") ? estimateActivity.getString(R.string.undecided_vote_response) : estimateActivity.getString(R.string.successful_vote_response, vote);
        handleVoteFinish(toastMessage);
    }

    /**
     *
     */
    public void handleFailedVote(String vote) {
        if (isHosting()) {
            sendVoteResultPayloads(VOTE_ERROR, vote);
        }
        handleVoteFinish(estimateActivity.getString(R.string.error_vote_response, vote));
    }

    private void handleVoteFinish(String toastMessage) {
        votingFinished = true;
        Toast.makeText(estimateActivity, toastMessage, Toast.LENGTH_LONG).show();
        stopConnections();
        estimateActivity.finish();
    }


    /**
     *
     */
    private void handleEndpointLost(@NonNull String endpointId) {
        Timber.i("onEndpointLost: endpoint lost, removing from endpoint list");
        removeUserFromList(hosts, endpointId);
        sessionListAdapter.notifyDataSetChanged();
    }

    /**
     *
     */
    private void handleEndpointFound(@NonNull String endpointId, @NonNull DiscoveredEndpointInfo info) {
        Timber.i("onEndpointFound: endpoint found, adding to host list");

        String hostName = getHostName(info.getEndpointName());
        if (hostName != null && sessionListAdapter != null) {
            hosts.add(new EstimateUser(endpointId, hostName));
            sessionListAdapter.notifyDataSetChanged();
        }
    }
}

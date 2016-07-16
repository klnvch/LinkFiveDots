package by.klnvch.link5dots.online;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMultiplayer;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;

import java.util.ArrayList;
import java.util.List;

import by.klnvch.link5dots.Dot;
import by.klnvch.link5dots.GameView;
import by.klnvch.link5dots.HighScore;
import by.klnvch.link5dots.R;
import by.klnvch.link5dots.settings.SettingsUtils;

public class OnlineGameActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        RealTimeMessageReceivedListener, RoomStatusUpdateListener, RoomUpdateListener,
        OnInvitationReceivedListener,
        View.OnClickListener, RealTimeMultiplayer.ReliableMessageSentCallback {

    private static final String TAG = "OnlineGame";

    private static final String STATE_RESOLVING_ERROR = "resolving_error";

    private static final int REQUEST_RESOLVE_ERROR = 1001;
    private static final int RC_DUMMY = 1002;
    private final static int RC_SELECT_PLAYERS = 10000;
    private final static int RC_INVITATION_INBOX = 10001;
    private final static int RC_WAITING_ROOM = 10002;

    private GameView view;
    private int currentScreen = R.id.screen_wait;

    private GoogleApiClient mGoogleApiClient;
    private boolean mResolvingError = false;
    // Has the user clicked the sign-in button?
    private boolean mSignInClicked = false;
    private boolean mAutoStartSignInFlow = true;

    private String mRoomId;
    private String currentParticipantId;
    private String opponentParticipantId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int orientation = getIntent().getIntExtra("orientation", -1);
        switch (orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                break;
        }
        setContentView(R.layout.activity_online);

        View signInButton = findViewById(R.id.button_sign_in);
        assert signInButton != null;
        signInButton.setOnClickListener(this);

        view = (GameView) findViewById(R.id.game_view);

        view.setOnGameEventListener(new GameView.OnGameEventListener() {
            @Override
            public void onMoveDone(Dot currentDot, Dot previousDot) {
                currentDot.setType(Dot.USER);
                view.setDot(currentDot);
                sendMessage(Protocol.createDotMessage(currentDot));
            }

            @Override
            public void onGameEnd(HighScore highScore) {

            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();

        if (savedInstanceState != null) {
            mResolvingError = savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);
            mAutoStartSignInFlow = savedInstanceState.getBoolean("mAutoStartSignInFlow", true);
        }

        String username = SettingsUtils.getUserName(this, getString(R.string.device_info_default));
        TextView tvUsername = (TextView) findViewById(R.id.user_name);
        assert tvUsername != null;
        tvUsername.setText(username);

        view.restore(savedInstanceState);
        view.invalidate();
        view.isOver();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        view.save(outState);
        super.onSaveInstanceState(outState);
        outState.putBoolean("mAutoStartSignInFlow", mAutoStartSignInFlow);
        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
    }

    @Override
    public void onBackPressed() {
        switch (currentScreen) {
            case R.id.screen_game_board:
                if (mRoomId != null) {
                    new AlertDialog.Builder(this)
                            .setMessage(getString(R.string.bluetooth_is_disconnect_question, "unknown"))
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    leave();
                                }
                            })
                            .setNegativeButton(R.string.no, null)
                            .show();
                } else {
                    switchToScreen(R.id.screen_menu);
                }
                break;
            default:
                super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.online_picker, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out:
                if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                    mSignInClicked = false;
                    mAutoStartSignInFlow = false;
                    Games.signOut(mGoogleApiClient);
                    mGoogleApiClient.disconnect();
                    switchToScreen(R.id.screen_sign_in);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult requestCode: " + requestCode + ", resultCode: " + resultCode);
        switch (requestCode) {
            case RC_WAITING_ROOM:
                switch (resultCode) {
                    case RESULT_OK:
                        // TODO: be happy
                        break;
                    case GamesActivityResultCodes.RESULT_LEFT_ROOM:
                    case GamesActivityResultCodes.RESULT_INVALID_ROOM:
                    default:
                        leave();
                        switchToScreen(R.id.screen_wait);
                        break;
                }
                break;
            case RC_SELECT_PLAYERS:
                switch (resultCode) {
                    case RESULT_OK:
                        ArrayList<String> invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);
                        if (invitees.size() == 1) {
                            invitePlayer(invitees.get(0));
                            switchToScreen(R.id.screen_wait);
                        }
                        break;
                    default:
                        switchToScreen(R.id.screen_menu);
                }
                break;
            case RC_INVITATION_INBOX:
                // we got the result from the "select invitation" UI (invitation inbox). We're
                // ready to accept the selected invitation:
                handleInvitationInboxResult(resultCode, data);
                break;
            case REQUEST_RESOLVE_ERROR:
                mSignInClicked = false;
                mResolvingError = false;
                switchToScreen(R.id.screen_sign_in);

                switch (resultCode) {
                    case RESULT_OK:
                        if (!mGoogleApiClient.isConnecting() && !mGoogleApiClient.isConnected()) {
                            mGoogleApiClient.connect();
                        }
                        break;
                    case GamesActivityResultCodes.RESULT_APP_MISCONFIGURED:
                        new AlertDialog.Builder(this)
                                .setMessage("Result code sent back to the calling Activity when the game is not properly configured to access the Games service. Developers should check the logs for more details.")
                                .setPositiveButton(R.string.okay, null)
                                .show();
                        break;
                    case GamesActivityResultCodes.RESULT_SIGN_IN_FAILED:
                        new AlertDialog.Builder(this)
                                .setMessage(R.string.vpn_no_network)
                                .setPositiveButton(R.string.okay, null)
                                .show();
                        break;
                    case GamesActivityResultCodes.RESULT_LICENSE_FAILED:

                        break;
                    default:
                        final int errorCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
                        GoogleApiAvailability.getInstance().showErrorDialogFragment(this, errorCode, RC_DUMMY);

                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // Handle the result of the invitation inbox UI, where the player can pick an invitation
    // to accept. We react by accepting the selected invitation, if any.
    private void handleInvitationInboxResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            Log.w(TAG, "*** invitation inbox UI cancelled, " + response);
            switchToMainScreen();
            return;
        }

        Log.d(TAG, "Invitation inbox UI succeeded.");
        Invitation inv = data.getExtras().getParcelable(Multiplayer.EXTRA_INVITATION);

        // accept invitation
        if (inv != null) {
            acceptInviteToRoom(inv.getInvitationId());
        }
    }

    private void acceptInviteToRoom(String invId) {
        // TODO: uncomment
        //if (mService != null) {
        //    mService.acceptInvitation(mGoogleApiClient, invId);
        //
        //    switchToScreen(R.id.screen_wait);
        //    resetGameVars();
        //}
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!mGoogleApiClient.isConnected()) {
            if (mAutoStartSignInFlow) {
                mAutoStartSignInFlow = false;
                mGoogleApiClient.connect();
                switchToScreen(R.id.screen_wait);
            } else {
                switchToScreen(R.id.screen_sign_in);
            }
        } else {
            switchToScreen(R.id.screen_menu);
        }
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "onConnected: " + connectionHint);

        mAutoStartSignInFlow = true;

        Games.Invitations.registerInvitationListener(mGoogleApiClient, this);

        if (connectionHint != null) {
            Invitation invitation = connectionHint.getParcelable(Multiplayer.EXTRA_INVITATION);
            if (invitation != null && invitation.getInvitationId() != null) {
                acceptInviteToRoom(invitation.getInvitationId());
                return;
            }
        }
        switchToMainScreen();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended: " + i);
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.d(TAG, "onConnectionFailed() called, result: " + result);

        if (mResolvingError) {
            return;
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                mGoogleApiClient.connect();
            }
        } else {
            showErrorDialog(result.getErrorCode());
            mResolvingError = true;
        }

        switchToScreen(R.id.screen_sign_in);
    }

    private void showErrorDialog(int errorCode) {
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        Bundle args = new Bundle();
        args.putInt("error_code", errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), "error_dialog");
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        mResolvingError = false;
    }

    private void updateRoom(Room room) {

    }

    private void sendMessage(byte[] messageData) {
        Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, this, messageData, mRoomId, opponentParticipantId);
    }

    public void switchToScreen(int screenId) {
        currentScreen = screenId;
        findViewById(R.id.screen_menu).setVisibility(screenId == R.id.screen_menu ? View.VISIBLE : View.GONE);
        findViewById(R.id.screen_sign_in).setVisibility(screenId == R.id.screen_sign_in ? View.VISIBLE : View.GONE);
        findViewById(R.id.screen_wait).setVisibility(screenId == R.id.screen_wait ? View.VISIBLE : View.GONE);
        findViewById(R.id.screen_game_board).setVisibility(screenId == R.id.screen_game_board ? View.VISIBLE : View.INVISIBLE);
    }

    private void switchToMainScreen() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            if (mRoomId != null) {
                switchToScreen(R.id.screen_game_board);
            } else {
                switchToScreen(R.id.screen_menu);
            }
        } else {
            switchToScreen(R.id.screen_sign_in);
        }
    }

    public Intent getSelectOpponentsIntent() {
        return Games.RealTimeMultiplayer.getSelectOpponentsIntent(mGoogleApiClient, 1, 1, false);
    }

    public Intent getInvitationInboxIntent() {
        return Games.Invitations.getInvitationInboxIntent(mGoogleApiClient);
    }

    public Intent getWaitingRoomIntent(@NonNull Room room) {
        return Games.RealTimeMultiplayer.getWaitingRoomIntent(mGoogleApiClient, room, Integer.MAX_VALUE);
    }

    public void quickGame() {
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(1, 1, 0);
        RoomConfig roomConfig = RoomConfig.builder(this)
                .setMessageReceivedListener(this)
                .setRoomStatusUpdateListener(this)
                .setAutoMatchCriteria(autoMatchCriteria)
                .build();
        Games.RealTimeMultiplayer.create(mGoogleApiClient, roomConfig);
    }

    public void invitePlayer(@NonNull String player) {
        RoomConfig roomConfig = RoomConfig.builder(this)
                .addPlayersToInvite(player)
                .setMessageReceivedListener(this)
                .setRoomStatusUpdateListener(this)
                .build();
        Games.RealTimeMultiplayer.create(mGoogleApiClient, roomConfig);
    }

    public void acceptInvitation(@NonNull String invitationId) {
        RoomConfig roomConfig = RoomConfig.builder(this)
                .setInvitationIdToAccept(invitationId)
                .setMessageReceivedListener(this)
                .setRoomStatusUpdateListener(this)
                .build();
        Games.RealTimeMultiplayer.join(mGoogleApiClient, roomConfig);
    }

    public void leave() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            if (mRoomId != null) {
                Games.RealTimeMultiplayer.leave(mGoogleApiClient, this, mRoomId);
            } else {
                Log.e(TAG, "trying to leave but no room id");
            }
        } else {
            Log.e(TAG, "trying to leave while disconnected");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_sign_in:
                mSignInClicked = true;
                mGoogleApiClient.connect();
                switchToScreen(R.id.screen_wait);
                break;
            case R.id.button_quick_game:
                quickGame();
                switchToScreen(R.id.screen_wait);
                // issue: waiting room disappears on orientation change
                //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                //    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
                //} else {
                //int currentOrientation = getResources().getConfiguration().orientation;
                //switch (currentOrientation) {
                //    case Configuration.ORIENTATION_LANDSCAPE:
                //        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                //        break;
                //    case Configuration.ORIENTATION_PORTRAIT:
                //        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                //        break;
                //}
                //}
                break;
            case R.id.button_invite_players:
                startActivityForResult(getSelectOpponentsIntent(), RC_SELECT_PLAYERS);
                switchToScreen(R.id.screen_wait);
                break;
            case R.id.button_show_invitations:
                startActivityForResult(getInvitationInboxIntent(), RC_INVITATION_INBOX);
                switchToScreen(R.id.screen_wait);
                break;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //  room update listener
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onRoomCreated(int statusCode, Room room) {
        switch (statusCode) {
            case GamesStatusCodes.STATUS_OK:
                Log.d(TAG, "RoomUpdateListener.onRoomCreated: " + statusCode + " - " + room);
                break;
            case GamesStatusCodes.STATUS_CLIENT_RECONNECT_REQUIRED:
                Log.e(TAG, "The GoogleApiClient is in an inconsistent state and must reconnect to the service to resolve the issue. Further calls to the service using the current connection are unlikely to succeed.");
                break;
            case GamesStatusCodes.STATUS_REAL_TIME_CONNECTION_FAILED:
                Log.e(TAG, "Failed to initialize the network connection for a real-time room.");
                break;
            case GamesStatusCodes.STATUS_MULTIPLAYER_DISABLED:
                Log.e(TAG, "This game does not support multiplayer. This could occur if the linked app is not configured appropriately in the developer console.");
                break;
            case GamesStatusCodes.STATUS_INTERNAL_ERROR:
                Log.e(TAG, "An unspecified error occurred; no more specific information is available. The device logs may provide additional data.");
                break;

        }
        if (statusCode == GamesStatusCodes.STATUS_OK) {
            mRoomId = room.getRoomId();
            startActivityForResult(getWaitingRoomIntent(room), RC_WAITING_ROOM);
        } else {
            switchToScreen(R.id.screen_menu);
            //TODO: show error dialog and send data to analyze
        }
    }

    @Override
    public void onJoinedRoom(int statusCode, Room room) {
        Log.d(TAG, "RoomUpdateListener.onJoinedRoom: " + statusCode + " - " + room);
        if (statusCode == GamesStatusCodes.STATUS_OK) {
            startActivityForResult(getWaitingRoomIntent(room), RC_WAITING_ROOM);
        } else {
            switchToScreen(R.id.screen_menu);
            //TODO: show error dialog and send data to analyze
        }

    }

    @Override
    public void onLeftRoom(int statusCode, String roomId) {
        Log.d(TAG, "RoomUpdateListener.onLeftRoom: " + statusCode + " - " + roomId);
        switchToScreen(R.id.screen_menu);
    }

    @Override
    public void onRoomConnected(int statusCode, Room room) {
        Log.d(TAG, "RoomUpdateListener.onRoomConnected: " + statusCode + " - " + room);
        if (statusCode == GamesStatusCodes.STATUS_OK) {
            switchToScreen(R.id.screen_game_board);
            setTitle(R.string.bt_message_your_turn);
            // set participants ids
            String currentPlayerId = Games.Players.getCurrentPlayerId(mGoogleApiClient);
            currentParticipantId = room.getParticipantId(currentPlayerId);
            ArrayList<String> participantIds = room.getParticipantIds();
            participantIds.remove(currentParticipantId);
            opponentParticipantId = participantIds.get(0);
            // send player name
            sendMessage(Protocol.createInitMessage(SettingsUtils.getUserName(this, getString(R.string.device_info_default))));
        } else {
            switchToScreen(R.id.screen_menu);
            //TODO: show error dialog and send data to analyze
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //  room status update listener
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onConnectedToRoom(Room room) {
        Log.d(TAG, "onConnectedToRoom: " + room);
    }

    @Override
    public void onDisconnectedFromRoom(Room room) {
        Log.d(TAG, "RoomStatusUpdateListener.onDisconnectedFromRoom: " + room);
        mRoomId = null;
        setTitle(R.string.bluetooth_disconnected);
    }

    @Override
    public void onPeerDeclined(Room room, List<String> list) {
        Log.d(TAG, "onPeerDeclined: " + room + ", " + list);
        updateRoom(room);
    }

    @Override
    public void onPeerInvitedToRoom(Room room, List<String> list) {
        Log.d(TAG, "onPeerInvitedToRoom: " + room + ", " + list);
        updateRoom(room);
    }

    @Override
    public void onP2PDisconnected(String participant) {
        Log.d(TAG, "onP2PDisconnected: " + participant);
    }

    @Override
    public void onP2PConnected(String participant) {
        Log.d(TAG, "onP2PConnected: " + participant);
    }

    @Override
    public void onPeerJoined(Room room, List<String> list) {
        Log.d(TAG, "onPeerJoined: " + room + ", " + list);
        updateRoom(room);
    }

    @Override
    public void onPeerLeft(Room room, List<String> list) {
        Log.d(TAG, "onPeerLeft: " + room + ", " + list);
        updateRoom(room);
    }

    @Override
    public void onRoomAutoMatching(Room room) {
        Log.d(TAG, "onRoomAutoMatching: " + room);
        updateRoom(room);
    }

    @Override
    public void onRoomConnecting(Room room) {
        Log.d(TAG, "onRoomConnecting: " + room);
        updateRoom(room);
    }

    @Override
    public void onPeersConnected(Room room, List<String> list) {
        Log.d(TAG, "onPeersConnected: " + room + ", " + list);
        updateRoom(room);
    }

    @Override
    public void onPeersDisconnected(Room room, List<String> list) {
        Log.d(TAG, "onPeersDisconnected: " + room + ", " + list);
        updateRoom(room);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //  real time message received listener
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onRealTimeMessageReceived(RealTimeMessage message) {
        Log.d(TAG, "onRealTimeMessageReceived: " + message);
        byte[] messageData = message.getMessageData();
        Protocol.parseMessage(messageData, new Protocol.OnParsedListener() {
            @Override
            public void onInitMessage(@NonNull String name) {
                TextView tvOpponentName = (TextView) findViewById(R.id.opponent_name);
                assert tvOpponentName != null;
                tvOpponentName.setText(name);
                view.resetGame();
            }

            @Override
            public void onDotMessage(@NonNull Dot dot) {
                dot.setType(Dot.OPPONENT);
                view.setDot(dot);
            }
        });

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //  on invitation received listener
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onInvitationReceived(Invitation invitation) {
        Log.d(TAG, "onInvitationReceived: " + invitation);
    }

    @Override
    public void onInvitationRemoved(String s) {
        Log.d(TAG, "onInvitationRemoved: " + s);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //  on sent callback listener
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onRealTimeMessageSent(int statusCode, int tokenId, String recipientParticipantId) {
        Log.d(TAG, "onRealTimeMessageSent: " + statusCode + ", " + tokenId + ", " + recipientParticipantId);
        switch (statusCode) {
            case GamesStatusCodes.STATUS_OK:
                break;
            case GamesStatusCodes.STATUS_REAL_TIME_MESSAGE_SEND_FAILED:
                break;
            case GamesStatusCodes.STATUS_REAL_TIME_ROOM_NOT_JOINED:
                break;
        }
    }

    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() {
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int errorCode = this.getArguments().getInt("error_code");
            return GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), errorCode,
                    REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((OnlineGameActivity) getActivity()).onDialogDismissed();
        }
    }
}
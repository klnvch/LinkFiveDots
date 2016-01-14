package by.klnvch.link5dots.online;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
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
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;
import com.google.android.gms.plus.Plus;

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
        View.OnClickListener {

    private static final String TAG = "OnlineGame";

    private static final String STATE_RESOLVING_ERROR = "resolving_error";

    private static final int REQUEST_RESOLVE_ERROR = 1001;
    private static final int RC_DUMMY = 1002;
    private final static int RC_SELECT_PLAYERS = 10000;
    private final static int RC_INVITATION_INBOX = 10001;
    private final static int RC_WAITING_ROOM = 10002;

    private GameView view;
    private int currentScreen = R.id.screen_wait;

    private boolean isLeaving = false;

    private GoogleApiClient mGoogleApiClient;
    private boolean mResolvingError = false;
    // Has the user clicked the sign-in button?
    private boolean mSignInClicked = false;
    private boolean mAutoStartSignInFlow = true;

    private RetainedFragment mFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online);

        view = (GameView)findViewById(R.id.game_view);

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

        findViewById(R.id.button_invite_players).setOnClickListener(this);
        findViewById(R.id.button_quick_game).setOnClickListener(this);
        findViewById(R.id.button_show_invitations).setOnClickListener(this);
        findViewById(R.id.button_sign_in).setOnClickListener(this);

        mFragment = (RetainedFragment) getSupportFragmentManager().findFragmentByTag("fragment");
        if (mFragment == null) {
            mFragment = new RetainedFragment();
            getSupportFragmentManager().beginTransaction().add(mFragment, "fragment").commit();
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();

        mResolvingError = savedInstanceState != null
                && savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);

        String username = SettingsUtils.getUserName(this, getString(R.string.device_info_default));
        TextView tvUsername = (TextView)findViewById(R.id.user_name);
        tvUsername.setText(username);

        view.restore(savedInstanceState);
        view.invalidate();
        view.isOver();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        view.save(outState);
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
    }

    @Override
    public void onBackPressed() {
        switch (currentScreen) {
            case R.id.screen_game_board:
                new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.bluetooth_is_disconnect_question, "unknown"))
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int which) {
                                leave();
                            }
                        })
                        .setNegativeButton(R.string.no, null)
                        .show();
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
        switch (requestCode) {
            case RC_WAITING_ROOM:
                switch (resultCode) {
                    case RESULT_OK:
                        // TODO: be happy
                        break;
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

                        break;
                    case GamesActivityResultCodes.RESULT_SIGN_IN_FAILED:
                        new AlertDialog.Builder(this)
                                .setMessage(R.string.sign_in_failed)
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
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
            switchToScreen(R.id.screen_wait);
        } else {
            switchToScreen(R.id.screen_menu);
        }
        super.onStart();
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

        Games.Invitations.registerInvitationListener(mGoogleApiClient, mFragment);

        if (connectionHint != null) {
            Invitation invitation = connectionHint.getParcelable(Multiplayer.EXTRA_INVITATION);
            if (invitation != null && invitation.getInvitationId() != null) {
                acceptInviteToRoom(invitation.getInvitationId());
                return;
            }
        }
        switchToMainScreen();

        if (isLeaving) {
            leave();
            isLeaving = false;
        }
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
        Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, mFragment, messageData, mFragment.getRoomId(), mFragment.getParticipantId2());
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
            if (mFragment.getRoomId() != null) {
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
        RoomConfig roomConfig = RoomConfig.builder(mFragment)
                .setMessageReceivedListener(mFragment)
                .setRoomStatusUpdateListener(mFragment)
                .setAutoMatchCriteria(autoMatchCriteria)
                .build();
        Games.RealTimeMultiplayer.create(mGoogleApiClient, roomConfig);
    }

    public void invitePlayer(@NonNull String player) {
        RoomConfig roomConfig = RoomConfig.builder(mFragment)
                .addPlayersToInvite(player)
                .setMessageReceivedListener(mFragment)
                .setRoomStatusUpdateListener(mFragment)
                .build();
        Games.RealTimeMultiplayer.create(mGoogleApiClient, roomConfig);
    }

    public void acceptInvitation(@NonNull String invitationId) {
        RoomConfig roomConfig = RoomConfig.builder(mFragment)
                .setInvitationIdToAccept(invitationId)
                .setMessageReceivedListener(mFragment)
                .setRoomStatusUpdateListener(mFragment)
                .build();
        Games.RealTimeMultiplayer.join(mGoogleApiClient, roomConfig);
    }

    public void leave() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            if (mFragment.getRoomId() != null) {
                Games.RealTimeMultiplayer.leave(mGoogleApiClient, mFragment, mFragment.getRoomId());
            }
        } else {
            isLeaving = true;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_sign_in:
                mSignInClicked = true;
                mGoogleApiClient.connect();
                break;
            case R.id.button_quick_game:
                quickGame();
                switchToScreen(R.id.screen_wait);
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
    public void onRoomCreated(int i, Room room) {
        startActivityForResult(getWaitingRoomIntent(room), RC_WAITING_ROOM);
    }

    @Override
    public void onJoinedRoom(int i, Room room) {
        startActivityForResult(getWaitingRoomIntent(room), RC_WAITING_ROOM);
    }

    @Override
    public void onLeftRoom(int i, String s) {
        switchToScreen(R.id.screen_menu);
    }

    @Override
    public void onRoomConnected(int i, Room room) {
        switchToScreen(R.id.screen_game_board);
        // set participants ids
        String currentPlayerId = Games.Players.getCurrentPlayerId(mGoogleApiClient);
        String currentParticipantId = room.getParticipantId(currentPlayerId);
        ArrayList<String> participantIds = room.getParticipantIds();
        participantIds.remove(currentParticipantId);
        String opponentParticipantId = participantIds.get(0);
        mFragment.setParticipantId1(currentParticipantId);
        mFragment.setParticipantId2(opponentParticipantId);
        // send player name
        sendMessage(Protocol.createInitMessage(SettingsUtils.getUserName(this, getString(R.string.device_info_default))));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //  room status update listener
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onConnectedToRoom(Room room) {
    }

    @Override
    public void onDisconnectedFromRoom(Room room) {
    }

    @Override
    public void onPeerDeclined(Room room, List<String> arg1) {
        updateRoom(room);
    }

    @Override
    public void onPeerInvitedToRoom(Room room, List<String> arg1) {
        updateRoom(room);
    }

    @Override
    public void onP2PDisconnected(String participant) {
    }

    @Override
    public void onP2PConnected(String participant) {
    }

    @Override
    public void onPeerJoined(Room room, List<String> arg1) {
        updateRoom(room);
    }

    @Override
    public void onPeerLeft(Room room, List<String> peersWhoLeft) {
        updateRoom(room);
    }

    @Override
    public void onRoomAutoMatching(Room room) {
        updateRoom(room);
    }

    @Override
    public void onRoomConnecting(Room room) {
        updateRoom(room);
    }

    @Override
    public void onPeersConnected(Room room, List<String> peers) {
        updateRoom(room);
    }

    @Override
    public void onPeersDisconnected(Room room, List<String> peers) {
        updateRoom(room);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //  real time message received listener
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onRealTimeMessageReceived(RealTimeMessage message) {
        byte[] messageData = message.getMessageData();
        Protocol.parseMessage(messageData, new Protocol.OnParsedListener() {
            @Override
            public void onInitMessage(@NonNull String name) {
                TextView tvOpponentName = (TextView) findViewById(R.id.opponent_name);
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

    }

    @Override
    public void onInvitationRemoved(String s) {

    }

    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() { }

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
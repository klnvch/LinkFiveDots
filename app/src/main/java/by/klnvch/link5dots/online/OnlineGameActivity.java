package by.klnvch.link5dots.online;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;
import com.google.android.gms.plus.Plus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import by.klnvch.link5dots.R;

public class OnlineGameActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        RealTimeMessageReceivedListener, RoomStatusUpdateListener, RoomUpdateListener,
        OnInvitationReceivedListener,
        View.OnClickListener {

    private static final String TAG = "OnlineGame";

    private static final int RC_SIGN_IN = 9001;
    private final static int RC_SELECT_PLAYERS = 10000;
    private final static int RC_INVITATION_INBOX = 10001;
    private final static int RC_WAITING_ROOM = 10002;

    private boolean isLeaving = false;


    private final Map<String, Integer> mParticipantScore = new HashMap<>();
    // Client used to interact with Google APIs.
    private GoogleApiClient mGoogleApiClient;
    // Are we currently resolving a connection failure?
    private boolean mResolvingConnectionFailure = false;
    // Has the user clicked the sign-in button?
    private boolean mSignInClicked = false;
    // Set to true to automatically start the sign in flow when the Activity starts.
    // Set to false to require the user to click the button in order to sign in.
    private boolean mAutoStartSignInFlow = true;
    // The participants in the currently active game
    private ArrayList<Participant> mParticipants = null;
    // My participant ID in the currently active game
    private String mMyId = null;

    private RetainedFragment mFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online);

        findViewById(R.id.button_invite_players).setOnClickListener(this);
        findViewById(R.id.button_quick_game).setOnClickListener(this);
        findViewById(R.id.button_show_invitations).setOnClickListener(this);
        findViewById(R.id.button_sign_in).setOnClickListener(this);

        mFragment = (RetainedFragment) getSupportFragmentManager().findFragmentByTag("fragment");
        if (mFragment == null) {
            mFragment = new RetainedFragment();
            getSupportFragmentManager().beginTransaction().add(mFragment, "fragment").commit();
        }

        // Create the Google Api Client with access to Plus and Games
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();
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
                mSignInClicked = false;
                Games.signOut(mGoogleApiClient);
                mGoogleApiClient.disconnect();
                switchToScreen(R.id.button_sign_in);
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
                        leaveRoom();
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
                        switchToScreen(R.id.screen_main);
                }
                break;
            case RC_INVITATION_INBOX:
                // we got the result from the "select invitation" UI (invitation inbox). We're
                // ready to accept the selected invitation:
                handleInvitationInboxResult(resultCode, data);
                break;
            case RC_SIGN_IN:
                Log.d(TAG, "onActivityResult with requestCode == RC_SIGN_IN, responseCode="
                        + resultCode + ", intent=" + data);
                mSignInClicked = false;
                mResolvingConnectionFailure = false;
                if (resultCode == RESULT_OK) {
                    mGoogleApiClient.connect();
                } else {
                    BaseGameUtils.showActivityResultError(this, requestCode, resultCode, R.string.signin_other_error);
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
            switchToScreen(R.id.screen_main);
        }
        super.onStart();
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
            leaveRoom();
            isLeaving = false;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended: " + i);
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed() called, result: " + connectionResult);

        if (mResolvingConnectionFailure) {
            Log.d(TAG, "onConnectionFailed() ignoring connection failure; already resolving.");
            return;
        }

        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = BaseGameUtils.resolveConnectionFailure(this, mGoogleApiClient,
                    connectionResult, RC_SIGN_IN, getString(R.string.signin_other_error));
        }

        switchToScreen(R.id.button_sign_in);
    }

    /*
     * COMMUNICATIONS SECTION. Methods that implement the game's network
     * protocol.
     */

    private void updateRoom(Room room) {
        if (room != null) {
            mParticipants = room.getParticipants();
        }
    }

    // Broadcast my score to everybody else.
    private void broadcastScore(boolean finalScore) {

        // Send to every other participant.
        for (Participant p : mParticipants) {
            if (p.getParticipantId().equals(mMyId))
                continue;
            if (p.getStatus() != Participant.STATUS_JOINED)
                continue;
            if (finalScore) {
                //Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, mMsgBuf, mRoomId, p.getParticipantId());
            } else {
                //Games.RealTimeMultiplayer.sendUnreliableMessage(mGoogleApiClient, mMsgBuf, mRoomId, p.getParticipantId());
            }
        }
    }

    public void switchToScreen(int screenId) {
        findViewById(R.id.screen_main).setVisibility(screenId == R.id.screen_main ? View.VISIBLE : View.GONE);
        findViewById(R.id.button_sign_in).setVisibility(screenId == R.id.button_sign_in ? View.VISIBLE : View.GONE);
        findViewById(R.id.screen_wait).setVisibility(screenId == R.id.screen_wait ? View.VISIBLE : View.GONE);
    }

    private void switchToMainScreen() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            switchToScreen(R.id.screen_main);
        } else {
            switchToScreen(R.id.button_sign_in);
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

    public void leaveRoom() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Games.RealTimeMultiplayer.leave(mGoogleApiClient, mFragment, mFragment.getRoomId());
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
        switchToScreen(R.id.screen_main);
    }

    @Override
    public void onRoomConnected(int i, Room room) {
        // TODO: be happy
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //  room status update listener
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onConnectedToRoom(Room room) {
        mParticipants = room.getParticipants();
        mMyId = room.getParticipantId(Games.Players.getCurrentPlayerId(mGoogleApiClient));
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
    public void onRealTimeMessageReceived(RealTimeMessage rtm) {
        byte[] buf = rtm.getMessageData();
        String sender = rtm.getSenderParticipantId();
        Log.d(TAG, "Message received: " + (char) buf[0] + "/" + (int) buf[1]);

        if (buf[0] == 'F' || buf[0] == 'U') {
            // score update.
            int existingScore = mParticipantScore.containsKey(sender) ?
                    mParticipantScore.get(sender) : 0;
            int thisScore = (int) buf[1];
            if (thisScore > existingScore) {
                // this check is necessary because packets may arrive out of
                // order, so we
                // should only ever consider the highest score we received, as
                // we know in our
                // game there is no way to lose points. If there was a way to
                // lose points,
                // we'd have to add a "serial number" to the packet.
                mParticipantScore.put(sender, thisScore);
            }
        }
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
}
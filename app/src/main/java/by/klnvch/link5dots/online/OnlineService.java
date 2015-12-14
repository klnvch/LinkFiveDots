package by.klnvch.link5dots.online;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;
import com.google.android.gms.plus.Plus;

import java.util.List;

public class OnlineService extends Service implements ActivityToServiceListener,
        RoomUpdateListener, RealTimeMessageReceivedListener, RoomStatusUpdateListener,
        OnInvitationReceivedListener {

    private static final String TAG = "OnlineService";

    private final IBinder mBinder = new LocalBinder();


    private ServiceToActivityListener mListener = null;
    private Room mRoom = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void setListener(ServiceToActivityListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public void quickGame(@NonNull GoogleApiClient googleApiClient) {
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(1, 1, 0);
        RoomConfig roomConfig = RoomConfig.builder(this)
                .setMessageReceivedListener(this)
                .setRoomStatusUpdateListener(this)
                .setAutoMatchCriteria(autoMatchCriteria)
                .build();
        Games.RealTimeMultiplayer.create(googleApiClient, roomConfig);
    }

    @Override
    public void invitePlayer(@NonNull GoogleApiClient googleApiClient, String player) {
        RoomConfig roomConfig = RoomConfig.builder(this)
                .addPlayersToInvite(player)
                .setMessageReceivedListener(this)
                .setRoomStatusUpdateListener(this)
                .build();
        Games.RealTimeMultiplayer.create(googleApiClient, roomConfig);
    }

    @Override
    public void acceptInvitation(@NonNull GoogleApiClient googleApiClient, String invitationId) {
        RoomConfig roomConfig = RoomConfig.builder(this)
                .setInvitationIdToAccept(invitationId)
                .setMessageReceivedListener(this)
                .setRoomStatusUpdateListener(this)
                .build();
        Games.RealTimeMultiplayer.join(googleApiClient, roomConfig);
    }

    @Override
    public void leaveRoom(@NonNull GoogleApiClient googleApiClient) {
        if (mRoom != null) {
            Games.RealTimeMultiplayer.leave(googleApiClient, this, mRoom.getRoomId());
            mRoom = null;
        }
    }

    // from room update listener

    @Override
    public void onRoomCreated(int i, Room room) {
        /*
        Log.d(TAG, "onRoomCreated(" + statusCode + ", " + room + ")");
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            Log.e(TAG, "*** Error: onRoomCreated, status " + statusCode);
            showGameError();
            return;
        }

        // save room ID so we can leave cleanly before the game starts.
        mRoomId = room.getRoomId();

        // show the waiting room UI

        startActivityForResult(intent, RC_WAITING_ROOM);
         */
    }

    @Override
    public void onJoinedRoom(int i, Room room) {
        /*
        Log.d(TAG, "onJoinedRoom(" + statusCode + ", " + room + ")");
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
            showGameError();
            return;
        }

        showWaitingRoom(room);
         */
    }

    @Override
    public void onLeftRoom(int i, String s) {
        /*
        switchToMainScreen();
         */
    }

    @Override
    public void onRoomConnected(int i, Room room) {
        /*
        mParticipants = room.getParticipants();
        mMyId = room.getParticipantId(Games.Players.getCurrentPlayerId(mGoogleApiClient));

        or

        Log.d(TAG, "onRoomConnected(" + statusCode + ", " + room + ")");
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
            showGameError();
            return;
        }
        updateRoom(room);
         */
    }

    // from room real time receive message listener

    @Override
    public void onRealTimeMessageReceived(RealTimeMessage realTimeMessage) {

    }

    // from room status update listener

    @Override
    public void onRoomConnecting(Room room) {

    }

    @Override
    public void onRoomAutoMatching(Room room) {

    }

    @Override
    public void onPeerInvitedToRoom(Room room, List<String> list) {

    }

    @Override
    public void onPeerDeclined(Room room, List<String> list) {

    }

    @Override
    public void onPeerJoined(Room room, List<String> list) {

    }

    @Override
    public void onPeerLeft(Room room, List<String> list) {

    }

    @Override
    public void onConnectedToRoom(Room room) {

    }

    @Override
    public void onDisconnectedFromRoom(Room room) {
        /*
        mRoomId = null;
        showGameError();
         */
    }

    @Override
    public void onPeersConnected(Room room, List<String> list) {

    }

    @Override
    public void onPeersDisconnected(Room room, List<String> list) {

    }

    @Override
    public void onP2PConnected(String s) {

    }

    @Override
    public void onP2PDisconnected(String s) {

    }

    // for invitation received listener

    @Override
    public void onInvitationReceived(Invitation invitation) {
        /*
        mIncomingInvitationId = invitation.getInvitationId();
        ((TextView) findViewById(R.id.incoming_invitation_text)).setText(invitation.getInviter().getDisplayName() + " " + getString(R.string.is_inviting_you));
         */
    }

    @Override
    public void onInvitationRemoved(String s) {
        /*
        if (mIncomingInvitationId.equals(invitationId)) {
            mIncomingInvitationId = null;
            switchToScreen(mCurScreen);
        }
         */
    }

    ///

    public class LocalBinder extends Binder {
        OnlineService getService() {
            return OnlineService.this;
        }
    }
}
package by.klnvch.link5dots.online;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMultiplayer;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;

import java.util.List;

public class RetainedFragment extends Fragment implements
        RoomUpdateListener,
        OnInvitationReceivedListener,
        RoomStatusUpdateListener,
        RealTimeMessageReceivedListener,
        RealTimeMultiplayer.ReliableMessageSentCallback {

    private static final String TAG = "OnlineGame";

    private OnInvitationReceivedListener mOnInvitationReceivedListener = null;
    private RoomUpdateListener mRoomUpdateListener = null;
    private RoomStatusUpdateListener mRoomStatusUpdateListener = null;
    private RealTimeMessageReceivedListener mRealTimeMessageReceivedListener = null;

    private String mRoomId;

    private String mParticipantId1; // current player
    private String mParticipantId2; // opponent player

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Context context) {
        Log.d(TAG, "onAttach");
        super.onAttach(context);
        mOnInvitationReceivedListener = (OnInvitationReceivedListener) context;
        mRoomUpdateListener = (RoomUpdateListener) context;
        mRoomStatusUpdateListener = (RoomStatusUpdateListener) context;
        mRealTimeMessageReceivedListener = (RealTimeMessageReceivedListener) context;
    }

    @Override
    public void onDetach() {
        mOnInvitationReceivedListener = null;
        mRoomUpdateListener = null;
        mRoomStatusUpdateListener = null;
        mRealTimeMessageReceivedListener = null;
        super.onDetach();
    }

    public String getRoomId() {
        return mRoomId;
    }

    public String getParticipantId2() {
        return mParticipantId2;
    }

    public void setParticipantId2(String participantId2) {
        this.mParticipantId2 = participantId2;
    }

    public String getParticipantId1() {
        return mParticipantId1;
    }

    public void setParticipantId1(String participantId1) {
        this.mParticipantId1 = participantId1;
    }

    @Override
    public void onRoomCreated(int statusCode, Room room) {
        Log.d(TAG, "onRoomCreated: " + statusCode + " - " + room);
        switch (statusCode) {
            case GamesStatusCodes.STATUS_OK:
                mRoomId = room.getRoomId();
                if (mRoomUpdateListener != null) {
                    mRoomUpdateListener.onRoomCreated(statusCode, room);
                }
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
    }

    @Override
    public void onJoinedRoom(int statusCode, Room room) {
        Log.d(TAG, "onJoinedRoom: " + statusCode + " - " + room);
        if (statusCode == GamesStatusCodes.STATUS_OK) {
            if (mRoomUpdateListener != null) {
                mRoomUpdateListener.onJoinedRoom(statusCode, room);
            }
        }
    }

    @Override
    public void onLeftRoom(int statusCode, String roomId) {
        Log.d(TAG, "onLeftRoom: " + statusCode + " - " + roomId);
        if (statusCode == GamesStatusCodes.STATUS_OK) {
            mRoomId = null;
            if (mRoomUpdateListener != null) {
                mRoomUpdateListener.onLeftRoom(statusCode, roomId);
            }
        }
    }

    @Override
    public void onRoomConnected(int statusCode, Room room) {
        Log.d(TAG, "onRoomConnected: " + statusCode + " - " + room);
        if (statusCode == GamesStatusCodes.STATUS_OK) {
            if (mRoomUpdateListener != null) {
                mRoomUpdateListener.onRoomConnected(statusCode, room);
            }
        }
    }

    @Override
    public void onInvitationReceived(Invitation invitation) {
        Log.d(TAG, "onInvitationReceived: " + invitation);
        if (mOnInvitationReceivedListener != null) {
            mOnInvitationReceivedListener.onInvitationReceived(invitation);
        }
    }

    @Override
    public void onInvitationRemoved(String s) {
        Log.d(TAG, "onInvitationRemoved: " + s);
        if (mOnInvitationReceivedListener != null) {
            mOnInvitationReceivedListener.onInvitationRemoved(s);
        }
    }

    @Override
    public void onRoomConnecting(Room room) {
        Log.d(TAG, "onRoomConnecting: " + room);
        if (mRoomStatusUpdateListener != null) {
            mRoomStatusUpdateListener.onRoomConnecting(room);
        }
    }

    @Override
    public void onRoomAutoMatching(Room room) {
        Log.d(TAG, "onRoomAutoMatching: " + room);
        if (mRoomStatusUpdateListener != null) {
            mRoomStatusUpdateListener.onRoomAutoMatching(room);
        }
    }

    @Override
    public void onPeerInvitedToRoom(Room room, List<String> list) {
        Log.d(TAG, "onPeerInvitedToRoom: " + room + ", " + list);
        if (mRoomStatusUpdateListener != null) {
            mRoomStatusUpdateListener.onPeerInvitedToRoom(room, list);
        }
    }

    @Override
    public void onPeerDeclined(Room room, List<String> list) {
        Log.d(TAG, "onPeerDeclined: " + room + ", " + list);
        if (mRoomStatusUpdateListener != null) {
            mRoomStatusUpdateListener.onPeerDeclined(room, list);
        }
    }

    @Override
    public void onPeerJoined(Room room, List<String> list) {
        Log.d(TAG, "onPeerJoined: " + room + ", " + list);
        if (mRoomStatusUpdateListener != null) {
            mRoomStatusUpdateListener.onPeerJoined(room, list);
        }
    }

    @Override
    public void onPeerLeft(Room room, List<String> list) {
        Log.d(TAG, "onPeerLeft: " + room + ", " + list);
        if (mRoomStatusUpdateListener != null) {
            mRoomStatusUpdateListener.onPeerLeft(room, list);
        }
    }

    @Override
    public void onConnectedToRoom(Room room) {
        Log.d(TAG, "onConnectedToRoom: " + room);
        if (mRoomStatusUpdateListener != null) {
            mRoomStatusUpdateListener.onConnectedToRoom(room);
        }
    }

    @Override
    public void onDisconnectedFromRoom(Room room) {
        Log.d(TAG, "onDisconnectedFromRoom: " + room);
        mRoomId = null;
        mParticipantId1 = null;
        mParticipantId2 = null;
        if (mRoomStatusUpdateListener != null) {
            mRoomStatusUpdateListener.onDisconnectedFromRoom(room);
        }
    }

    @Override
    public void onPeersConnected(Room room, List<String> list) {
        Log.d(TAG, "onPeersConnected: " + room + ", " + list);
        if (mRoomStatusUpdateListener != null) {
            mRoomStatusUpdateListener.onPeersConnected(room, list);
        }
    }

    @Override
    public void onPeersDisconnected(Room room, List<String> list) {
        Log.d(TAG, "onPeersDisconnected: " + room + ", " + list);
        if (mRoomStatusUpdateListener != null) {
            mRoomStatusUpdateListener.onPeersDisconnected(room, list);
        }
    }

    @Override
    public void onP2PConnected(String s) {
        Log.d(TAG, "onP2PConnected: " + s);
        if (mRoomStatusUpdateListener != null) {
            mRoomStatusUpdateListener.onP2PConnected(s);
        }
    }

    @Override
    public void onP2PDisconnected(String s) {
        Log.d(TAG, "onP2PDisconnected: " + s);
        if (mRoomStatusUpdateListener != null) {
            mRoomStatusUpdateListener.onP2PDisconnected(s);
        }
    }

    @Override
    public void onRealTimeMessageReceived(RealTimeMessage message) {
        Log.d(TAG, "onRealTimeMessageReceived: " + message);
        if (mRealTimeMessageReceivedListener != null) {
            mRealTimeMessageReceivedListener.onRealTimeMessageReceived(message);
        }
    }

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
}

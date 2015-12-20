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
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;

import java.util.List;

public class RetainedFragment extends Fragment implements
        RoomUpdateListener,
        OnInvitationReceivedListener,
        RoomStatusUpdateListener,
        RealTimeMessageReceivedListener {

    private static final String TAG = "OnlineGame";

    private OnInvitationReceivedListener mOnInvitationReceivedListener = null;
    private RoomUpdateListener mRoomUpdateListener = null;
    private RoomStatusUpdateListener mRoomStatusUpdateListener = null;
    private RealTimeMessageReceivedListener mRealTimeMessageReceivedListener = null;

    private String mRoomId;

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

    @Override
    public void onRoomCreated(int statusCode, Room room) {
        Log.d(TAG, "onRoomCreated: " + statusCode + " - " + room);
        if (statusCode == GamesStatusCodes.STATUS_OK) {
            mRoomId = room.getRoomId();
            if (mRoomUpdateListener != null) {
                mRoomUpdateListener.onRoomCreated(statusCode, room);
            }
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
    public void onRealTimeMessageReceived(RealTimeMessage realTimeMessage) {
        Log.d(TAG, "onRealTimeMessageReceived: " + realTimeMessage);
        if (mRealTimeMessageReceivedListener != null) {
            mRealTimeMessageReceivedListener.onRealTimeMessageReceived(realTimeMessage);
        }
    }
}

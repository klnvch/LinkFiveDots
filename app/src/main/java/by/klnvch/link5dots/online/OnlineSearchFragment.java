package by.klnvch.link5dots.online;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.internal.GamesClientImpl;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchConfig;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;

import java.util.ArrayList;

import by.klnvch.link5dots.Game;
import by.klnvch.link5dots.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class OnlineSearchFragment extends Fragment {

    public static final String TAG = "OnlineSearchFragment";

    final static int RC_SELECT_PLAYERS = 10000;

    private OnFragmentInteractionListener mListener;

    private final TurnBasedMatchConfig autoMatchingConfig = TurnBasedMatchConfig
            .builder()
            .setAutoMatchCriteria(RoomConfig.createAutoMatchCriteria(1, 1, 0))
            .build();

    private final ResultCallback<TurnBasedMultiplayer.InitiateMatchResult> createAutoMatchCallback =
            new ResultCallback<TurnBasedMultiplayer.InitiateMatchResult>() {
                @Override
                public void onResult(TurnBasedMultiplayer.InitiateMatchResult result) {
                    TurnBasedMatch match = result.getMatch();

                    if (OnlineUtils.checkStatusCode(getContext(), result.getStatus().getStatusCode())) {
                        if (match.getData() != null) {
                            Intent intent = new Intent(getContext(), OnlineActivity.class);
                            intent.putExtra(Multiplayer.EXTRA_TURN_BASED_MATCH, match);
                            startActivity(intent);
                        } else {
                            Game mGame = new Game("host", "guest");
                            GoogleApiClient mGoogleApiClient = mListener.getGoogleApiClient();

                            Games.TurnBasedMultiplayer
                                    .takeTurn(mGoogleApiClient, match.getMatchId(), mGame.toByteArray(), null)
                                    .setResultCallback(firstTurnCallBack);

                        }
                    }
                }
            };

    private final ResultCallback<TurnBasedMultiplayer.InitiateMatchResult> createOpponentMatchCallback =
            new ResultCallback<TurnBasedMultiplayer.InitiateMatchResult>() {
                @Override
                public void onResult(TurnBasedMultiplayer.InitiateMatchResult result) {
                    TurnBasedMatch match = result.getMatch();

                    if (OnlineUtils.checkStatusCode(getContext(), result.getStatus().getStatusCode())) {
                        if (match.getData() != null) {
                            Intent intent = new Intent(getContext(), OnlineActivity.class);
                            intent.putExtra(Multiplayer.EXTRA_TURN_BASED_MATCH, match);
                            startActivity(intent);
                        } else {
                            Game mGame = new Game("host", "guest");
                            GoogleApiClient mGoogleApiClient = mListener.getGoogleApiClient();

                            String playerId = Games.Players.getCurrentPlayerId(mGoogleApiClient);
                            String myParticipantId = match.getParticipantId(playerId);

                            Games.TurnBasedMultiplayer
                                    .takeTurn(mGoogleApiClient, match.getMatchId(), mGame.toByteArray(), null)
                                    .setResultCallback(firstTurnCallBack);
                        }
                    }
                }
            };

    private final ResultCallback<TurnBasedMultiplayer.UpdateMatchResult> firstTurnCallBack =
            new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
        @Override
        public void onResult(TurnBasedMultiplayer.UpdateMatchResult result) {
            if (OnlineUtils.checkStatusCode(getContext(), result.getStatus().getStatusCode())) {
                OnlineUtils.checkTurnStatus(getContext(), result.getMatch().getStatus(), result.getMatch().getTurnStatus());
            }
        }
    };

    public OnlineSearchFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_online_search, container, false);

        view.findViewById(R.id.quickMatchButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final GoogleApiClient mGoogleApiClient = mListener.getGoogleApiClient();

                Games.TurnBasedMultiplayer
                        .createMatch(mGoogleApiClient, autoMatchingConfig)
                        .setResultCallback(createAutoMatchCallback);
            }
        });

        view.findViewById(R.id.startMatchButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final GoogleApiClient mGoogleApiClient = mListener.getGoogleApiClient();
                if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                    Intent intent = Games.TurnBasedMultiplayer
                            .getSelectOpponentsIntent(mGoogleApiClient, 1, 1, false);
                    startActivityForResult(intent, RC_SELECT_PLAYERS);
                }
            }
        });

        view.findViewById(R.id.checkGamesButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final GoogleApiClient mGoogleApiClient = mListener.getGoogleApiClient();
                if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                    Intent intent = Games.TurnBasedMultiplayer.getInboxIntent(mGoogleApiClient);
                    startActivityForResult(intent, 3);
                }
            }
        });

        return view;
    }

    public void onButtonPressed(int action) {
        if (mListener != null) {
            mListener.onFragmentInteraction(action);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RC_SELECT_PLAYERS:
                if (resultCode == Activity.RESULT_OK) {

                    ArrayList<String> playerIds = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);

                    TurnBasedMatchConfig turnBasedMatchConfig = TurnBasedMatchConfig
                            .builder()
                            .addInvitedPlayer(playerIds.get(0))
                            .build();

                    GoogleApiClient mGoogleApiClient = mListener.getGoogleApiClient();

                    Games.TurnBasedMultiplayer
                            .createMatch(mGoogleApiClient, turnBasedMatchConfig)
                            .setResultCallback(createOpponentMatchCallback);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }
}

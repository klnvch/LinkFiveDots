package by.klnvch.link5dots.online;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.turnbased.OnTurnBasedMatchUpdateReceivedListener;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;
import com.google.android.gms.plus.Plus;

import java.util.ArrayList;

import by.klnvch.link5dots.Game;
import by.klnvch.link5dots.R;

public class OnlinePickerActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        OnInvitationReceivedListener,
        OnTurnBasedMatchUpdateReceivedListener,
        OnFragmentInteractionListener{

    private static final String TAG = "OnlinePickerActivity";

    private GoogleApiClient mGoogleApiClient;

    // Are we currently resolving a connection failure?
    private boolean mResolvingConnectionFailure = false;

    // Local convenience pointers
    public TextView mDataView;
    public TextView mTurnTextView;

    // For our intents
    private static final int RC_SIGN_IN = 9001;

    // Should I be showing the turn API?
    public boolean isDoingTurn = false;

    // This is the current match we're in; null if not loaded
    public TurnBasedMatch mMatch;

    // This is the current match data after being unpersisted.
    // Do not retain references to match data once you have
    // taken an action on the match, such as takeTurn()
    public Game mGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.online);

        if (savedInstanceState != null) {
            mMatch = savedInstanceState.getParcelable(Multiplayer.EXTRA_TURN_BASED_MATCH);
        } else {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, new OnlineSearchFragment(), OnlineSearchFragment.TAG)
                    .commit();
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();

        mDataView = ((TextView) findViewById(R.id.data_view));
        mTurnTextView = ((TextView) findViewById(R.id.turn_counter_view));
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMatch != null) {
            outState.putParcelable(Multiplayer.EXTRA_TURN_BASED_MATCH, mMatch);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "onConnected");

        Games.Invitations.registerInvitationListener(mGoogleApiClient, this);
        Games.TurnBasedMultiplayer.registerMatchUpdateListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended: " + i);
        mGoogleApiClient.connect();
        setViewVisibility();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed(): attempting to resolve");
        if (mResolvingConnectionFailure) {
            // Already resolving
            Log.d(TAG, "onConnectionFailed(): ignoring connection failure, already resolving.");
            return;
        }

        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, RC_SIGN_IN);
            } catch (IntentSender.SendIntentException e) {
                mGoogleApiClient.connect();
            }
        } else {
            int errorCode = connectionResult.getErrorCode();
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, errorCode, RC_SIGN_IN);
            if (dialog != null) {
                dialog.show();
            } else {
                showAlert(getString(R.string.signin_other_error));
            }
        }
    }

    // In-game controls

    // Cancel the game. Should possibly wait until the game is canceled before
    // giving up on the view.
    public void onCancelClicked(View view) {
        Games.TurnBasedMultiplayer.cancelMatch(mGoogleApiClient, mMatch.getMatchId())
                .setResultCallback(new ResultCallback<TurnBasedMultiplayer.CancelMatchResult>() {
                    @Override
                    public void onResult(TurnBasedMultiplayer.CancelMatchResult result) {
                        processResult(result);
                    }
                });
        isDoingTurn = false;
        setViewVisibility();
    }

    // Leave the game during your turn. Note that there is a separate
    // Games.TurnBasedMultiplayer.leaveMatch() if you want to leave NOT on your turn.
    public void onLeaveClicked(View view) {
        String nextParticipantId = getNextParticipantId();

        Games.TurnBasedMultiplayer.leaveMatchDuringTurn(mGoogleApiClient, mMatch.getMatchId(),
                nextParticipantId).setResultCallback(
                new ResultCallback<TurnBasedMultiplayer.LeaveMatchResult>() {
                    @Override
                    public void onResult(TurnBasedMultiplayer.LeaveMatchResult result) {
                        processResult(result);
                    }
                });
        setViewVisibility();
    }

    // Finish the game. Sometimes, this is your only choice.
    public void onFinishClicked(View view) {
        Games.TurnBasedMultiplayer.finishMatch(mGoogleApiClient, mMatch.getMatchId())
                .setResultCallback(new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
                    @Override
                    public void onResult(TurnBasedMultiplayer.UpdateMatchResult result) {
                        processResult(result);
                    }
                });

        isDoingTurn = false;
        setViewVisibility();
    }


    // Upload your new game state, then take a turn, and pass it on to the next
    // player.
    public void onDoneClicked(View view) {

        String nextParticipantId = getNextParticipantId();
        // Create the next turn
        mGame.turnCounter += 1;
        mGame.setHostName(mDataView.getText().toString());

        Games.TurnBasedMultiplayer.takeTurn(mGoogleApiClient, mMatch.getMatchId(),
                mGame.toByteArray(), nextParticipantId).setResultCallback(
                new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
                    @Override
                    public void onResult(TurnBasedMultiplayer.UpdateMatchResult result) {
                        processResult(result);
                    }
                });

        mGame = null;
    }

    // Sign-in, Sign out behavior

    // Update the visibility based on what state we're in.
    public void setViewVisibility() {
        boolean isSignedIn = (mGoogleApiClient != null) && (mGoogleApiClient.isConnected());

        if (!isSignedIn) {
            findViewById(R.id.gameplay_layout).setVisibility(View.GONE);
            return;
        }

        if (isDoingTurn) {
            findViewById(R.id.gameplay_layout).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.gameplay_layout).setVisibility(View.GONE);
        }
    }

    // Switch to game-play view.
    public void setGamePlayUI() {
        isDoingTurn = true;
        setViewVisibility();
        mDataView.setText(mGame.getHostName());
        mTurnTextView.setText("Turn " + mGame.turnCounter);
    }

    // Rematch dialog
    public void askForRematch() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setMessage("Do you want a rematch?");

        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Sure, rematch!",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                rematch();
                            }
                        })
                .setNegativeButton("No.", null);

        alertDialogBuilder.show();
    }

    // This function is what gets called when you return from either the Play
    // Games built-in inbox, or else the create game built-in interface.
    @Override
    public void onActivityResult(int request, int response, Intent data) {
        super.onActivityResult(request, response, data);
        if (request == RC_SIGN_IN) {
            mResolvingConnectionFailure = false;
            if (response == Activity.RESULT_OK) {
                mGoogleApiClient.connect();
            } else {
                showActivityResultError(request, response, R.string.signin_other_error);
            }
        }
    }

    // startMatch() happens in response to the createTurnBasedMatch()
    // above. This is only called on success, so we should have a
    // valid match object. We're taking this opportunity to setup the
    // game, saving our initial state. Calling takeTurn() will
    // callback to OnTurnBasedMatchUpdated(), which will show the game
    // UI.
    public void startMatch(TurnBasedMatch match) {
        mGame = new Game("host", "guest");

        mMatch = match;

        String playerId = Games.Players.getCurrentPlayerId(mGoogleApiClient);
        String myParticipantId = mMatch.getParticipantId(playerId);// TODO: change for auto-matching

        Games.TurnBasedMultiplayer.takeTurn(
                mGoogleApiClient, match.getMatchId(), mGame.toByteArray(), myParticipantId)
                .setResultCallback(
                        new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
                            @Override
                            public void onResult(TurnBasedMultiplayer.UpdateMatchResult result) {
                                processResult(result);
                            }
                        });
    }

    // If you choose to rematch, then call it and wait for a response.
    public void rematch() {
        Games.TurnBasedMultiplayer.rematch(mGoogleApiClient, mMatch.getMatchId()).setResultCallback(
                new ResultCallback<TurnBasedMultiplayer.InitiateMatchResult>() {
                    @Override
                    public void onResult(TurnBasedMultiplayer.InitiateMatchResult result) {
                        processResult(result);
                    }
                });
        mMatch = null;
        isDoingTurn = false;
    }

    /**
     * Get the next participant. In this function, we assume that we are
     * round-robin, with all known players going before all automatch players.
     * This is not a requirement; players can go in any order. However, you can
     * take turns in any order.
     *
     * @return participantId of next player, or null if automatching
     */
    public String getNextParticipantId() {

        String playerId = Games.Players.getCurrentPlayerId(mGoogleApiClient);
        String myParticipantId = mMatch.getParticipantId(playerId);

        ArrayList<String> participantIds = mMatch.getParticipantIds();

        int desiredIndex = -1;

        for (int i = 0; i < participantIds.size(); i++) {
            if (participantIds.get(i).equals(myParticipantId)) {
                desiredIndex = i + 1;
            }
        }

        if (desiredIndex < participantIds.size()) {
            return participantIds.get(desiredIndex);
        }

        if (mMatch.getAvailableAutoMatchSlots() <= 0) {
            // You've run out of auto-match slots, so we start over.
            return participantIds.get(0);
        } else {
            // You have not yet fully auto-matched, so null will find a new
            // person to play against.
            return null;
        }
    }

    // This is the main function that gets called when players choose a match
    // from the inbox, or else create a match and want to start it.
    public void updateMatch(TurnBasedMatch match) {
        mMatch = match;

        int status = match.getStatus();
        int turnStatus = match.getTurnStatus();

        switch (status) {
            case TurnBasedMatch.MATCH_STATUS_CANCELED:
                OnlineUtils.showWarning(this, "Canceled!", "This game was canceled!");
                return;
            case TurnBasedMatch.MATCH_STATUS_EXPIRED:
                OnlineUtils.showWarning(this, "Expired!", "This game is expired.  So sad!");
                return;
            case TurnBasedMatch.MATCH_STATUS_AUTO_MATCHING:
                OnlineUtils.showWarning(this, "Waiting for auto-match...",
                        "We're still waiting for an automatch partner.");
                return;
            case TurnBasedMatch.MATCH_STATUS_COMPLETE:
                if (turnStatus == TurnBasedMatch.MATCH_TURN_STATUS_COMPLETE) {
                    OnlineUtils.showWarning(this,
                            "Complete!",
                            "This game is over; someone finished it, and so did you!  There is nothing to be done.");
                    break;
                }

                // Note that in this state, you must still call "Finish" yourself,
                // so we allow this to continue.
                OnlineUtils.showWarning(this, "Complete!",
                        "This game is over; someone finished it!  You can only finish it now.");
        }

        // OK, it's active. Check on turn status.
        switch (turnStatus) {
            case TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN:
                mGame = Game.parseByteArray(mMatch.getData());
                setGamePlayUI();
                return;
            case TurnBasedMatch.MATCH_TURN_STATUS_THEIR_TURN:
                // Should return results.
                OnlineUtils.showWarning(this, "Alas...", "It's not your turn.");
                break;
            case TurnBasedMatch.MATCH_TURN_STATUS_INVITED:
                OnlineUtils.showWarning(this, "Good initiative!",
                        "Still waiting for invitations.\n\nBe patient!");
        }

        mGame = null;

        setViewVisibility();
    }

    private void processResult(TurnBasedMultiplayer.CancelMatchResult result) {

        if (!OnlineUtils.checkStatusCode(this, result.getStatus().getStatusCode())) {
            return;
        }

        isDoingTurn = false;

        OnlineUtils.showWarning(this, "Match", "This match is canceled.  All other players will have their game ended.");
    }

    private void processResult(TurnBasedMultiplayer.InitiateMatchResult result) {
        TurnBasedMatch match = result.getMatch();

        if (OnlineUtils.checkStatusCode(this, result.getStatus().getStatusCode())) {
            if (match.getData() != null) {
                updateMatch(match);
            } else {
                startMatch(match);
            }
        }
    }


    private void processResult(TurnBasedMultiplayer.LeaveMatchResult result) {
        TurnBasedMatch match = result.getMatch();
        if (!OnlineUtils.checkStatusCode(this, result.getStatus().getStatusCode())) {
            return;
        }
        isDoingTurn = (match.getTurnStatus() == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN);
        OnlineUtils.showWarning(this, "Left", "You've left this match.");
    }


    public void processResult(TurnBasedMultiplayer.UpdateMatchResult result) {
        TurnBasedMatch match = result.getMatch();
        if (!OnlineUtils.checkStatusCode(this, result.getStatus().getStatusCode())) {
            return;
        }
        if (match.canRematch()) {
            askForRematch();
        }

        isDoingTurn = (match.getTurnStatus() == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN);

        if (isDoingTurn) {
            updateMatch(match);
            return;
        }

        setViewVisibility();
    }

    // Handle notification events.
    @Override
    public void onInvitationReceived(Invitation invitation) {
        Toast.makeText(
                this,
                "An invitation has arrived from "
                        + invitation.getInviter().getDisplayName(), Toast.LENGTH_LONG)
                .show();
    }

    @Override
    public void onInvitationRemoved(String invitationId) {
        Toast.makeText(this, "An invitation was removed.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onTurnBasedMatchReceived(TurnBasedMatch match) {
        Toast.makeText(this, "A match was updated.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onTurnBasedMatchRemoved(String matchId) {
        Toast.makeText(this, "A match was removed.", Toast.LENGTH_LONG).show();

    }

    @Override
    public void onFragmentInteraction(int action) {
        switch (action) {
            case OnFragmentInteractionListener.ACTION_SELECT_OPPONENT:
                break;
            case OnFragmentInteractionListener.ACTION_AUTO_MATCH:
                break;
            case OnFragmentInteractionListener.ACTION_CHECK_GAME:
                break;
        }
    }

    @Override
    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }


    public void showActivityResultError(int requestCode, int actResp, int errorDescription) {
        Dialog errorDialog;

        switch (actResp) {
            case GamesActivityResultCodes.RESULT_APP_MISCONFIGURED:
                errorDialog = makeSimpleDialog(getString(R.string.app_misconfigured));
                break;
            case GamesActivityResultCodes.RESULT_SIGN_IN_FAILED:
                errorDialog = makeSimpleDialog(getString(R.string.sign_in_failed));
                break;
            case GamesActivityResultCodes.RESULT_LICENSE_FAILED:
                errorDialog = makeSimpleDialog(getString(R.string.license_failed));
                break;
            default:
                // No meaningful Activity response code, so generate default Google
                // Play services dialog
                final int errorCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
                errorDialog = GoogleApiAvailability.getInstance().getErrorDialog(this, errorCode, requestCode, null);
                if (errorDialog == null) {
                    // get fallback dialog
                    Log.e("BaseGamesUtils",
                            "No standard error dialog available. Making fallback dialog.");
                    errorDialog = makeSimpleDialog(getString(errorDescription));
                }
        }

        errorDialog.show();
    }

    public Dialog makeSimpleDialog(String text) {
        return new AlertDialog.Builder(this)
                .setMessage(text)
                .setNeutralButton(android.R.string.ok, null)
                .create();
    }

    public void showAlert(String message) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setNeutralButton(android.R.string.ok, null)
                .show();
    }

    /**
     * For use in sample code only. Checks if the sample was set up correctly,
     * including changing the package name to a non-Google package name and
     * replacing the placeholder IDs. Shows alert dialogs to notify about problems.
     * DO NOT call this method from a production app, it's meant only for samples!
     * @param resIds the resource IDs to check for placeholders
     * @return true if sample is set up correctly; false otherwise.
     */
    public boolean verifySampleSetup(Activity activity, int... resIds) {
        StringBuilder problems = new StringBuilder();
        boolean problemFound = false;
        problems.append("The following set up problems were found:\n\n");

        // Did the developer forget to change the package name?
        if (activity.getPackageName().startsWith("com.google.example.games")) {
            problemFound = true;
            problems.append("- Package name cannot be com.google.*. You need to change the "
                    + "sample's package name to your own package.").append("\n");
        }

        for (int i : resIds) {
            if (activity.getString(i).toLowerCase().contains("replaceme")) {
                problemFound = true;
                problems.append("- You must replace all " +
                        "placeholder IDs in the ids.xml file by your project's IDs.").append("\n");
                break;
            }
        }

        if (problemFound) {
            problems.append("\n\nThese problems may prevent the app from working properly.");
            showAlert(problems.toString());
            return false;
        }

        return true;
    }
}

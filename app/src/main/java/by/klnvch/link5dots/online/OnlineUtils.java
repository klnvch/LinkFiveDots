package by.klnvch.link5dots.online;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;

import by.klnvch.link5dots.Game;
import by.klnvch.link5dots.R;

public class OnlineUtils {

    // This is the main function that gets called when players choose a match
    // from the inbox, or else create a match and want to start it.
    public static void checkTurnStatus(Context context, int status, int turnStatus) {

        switch (status) {
            case TurnBasedMatch.MATCH_STATUS_CANCELED:
                OnlineUtils.showWarning(context, "Canceled!", "This game was canceled!");
                return;
            case TurnBasedMatch.MATCH_STATUS_EXPIRED:
                OnlineUtils.showWarning(context, "Expired!", "This game is expired.  So sad!");
                return;
            case TurnBasedMatch.MATCH_STATUS_AUTO_MATCHING:
                OnlineUtils.showWarning(context, "Waiting for auto-match...",
                        "We're still waiting for an automatch partner.");
                return;
            case TurnBasedMatch.MATCH_STATUS_COMPLETE:
                if (turnStatus == TurnBasedMatch.MATCH_TURN_STATUS_COMPLETE) {
                    OnlineUtils.showWarning(context,
                            "Complete!",
                            "This game is over; someone finished it, and so did you!  There is nothing to be done.");
                    break;
                }

                // Note that in this state, you must still call "Finish" yourself,
                // so we allow this to continue.
                OnlineUtils.showWarning(context, "Complete!",
                        "This game is over; someone finished it!  You can only finish it now.");
        }

        // OK, it's active. Check on turn status.
        switch (turnStatus) {
            case TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN:
                OnlineUtils.showWarning(context, "Yes...", "It's your turn.");
                return;
            case TurnBasedMatch.MATCH_TURN_STATUS_THEIR_TURN:
                // Should return results.
                OnlineUtils.showWarning(context, "Alas...", "It's not your turn.");
                break;
            case TurnBasedMatch.MATCH_TURN_STATUS_INVITED:
                OnlineUtils.showWarning(context, "Good initiative!",
                        "Still waiting for invitations.\n\nBe patient!");
        }
    }

    public static boolean checkStatusCode(Context context, int statusCode) {
        switch (statusCode) {
            case GamesStatusCodes.STATUS_OK:
                return true;
            case GamesStatusCodes.STATUS_NETWORK_ERROR_OPERATION_DEFERRED:
                // This is OK; the action is stored by Google Play Services and will
                // be dealt with later.
                Toast.makeText(
                        context,
                        "Stored action for later.  (Please remove this toast before release.)",
                        Toast.LENGTH_LONG).show();
                // NOTE: This toast is for informative reasons only; please remove
                // it from your final application.
                return true;
            case GamesStatusCodes.STATUS_MULTIPLAYER_ERROR_NOT_TRUSTED_TESTER:
                showErrorMessage(context, R.string.status_multiplayer_error_not_trusted_tester);
                break;
            case GamesStatusCodes.STATUS_MATCH_ERROR_ALREADY_REMATCHED:
                showErrorMessage(context, R.string.match_error_already_rematched);
                break;
            case GamesStatusCodes.STATUS_NETWORK_ERROR_OPERATION_FAILED:
                showErrorMessage(context, R.string.network_error_operation_failed);
                break;
            case GamesStatusCodes.STATUS_CLIENT_RECONNECT_REQUIRED:
                showErrorMessage(context, R.string.client_reconnect_required);
                break;
            case GamesStatusCodes.STATUS_INTERNAL_ERROR:
                showErrorMessage(context, R.string.internal_error);
                break;
            case GamesStatusCodes.STATUS_MATCH_ERROR_INACTIVE_MATCH:
                showErrorMessage(context, R.string.match_error_inactive_match);
                break;
            case GamesStatusCodes.STATUS_MATCH_ERROR_LOCALLY_MODIFIED:
                showErrorMessage(context, R.string.match_error_locally_modified);
                break;
            case GamesStatusCodes.STATUS_MULTIPLAYER_ERROR_INVALID_OPERATION:
                showWarning(context, "STATUS_MULTIPLAYER_ERROR_INVALID_OPERATION", "This multiplayer operation is not valid, and the server rejected it. Check the logs for more information.");
                break;
            default:
                showErrorMessage(context, R.string.unexpected_status);
                Log.d("OnlineUtils", "Did not have warning or string to deal with: " + statusCode);
        }

        return false;
    }

    public static void showErrorMessage(Context context, int stringId) {
        showWarning(context, "Warning", context.getResources().getString(stringId));
    }

    public static void showWarning(Context context, String title, String message) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", null)
                .show();
    }
}

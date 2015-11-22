package by.klnvch.link5dots.online;

import com.google.android.gms.common.api.GoogleApiClient;

public interface OnFragmentInteractionListener {
    int ACTION_SIGN_IN = 0;
    int ACTION_SIGN_OUT = 1;
    int ACTION_SELECT_OPPONENT = 2;
    int ACTION_AUTO_MATCH = 3;
    int ACTION_CHECK_GAME = 4;
    void onFragmentInteraction(int action);
    GoogleApiClient getGoogleApiClient();
}

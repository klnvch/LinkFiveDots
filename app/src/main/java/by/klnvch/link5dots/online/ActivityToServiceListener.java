package by.klnvch.link5dots.online;

import android.support.annotation.NonNull;

import com.google.android.gms.common.api.GoogleApiClient;

public interface ActivityToServiceListener {
    void setListener(ServiceToActivityListener Listener);
    void quickGame(@NonNull GoogleApiClient googleApiClient);
    void invitePlayer(@NonNull GoogleApiClient googleApiClient, String playerId);
    void acceptInvitation(@NonNull GoogleApiClient googleApiClient, String invitationId);
    void leaveRoom(@NonNull GoogleApiClient googleApiClient);
}

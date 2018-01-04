package de.htwberlin.f4.ai.ma.indoorroutefinder.location.locator;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import de.htwberlin.f4.ai.ma.indoorroutefinder.location.locator.listeners.LocationChangeListener;
import de.htwberlin.f4.ai.ma.indoorroutefinder.node.Node;

public class CombinedLocator implements Locator, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private Node location;
    private long timestamp;
    private List<LocationChangeListener> listeners;
    private GoogleApiClient googleApiClient;
    private Context context;

    CombinedLocator(Context context) {
        this.listeners = new ArrayList<>();
        this.googleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        this.googleApiClient.connect();
    }

    @Override
    public void startLocationUpdates() {

    }

    @Override
    public void stopLocationUpdates() {

    }

    @Override
    public void registerLocationListener(LocationChangeListener listener) {

    }

    @Override
    public void unregisterLocationListener(LocationChangeListener listener) {

    }

    @Override
    public Node getLastLocation() {
        return null;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //TODO
    }

    @Override
    public void onConnectionSuspended(int i) {
        //TODO
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //TODO
    }
}

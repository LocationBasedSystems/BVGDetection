package de.htwberlin.f4.ai.ma.indoorroutefinder.location.locator;

import com.google.android.gms.location.LocationRequest;

import java.util.List;

import de.htwberlin.f4.ai.ma.indoorroutefinder.location.locator.listeners.LocationChangeListener;
import de.htwberlin.f4.ai.ma.indoorroutefinder.node.Node;

public interface Locator {
    void startLocationUpdates();
    void stopLocationUpdates();
    void registerLocationListener(LocationChangeListener listener);
    void unregisterLocationListener(LocationChangeListener listener);
    void setLocationRequest(LocationRequest locationRequest);
    List<Node> sortByDistanceNearestFirst(Node origin, List<Node> nodes);
    LocationRequest getLocationRequest();
    Node getLastLocation();
}

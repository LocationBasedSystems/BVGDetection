package de.htwberlin.f4.ai.ma.indoorroutefinder.location.locator;

import de.htwberlin.f4.ai.ma.indoorroutefinder.location.locator.listeners.LocationChangeListener;
import de.htwberlin.f4.ai.ma.indoorroutefinder.node.Node;

public interface Locator {
    void startLocationUpdates();
    void stopLocationUpdates();
    void registerLocationListener(LocationChangeListener listener);
    void unregisterLocationListener(LocationChangeListener listener);
    Node getLastLocation();
}

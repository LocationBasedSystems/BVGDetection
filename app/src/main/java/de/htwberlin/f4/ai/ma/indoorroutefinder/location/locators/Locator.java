package de.htwberlin.f4.ai.ma.indoorroutefinder.location.locators;

import de.htwberlin.f4.ai.ma.indoorroutefinder.location.locators.listeners.LocationChangeListener;
import de.htwberlin.f4.ai.ma.indoorroutefinder.node.Node;

public interface Locator {
    void registerLocationListener(LocationChangeListener listener);
    void unregisterLocationListener(LocationChangeListener listener);
    Node getLastLocation();
}

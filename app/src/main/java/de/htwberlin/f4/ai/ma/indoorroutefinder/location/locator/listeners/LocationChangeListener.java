package de.htwberlin.f4.ai.ma.indoorroutefinder.location.locator.listeners;

import de.htwberlin.f4.ai.ma.indoorroutefinder.location.locator.LocationSource;
import de.htwberlin.f4.ai.ma.indoorroutefinder.node.Node;

public interface LocationChangeListener {
    void onLocationChanged(Node newLocation, LocationSource source);
}

package de.htwberlin.f4.ai.ma.indoorroutefinder.location.locators.listeners;

import de.htwberlin.f4.ai.ma.indoorroutefinder.node.Node;

public interface LocationChangeListener {
    void onLocationChanged(Node newLocation);
}

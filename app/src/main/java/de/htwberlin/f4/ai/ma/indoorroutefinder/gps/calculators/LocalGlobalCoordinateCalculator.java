package de.htwberlin.f4.ai.ma.indoorroutefinder.gps.calculators;

import android.location.Location;

import de.htwberlin.f4.ai.ma.indoorroutefinder.node.Node;

/**
 * @author Emil Schoenawa (eschoenawa; Matr. Nr.: 554086)
 * @version 05.12.2017
 */

public interface LocalGlobalCoordinateCalculator {
    Location getGlobalCoordinates(String offsetInLocalCoords, Location source);
    String calculateOffset(String sourceLocalCoords, String targetLocalCoords);
}

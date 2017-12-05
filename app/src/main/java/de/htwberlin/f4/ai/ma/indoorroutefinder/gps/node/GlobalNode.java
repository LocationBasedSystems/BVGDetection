package de.htwberlin.f4.ai.ma.indoorroutefinder.gps.node;

import android.location.Location;

import de.htwberlin.f4.ai.ma.indoorroutefinder.node.Node;

/**
 * Created by emils on 05.12.2017.
 */

public interface GlobalNode extends Node {
    public double getGlobalCalculationInaccuracyRating();
    public void setGlobalCalculationInaccuracyRating(double globalCalculationInaccuracyRating);
    public double getLatitude();
    public void setLatitude(double latitude);
    public double getLongitude();
    public void setLongitude(double longitude);
    public double getAltitude();
    public void setAltitude(double altitude);
    public Node getNode();
    public boolean hasGlobalCoordinates();
    public boolean hasGlobalAltitude();
    public Location getLocation();
}

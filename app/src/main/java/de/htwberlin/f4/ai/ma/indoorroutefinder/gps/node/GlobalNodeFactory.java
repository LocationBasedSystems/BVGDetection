package de.htwberlin.f4.ai.ma.indoorroutefinder.gps.node;

import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.Fingerprint;
import de.htwberlin.f4.ai.ma.indoorroutefinder.node.Node;

/**
 * Created by emils on 05.12.2017.
 */

public class GlobalNodeFactory {
    public static GlobalNode createInstance(String id, String description, Fingerprint fingerprint, String coordinates, String picturePath, double globalCalculationInaccuracyRating, double latitude, double longitude, double altitude) {
        return new GlobalNodeImpl(id, description, fingerprint, coordinates, picturePath, globalCalculationInaccuracyRating, latitude, longitude, altitude);
    }

    public static GlobalNode createInstance(Node n) {
        return new GlobalNodeImpl(n);
    }
}

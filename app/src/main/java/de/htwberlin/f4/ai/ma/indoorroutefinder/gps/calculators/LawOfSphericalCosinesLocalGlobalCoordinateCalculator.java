package de.htwberlin.f4.ai.ma.indoorroutefinder.gps.calculators;

import android.location.Location;

import de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.WKT;

public class LawOfSphericalCosinesLocalGlobalCoordinateCalculator implements LocalGlobalCoordinateCalculator {
    @Override
    public Location getGlobalCoordinates(String offsetInLocalCoords, Location source) {
        //How it works: https://math.stackexchange.com/questions/2364154/anyone-knows-the-reference-for-this-formula-or-can-derive-them-in-spherical-coor
        float[] offset = WKT.strToCoord(offsetInLocalCoords);
        if (offset != null && offset.length == 3) {
            //TODO Implement
        }
        return null;
    }

    @Override
    public String calculateOffset(String sourceLocalCoords, String targetLocalCoords) {
        return null;
    }
}

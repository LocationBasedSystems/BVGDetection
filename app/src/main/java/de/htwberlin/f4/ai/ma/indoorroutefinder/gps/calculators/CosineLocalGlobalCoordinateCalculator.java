package de.htwberlin.f4.ai.ma.indoorroutefinder.gps.calculators;

import android.location.Location;
import android.os.Debug;
import android.util.Log;

import de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.WKT;

public class CosineLocalGlobalCoordinateCalculator implements LocalGlobalCoordinateCalculator {

    private static final double EARTH_RADIUS = 6371000;

    @Override
    public Location getGlobalCoordinates(String offsetInLocalCoords, Location source) {
        float[] offset = WKT.strToCoord(offsetInLocalCoords);
        if (offset != null && offset.length == 3) {
            Location result = new Location(source);
            // Get Values in radians
            Log.d("POSITIONCALCULATOR", "Start lat: " + source.getLatitude());
            Log.d("POSITIONCALCULATOR", "Start long: " + source.getLongitude());
            double latitude = Math.toRadians(source.getLatitude());
            double longitude = Math.toRadians(source.getLongitude());
            double angle = Math.toRadians(getHeading(offset[0], offset[1]));
            double angularDistance = getAngularDistance(offset[0], offset[1]);

            /**
             * Formulae for lat/long derived from spherical law of cosines (dt.: Seiten-Kosinussatz bzw. Winkel-Kosinussatz für das allgemeine Kugeldreieck)
             * source latitude: φ1
             * target latitude: φ2
             * source longitude: λ1
             * target longitude: λ2
             * angular distance: δ = sqrt(north^2 + east^2)/earth_radius
             * polar angle (like a compass heading): θ = atan2(east, north)
             */
            // φ2 = asin( sin φ1 * cos δ + cos φ1 * sin δ * cos θ )
            result.setLatitude(Math.toDegrees(Math.asin((Math.sin(latitude) * Math.cos(angularDistance)) + (Math.cos(latitude) * Math.sin(angularDistance) * Math.cos(angle)))));
            // 	λ2 = λ1 + atan2( sin θ * sin δ * cos φ1, cos δ − sin φ1 * sin φ2 )
            result.setLongitude(Math.toDegrees(longitude + (Math.atan2(Math.sin(angle) * Math.sin(angularDistance) * Math.cos(latitude), Math.cos(angularDistance) - (Math.sin(latitude) * Math.sin(Math.toRadians(result.getLatitude())))))));
            double altitude = Double.NaN;
            if (source.hasAltitude()) {
                altitude = source.getAltitude();
                altitude += offset[2];
                result.setAltitude(altitude);
            }
            Log.d("POSITIONCALCULATOR", "End lat: " + result.getLatitude());
            Log.d("POSITIONCALCULATOR", "End long: " + result.getLongitude());
            return result;
        }
        return null;
    }

    @Override
    public String calculateOffset(String sourceLocalCoords, String targetLocalCoords) {
        float[] source = WKT.strToCoord(sourceLocalCoords);
        float[] target = WKT.strToCoord(targetLocalCoords);
        float[] result = new float[3];
        if (source != null && target != null && source.length == 3 && target.length == 3) {
            for (int i = 0; i < 3; i++) {
                result[i] = target[i] - source [i];
            }
            return WKT.coordToStr(result);
        }
        return null;
    }

    private double getHeading(float north, float east) {
        double angle = Math.toDegrees(Math.atan2(east, north));

        if(angle < 0){
            angle += 360;
        }

        return angle;
    }

    private double getAngularDistance(float north, float east) {
        //TODO add d/r um zu einem Winkelabstand zu wandeln
        double distance = Math.sqrt(Math.pow(north, 2) + Math.pow(east, 2));
        double angularDistance = distance / EARTH_RADIUS;
        return angularDistance;
    }
}

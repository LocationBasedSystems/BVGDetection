package de.htwberlin.f4.ai.ma.indoorroutefinder.gps.calculators;

import android.location.Location;

import de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.WKT;

/**
 * IMPORTANT NOTE: This class is not functional and was abandoned and replaced by the {@link CosineLocalGlobalCoordinateCalculator}. It may be implemented correctly at a later time.
 * @author Emil Schoenawa (eschoenawa; Matr. Nr.: 554086)
 * @version 05.12.2017
 */
@Deprecated
public class SimpleHaversineLocalGlobalCoordinateCalculator implements LocalGlobalCoordinateCalculator {

    // earth circumference in km
    private static final double earthCircumference = 40075.04;

    SimpleHaversineLocalGlobalCoordinateCalculator() {
    }

    @Override
    public Location getGlobalCoordinates(String offsetInLocalCoords, Location source) {
        //TODO This method is not tested and will most likely not work correctly
        float[] offset = WKT.strToCoord(offsetInLocalCoords);
        if (offset != null && offset.length == 3) {
            Location result = new Location(source);
            double latitude = source.getLatitude();
            double longitude = source.getLongitude();
            double altitude = Double.NaN;
            double uncorrectedLongitudeWithUnchangedLatitude = longitude + (360 * offset[1] / 1000 / earthCircumference / Math.cos((Math.PI / 180) * latitude));
            //Plus as latitudes get smaller to the south and bigger to the north and vecor gives direction to north
            latitude = correctLatitude(latitude + (360 * offset[0] / 1000 / earthCircumference));
            double uncorrectedLongitudeWithChangedLatitude = longitude + (360 * offset[1] / 1000 / earthCircumference / Math.cos((Math.PI / 180) * latitude));
            //Average longitude between point with first lat then long and point with first long then lat
            longitude = correctLongitude((uncorrectedLongitudeWithUnchangedLatitude + uncorrectedLongitudeWithChangedLatitude) / 2);

            result.setLatitude(latitude);
            result.setLongitude(longitude);
            if (source.hasAltitude()) {
                altitude = source.getAltitude();
                altitude += offset[3];
                result.setAltitude(altitude);
            }
            return result;
        }
        else {
            return null;
        }
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

    private double correctLatitude(double uncorrectedLatitude) {
        //TODO This method is not tested and may not work correctly
        if (uncorrectedLatitude < -90) {
            return uncorrectedLatitude > -180 ? -90 - (uncorrectedLatitude + 90) : correctLatitude(uncorrectedLatitude + 180);
        }
        else if (uncorrectedLatitude > 90) {
            return uncorrectedLatitude < 180 ? 90 - (uncorrectedLatitude - 90) : correctLatitude(uncorrectedLatitude - 180);
        }
        else {
            return uncorrectedLatitude;
        }
    }

    private double correctLongitude(double uncorrectedLongitude) {
        //TODO This method is not tested and may not work correctly
        if (uncorrectedLongitude < -180) {
            return uncorrectedLongitude > -360 ? 180 - (uncorrectedLongitude + 180) : correctLongitude(uncorrectedLongitude + 360);
        }
        else if (uncorrectedLongitude > 180) {
            return uncorrectedLongitude < 360 ? -180 + (uncorrectedLongitude - 180) : correctLongitude(uncorrectedLongitude - 360);
        }
        else {
            return uncorrectedLongitude;
        }
    }
}

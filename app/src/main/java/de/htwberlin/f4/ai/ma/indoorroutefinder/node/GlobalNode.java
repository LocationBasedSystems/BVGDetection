package de.htwberlin.f4.ai.ma.indoorroutefinder.node;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.Fingerprint;

public class GlobalNode implements Node {

    private static final String LOG_PREFIX = "GLOBALNODE";

    private String id;
    private String description;
    private Fingerprint fingerprint;
    private String coordinates;
    private String picturePath;
    private boolean localCoordinatesCalculated;
    private boolean globalCoordinatesCalculated;
    private double latitude;
    private double longitude;
    private double altitude;

    public GlobalNode(String id, String description, Fingerprint fingerprint, String coordinates, String picturePath, boolean localCoordinatesCalculated, boolean globalCoordinatesCalculated, double latitude, double longitude, double altitude) {
        this.id = id;
        this.description = description;
        this.fingerprint = fingerprint;
        this.coordinates = coordinates;
        this.picturePath = picturePath;
        this.localCoordinatesCalculated = localCoordinatesCalculated;
        this.globalCoordinatesCalculated = globalCoordinatesCalculated;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }

    public GlobalNode(Node n) {
        this.id = n.getId();
        this.description = n.getDescription();
        this.fingerprint = n.getFingerprint();
        this.coordinates = n.getCoordinates();
        this.picturePath = n.getPicturePath();
        this.setAdditionalInfo(n.getAdditionalInfo());
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public Fingerprint getFingerprint() {
        return this.fingerprint;
    }

    @Override
    public String getCoordinates() {
        return this.coordinates;
    }

    @Override
    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }

    @Override
    public String getPicturePath() {
        return this.picturePath;
    }

    @Override
    public String getAdditionalInfo() {
        Gson gson = new Gson();
        AdditionalInfo info = new AdditionalInfo(this.localCoordinatesCalculated, this.globalCoordinatesCalculated, this.latitude, this.longitude, this.altitude);
        return gson.toJson(info);
    }

    @Override
    public void setAdditionalInfo(String additionalInfo) {
        Gson gson = new Gson();
        try {
            this.setAdditionalInfoFromObject(gson.fromJson(additionalInfo, AdditionalInfo.class));
        } catch (JsonSyntaxException e) {
            Log.e(LOG_PREFIX, "Unable to parse additional info, ignoring...", e);
        }
    }

    private void setAdditionalInfoFromObject(AdditionalInfo additionalInfo) {
        this.localCoordinatesCalculated = additionalInfo.localCoordinatesCalculated;
        this.globalCoordinatesCalculated = additionalInfo.globalCoordinatesCalculated;
        this.latitude = additionalInfo.latitude;
        this.longitude = additionalInfo.longitude;
        this.altitude = additionalInfo.altitude;
    }

    private AdditionalInfo getAdditionalInfoAsObject() {
        AdditionalInfo result = new AdditionalInfo();
        result.localCoordinatesCalculated = this.localCoordinatesCalculated;
        result.globalCoordinatesCalculated = this.globalCoordinatesCalculated;
        result.latitude = this.latitude;
        result.longitude = this.longitude;
        result.altitude = this.altitude;
        return result;
    }

    /**
     * Helperclass for serialization.
     */
    private class AdditionalInfo {
        private boolean localCoordinatesCalculated;
        private boolean globalCoordinatesCalculated;
        private double latitude;
        private double longitude;
        private double altitude;

        private AdditionalInfo() {
            localCoordinatesCalculated = false;
            globalCoordinatesCalculated = false;
            latitude = Double.NaN;
            longitude = Double.NaN;
        }

        public AdditionalInfo(boolean localCoordinatesCalculated, boolean globalCoordinatesCalculated, double latitude, double longitude, double altitude) {
            this.localCoordinatesCalculated = localCoordinatesCalculated;
            this.globalCoordinatesCalculated = globalCoordinatesCalculated;
            this.latitude = latitude;
            this.longitude = longitude;
            this.altitude = altitude;
        }

        public boolean isLocalCoordinatesCalculated() {
            return localCoordinatesCalculated;
        }

        public void setLocalCoordinatesCalculated(boolean localCoordinatesCalculated) {
            this.localCoordinatesCalculated = localCoordinatesCalculated;
        }

        public boolean isGlobalCoordinatesCalculated() {
            return globalCoordinatesCalculated;
        }

        public void setGlobalCoordinatesCalculated(boolean globalCoordinatesCalculated) {
            this.globalCoordinatesCalculated = globalCoordinatesCalculated;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public double getAltitude() {
            return altitude;
        }

        public void setAltitude(double altitude) {
            this.altitude = altitude;
        }
    }
}

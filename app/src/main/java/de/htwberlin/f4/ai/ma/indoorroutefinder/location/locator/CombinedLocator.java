package de.htwberlin.f4.ai.ma.indoorroutefinder.location.locator;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import de.htwberlin.f4.ai.ma.indoorroutefinder.dijkstra.DijkstraAlgorithm;
import de.htwberlin.f4.ai.ma.indoorroutefinder.dijkstra.DijkstraAlgorithmFactory;
import de.htwberlin.f4.ai.ma.indoorroutefinder.dijkstra.DijkstraNode;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.AsyncResponse;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.Fingerprint;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.FingerprintTask;
import de.htwberlin.f4.ai.ma.indoorroutefinder.gps.node.GlobalNode;
import de.htwberlin.f4.ai.ma.indoorroutefinder.gps.node.GlobalNodeFactory;
import de.htwberlin.f4.ai.ma.indoorroutefinder.location.location_calculator.LocationCalculator;
import de.htwberlin.f4.ai.ma.indoorroutefinder.location.location_calculator.LocationCalculatorFactory;
import de.htwberlin.f4.ai.ma.indoorroutefinder.location.locator.listeners.LocationChangeListener;
import de.htwberlin.f4.ai.ma.indoorroutefinder.node.Node;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandler;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandlerFactory;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class CombinedLocator implements Locator, LocationListener, AsyncResponse, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private Node location;
    private Location gpsLocation;
    private long timestamp;

    private List<LocationChangeListener> listeners;
    private DatabaseHandler databaseHandler;

    private GoogleApiClient googleApiClient;
    private Context context;
    private boolean gpsUpdatesRunning;
    private LocationRequest locationRequest;

    private Handler handler;
    private Runnable fingerprintRepeater;
    private WifiManager wifiManager;

    //TODO put these in config
    private static final int WIFI_FINGERPRINT_REPEAT_DELAY = 4000;
    private static final int WIFI_FINGERPRINT_DURATION = 3000;
    private static final long MAXIMUM_LOCATION_AGE_MILLIS = 5000L;
    //TODO maybe make this dependent on gps accuracy?
    private static final int GPS_LOCATION_RADIUS_METERS = 40;

    CombinedLocator(Context context) {
        this.listeners = new ArrayList<>();
        this.context = context;
        this.googleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        this.googleApiClient.connect();
        this.gpsUpdatesRunning = false;
        this.locationRequest = getDefaultLocationRequest();
        this.wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        this.timestamp = -1l;
        this.databaseHandler = DatabaseHandlerFactory.getInstance(context);
    }

    @RequiresPermission(allOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
    @Override
    public void startLocationUpdates() {
        if (this.googleApiClient.isConnected()) {
            if (ActivityCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                throw new SecurityException("No permission to access location!");
            }
            //noinspection deprecation
            LocationServices.FusedLocationApi.requestLocationUpdates(this.googleApiClient, this.locationRequest, this);
        }
        else if (!this.googleApiClient.isConnecting()) {
            this.googleApiClient.connect();
        }
        this.gpsUpdatesRunning = true;
        startWiFiLocationUpdates();
    }

    @Override
    public void stopLocationUpdates() {
        if (this.gpsUpdatesRunning) {
            //noinspection deprecation
            LocationServices.FusedLocationApi.removeLocationUpdates(this.googleApiClient, this);
            this.gpsUpdatesRunning = false;
        }
        stopWiFiLocationUpdates();
    }

    @Override
    public synchronized void registerLocationListener(LocationChangeListener listener) {
        this.listeners.add(listener);
        listener.onLocationChanged(this.location, LocationSource.GOOGLE_PLAY_SERVICES_FUSED_LOCATION_API);
    }

    @Override
    public synchronized void unregisterLocationListener(LocationChangeListener listener) {
        this.listeners.remove(listener);

    }

    @Override
    public void setLocationRequest(LocationRequest locationRequest) {
        this.locationRequest = locationRequest;
        if (this.gpsUpdatesRunning && !(ActivityCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            this.stopLocationUpdates();
            this.startLocationUpdates();
        }
    }

    @Override
    public List<Node> sortByDistanceNearestFirst(Node origin, List<Node> nodes) {
        ArrayList<Node> result = new ArrayList<>();
        DijkstraAlgorithm da = DijkstraAlgorithmFactory.createInstance(this.context, false);
        da.execute(origin.getId());
        while (nodes.size() > 0) {
            Node n = findNearest(da, nodes);
            if (n == null) {
                result.addAll(nodes);
                break;
            }
            else {
                result.add(n);
                nodes.remove(n);
            }
        }
        return result;
    }

    private Node findNearest(DijkstraAlgorithm da, List<Node> nodes) {
        Node result = null;
        double min = Double.MAX_VALUE;
        for (Node n : nodes) {
            double distance = distanceBetweenStartAndTargetNode(da, n);
            if (distance < min) {
                result = n;
                min = distance;
            }
        }
        return result;
    }

    private double distanceBetweenStartAndTargetNode(DijkstraAlgorithm da, Node target) {
        return da.getShortestDistance(new DijkstraNode(target));
    }

    @Override
    public LocationRequest getLocationRequest() {
        return this.locationRequest;
    }

    @Override
    public Node getLastLocation() {
        return this.location;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (this.gpsUpdatesRunning) {
            if (ActivityCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            //noinspection deprecation
            LocationServices.FusedLocationApi.requestLocationUpdates(this.googleApiClient, this.locationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        //TODO Maybe disable locating
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //TODO Notify somehow?
    }

    @Override
    public void onLocationChanged(Location location) {
        this.gpsLocation = location;
    }

    @Override
    public void processFinish(Fingerprint fingerprint, int seconds) {
        if (fingerprint != null) {
            LocationCalculator locationCalculator = LocationCalculatorFactory.createInstance(this.context);
            final String foundNode = locationCalculator.calculateNodeId(fingerprint);

            if (foundNode != null) {
                boolean notify = this.location == null || !this.location.getId().equals(foundNode);
                this.location = databaseHandler.getNode(foundNode);
                if (this.location != null) {
                    //TODO Test plausability using gps?
                    if (notify) {
                        notifyListeners(LocationSource.WIFI_FINGERPRINT);
                    }
                    this.timestamp = System.currentTimeMillis();
                }
            }
        }
        else {
            fallbackToGPS();
        }
    }

    private synchronized void notifyListeners(LocationSource source) {
        if (location!=null) {
            //Toast.makeText(this.context, "LocUpdate :" + this.location.getId(), Toast.LENGTH_SHORT).show();
        }
        for (LocationChangeListener listener : this.listeners) {
            listener.onLocationChanged(this.location, source);
        }
    }

    private void fallbackToGPS() {
        //TODO Try to set Node based on GPS, remember to update timestamp and notify listeners
        if (this.gpsLocation != null) {
            List<Node> allNodes = databaseHandler.getAllNodes();
            Node result = null;
            double distance = Double.NaN;
            for (Node n : allNodes) {
                GlobalNode temp = GlobalNodeFactory.createInstance(n);
                if (temp.hasGlobalCoordinates()) {
                    float[] results = new float[1];
                    Location.distanceBetween(this.gpsLocation.getLatitude(), this.gpsLocation.getLongitude(), temp.getLatitude(), temp.getLongitude(), results);
                    if (results[0] <= GPS_LOCATION_RADIUS_METERS) {
                        if (result == null || distance > results[0]) {
                            result = n;
                            distance = results[0];
                        }
                    }
                }
            }
            if (result != null) {
                this.timestamp = System.currentTimeMillis();
                boolean notify = this.location == null || !this.location.getId().equals(result.getId());
                this.location = result;
                if (notify) {
                    notifyListeners(LocationSource.GOOGLE_PLAY_SERVICES_FUSED_LOCATION_API);
                }
                return;
            }
        }
        // No Location found, set location to null, if ttl has run out
        long deltaT = System.currentTimeMillis() - timestamp;
        if ((this.timestamp != -1 && deltaT > MAXIMUM_LOCATION_AGE_MILLIS) || this.timestamp == -1) {
            this.timestamp = -1;
            boolean notify = this.location != null;
            this.location = null;
            if (notify) {
                notifyListeners(LocationSource.GOOGLE_PLAY_SERVICES_FUSED_LOCATION_API);
            }
        }
        else if (deltaT < 0) {
            Log.wtf("CombinedLocator", "Negative deltaT! This shouldn't happen, did the system time change?");
            this.timestamp = System.currentTimeMillis();
        }
    }

    private void startWiFiLocationUpdates() {
        this.handler = new Handler();

        this.fingerprintRepeater = new Runnable(){
            public void run(){
                //TODO Add filter string to constructor or make it settable
                String filterString = null;
                if (WIFI_FINGERPRINT_DURATION >= WIFI_FINGERPRINT_REPEAT_DELAY) {
                    Log.w("CombinedLocator", "WARNING! The recording duration for fingerprints is bigger than the delay between recordings. This may lead to unexpected behaviour and StackOverflow-Errors.");
                }
                FingerprintTask fingerprintTask = new FingerprintTask(filterString, WIFI_FINGERPRINT_DURATION / 1000, wifiManager, true, null, null, null);
                fingerprintTask.delegate = CombinedLocator.this;
                fingerprintTask.execute();
                handler.postDelayed(this, WIFI_FINGERPRINT_REPEAT_DELAY);
            }
        };

        this.handler.postDelayed(this.fingerprintRepeater, WIFI_FINGERPRINT_REPEAT_DELAY);
    }

    private void stopWiFiLocationUpdates() {
        if (this.handler != null) {
            this.handler.removeCallbacks(this.fingerprintRepeater);
        }
    }

    private static LocationRequest getDefaultLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    @Override
    public void setContext(Context context){
        this.context = context;
    }
}

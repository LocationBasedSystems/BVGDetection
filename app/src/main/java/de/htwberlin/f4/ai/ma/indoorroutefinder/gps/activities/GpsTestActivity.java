package de.htwberlin.f4.ai.ma.indoorroutefinder.gps.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import de.htwberlin.f4.ai.ma.indoorroutefinder.R;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.BaseActivity;
import de.htwberlin.f4.ai.ma.indoorroutefinder.gps.node.GlobalNode;
import de.htwberlin.f4.ai.ma.indoorroutefinder.gps.node.GlobalNodeFactory;
import de.htwberlin.f4.ai.ma.indoorroutefinder.gps.updaters.GlobalCoordinateUpdater;
import de.htwberlin.f4.ai.ma.indoorroutefinder.gps.updaters.GlobalCoordinateUpdaterFactory;
import de.htwberlin.f4.ai.ma.indoorroutefinder.location.locator.LocationSource;
import de.htwberlin.f4.ai.ma.indoorroutefinder.location.locator.Locator;
import de.htwberlin.f4.ai.ma.indoorroutefinder.location.locator.LocatorFactory;
import de.htwberlin.f4.ai.ma.indoorroutefinder.location.locator.listeners.LocationChangeListener;
import de.htwberlin.f4.ai.ma.indoorroutefinder.node.Node;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandler;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandlerFactory;

public class GpsTestActivity extends BaseActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, LocationChangeListener {

    private DatabaseHandler databaseHandler;
    private List<Node> allNodes;

    private Spinner nodeSpinner;
    private TextView txtName;
    private TextView txtCoords;
    private TextView txtAccuracy;
    private TextView txtGpsCoords;
    private TextView txtGpsAccuracy;
    private TextView txtGpsDistance;

    private GoogleApiClient googleApiClient;
    private Location gpsLocation;

    private Locator locator;
    private Node nodeLocation;

    private ArrayList<String> itemsNodeSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout contentFrameLayout = (FrameLayout) findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_gps_test, contentFrameLayout);

        // Google API Client (for GPS)
        this.googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        this.googleApiClient.connect();

        databaseHandler = DatabaseHandlerFactory.getInstance(this);
        allNodes = databaseHandler.getAllNodes();
        itemsNodeSpinner = new ArrayList<>();

        nodeSpinner = (Spinner) findViewById(R.id.gps_node_spinner);
        txtName = (TextView) findViewById(R.id.txtNodeName);
        txtCoords = (TextView) findViewById(R.id.txtNodeCoordinates);
        txtAccuracy = (TextView) findViewById(R.id.txtNodeAccuracy);
        txtGpsCoords = (TextView) findViewById(R.id.txtGpsCoords);
        txtGpsAccuracy = (TextView) findViewById(R.id.txtGpsAccuracy);
        txtGpsDistance = (TextView) findViewById(R.id.txtGpsDistance);

        for (Node node : allNodes) {
            itemsNodeSpinner.add(node.getId());
        }

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, itemsNodeSpinner);
        nodeSpinner.setAdapter(adapter);

        nodeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateUI();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                txtName.setText("Nothing selected!");
                txtCoords.setText("None");
                txtAccuracy.setText("-");
            }
        });

        this.locator = LocatorFactory.getInstance(this);
        this.locator.registerLocationListener(this);
        this.locator.startLocationUpdates();
    }

    public void updateGpsPositions(View view) {
        GlobalCoordinateUpdater updater = GlobalCoordinateUpdaterFactory.getInstance(this);
        updater.updateGlobalCoordinates();

        itemsNodeSpinner.clear();
        allNodes = databaseHandler.getAllNodes();
        for (Node node : allNodes) {
            itemsNodeSpinner.add(node.getId());
        }

        updateUI();
    }

    private void updateUI() {
        GlobalNode selected = GlobalNodeFactory.createInstance(databaseHandler.getNode(itemsNodeSpinner.get(nodeSpinner.getSelectedItemPosition())));
        if (selected != null) {
            txtName.setText(selected.getId());
            if (this.gpsLocation != null) {
                this.txtGpsCoords.setText("lat.: " + gpsLocation.getLatitude() + "°; long.: " + gpsLocation.getLongitude() + "°");
                this.txtGpsAccuracy.setText("+/-" + gpsLocation.getAccuracy() + "m");
            }
            if (selected.hasGlobalCoordinates()) {
                txtCoords.setText("lat.: " + selected.getLatitude() + "°; long.: " + selected.getLongitude() + "°");
                txtAccuracy.setText("+/-" + selected.getGlobalCalculationInaccuracyRating() + "m");
                if (this.gpsLocation != null) {
                    this.txtGpsDistance.setText(this.gpsLocation.distanceTo(selected.getLocation()) + "m");
                }
            } else {
                txtCoords.setText("None");
                txtAccuracy.setText("-");
                this.txtGpsDistance.setText("Unavailable");
            }
        } else {
            txtName.setText("NULL!");
            txtCoords.setText("None");
            txtAccuracy.setText("-");
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(100);
        locationRequest.setFastestInterval(100);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: No Perms granted
            Toast.makeText(this, "Unable to fetch position data!", Toast.LENGTH_LONG);
            return;
        }
        else {
            //noinspection deprecation
            LocationServices.FusedLocationApi.requestLocationUpdates(this.googleApiClient, locationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //TODO Failed to connect GoogleApiClient, GPS stays disabled
        Toast.makeText(this, "Failed to connect Google API Client. GPS not available.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        this.gpsLocation = location;
        updateUI();
    }

    @Override
    public void onLocationChanged(Node newLocation, LocationSource source) {
        this.nodeLocation = newLocation;
        if (this.nodeLocation != null) {
            Log.d("NEUER ORT: ", this.nodeLocation.getId());
        }
    }
}

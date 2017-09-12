package de.htwberlin.f4.ai.ma.android.measure;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.example.carol.bvg.R;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.htwberlin.f4.ai.ma.android.BaseActivity;
import de.htwberlin.f4.ai.ma.android.calibrate.CalibratePersistance;
import de.htwberlin.f4.ai.ma.android.calibrate.CalibratePersistanceImpl;
import de.htwberlin.f4.ai.ma.android.measure.barcode.BarcodeCaptureActivity;
import de.htwberlin.f4.ai.ma.android.measure.edges.StepData;
import de.htwberlin.f4.ai.ma.android.sensors.Sensor;
import de.htwberlin.f4.ai.ma.android.sensors.SensorChecker;
import de.htwberlin.f4.ai.ma.android.sensors.SensorCheckerImpl;
import de.htwberlin.f4.ai.ma.android.sensors.SensorData;
import de.htwberlin.f4.ai.ma.android.sensors.SensorDataModel;
import de.htwberlin.f4.ai.ma.android.sensors.SensorDataModelImpl;
import de.htwberlin.f4.ai.ma.android.sensors.SensorListener;
import de.htwberlin.f4.ai.ma.android.sensors.SensorType;
import de.htwberlin.f4.ai.ma.edge.EdgeFactory;
import de.htwberlin.f4.ai.ma.fingerprint.FingerprintFactory;
import de.htwberlin.f4.ai.ma.fingerprint.accesspointsample.AccessPointSampleFactory;
import de.htwberlin.f4.ai.ma.location.location_calculator.LocationCalculator;
import de.htwberlin.f4.ai.ma.location.location_calculator.LocationCalculatorFactory;
import de.htwberlin.f4.ai.ma.measurement.IndoorMeasurement;
import de.htwberlin.f4.ai.ma.measurement.IndoorMeasurementFactory;
import de.htwberlin.f4.ai.ma.measurement.IndoorMeasurementType;

import de.htwberlin.f4.ai.ma.measurement.WKT;
import de.htwberlin.f4.ai.ma.measurement.modules.stepdirection.StepDirection;
import de.htwberlin.f4.ai.ma.measurement.modules.stepdirection.StepDirectionDetectListener;
import de.htwberlin.f4.ai.ma.edge.Edge;
import de.htwberlin.f4.ai.ma.node.Node;
import de.htwberlin.f4.ai.ma.fingerprint.SignalInformation;
import de.htwberlin.f4.ai.ma.fingerprint.accesspointsample.AccessPointSample;
import de.htwberlin.f4.ai.ma.node.NodeFactory;
import de.htwberlin.f4.ai.ma.persistence.DatabaseHandler;
import de.htwberlin.f4.ai.ma.persistence.DatabaseHandlerFactory;
import de.htwberlin.f4.ai.ma.node.MaxPictureActivity;
import de.htwberlin.f4.ai.ma.location.calculations.FoundNode;

/**
 * MeasureControllerImpl Class which implements the MeasureController Interface
 *
 * used for measuring the distance / calculate coordinates of nodes
 *
 * Author: Benjamin Kneer
 */

public class MeasureControllerImpl implements MeasureController {

    // timeperiod in ms for airpressure calibration
    private static final int CALIBRATION_TIME = 3000;

    private MeasureView view;
    private IndoorMeasurement indoorMeasurement;
    private Handler timerHandler;
    private MeasureCalibration pressureCalibration;
    private SensorDataModel sensorDataModel;
    private boolean calibrated;
    private AlertDialog calibrationDialog;
    private int stepCount;
    // edgedistance in m
    private float edgeDistance;

    private IndoorMeasurementType measurementType;
    private float lowpassFilterValue;
    private boolean useStepDirectionDetect;
    private float barometerThreshold;
    // DONT CHANGE IT AFTER LOADING CALIBRATION
    // because it's passed to IndoorMeasurement with settings
    private CalibrationData calibrationData;

    private Node startNode;
    private Node targetNode;
    // node retrieved from wifi
    private Node measuredNode;

    private List<StepData> stepList;
    private float[] coords = new float[3];
    private boolean handycapFriendly;

    // to deactivate wifi / qr code localization during measurement
    private boolean measurementRunning;


    /************************************************************************************
    *                                                                                   *
    *                               Activity Events                                     *
    *                                                                                   *
    *************************************************************************************/


    @Override
    public void onPause() {
        // stop all sensors
        if (indoorMeasurement != null) {
            indoorMeasurement.stop();
        }
        measurementRunning = false;
    }


    @Override
    public void onResume() {
        // check for selected nodes
        handleNodeSelection(startNode, targetNode);
        // get settings from default sharedpreferences and store it for later usage
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(view.getContext());
        lowpassFilterValue = Float.valueOf(sharedPreferences.getString("pref_lowpass_value", "0.1"));
        String type = sharedPreferences.getString("pref_measurement_type", "Variante B");
        measurementType = IndoorMeasurementType.fromString(type);
        useStepDirectionDetect = sharedPreferences.getBoolean("pref_stepdirection", false);
        barometerThreshold = Float.valueOf(sharedPreferences.getString("pref_barometer_threshold", "0.14"));
        // load steplength and stepperiod calibration
        CalibratePersistance calibratePersistance = new CalibratePersistanceImpl(view.getContext());
        calibrationData = calibratePersistance.load();
    }


    /************************************************************************************
    *                                                                                   *
    *                               Interface Methods                                   *
    *                                                                                   *
    *************************************************************************************/


    /**
     * set the responsible view
     *
     * @param view MeasureView
     */
    @Override
    public void setView(MeasureView view) {
        this.view = view;
    }


    /**
     * triggered by clicking start button.
     * initialize values, create IndoorMeasurement component,
     * start sensors, register listeners
     */
    @Override
    public void onStartClicked() {
        measurementRunning = true;

        // check for calibration
        // if not calibrated yet show dialog
        if (!alreadyCalibrated()) {
            showStepCalibrationRequiredDialog();
            return;
        }

        // check if every sensors required is available
        if (!sensorsAvailable(measurementType)) {
            showSensorsNotAvailableDialog();
            return;
        }

        // initialize values
        stepList = new ArrayList<>();
        stepCount = 0;
        edgeDistance = 0f;
        coords = new float[3];
        calibrated = false;
        sensorDataModel = new SensorDataModelImpl();
        indoorMeasurement = IndoorMeasurementFactory.getIndoorMeasurement(view.getContext());
        // initialize view
        view.updateStepCount(stepCount);
        view.updateDistance(edgeDistance);

        // check if the startnode already got coordinates
        if (startNode.getCoordinates().length() > 0) {
            coords = WKT.strToCoord(startNode.getCoordinates());
            // update coordinates in view
            view.updateCoordinates(coords[0], coords[1], coords[2]);
        } else {
            view.updateCoordinates(0.0f, 0.0f, 0.0f);
        }

        // set sensor listeners
        indoorMeasurement.setSensorListener(new SensorListener() {
            @Override
            public void valueChanged(SensorData sensorData) {
                SensorType sensorType = sensorData.getSensorType();
                switch (sensorType) {

                    case COMPASS_FUSION:
                        // update the compass with new value
                        view.updateAzimuth(sensorData.getValues()[0]);
                        break;

                    case COMPASS_SIMPLE:
                        // update the compass with new value
                        view.updateAzimuth(sensorData.getValues()[0]);
                        break;

                    case BAROMETER:
                        // store barometer data in model, while calibration
                        // isn't finished
                        if (!calibrated) {
                            sensorDataModel.insertData(sensorData);
                        }
                        break;

                    case STEP_DETECTOR:
                        handleNewStep();
                        // fix first step bug...
                        // first step won't get recognized by sensor so we just fake the step
                        // and assume the user walks atleast for the two first steps in  the
                        // same direction
                        if (stepCount == 1) {
                            handleNewStep();
                        }
                        break;
                    default:
                        break;
                }
            }
        });

        // set listener for stepdirection module
        indoorMeasurement.setStepDirectionListener(new StepDirectionDetectListener() {
            @Override
            public void onDirectionDetect(final StepDirection stepDirection) {
                showStepDirectionDialog(stepDirection);
            }
        });

        // determine which compasstype we are gonna use (compassFusion / compassSimple)
        SensorType compassType = null;
        // VARIANT_A and VARIANT_B is using COMPASS_FUSION
        if (measurementType == IndoorMeasurementType.VARIANT_A || measurementType == IndoorMeasurementType.VARIANT_B) {
            compassType = SensorType.COMPASS_FUSION;
        }
        // VARIANT_C and VARIANT_D is using COMPASS_SIMPLE
        else if (measurementType == IndoorMeasurementType.VARIANT_C || measurementType == IndoorMeasurementType.VARIANT_D) {
            compassType = SensorType.COMPASS_SIMPLE;
        }

        // start barometer sensor for upcoming airpressure calibration
        indoorMeasurement.startSensors(Sensor.SENSOR_RATE_MEASUREMENT,
                                       SensorType.BAROMETER);

        // start compass sensor with ui delay
        indoorMeasurement.startSensors(Sensor.SENSOR_RATE_UI, compassType);

        // start airpressure calibration
        calibrate();
        // show calibration dialog
        showWaitForCalibrationDialoag();
    }


    /**
     * triggered by clicking stop button
     *
     * stop sensors and show finish dialog
     */
    @Override
    public void onStopClicked() {
        if (indoorMeasurement != null) {
            indoorMeasurement.stop();
        }

        if (coords != null) {
            showMeasureFinishDialog();
        }

        measurementRunning = false;
    }


    /**
     * triggered by clicking step button
     *
     * in case a step was missed by sensor, the user can manually add a step
     */
    @Override
    public void onStepClicked() {
        handleNewStep();
    }


    /**
     * triggered when user selects a start node
     *
     * check if start and target node are not identical,
     * deactive start / stop button if needed
     *
     * @param node selected start node
     */
    @Override
    public void onStartNodeSelected(Node node) {
        startNode = node;
        handleNodeSelection(startNode, targetNode);
    }


    /**
     * triggered when user selects a target node
     *
     * check if start and target node are not identical,
     * deactive start / stop button if needed
     *
     * @param node selected target node
     */
    @Override
    public void onTargetNodeSelected(Node node) {
        targetNode = node;
        handleNodeSelection(startNode, targetNode);
    }


    /**
     * triggered by clicking on the arrow between start and target node
     *
     * if there is an existing edge between the nodes, load the EdgeDetailsView
     */
    @Override
    public void onEdgeDetailsClicked() {
        DatabaseHandler databaseHandler = DatabaseHandlerFactory.getInstance(view.getContext());
        Edge edge = EdgeFactory.createInstance(startNode, targetNode, false, 0);
        // check if edge between nodes exists
        if (databaseHandler.checkIfEdgeExists(edge)) {
            // load EdgeDetails View
            BaseActivity activity = (BaseActivity) view;
            activity.loadEdgeDetails(startNode.getId(), targetNode.getId());
        }
    }


    /**
     * triggered by clicking on the startnode image
     *
     * open view with fullscreen picture of the start node
     */
    @Override
    public void onStartNodeImageClicked() {
        if (startNode != null && startNode.getPicturePath() != null) {
            // using activity from johann
            Intent intent = new Intent(view.getContext(), MaxPictureActivity.class);
            intent.putExtra("picturePath", startNode.getPicturePath());
            BaseActivity activity = (BaseActivity) view;
            activity.startActivity(intent);
        }
    }


    /**
     * triggered by clicking on the targetnode image
     *
     * open view with fullscreen picture of the target node
     */
    @Override
    public void onTargetNodeImageClicked() {
        if (targetNode != null && targetNode.getPicturePath() != null) {
            // using activity from johann
            Intent intent = new Intent(view.getContext(), MaxPictureActivity.class);
            intent.putExtra("picturePath", targetNode.getPicturePath());
            BaseActivity activity = (BaseActivity) view;
            activity.startActivity(intent);
        }
    }


    /**
     * triggered by clicking on wifi icon
     *
     * Open dialog with all available wifi networks to choose from.
     * Try to find a location / node using wifi fingerprinting
     */
    @Override
    public void onLocateWifiClicked() {
        // if the measurement isnt active
        if (!measurementRunning) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(view.getContext());
            alertDialogBuilder.setTitle("Startposition");
            alertDialogBuilder.setMessage("Wollen Sie die Startposition per WLAN ermitteln?");
            alertDialogBuilder.setCancelable(true);
            alertDialogBuilder.setIcon(R.drawable.locate_wifi);

            // ask user if he really wants to find a location using wifi fingerprinting
            alertDialogBuilder.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // scan for available wifi networks
                    WifiManager wifiManager = (WifiManager) view.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    wifiManager.startScan();
                    List<ScanResult> wifiScanList = wifiManager.getScanResults();

                    // store wifi ssids
                    final ArrayList<String> wifiNamesList = new ArrayList<>();
                    for (ScanResult sr : wifiScanList) {
                        if (!wifiNamesList.contains(sr.SSID) && !sr.SSID.equals("")) {
                            wifiNamesList.add(sr.SSID);
                        }
                    }

                    // fill array with wifi ssids
                    final CharSequence wifiArray[] = new CharSequence[wifiNamesList.size()-1];
                    for (int i = 0; i < wifiArray.length; i++) {
                        wifiArray[i] = wifiNamesList.get(i);
                    }

                    // create dialog with found wifis
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setTitle("WLAN wählen");
                    builder.setItems(wifiArray, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getMeasuredNode(wifiNamesList.get(which), 3);
                        }
                    });
                    builder.show();
                }
            });

            alertDialogBuilder.setNegativeButton("Nein", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }


    /**
     * triggered by clicking on QR icon
     *
     * try to find a location / node using a qr code.
     * this qr code has to contain a valid json string with node id
     * and coordinates
     */
    @Override
    public void onLocateQrClicked() {
        // if there is no active measurement
        if (!measurementRunning) {
            // create dialog
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(view.getContext());
            alertDialogBuilder.setTitle("Startposition");
            alertDialogBuilder.setMessage("Wollen Sie die Startposition per QR-Code ermitteln?");
            alertDialogBuilder.setCancelable(true);
            alertDialogBuilder.setIcon(R.drawable.locate_qr);
            // if the user wants to find location using qr code
            alertDialogBuilder.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Intent intent = new Intent(view.getContext().getApplicationContext(), BarcodeCaptureActivity.class);
                    Activity activity = (Activity) view;
                    activity.startActivityForResult(intent, 1);
                }
            });
            // cancel
            alertDialogBuilder.setNegativeButton("Nein", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }


    /**
     * triggered when qr code is recognized
     *
     * the qr code should contain a valid json string like:
     *
     * {"id": "nodeid","coordinates": "POINT Z(0.1 0.2 0.3)"}
     *
     * @param qr qr code content
     */
    @Override
    public void onQrResult(String qr) {
        try {
            JSONObject jsonObject = new JSONObject(qr);
            // find id
            String id = jsonObject.getString("id");
            // find coordinates
            String coordinates = jsonObject.getString("coordinates");

            DatabaseHandler databaseHandler = DatabaseHandlerFactory.getInstance(view.getContext());

            // check if node from qr code already exists
            Node node = databaseHandler.getNode(id);

            // node exists
            if (node != null) {
                // update existing node with coords from qr code
                node.setCoordinates(coordinates);
                databaseHandler.updateNode(node, node.getId());
                // update ui
                view.setStartNode(node);
            }
            // new node
            else {
                // create a new node
                node = NodeFactory.createInstance(id, null, null, coordinates, null, null);
                // save the node into database
                databaseHandler.insertNode(node);
                // update ui
                view.setStartNode(node);
            }

        } catch (JSONException e) {
            Toast toast = Toast.makeText(view.getContext(), "Ungültiger QR-Code", Toast.LENGTH_SHORT);
            toast.show();
        }

    }


    /**
     * triggered when user marks the startnode as nullpoint
     *
     * thats required because it's not allowed to change the coordinates
     * of a nullpoint node or start a measurement between two
     * nullpoint nodes (each train station got 1 nullpoint and usually
     * they are not connected through walking)
     *
     * @param checked nullpoint yes / no
     */
    @Override
    public void onNullpointCheckedStartNode(boolean checked) {

        DatabaseHandler databaseHandler = DatabaseHandlerFactory.getInstance(view.getContext());
        if (startNode != null) {
            if (checked) {
                // store nullpoint flag in additional info field
                startNode.setAdditionalInfo("NULLPOINT");
            } else {
                startNode.setAdditionalInfo("");
            }
            databaseHandler.updateNode(startNode, startNode.getId());
        }
    }


    /**
     * triggered by stairs switch
     *
     * tells the indoormeasurement component if the next step is
     * a stair or not. depending on that, the distance is calculated
     *
     * @param checked stairs yes / no
     */
    @Override
    public void onStairsToggle(boolean checked) {
        if (calibrationData != null) {
            calibrationData.setStairs(checked);
        }
    }


    /************************************************************************************
    *                                                                                   *
    *                               Class Methods                                       *
    *                                                                                   *
    *************************************************************************************/


    /**
     * from johann, modifed
     *
     * find a node from a wifi ssid using wifi fingerprinting
     *
     * @param wlanName wifi ssid
     * @param times how often we should scan
     */
    private void getMeasuredNode(final String wlanName, final int times) {
        // get the wifi manager
        WifiManager wifiManager = (WifiManager) view.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

        // scan
        Multimap<String, Integer> multiMap = ArrayListMultimap.create();
        for (int i = 0; i < times; i++) {

            wifiManager.startScan();

            List<ScanResult> wifiScanList = wifiManager.getScanResults();

            if(wifiScanList.get(0).timestamp == 0 && times == 1) {
                return;
            }


            for (final ScanResult sr : wifiScanList) {
                if (sr.SSID.equals(wlanName)) {
                    multiMap.put(sr.BSSID, sr.level);
                }
            }

            wifiScanList.clear();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // find node
        Node node = makeFingerprint(multiMap);
        // if we found a node
        if (node != null) {
            measuredNode = node;
            Toast toast = Toast.makeText(view.getContext(), "Ort gefunden: " + node.getId(), Toast.LENGTH_SHORT);
            toast.show();
            view.setStartNode(measuredNode);
        }
        // if we didnt find a node
        else {
            Toast toast = Toast.makeText(view.getContext(), "Es wurde kein Ort gefunden", Toast.LENGTH_SHORT);
            toast.show();
        }
    }



    /**
     * from johann, modified
     *
     * get a node by fingerprint
     *
     * @param multiMap map with scanresults
     * @return matching node
     */
    private Node makeFingerprint(Multimap<String, Integer> multiMap) {
        Set<String> bssid = multiMap.keySet();
        DatabaseHandler databaseHandler = DatabaseHandlerFactory.getInstance(view.getContext());
        final List<Node> actuallyNode = new ArrayList<>();
        final List<SignalInformation> signalInformationList = new ArrayList<>();

        for (String s : bssid) {
            int value = 0;
            int counter = 0;

            for (int test : multiMap.get(s)) {
                counter++;
                value += test;
            }
            value = value / counter;

            List<AccessPointSample> SsiList = new ArrayList<>();
            AccessPointSample signal = AccessPointSampleFactory.createInstance(s, value);
            SsiList.add(signal);
            SignalInformation signalInformation = new SignalInformation("", SsiList);
            signalInformationList.add(signalInformation);

        }

        LocationCalculator locationCalculator = LocationCalculatorFactory.createInstance(view.getContext());
        FoundNode foundNode = locationCalculator.calculateNodeId(FingerprintFactory.createInstance("", signalInformationList));
        Node result = null;
        if (foundNode != null) {
            result = databaseHandler.getNode(foundNode.getId());
        }

        return result;
    }


    /**
     * Method for handling a new step registered by the step_detector sensor
     *
     * calculate distance, save step data informations and update ui
     *
     */
    private void handleNewStep() {
        // with each step we get the new position
        stepCount++;
        // get coordinates from indoormeasurement component and convert WKT String back to float[]
        float[] newStepCoords = WKT.strToCoord(indoorMeasurement.getCoordinates());
        if (newStepCoords != null) {
            // calculate distance from previous step to new step
            float stepDistance = calcDistance(coords[0], coords[1], coords[2], newStepCoords[0], newStepCoords[1], newStepCoords[2]);
            // update overall distance
            edgeDistance += stepDistance;
            // update ui
            view.updateDistance(edgeDistance);
            view.updateCoordinates(newStepCoords[0], newStepCoords[1], newStepCoords[2]);
            // save step data for later usage
            StepData stepData = new StepData();
            stepData.setStepName("Step " + stepCount);
            coords = new float[newStepCoords.length];
            System.arraycopy(newStepCoords, 0, coords, 0, newStepCoords.length);
            stepData.setCoords(newStepCoords);
            stepList.add(stepData);
        }
        // update stepcount view
        view.updateStepCount(stepCount);
    }


    /**
     * handle the selection of start / target node from dropdown in view
     *
     * update view, enable / disable buttons if required
     *
     * @param start start node
     * @param target target node
     */
    private void handleNodeSelection(Node start, Node target) {
        // check if startnode and targetnode was selected
        if (start != null && target != null) {
            // update coordinates of startnode
            if (start.getCoordinates().length() > 0) {
                float[] coordinates = WKT.strToCoord(start.getCoordinates());
                view.updateStartNodeCoordinates(coordinates[0], coordinates[1], coordinates[2]);
            } else {
                view.updateStartNodeCoordinates(0f, 0f, 0f);
            }
            // update coordinates of targetnode
            if (target.getCoordinates().length() > 0) {
                float[] coordinates = WKT.strToCoord(target.getCoordinates());
                view.updateTargetNodeCoordinates(coordinates[0], coordinates[1], coordinates[2]);
            } else {
                view.updateTargetNodeCoordinates(0f, 0f, 0f);
            }

            // check if start and targetnode are different, if yes we have to update edge information
            // and enable measurement start
            boolean different = checkNodesDifferent(start, target);
            if (different) {
                // make sure that both nodes aren't nullpoints, because its not allowed to measure between two nullpoints
                if ((start.getAdditionalInfo().contains("NULLPOINT") && !target.getAdditionalInfo().contains("NULLPOINT")) ||
                        (!start.getAdditionalInfo().contains("NULLPOINT") && target.getAdditionalInfo().contains("NULLPOINT")) ||
                        (!start.getAdditionalInfo().contains("NULLPOINT") && !target.getAdditionalInfo().contains("NULLPOINT"))) {
                    view.enableStart();

                    DatabaseHandler databaseHandler = DatabaseHandlerFactory.getInstance(view.getContext());

                    // check if edge already exists
                    Edge existingEdge = databaseHandler.getEdge(startNode, targetNode);
                    if (existingEdge != null) {
                        // if we found the correct edge update view with correct data
                        view.updateEdge(existingEdge);
                    }
                    // if there isn't an edge, update view with placeholder data
                    else {
                        Edge placeHolderEdge = EdgeFactory.createInstance(null, null, true, 0);
                        view.updateEdge(placeHolderEdge);
                    }
                }
                // if both nodes are nullpoints disable the start button
                else {
                    view.disableStart();
                }
            }
            // if start and targetnode are equal just update the edge with placeholder and disable start button
            else {
                view.disableStart();
                Edge placeHolderEdge = EdgeFactory.createInstance(null, null, true, 0);
                view.updateEdge(placeHolderEdge);
            }
        }
    }


    /**
     * check if start and targetnodes are different depending on node id
     *
     * @param node1 start node
     * @param node2 target node
     * @return
     */
    private boolean checkNodesDifferent(Node node1, Node node2) {
        // since id is unique, we just check for id
        if (!node1.getId().equals(node2.getId())) {
            return true;
        }
        return false;
    }


    /**
     * calibrate the airpressure, calculate average.
     *
     * measurement and calculation is done using a thread
     *
     * could be improved usign AsyncTask
     *
     */
    private void calibrate() {
        // create new thread handler and calibration runnable
        timerHandler = new Handler(Looper.getMainLooper());
        pressureCalibration = new MeasureCalibration(sensorDataModel);

        // set listener
        pressureCalibration.setListener(new MeasureCalibrationListener() {
            @Override
            public void onFinish(float airPressure) {
                calibrationDialog.dismiss();
                // stop thread
                timerHandler.removeCallbacks(pressureCalibration);
                calibrated = true;

                if (calibrationData != null) {
                    // get coordinates from startnode and initialize measurement with those.
                    // if the node doesn't have any coordinates we initialize with 0,0,0
                    String startCoordinatesStr = startNode.getCoordinates();
                    if (startCoordinatesStr != null && startCoordinatesStr.length() > 0) {
                        float[] startCoordinates = WKT.strToCoord(startCoordinatesStr);

                        calibrationData.setCoordinates(startCoordinates);
                    }

                    // set settings loaded from defaultsharedpreferences
                    calibrationData.setIndoorMeasurementType(measurementType);
                    calibrationData.setLowpassFilterValue(lowpassFilterValue);
                    calibrationData.setUseStepDirection(useStepDirectionDetect);
                    calibrationData.setBarometerThreshold(barometerThreshold);
                    // save new calibrated airpressure
                    calibrationData.setAirPressure(airPressure);
                    // calibrate the indoormeasurement
                    indoorMeasurement.calibrate(calibrationData);
                    // start step detector with 0 delay
                    indoorMeasurement.startSensors(Sensor.SENSOR_RATE_FASTEST,
                            SensorType.STEP_DETECTOR);
                    // start measurement
                    indoorMeasurement.start();
                }
            }
        });

        // start thread with delay
        // we use 3 seconds for default, so the calibration runnable can gather enough data
        timerHandler.postDelayed(pressureCalibration, CALIBRATION_TIME);
    }


    /**
     * check if steplength and period is calibrated.
     * if not, show dialog and load the CalibrateView
     *
     * @return calibrated yes / no
     */
    private boolean alreadyCalibrated() {
        CalibratePersistance calibratePersistance = new CalibratePersistanceImpl(view.getContext());
        return calibratePersistance.load() != null;
    }


    /**
     * calculate the distance from 2 points in 3 dimensions
     *
     * @param x1    x1 value
     * @param y1    y1 value
     * @param z1    z1 value
     * @param x2    x2 value
     * @param y2    y2 value
     * @param z2    z2 value
     * @return      distance
     */
    private float calcDistance(float x1, float y1, float z1, float x2, float y2, float z2) {
        return (float) Math.sqrt(Math.pow((x1-x2), 2) + Math.pow((y1-y2), 2) + Math.pow((z1-z2),2));
    }


    /**
     * check if all required sensors are available on the device
     *
     * @param indoorMeasurementType measurementtype
     * @return all sensors available / not available
     */
    private boolean sensorsAvailable(IndoorMeasurementType indoorMeasurementType) {
        SensorChecker sensorChecker = new SensorCheckerImpl(view.getContext());
        boolean result = sensorChecker.checkSensor(indoorMeasurementType);

        return result;
    }


    /**
     * Show Dialog if a required sensors is not available on the device
     */
    private void showSensorsNotAvailableDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(view.getContext());
        alertDialogBuilder.setTitle("Ungültige Messvariante");
        alertDialogBuilder.setMessage("Ein oder mehrere Sensor(en) werden von ihrem Gerät nicht unterstützt. Bitte wählen Sie eine andere Messvariante!");
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setIcon(R.drawable.error);

        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    /**
     * Show a dialog if steplength and stepperiod isnt calibrated yet
     */
    private void showStepCalibrationRequiredDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(view.getContext());
        alertDialogBuilder.setTitle("Kalibrierung notwendig");
        alertDialogBuilder.setMessage("Bitte führen Sie zuerst eine Schrittkalibrierung durch!");
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setIcon(R.drawable.error);

        // load calibration view
        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                BaseActivity activity = (BaseActivity) view;
                activity.loadCalibrate();
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    /**
     * Show a dialog to wait till calibration is done
     */
    private void showWaitForCalibrationDialoag() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(view.getContext());
        alertDialogBuilder.setMessage("Bitte warten");
        alertDialogBuilder.setTitle("Kalibrierung im gange");
        calibrationDialog = alertDialogBuilder.create();
        calibrationDialog.setCancelable(false);
        calibrationDialog.show();
    }


    /**
     * Show a dialog when the measurement is done.
     * Ask if the way was handycap friendly and save data afterwards
     */
    private void showMeasureFinishDialog() {
        // ask the user if the way was handycap friendly and save measurement data afterwards
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(view.getContext());
        alertDialogBuilder.setTitle("Barrierefreiheit");
        alertDialogBuilder.setMessage("War der zurückgelegte Weg barrierefrei?");
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setIcon(R.drawable.barrierefrei);

        alertDialogBuilder.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                handycapFriendly = true;
                saveMeasurementData();
                dialog.dismiss();
            }
        });

        alertDialogBuilder.setNegativeButton("Nein", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                handycapFriendly = false;
                saveMeasurementData();
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    /**
     * Dialog for detected Stepdirection
     *
     * When there is a step != forward, inform the user
     *
     * @param stepDirection detected stepdirection
     */
    private void showStepDirectionDialog(StepDirection stepDirection) {
        if (stepDirection != StepDirection.FORWARD) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(view.getContext());
            alertDialogBuilder.setTitle("Falsche Schrittrichtung");
            alertDialogBuilder.setMessage("Es wurde ein " + stepDirection + "schritt festgestellt. Falls dies so ist, starten Sie die Messung bitte neu");
            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.setIcon(R.drawable.error);

            // reset everything and start new measurement when there is an invalid direction
            alertDialogBuilder.setPositiveButton("Ja, das ist korrekt", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // reset
                    indoorMeasurement.stop();
                    stepList = new ArrayList<>();
                    stepCount = 0;
                    edgeDistance = 0f;
                    // update view
                    view.updateStepCount(stepCount);
                    view.updateDistance(edgeDistance);
                    view.disableStop();
                    view.disableAdd();
                    view.enableStart();

                    // check if the startnode already got coordinates
                    if (startNode.getCoordinates().length() > 0) {
                        coords = WKT.strToCoord(startNode.getCoordinates());
                        // update coordinates in view
                        view.updateCoordinates(coords[0], coords[1], coords[2]);
                    } else {
                        coords[0] = 0.0f;
                        coords[1] = 0.0f;
                        coords[2] = 0.0f;
                        view.updateCoordinates(0.0f, 0.0f, 0.0f);
                    }
                }
            });

            // ignore warning
            alertDialogBuilder.setNegativeButton("Nein, weiter", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }


    /**
     * save the measurement data like coordinates, edge details...
     */
    private void saveMeasurementData() {
        // make sure the target isnt the nullpoint, nullpoint coordinate change isnt allowed!
        // if its the nullpoint, we just save the edge data and dont update coordinates
        if (!targetNode.getAdditionalInfo().contains("NULLPOINT")) {
            targetNode.setCoordinates(WKT.coordToStr(coords));
            view.updateTargetNodeCoordinates(coords[0], coords[1], coords[2]);
        }

        List<String> stepCoords = new ArrayList<>();
        // convert coordinates to string and save into list for edge
        for (StepData stepData : stepList) {
            stepCoords.add(WKT.coordToStr(stepData.getCoords()));
        }

        DatabaseHandler databaseHandler = DatabaseHandlerFactory.getInstance(view.getContext());
        Edge edge = databaseHandler.getEdge(startNode, targetNode);
        boolean edgeFound;

        // if there is no edge between start and targetnode yet, we create a new one
        if (edge == null) {
            edge = EdgeFactory.createInstance(startNode, targetNode, handycapFriendly, stepCoords, 0, "");
            edgeFound = false;
        }
        // otherwise we update existing edge
        else {
            edge.setAccessibility(handycapFriendly);
            edge.getStepCoordsList().clear();
            edge.getStepCoordsList().addAll(stepCoords);
            edgeFound = true;
        }
        // set edge distance
        edge.setWeight(edgeDistance);
        // insert edge into db when there is no existing edge yet
        if (!edgeFound) {
            databaseHandler.insertEdge(edge);
        }
        // update edge if there was an existing edge
        else {
            databaseHandler.updateEdge(edge);
        }
        // update our targetnode
        databaseHandler.updateNode(targetNode, targetNode.getId());
        // update view with new edge data
        view.updateEdge(edge);
    }

    //todo: remove
    private void save() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File (sdCard.getAbsolutePath() + "/Coordinates/RecordData");
        if (!dir.exists()) {
            dir.mkdirs();
        }


        File file = new File(dir, "steps" + timestamp + ".txt");

        FileOutputStream outputStream;


        try {
            outputStream = new FileOutputStream(file);
            int i = 0;
            for (StepData stepData: stepList) {
                i++;
                StringBuilder builder = new StringBuilder();
                builder.append("Step " + i + ";" + stepData.getCoords()[0] + ";" + stepData.getCoords()[1] + ";" + stepData.getCoords()[2]);
                outputStream.write(builder.toString().getBytes());
                outputStream.write(System.lineSeparator().getBytes());
            }



            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

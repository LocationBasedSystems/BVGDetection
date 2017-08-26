package de.htwberlin.f4.ai.ma.android.sensors.compass;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.view.Surface;

import java.sql.Timestamp;

import de.htwberlin.f4.ai.ma.android.sensors.SensorData;
import de.htwberlin.f4.ai.ma.android.sensors.SensorListener;
import de.htwberlin.f4.ai.ma.android.sensors.SensorType;


/**
 * Created by benni on 23.07.2017.
 */

public class CompassSimple implements SensorEventListener, de.htwberlin.f4.ai.ma.android.sensors.Sensor{

    private static final SensorType SENSORTYPE = SensorType.COMPASS_SIMPLE;

    private SensorManager sensorManager;
    private SensorListener listener;
    private Sensor accelerometerSensor;
    private Sensor magneticFieldSensor;

    private float[] orientation;
    private float[] rotationMatrix;
    private float[] accelerometerValues;
    private float[] magneticValues;

    private float azimuth;
    private float pitch;
    private float roll;
    private SensorData sensorData;
    private int sensorRate;
    private Context context;


    public CompassSimple(Context context, int sensorRate) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensorData = new SensorData();
        sensorData.setSensorType(SENSORTYPE);
        orientation = new float[3];
        rotationMatrix = new float[16];
        accelerometerValues = new float[3];
        magneticValues = new float[3];
        this.sensorRate = sensorRate;
        this.context = context;
    }

    @Override
    public void start() {
        accelerometerSensor = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER);
        magneticFieldSensor = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_MAGNETIC_FIELD);

        if ((accelerometerSensor != null) && (magneticFieldSensor != null)) {
            sensorManager.registerListener(this, accelerometerSensor, sensorRate);
            sensorManager.registerListener(this, magneticFieldSensor, sensorRate);
        }
    }

    @Override
    public void stop() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this, accelerometerSensor);
            sensorManager.unregisterListener(this, magneticFieldSensor);
        }
    }

    @Override
    public SensorData getValues() {
        return sensorData;
    }


    @Override
    public boolean isSensorAvailable() {
        if ((sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER) == null) ||
                (sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_MAGNETIC_FIELD) == null)) {
            return false;
        }

        return true;
    }

    @Override
    public void setListener(SensorListener listener) {
        this.listener = listener;
    }

    @Override
    public SensorType getSensorType() {
        return SENSORTYPE;
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == android.hardware.Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(sensorEvent.values, 0, accelerometerValues, 0, sensorEvent.values.length);
        } else if (sensorEvent.sensor.getType() == android.hardware.Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(sensorEvent.values, 0, magneticValues, 0, sensorEvent.values.length);
        }

        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerValues, magneticValues);

        float[] remapped = new float[16];

        // handle portrait and landscape mode
        // source: https://stackoverflow.com/questions/18782829/android-sensormanager-strange-how-to-remapcoordinatesystem

        int screenRotation = ((Activity) context).getWindowManager().getDefaultDisplay().getRotation();
        int axisX = SensorManager.AXIS_X;
        int axisY = SensorManager.AXIS_Y;

        switch (screenRotation) {
            case Surface.ROTATION_0:
                axisX = SensorManager.AXIS_X;
                axisY = SensorManager.AXIS_Y;
                break;
            case Surface.ROTATION_90:
                axisX = SensorManager.AXIS_Y;
                axisY = SensorManager.AXIS_MINUS_X;
                break;
            case Surface.ROTATION_180:
                axisX = SensorManager.AXIS_MINUS_X;
                axisY = SensorManager.AXIS_MINUS_Y;
                break;
            case Surface.ROTATION_270:
                axisX = SensorManager.AXIS_MINUS_Y;
                axisY = SensorManager.AXIS_X;
                break;
            default:
                break;

        }
        // remap coordinate system according to screen orientation
        SensorManager.remapCoordinateSystem(rotationMatrix, axisX, axisY, remapped);

        // default we assume the phone is "laying" on its back in portrait mode, so the azimuth is calculcated
        // related to the default y axis of the phone (points out from the top edge)
        // for example: phone is laying on its back towards north -> shows 0°
        // spin phone while still laying on its back -> change in azimuth

        // original values are within [-180,180]
        orientation = SensorManager.getOrientation(remapped, orientation);

        azimuth = (float) (Math.toDegrees(orientation[0]) + 360) % 360;
        pitch = (float) (Math.toDegrees(orientation[1]));
        roll = (float) (Math.toDegrees(orientation[2]));

        // detect if the phone is "standing" (selfie camera on top or bottom edge) and screen is facing the user.
        // the normal camera on the backside of the phone points away from the user.
        // if yes, we have to remap the axis.
        // Now the azimuth is calculated relative to the camera(on the backside) / z axis of the phone.
        // we remap the earth coordinates z axis on the phones y axis and calculate the correct azimuth
        // IMPORTANT: SCREEN MUST POINT TOWARDS USER
        if (Math.abs(pitch) > 70) {
            SensorManager.remapCoordinateSystem(remapped, SensorManager.AXIS_X, SensorManager.AXIS_Z, remapped);
            orientation = SensorManager.getOrientation(remapped, orientation);
            azimuth = (float) (Math.toDegrees(orientation[0]) + 360) % 360;
            pitch = (float) (Math.toDegrees(orientation[1]));
            roll = (float) (Math.toDegrees(orientation[2]));
        }



        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        long realTimestamp = timestamp.getTime();
        float[] values = new float[]{azimuth, pitch, roll};

        long timeOffset = System.currentTimeMillis() - SystemClock.elapsedRealtime();
        long calcTimestamp = (sensorEvent.timestamp / 1000000L) + timeOffset;

        sensorData = new SensorData();
        sensorData.setSensorType(SENSORTYPE);
        sensorData.setTimestamp(calcTimestamp);
        sensorData.setValues(values);

        if (listener != null) {
            listener.valueChanged(sensorData);
        }
    }

    @Override
    public void onAccuracyChanged(android.hardware.Sensor sensor, int i) {

    }
}
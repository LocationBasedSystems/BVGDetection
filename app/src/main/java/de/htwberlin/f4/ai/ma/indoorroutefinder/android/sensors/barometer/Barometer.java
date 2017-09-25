package de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.barometer;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;

import java.sql.Timestamp;

import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.SensorData;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.SensorListener;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.SensorType;

/**
 * Barometer Class which implements the Sensor and SensorEventListener Interface
 *
 * Used Android Sensor: Sensor.TYPE_PRESSURE
 *
 * Author: Benjamin Kneer
 */

public class Barometer implements SensorEventListener, de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.Sensor {

    private static final SensorType SENSORTYPE = SensorType.BAROMETER;

    private SensorManager sensorManager;
    private SensorListener listener;

    private SensorData sensorData;
    private int sensorRate;


    public Barometer(Context context, int sensorRate) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensorData = new SensorData();
        sensorData.setSensorType(SENSORTYPE);
        this.sensorRate = sensorRate;
    }


    /************************************************************************************
    *                                                                                   *
    *                               Sensor Interface Methods                            *
    *                                                                                   *
    *************************************************************************************/


    /**
     * Get sensor from SensorManager and register Listener
     */
    @Override
    public void start() {
        Sensor barometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        if (barometerSensor != null) {
            sensorManager.registerListener(this, barometerSensor, sensorRate);
        }
    }


    /**
     * Unregister sensor listener
     */
    @Override
    public void stop() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }


    /**
     * Get the latest sensor values
     *
     * @return latest sensor values
     */
    @Override
    public SensorData getValues() {
        return sensorData;
    }


    /**
     * Check if the sensor is available on the device
     *
     * @return available true / false
     */
    @Override
    public boolean isSensorAvailable() {
        if (sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) == null) {
            return false;
        }

        return true;
    }


    /**
     * set the custom sensor listener
     *
     * @param listener sensorlistener
     */
    @Override
    public void setListener(SensorListener listener) {
        this.listener = listener;
    }


    /**
     * get the type of sensor
     *
     * @return sensortype
     */
    @Override
    public SensorType getSensorType() {
        return SENSORTYPE;
    }


    /************************************************************************************
    *                                                                                   *
    *                      SensorEventListener Interface Methods                        *
    *                                                                                   *
    *************************************************************************************/


    /**
     * Copy sensor values and create SensorData Object with sensortype, correct timestamp and
     * sensor values
     *
     * values[0]: Atmospheric pressure in hPa
     *
     * @param sensorEvent
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_PRESSURE) {
            float[] values = new float[sensorEvent.values.length];
            System.arraycopy(sensorEvent.values, 0, values, 0, sensorEvent.values.length);

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
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}

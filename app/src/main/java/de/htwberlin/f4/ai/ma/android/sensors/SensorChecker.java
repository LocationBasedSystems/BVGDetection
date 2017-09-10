package de.htwberlin.f4.ai.ma.android.sensors;

import de.htwberlin.f4.ai.ma.measurement.IndoorMeasurementType;

/**
 * SensorChecker Interface
 *
 * Used to check if every required sensor is available on the device
 *
 * Author: Benjamin Kneer
 */

public interface SensorChecker {

    // check if every required sensor for the IndoorMeasurementType is available
    boolean checkSensor(IndoorMeasurementType indoorMeasurementType);
}

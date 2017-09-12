package de.htwberlin.f4.ai.ma.measurement.modules;

/**
 * AltitudeModule Interface
 *
 * used to get the relative altitude / altitude change
 *
 * Author: Benjamin Kneer
 */

public interface AltitudeModule{

    // get the relative altitude
    float getAltitude();

    // start module
    void start();

    // stop module
    void stop();
}

package de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.modules;

/**
 * PositionModule Interface
 *
 * used to calculate the current position (cartesian coordinates)
 *
 * Author: Benjamin Kneer
 */

public interface PositionModule{

    // calculate the cartesian coordinates of the current position
    float[] calculatePosition();

    // start module
    void start();

    // stop module
    void stop();
}

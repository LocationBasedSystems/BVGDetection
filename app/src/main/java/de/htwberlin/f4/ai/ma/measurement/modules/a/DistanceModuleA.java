package de.htwberlin.f4.ai.ma.measurement.modules.a;

import android.content.Context;

import de.htwberlin.f4.ai.ma.android.sensors.SensorDataModel;
import de.htwberlin.f4.ai.ma.android.sensors.SensorDataModelImpl;
import de.htwberlin.f4.ai.ma.android.sensors.SensorFactory;
import de.htwberlin.f4.ai.ma.android.sensors.SensorFactoryImpl;
import de.htwberlin.f4.ai.ma.measurement.modules.DistanceModule;

/**
 * Simply calculate distance by using the previously calibrated
 * step length
 */

public class DistanceModuleA implements DistanceModule {

    protected float stepLength;
    protected Context context;

    public DistanceModuleA(Context context, float stepLength) {
        this.context = context;
        this.stepLength = stepLength;
    }

    @Override
    public float getDistance() {
        return stepLength;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}

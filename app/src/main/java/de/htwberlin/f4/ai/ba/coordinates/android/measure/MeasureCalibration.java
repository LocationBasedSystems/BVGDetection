package de.htwberlin.f4.ai.ba.coordinates.android.measure;

import java.util.List;

import de.htwberlin.f4.ai.ba.coordinates.android.sensors.SensorData;
import de.htwberlin.f4.ai.ba.coordinates.android.sensors.SensorDataModel;
import de.htwberlin.f4.ai.ba.coordinates.android.sensors.SensorType;

/**
 * Created by benni on 30.07.2017.
 */

public class MeasureCalibration implements Runnable {

    private SensorDataModel sensorDataModel;
    private MeasureCalibrationListener listener;

    public MeasureCalibration(SensorDataModel sensorDataModel) {
        this.sensorDataModel = sensorDataModel;
    }

    public void setListener(MeasureCalibrationListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        // simply calculating the avg, should be improved
        float pressureAvg = 0.0f;
        float azimuthAvg = 0.0f;

        // calc avg barometer
        List<SensorData> barometerData = sensorDataModel.getData().get(SensorType.BAROMETER);
        if (barometerData != null) {
            float pressureSum = 0.0f;

            // sum up all airpressure values
            for (SensorData data : barometerData) {
                pressureSum += data.getValues()[0];
            }
            // calculate avg
            pressureAvg = pressureSum / barometerData.size();
        }
        // calc avg azimuth
        List<SensorData> compassData = sensorDataModel.getData().get(SensorType.COMPASS_FUSION);
        if (compassData != null) {
            float azimuthSum = 0.0f;

            // sum up all azimuth values
            for (SensorData data : compassData) {
                azimuthSum += data.getValues()[0];
            }
            // calculate avg
            azimuthAvg = azimuthSum / compassData.size();
        }



        if (listener != null) {
            listener.onFinish(pressureAvg, azimuthAvg);
        }
    }

}
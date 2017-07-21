package de.htwberlin.f4.ai.ba.coordinates.android.calibrate;

import android.widget.Toast;

import de.htwberlin.f4.ai.ba.coordinates.android.sensors.stepcounter.StepCounter;
import de.htwberlin.f4.ai.ba.coordinates.android.sensors.stepcounter.StepCounterImpl;
import de.htwberlin.f4.ai.ba.coordinates.android.sensors.stepcounter.StepCounterListener;

/**
 * Created by benni on 18.07.2017.
 */

public class CalibrateControllerImpl implements CalibrateController {

    private CalibrateView view;
    private StepCounter stepCounter;


    @Override
    public void onStartStepSetupClick() {
        stepCounter = new StepCounterImpl(view.getContext());
        stepCounter.setListener(new StepCounterListener() {
            @Override
            public void valueChanged(Integer newValue) {
                view.updateStepCount(newValue);
            }
        });
        stepCounter.start();
    }

    @Override
    public void onStopStepSetupClick() {
        if (stepCounter != null) {
            stepCounter.stop();
            //view.updateStepCount(stepCounter.getValue());

        }

    }

    @Override
    public void onSaveClicked() {
        Toast toast = Toast.makeText(view.getContext(), "save", Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public void onStepIncreaseClicked() {
        Toast toast = Toast.makeText(view.getContext(), "step inc", Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public void onStepDecreaseClicked() {
        Toast toast = Toast.makeText(view.getContext(), "step dec", Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public void onPause() {
        if (stepCounter != null) {
            stepCounter.stop();
        }
    }

    @Override
    public void setView(CalibrateView view) {
        this.view = view;
    }


}

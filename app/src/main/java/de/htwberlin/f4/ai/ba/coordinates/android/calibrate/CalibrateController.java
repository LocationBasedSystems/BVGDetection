package de.htwberlin.f4.ai.ba.coordinates.android.calibrate;

/**
 * Created by benni on 18.07.2017.
 */

public interface CalibrateController {

    void setView(CalibrateView view);
    void onStartStepSetupClick();
    void onStopStepSetupClick();
    void onSaveClicked();
    void onStepIncreaseClicked();
    void onStepDecreaseClicked();
    void onPause();
}
package de.htwberlin.f4.ai.ma.indoorroutefinder.android.record;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import de.htwberlin.f4.ai.ma.indoorroutefinder.R;

import java.util.ArrayList;
import java.util.List;

import de.htwberlin.f4.ai.ma.indoorroutefinder.android.BaseActivity;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.SensorType;

/**
 * RecordViewImpl Class which implements the RecordView Interface
 *
 * Author: Benjamin Kneer
 */

public class RecordViewImpl extends BaseActivity implements RecordView {

    private RecordController controller;

    private TextView accelerationX;
    private TextView accelerationY;
    private TextView accelerationZ;

    private TextView accelerationLinearX;
    private TextView accelerationLinearY;
    private TextView accelerationLinearZ;

    private TextView gravityX;
    private TextView gravityY;
    private TextView gravityZ;

    private TextView gyroscopeX;
    private TextView gyroscopeY;
    private TextView gyroscopeZ;

    private TextView gyroscopeUncalibratedX;
    private TextView gyroscopeUncalibratedY;
    private TextView gyroscopeUncalibratedZ;
    private TextView gyroscopeUncalibratedDriftX;
    private TextView gyroscopeUncalibratedDriftY;
    private TextView gyroscopeUncalibratedDriftZ;

    private TextView magneticFieldX;
    private TextView magneticFieldY;
    private TextView magneticFieldZ;

    private TextView compassFusion;
    private TextView compassSimple;

    private TextView barometer;

    private TextView periodValue;

    private SeekBar periodSeekbar;

    private CheckBox cbAccelerometer;
    private CheckBox cbAcceleromterLinear;
    private CheckBox cbGravity;
    private CheckBox cbGyroscope;
    private CheckBox cbGyroscopeUncalibrated;
    private CheckBox cbMagneticField;
    private CheckBox cbCompassFusion;
    private CheckBox cbCompassSimple;
    private CheckBox cbPressure;

    private List<SensorType> sensorList;


    public RecordViewImpl() {
        controller = new RecordControllerImpl();
        controller.setView(this);
    }


    /************************************************************************************
    *                                                                                   *
    *                               Activity Events                                     *
    *                                                                                   *
    *************************************************************************************/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // find contentframe
        FrameLayout contentFrameLayout = (FrameLayout) findViewById(R.id.content_frame);
        // inflate coorect layout
        getLayoutInflater().inflate(R.layout.activity_sensor_record, contentFrameLayout);

        setTitle("Aufnehmen");

        sensorList = new ArrayList<>();
        /************        find UI Elements and set listeners          ************/

        accelerationX = (TextView) findViewById(R.id.fragment_record_acc_x);
        accelerationY = (TextView) findViewById(R.id.fragment_record_acc_y);
        accelerationZ = (TextView) findViewById(R.id.fragment_record_acc_z);

        accelerationLinearX = (TextView) findViewById(R.id.fragment_record_acc_linear_x);
        accelerationLinearY = (TextView) findViewById(R.id.fragment_record_acc_linear_y);
        accelerationLinearZ = (TextView) findViewById(R.id.fragment_record_acc_linear_z);

        gravityX = (TextView) findViewById(R.id.fragment_record_gravity_x);
        gravityY = (TextView) findViewById(R.id.fragment_record_gravity_y);
        gravityZ = (TextView) findViewById(R.id.fragment_record_gravity_z);

        gyroscopeX = (TextView) findViewById(R.id.fragment_record_gyroscope_x);
        gyroscopeY = (TextView) findViewById(R.id.fragment_record_gyroscope_y);
        gyroscopeZ = (TextView) findViewById(R.id.fragment_record_gyroscope_z);

        gyroscopeUncalibratedX = (TextView) findViewById(R.id.fragment_record_gyroscope_uncalibrated_x);
        gyroscopeUncalibratedY = (TextView) findViewById(R.id.fragment_record_gyroscope_uncalibrated_y);
        gyroscopeUncalibratedZ = (TextView) findViewById(R.id.fragment_record_gyroscope_uncalibrated_z);

        magneticFieldX = (TextView) findViewById(R.id.fragment_record_magneticfield_x);
        magneticFieldY = (TextView) findViewById(R.id.fragment_record_magneticfield_y);
        magneticFieldZ = (TextView) findViewById(R.id.fragment_record_magneticfield_z);

        compassFusion = (TextView) findViewById(R.id.fragment_record_compass_fusion);
        compassSimple = (TextView) findViewById(R.id.fragment_record_compass_simple);

        barometer = (TextView) findViewById(R.id.fragment_record_barometer);

        periodValue = (TextView) findViewById(R.id.fragment_record_period);

        periodSeekbar = (SeekBar) findViewById(R.id.fragment_record_period_seekbar);
        periodSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                periodValue.setText(String.valueOf(i));
                if (controller != null) {
                    controller.onSavePeriodChanged(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        periodSeekbar.setProgress(250);

        Button btnStart = (Button) findViewById(R.id.fragment_record_start_btn);
        btnStart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (controller != null) {
                    controller.onStartClicked(sensorList);
                }
            }
        });

        Button btnStop = (Button) findViewById(R.id.fragment_record_stop_btn);
        btnStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (controller != null) {
                    controller.onStopClicked();
                }
            }
        });

        cbAccelerometer = (CheckBox) findViewById(R.id.fragment_record_cb_accelerometer);
        cbAccelerometer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    sensorList.add(SensorType.ACCELEROMETER_SIMPLE);
                } else {
                    sensorList.remove(SensorType.ACCELEROMETER_SIMPLE);
                }
            }
        });

        cbAcceleromterLinear = (CheckBox) findViewById(R.id.fragment_record_cb_accelerometer_linear);
        cbAcceleromterLinear.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    sensorList.add(SensorType.ACCELEROMETER_LINEAR);
                } else {
                    sensorList.remove(SensorType.ACCELEROMETER_LINEAR);
                }
            }
        });

        cbGravity = (CheckBox) findViewById(R.id.fragment_record_cb_gravity);
        cbGravity.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    sensorList.add(SensorType.GRAVITY);
                } else {
                    sensorList.remove(SensorType.GRAVITY);
                }
            }
        });

        cbGyroscope = (CheckBox) findViewById(R.id.fragment_record_cb_gyroscope);
        cbGyroscope.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    sensorList.add(SensorType.GYROSCOPE);
                } else {
                    sensorList.remove(SensorType.GYROSCOPE);
                }
            }
        });

        cbGyroscopeUncalibrated = (CheckBox) findViewById(R.id.fragment_record_cb_gyroscope_uncalibrated);
        cbGyroscopeUncalibrated.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    sensorList.add(SensorType.GYROSCOPE_UNCALIBRATED);
                } else {
                    sensorList.remove(SensorType.GYROSCOPE_UNCALIBRATED);
                }
            }
        });

        cbMagneticField = (CheckBox) findViewById(R.id.fragment_record_cb_magneticfield);
        cbMagneticField.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    sensorList.add(SensorType.MAGNETIC_FIELD);
                } else {
                    sensorList.remove(SensorType.MAGNETIC_FIELD);
                }
            }
        });

        cbCompassFusion = (CheckBox) findViewById(R.id.fragment_record_cb_compassfusion);
        cbCompassFusion.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    sensorList.add(SensorType.COMPASS_FUSION);
                } else {
                    sensorList.remove(SensorType.COMPASS_FUSION);
                }
            }
        });

        cbCompassSimple = (CheckBox) findViewById(R.id.fragment_record_cb_compasssimple);
        cbCompassSimple.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    sensorList.add(SensorType.COMPASS_SIMPLE);
                } else {
                    sensorList.remove(SensorType.COMPASS_SIMPLE);
                }
            }
        });

        cbPressure = (CheckBox) findViewById(R.id.fragment_record_cb_pressure);
        cbPressure.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    sensorList.add(SensorType.BAROMETER);
                } else {
                    sensorList.remove(SensorType.BAROMETER);
                }
            }
        });
    }


    @Override
    public void onPause() {
        super.onPause();
        if (controller != null) {
            controller.onPause();
        }
    }


    /************************************************************************************
    *                                                                                   *
    *                               Interface Methods                                   *
    *                                                                                   *
    *************************************************************************************/


    /**
     * update the acceleration sensor view
     *
     * @param values new acceleration sensor values
     */
    @Override
    public void updateAcceleration(float[] values) {
        accelerationX.setText(getString(R.string.x_caption) + " " + values[0]);
        accelerationY.setText(getString(R.string.y_caption) + " " + values[1]);
        accelerationZ.setText(getString(R.string.z_caption) + " " + values[2]);
    }


    /**
     * update the acceleration_linear sensor view
     *
     * @param values new acceleration_linear sensor values
     */
    @Override
    public void updateAccelerationLinear(float[] values) {
        accelerationLinearX.setText(getString(R.string.x_caption) + " " + values[0]);
        accelerationLinearY.setText(getString(R.string.y_caption) + " " + values[1]);
        accelerationLinearZ.setText(getString(R.string.z_caption) + " " + values[2]);
    }


    /**
     * update the gravity sensor view
     *
     * @param values new gravity sensor values
     */
    @Override
    public void updateGravity(float[] values) {
        gravityX.setText(getString(R.string.x_caption) + " " + values[0]);
        gravityY.setText(getString(R.string.y_caption) + " " + values[1]);
        gravityZ.setText(getString(R.string.z_caption) + " " + values[2]);
    }


    /**
     * update the gyroscope sensor view
     *
     * @param values new gyroscope sensor values
     */
    @Override
    public void updateGyroscope(float[] values) {
        gyroscopeX.setText(getString(R.string.x_caption) + " " + values[0]);
        gyroscopeY.setText(getString(R.string.y_caption) + " " + values[1]);
        gyroscopeZ.setText(getString(R.string.z_caption) + " " + values[2]);
    }


    /**
     * update the gyroscope_uncalibrated view
     *
     * @param values new gyroscope_uncalibrated sensor values
     */
    @Override
    public void updateGyroscopeUncalibrated(float[] values) {
        gyroscopeUncalibratedX.setText(getString(R.string.x_caption) + " " + values[0]);
        gyroscopeUncalibratedY.setText(getString(R.string.y_caption) + " " + values[1]);
        gyroscopeUncalibratedZ.setText(getString(R.string.z_caption) + " " + values[2]);
    }


    /**
     * update the magneticfield sensor view
     *
     * @param values new magneticfield sensor values
     */
    @Override
    public void updateMagneticField(float[] values) {
        magneticFieldX.setText(getString(R.string.x_caption) + " " + values[0]);
        magneticFieldY.setText(getString(R.string.y_caption) + " " + values[1]);
        magneticFieldZ.setText(getString(R.string.z_caption) + " " + values[2]);
    }


    /**
     * update the compassfusion view
     *
     * @param value new compassfusion sensor values
     */
    @Override
    public void updateCompassFusion(float value) {
        compassFusion.setText(String.valueOf(value));
    }


    /**
     * update the compass simple view
     *
     * @param value new compass simple sensor values
     */
    @Override
    public void updateCompassSimple(float value) {
        compassSimple.setText(String.valueOf(value));
    }


    /**
     * update the barometer view
     *
     * @param value new barometer sensor values
     */
    @Override
    public void updatePressure(float value) {
        barometer.setText(value + " hPa");
    }


    /**
     * get the view's context
     *
     * @return context
     */
    @Override
    public Context getContext() {
        return this;
    }

}

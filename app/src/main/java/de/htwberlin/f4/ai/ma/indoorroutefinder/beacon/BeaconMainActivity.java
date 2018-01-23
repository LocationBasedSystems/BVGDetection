package de.htwberlin.f4.ai.ma.indoorroutefinder.beacon;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.Switch;

import de.htwberlin.f4.ai.ma.indoorroutefinder.R;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.BaseActivity;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandler;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandlerFactory;

public class BeaconMainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = (FrameLayout) findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_beacon_main, contentFrameLayout);
        setTitle("Beacon ");

        final Button buttonSend = (Button)findViewById(R.id.beacon_send_data);
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                //wifiConn.sendMessage(tmp);
            }
        });

        final Button buttonReceive = (Button)findViewById(R.id.beacon_receive_data);
        buttonReceive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        Switch enableSwitch = (Switch) findViewById(R.id.beacon_enable);
        enableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){

                    buttonSend.setEnabled(true);
                    buttonReceive.setEnabled(true);
                }
                else{
                    buttonSend.setEnabled(false);
                    buttonReceive.setEnabled(false);
                }
            }
        });
        //DatabaseHandlerFactory.getInstance(this).exportDatabase();
    }


}

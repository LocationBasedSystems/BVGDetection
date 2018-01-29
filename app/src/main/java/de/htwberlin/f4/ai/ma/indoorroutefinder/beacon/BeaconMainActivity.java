package de.htwberlin.f4.ai.ma.indoorroutefinder.beacon;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.TextView;

import de.htwberlin.f4.ai.ma.indoorroutefinder.R;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.BaseActivity;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandler;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandlerFactory;

public class BeaconMainActivity extends BaseActivity implements BeaconCallback {

    WiFiDirectConnector wifiConn;
    TextView statusText;
    TextView debugMessages;

    Button buttonSend;
    Button buttonReceive;
    Button buttonAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = (FrameLayout) findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_beacon_main, contentFrameLayout);
        setTitle("Beacon ");

        wifiConn = new WiFiDirectConnector(getApplicationContext(), this);
        wifiConn.initializeWiFiDirect();

        statusText = (TextView)findViewById(R.id.beacon_status_text);
        debugMessages = (TextView)findViewById(R.id.beacon_message_dump);

        buttonSend = (Button)findViewById(R.id.beacon_send_data);
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wifiConn.sendMessage(PROTOKOLL.cs_dataBegin + "indoor_data.db");
            }
        });

        buttonReceive = (Button)findViewById(R.id.beacon_receive_data);
        buttonReceive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wifiConn.sendMessage(PROTOKOLL.cs_dataRequest + "indoor_data.db");
            }
        });

        buttonAdmin = (Button)findViewById(R.id.beacon_request_admin);
        buttonAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wifiConn.sendMessage(PROTOKOLL.cs_addAdmin);
            }
        });

        Switch enableSwitch = (Switch) findViewById(R.id.beacon_enable);
        enableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    wifiConn.discoverPeers();
                }
                else{
                    buttonSend.setEnabled(false);
                    buttonReceive.setEnabled(false);
                    buttonAdmin.setEnabled(false);
                }
            }
        });
        //DatabaseHandlerFactory.getInstance(this).exportDatabase();
    }


    @Override
    public void receivedFile() {

    }

    @Override
    public void establishedConnection() {
        statusText.setText("Beacon Verbunden");
        buttonSend.setEnabled(true);
        buttonReceive.setEnabled(true);
        buttonAdmin.setEnabled(true);
    }

    @Override
    public void receivedMessage(String message) {
        debugMessages.append("\n" + message);
        if(message.equals("Connecting to Beacon")){
            statusText.setText("Verbinde zum Beacon...");
        }
    }
}

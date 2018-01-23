package de.htwberlin.f4.ai.ma.indoorroutefinder.beacon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by juliu on 23.01.2018.
 */

public class WiFiDirectConnector {

    private static final String TAG = "WiFiDirectConnector";

    //tmp
    Context context;

    TcpClient mTcpClient;
    String servIp ="";
    WifiP2pDevice beaconObj;

    IntentFilter peerfilter;
    IntentFilter connectionfilter;
    IntentFilter p2pEnabled;

    private Handler handler = new Handler();

    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel wifiDirectChannel;

    public List<WifiP2pDevice> deviceList = new ArrayList<WifiP2pDevice>();

    public WiFiDirectConnector(Context c){
        this.context = c;
        peerfilter = new IntentFilter(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        connectionfilter = new IntentFilter(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        p2pEnabled = new IntentFilter(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        context.registerReceiver(peerDiscoveryReceiver, peerfilter);
        context.registerReceiver(connectionChangedReceiver, connectionfilter);
        context.registerReceiver(p2pStatusReceiver, p2pEnabled);
    }

    public void initializeWiFiDirect() {
        wifiP2pManager =
                (WifiP2pManager)context.getSystemService(Context.WIFI_P2P_SERVICE);

        wifiDirectChannel = wifiP2pManager.initialize(context, context.getMainLooper(),
                new WifiP2pManager.ChannelListener() {
                    public void onChannelDisconnected() {
                        initializeWiFiDirect();
                    }
                }
        );
    }

    public void sendMessage(String s){
        if (mTcpClient != null) {
            mTcpClient.sendMessage(s);
        }
    }

    private WifiP2pManager.ActionListener actionListener = new WifiP2pManager.ActionListener() {
        public void onFailure(int reason) {
            String errorMessage = "WiFi Direct Failed: ";
            switch (reason) {
                case WifiP2pManager.BUSY :
                    errorMessage += "Framework busy."; break;
                case WifiP2pManager.ERROR :
                    errorMessage += "Internal error."; break;
                case WifiP2pManager.P2P_UNSUPPORTED :
                    errorMessage += "Unsupported."; break;
                default:
                    errorMessage += "Unknown error."; break;
            }
            Log.d(TAG, errorMessage);
        }

        public void onSuccess() {
            // Success!
            // Return values will be returned using a Broadcast Intent
            Log.d(TAG, "Successfull connection");

        }
    };

    public void connectTo(WifiP2pDevice device) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        wifiP2pManager.connect(wifiDirectChannel, config, actionListener);
    }

    public void checkList(){
        Iterator<WifiP2pDevice> iter = deviceList.iterator();
        while (iter.hasNext()) {
            WifiP2pDevice tmp = iter.next();
            if (tmp.deviceName.equals("SharkTerminal-1")) {
                Log.d(TAG, "about to connect to");
                Log.d(TAG, tmp.toString());
                beaconObj = tmp;
                Log.d(TAG, beaconObj.deviceAddress);
                //break;
            }
        }

        Log.d(TAG, "discovery done");
        if(beaconObj != null) {
            connectTo(beaconObj);
        }

        //new ConnectTask().execute(servIp);
    }

    public void discoverPeers() {
        wifiP2pManager.discoverPeers(wifiDirectChannel, actionListener);
    }

    BroadcastReceiver p2pStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(
                    WifiP2pManager.EXTRA_WIFI_STATE,
                    WifiP2pManager.WIFI_P2P_STATE_DISABLED);

            switch (state) {
                case (WifiP2pManager.WIFI_P2P_STATE_ENABLED):
                    //buttonDiscover.setEnabled(true);
                    break;
                default:
                    //buttonDiscover.setEnabled(false);
            }
        }
    };

    BroadcastReceiver peerDiscoveryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            wifiP2pManager.requestPeers(wifiDirectChannel,
                    new WifiP2pManager.PeerListListener() {
                        public void onPeersAvailable(WifiP2pDeviceList peers) {
                            Log.d(TAG, "peers discovered");

                            deviceList.clear();
                            deviceList.addAll(peers.getDeviceList());

                            checkList();
                        }
                    });
        }
    };

    BroadcastReceiver connectionChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            // Extract the NetworkInfo
            String extraKey = WifiP2pManager.EXTRA_NETWORK_INFO;
            NetworkInfo networkInfo =
                    (NetworkInfo)intent.getParcelableExtra(extraKey);

            // Check if we're connected
            Log.d(TAG, networkInfo.toString());
            if (networkInfo.isConnected()) {
                context.unregisterReceiver(peerDiscoveryReceiver);
                Log.d(TAG, "network connected");
                wifiP2pManager.requestConnectionInfo(wifiDirectChannel,
                        new WifiP2pManager.ConnectionInfoListener() {
                            public void onConnectionInfoAvailable(WifiP2pInfo info) {
                                // If the connection is established
                                if (info.groupFormed) {
                                    // If we're the server
                                    //info.groupOwnerAddress;
                                    if (info.isGroupOwner) {
                                        // TODO Initiate server socket.

                                        //checkList();

                                        showUserMessage("is GO");
                                        wifiP2pManager.stopPeerDiscovery(wifiDirectChannel,actionListener);

                                        while(servIp.equals("")) {
                                            try {
                                                BufferedReader br = new BufferedReader(new FileReader(new File("/proc/net/arp")));
                                                String line;

                                                while ((line = br.readLine()) != null) {
                                                    Log.d(TAG, line);
                                                    if (line.contains("p2p0")) { //beaconObj.deviceAddress)){
                                                        servIp = line.split(" ")[0];
                                                        Log.d(TAG, line);
                                                    }
                                                }

                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        Log.d(TAG, servIp);
                                        new ConnectTask().execute(servIp);

                                    }
                                    // If we're the client
                                    else{
                                        // TODO Initiate client socket.
                                        showUserMessage("is CLI");
                                        //Nicht gewollt
                                    }
                                }
                            }
                        });
            } else {
                Log.d(TAG, "Wi-Fi Direct Disconnected");
            }
        }
    };

    public void pause(){
        context.unregisterReceiver(peerDiscoveryReceiver);
        context.unregisterReceiver(connectionChangedReceiver);
        context.unregisterReceiver(p2pStatusReceiver);
    }

    public void resume(){
        context.registerReceiver(peerDiscoveryReceiver, peerfilter);
        context.registerReceiver(connectionChangedReceiver, connectionfilter);
        context.registerReceiver(p2pStatusReceiver, p2pEnabled);
    }

    public void showUserMessage(String s){
        Toast.makeText(context, s, Toast.LENGTH_LONG).show();
    }

    class ConnectTask extends AsyncTask<String, String, TcpClient> {

        @Override
        protected TcpClient doInBackground(String... message) {
            //we create a TCPClient object
            mTcpClient = new TcpClient(message[0], new TcpClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    publishProgress(message);
                }
            });
            mTcpClient.run();

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            //response received from server
            Log.d("test", "response " + values[0]);
            //process server response here....
            showUserMessage(values[0]);
        }

    }
}

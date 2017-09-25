package de.htwberlin.f4.ai.ma.indoorroutefinder.wifi_scanner;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Johann Winter
 */

class WifiScannerImpl implements WifiScanner {

    @Override
    public List<String> getAvailableNetworks(WifiManager wifiManager, boolean onlyNetworksWithTwoOrMoreAPs) {

        wifiManager.startScan();
        List<ScanResult> wifiScanList = wifiManager.getScanResults();

        List<String> SSIDs = new ArrayList<>();
        List<String> wifiNamesList = new ArrayList<>();
        List<String> SSIDsWithMoreThanOneBSSID = new ArrayList<>();


        for (ScanResult sr : wifiScanList) {
            SSIDs.add(sr.SSID);
            if (!wifiNamesList.contains(sr.SSID) && !sr.SSID.equals("")) {
                wifiNamesList.add(sr.SSID);
            }
        }

        // Filter for SSIDs with more than one BSSID (Accesspoint)
        for (String s : SSIDs) {
            int occurrences = Collections.frequency(SSIDs, s);
            if (occurrences > 1 && !SSIDsWithMoreThanOneBSSID.contains(s) && !s.equals("")) {
                SSIDsWithMoreThanOneBSSID.add(s);
            }
        }

        if (onlyNetworksWithTwoOrMoreAPs) {
            return SSIDsWithMoreThanOneBSSID;
        } else {
            return wifiNamesList;
        }
    }
}

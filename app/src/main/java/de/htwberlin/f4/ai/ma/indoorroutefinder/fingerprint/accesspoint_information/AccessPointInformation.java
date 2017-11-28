package de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.accesspoint_information;

import java.io.Serializable;

/**
 * Created by Johann Winter
 *
 * This interface is designed for AccessPointInformations which are recorded
 * every second for every accesspoint while fingerprinting process.
 */

public interface AccessPointInformation extends Serializable{

    /**
     * Getter for the MAC-address of an access point
     * @return the MAC-address string
     */
    String getMacAddress();

    /**
     * Getter for the signal strength (RSSI) in dBm of an access point
     * @return the signal strength (RSSI) in dBm
     */
    int getRssi();

    /**
     * Getter for the signal strength (RSSI) in milliwatt of an access point.
     * For later purposes; changing the signal strength measurement for a more precise algorithm.
     * @return the signal strength (RSSI) in milliwatt
     */
    double getMilliwatt();

}

package de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint;

import java.io.Serializable;
import java.util.List;

import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.accesspoint_information.AccessPointInformation;

/**
 * Created by Johann Winter
 *
 * A SignalSample consists of a timestamp and a list of AccessPointInformation at that time.
 */


public class SignalSample implements Serializable{

    private String timestamp;
    private List<AccessPointInformation> accessPointInformationList;

    public SignalSample(String timestamp, List<AccessPointInformation> accessPointInformations) {
        this.timestamp = timestamp;
        this.accessPointInformationList = accessPointInformations;
    }

    /**
     * Getter for the timestamp
     * @return the timestamp
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Getter for the list of SignalStrengths
     * @return the list of SignalStrengths
     */
    public List<AccessPointInformation> getAccessPointInformationList() {
        return accessPointInformationList;
    }

}
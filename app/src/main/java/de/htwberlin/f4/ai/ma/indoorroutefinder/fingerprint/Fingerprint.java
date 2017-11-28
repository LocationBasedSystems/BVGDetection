package de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Johann Winter
 *
 * This interface is for fingerprints.
 */

public interface Fingerprint extends Serializable {

    /**
     * Getter for the SSID (network name) of the measured wifi.
     * If the fingerprint is captured without a SSID-filter, it will return null.
     * @return the SSID
     */
    String getSsid();

    /**
     * Getter for the list of SignalSamples which contain the measured signal data of a fingerprint
     * @return the list of SignalSamples
     */
    List<SignalSample> getSignalSampleList();
}

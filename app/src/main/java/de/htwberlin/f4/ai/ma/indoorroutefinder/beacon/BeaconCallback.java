package de.htwberlin.f4.ai.ma.indoorroutefinder.beacon;

/**
 * Created by juliu on 28.01.2018.
 */

public interface BeaconCallback {
    public void receivedFile();
    public void establishedConnection();
    public void receivedMessage(String message);
}

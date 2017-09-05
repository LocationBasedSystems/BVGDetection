package de.htwberlin.f4.ai.ma.android.measure;


import de.htwberlin.f4.ai.ma.node.Node;

/**
 * MeasureController Interface
 *
 * used for measuring the distance / calculate coordinates of nodes
 *
 * Author: Benjamin Kneer
 */

public interface MeasureController {

    // set the responsible view
    void setView(MeasureView view);

    // triggered by clicking start button
    void onStartClicked();

    // triggered by clicking stop button
    void onStopClicked();

    // triggered by clicking step button
    void onStepClicked();

    // activity event
    void onPause();

    // activity event
    void onResume();

    // triggered when user selects a start node
    void onStartNodeSelected(Node node);

    // triggered when user selects a target node
    void onTargetNodeSelected(Node node);

    // triggered by clicking on the arrow between start and target node
    void onEdgeDetailsClicked();

    // triggered by clicking on the startnode image
    void onStartNodeImageClicked();

    // triggered by clicking on the targetnode image
    void onTargetNodeImageClicked();

    // triggered by clicking on wifi icon
    void onLocateWifiClicked();

    // triggered by clicking on QR icon
    void onLocateQrClicked();

    // triggered when qr code is recognized
    void onQrResult(String qr);

    // triggered when user marks the startnode as nullpoint
    void onNullpointCheckedStartNode(boolean checked);

    // triggered by stairs switch
    void onStairsToggle(boolean checked);
}

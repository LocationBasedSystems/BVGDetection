package de.htwberlin.f4.ai.ma.indoorroutefinder.edge;

import java.util.List;

import de.htwberlin.f4.ai.ma.indoorroutefinder.node.Node;

/**
 * Created by Johann Winter
 *
 * This interface is designed for Edges between two Nodes.
 * There are methods for getting these Nodes,
 * getting and setting accessibility (Barrierefreiheit),
 * the weight (distance between the two Nodes in meters)
 * and getting the coordinates from "Wegvermessung".
 *
 * Additionally, there are methods for getting and setting
 * an field for future purposes (getAdditionalInfo, setAdditionalInfo).
 */

public interface Edge {

    Node getNodeA();

    Node getNodeB();

    boolean getAccessibility();
    void setAccessibility(boolean accessable);

    float getWeight();
    void setWeight(float weight);

    List<String> getStepCoordsList();

    String getAdditionalInfo();
    void setAdditionalInfo(String additionalInfo);
}
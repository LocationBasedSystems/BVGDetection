package de.htwberlin.f4.ai.ma.indoorroutefinder.location.location_calculator;

import com.google.common.collect.Multimap;

import java.util.List;

import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.accesspoint_information.AccessPointInformation;
import de.htwberlin.f4.ai.ma.indoorroutefinder.location.calculations.RestructedNode;
import de.htwberlin.f4.ai.ma.indoorroutefinder.node.Node;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.Fingerprint;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.SignalSample;

/**
 * Created by Johann Winter
 */

public interface LocationCalculator {

    /**
     * Calculate a Node from a given Fingerprint
     *
     * @param fingerprint the input Fingerprint to be compared with all existent Nodes to get the position
     * @return the ID (name) of the resulting Node
     */
    String calculateNodeId(Fingerprint fingerprint);


    /**
     * Get a list of SignalStrengths by passing a list of SignalSample (unwrap).
     *
     * @param signalSampleList a list of SignalInformations
     * @return a list of SignalStrengthInformations
     */
    List<AccessPointInformation> getSignalStrengths(List<SignalSample> signalSampleList);


    /**
     * Rewrite the nodelist to restrucetd Nodes and delete weak MAC addresses
     *
     * @param allNodes list of all nodes
     * @return restructed Node list
     */
    List<RestructedNode> calculateNewNodeDateset(List<Node> allNodes);


    /**
     * Create a multimap with MAC address and signal strength values
     *
     * @param node input Node
     * @param macAdresses list of MAC addresses
     * @return multimap with mac address and signal strengths
     */
    Multimap<String, Double> getMultiMap(Node node, List<String> macAdresses);


    /**
     * Get all mac addresses of a specific Node
     *
     * @param node the Node
     * @return list of unique MAC addresses
     */
    List<String> getMacAddresses(Node node);

}
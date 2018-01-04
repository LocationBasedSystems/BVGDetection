package de.htwberlin.f4.ai.ma.indoorroutefinder.gps.updaters;

import android.location.Location;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.htwberlin.f4.ai.ma.indoorroutefinder.edge.Edge;
import de.htwberlin.f4.ai.ma.indoorroutefinder.gps.calculators.LocalGlobalCoordinateCalculator;
import de.htwberlin.f4.ai.ma.indoorroutefinder.gps.calculators.LocalGlobalCoordinateCalculatorFactory;
import de.htwberlin.f4.ai.ma.indoorroutefinder.gps.node.GlobalNode;
import de.htwberlin.f4.ai.ma.indoorroutefinder.gps.node.GlobalNodeFactory;
import de.htwberlin.f4.ai.ma.indoorroutefinder.node.Node;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandler;

/**
 * @author Emil Schoenawa (eschoenawa; Matr. Nr.: 554086)
 * @version 05.12.2017
 */

public class GlobalCoordinateUpdaterImpl implements GlobalCoordinateUpdater {

    private DatabaseHandler databaseHandler;

    //TODO Externalize or put in config/settings
    public static final double INACCURACY_PER_METER = 0.0259;

    public GlobalCoordinateUpdaterImpl(DatabaseHandler databaseHandler) {
        this.databaseHandler = databaseHandler;
    }

    @Override
    public void updateGlobalCoordinates() {
        Log.d("COORDUPD", "START OF COORD UPDATE");
        List<Node> nodes = databaseHandler.getAllNodes();

        while (!nodes.isEmpty()) {
            GlobalNode mostAccurate = null;
            for (Node n : nodes) {
                GlobalNode temp = GlobalNodeFactory.createInstance(n);
                if (temp.hasGlobalCoordinates() && ((mostAccurate == null) || (temp.getGlobalCalculationInaccuracyRating() < mostAccurate.getGlobalCalculationInaccuracyRating()))) {
                    mostAccurate = temp;
                    Log.d("COORDUPD", "new mostAccurate: " + mostAccurate.getId() + "; coords: " + mostAccurate.getCoordinates());
                }
            }
            if (mostAccurate != null && mostAccurate.getCoordinates() != null && !mostAccurate.getCoordinates().equals("")) {
                List<Edge> edges = databaseHandler.getAllEdges();
                Node mostAccurateNode = mostAccurate.getNode();
                for (Edge e : edges) {
                    GlobalNode otherNode = null;
                    if (areNodesEqual(e.getNodeA(), mostAccurateNode)) {
                        otherNode = GlobalNodeFactory.createInstance(e.getNodeB());

                    }
                    else if (areNodesEqual(e.getNodeB(), mostAccurateNode)) {
                        otherNode = GlobalNodeFactory.createInstance(e.getNodeA());
                    }
                    if (otherNode != null) {
                        Log.d("COORDUPD", "Other Node: " + otherNode.getId());

                        Log.d("COORDUPD", "otherNode != null: " + String.valueOf(otherNode != null));
                        Log.d("COORDUPD", "otherNode.getCoordinates() != null: " + String.valueOf(otherNode.getCoordinates() != null));
                        Log.d("COORDUPD", "!otherNode.getCoordinates().equals(\"\"): " + String.valueOf(!otherNode.getCoordinates().equals("")));
                        Log.d("COORDUPD", "!otherNode.hasGlobalCoordinates(): " + String.valueOf(!otherNode.hasGlobalCoordinates()));
                        Log.d("COORDUPD", "the Inaccuracy thing: " + String.valueOf(otherNode.getGlobalCalculationInaccuracyRating() > mostAccurate.getGlobalCalculationInaccuracyRating() + (e.getWeight() * INACCURACY_PER_METER)));
                    }

                    if (otherNode != null && otherNode.getCoordinates() != null && !otherNode.getCoordinates().equals("") && (!otherNode.hasGlobalCoordinates() || otherNode.getGlobalCalculationInaccuracyRating() > mostAccurate.getGlobalCalculationInaccuracyRating() + (e.getWeight() * INACCURACY_PER_METER))) {
                        Log.d("COORDUPD", "mostAccurate: " + mostAccurate.getId() + "; coords: " + mostAccurate.getCoordinates());
                        Log.d("COORDUPD", "otherNode: " + otherNode.getId() + "; coords: " + otherNode.getCoordinates());
                        LocalGlobalCoordinateCalculator calculator = LocalGlobalCoordinateCalculatorFactory.getInstance();
                        Location source = mostAccurate.getLocation();
                        Location newLocation = calculator.getGlobalCoordinates(calculator.calculateOffset(mostAccurate.getCoordinates(), otherNode.getCoordinates()), source);
                        if (newLocation != null) {
                            otherNode.setLatitude(newLocation.getLatitude());
                            otherNode.setLongitude(newLocation.getLongitude());
                            if (newLocation.hasAltitude()) {
                                otherNode.setAltitude(newLocation.getAltitude());
                            }
                            otherNode.setGlobalCalculationInaccuracyRating(mostAccurate.getGlobalCalculationInaccuracyRating() + (e.getWeight() * INACCURACY_PER_METER));
                            databaseHandler.updateNode(otherNode.getNode(), otherNode.getId());
                            for (Node n : nodes) {
                                if (areNodesEqual(otherNode.getNode(), n)) {
                                    nodes.remove(n);
                                    nodes.add(otherNode.getNode());
                                    Log.d("COORDUPD", "Updated " + n.getId());
                                    break;
                                }
                            }
                        }
                    }
                }
                for (Node n : nodes) {
                    if (areNodesEqual(n, mostAccurateNode)) {
                        nodes.remove(n);
                        Log.d("COORDUPD", "Removed " + n.getId());
                        break;
                    }
                }
            }
            else if (mostAccurate != null ) {
                for (Node n : nodes) {
                    if (areNodesEqual(n, mostAccurate.getNode())) {
                        nodes.remove(n);
                    }
                }
            }
            else {
                break;
            }
        }
        Log.d("COORDUPD", "END OF COORD UPDATE");
    }

    private boolean areNodesEqual(Node a, Node b) {
        return a.getId().equals(b.getId());
    }
}

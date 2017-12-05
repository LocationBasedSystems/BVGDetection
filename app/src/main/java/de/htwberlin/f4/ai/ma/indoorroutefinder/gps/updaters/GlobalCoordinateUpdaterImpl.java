package de.htwberlin.f4.ai.ma.indoorroutefinder.gps.updaters;

import android.location.Location;

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
        List<Node> nodes = databaseHandler.getAllNodes();
        while (!nodes.isEmpty()) {
            GlobalNode mostAccurate = null;
            for (Node n : nodes) {
                GlobalNode temp = GlobalNodeFactory.createInstance(n);
                if (temp.hasGlobalCoordinates() && ((mostAccurate == null) || (temp.getGlobalCalculationInaccuracyRating() < mostAccurate.getGlobalCalculationInaccuracyRating()))) {
                    mostAccurate = temp;
                }
            }
            if (mostAccurate != null) {
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
                    if (otherNode != null && (!otherNode.hasGlobalCoordinates() || otherNode.getGlobalCalculationInaccuracyRating() > mostAccurate.getGlobalCalculationInaccuracyRating() + (e.getWeight() * INACCURACY_PER_METER))) {
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
                        }
                    }
                }
                nodes.remove(mostAccurateNode);
            }
            else {
                break;
            }
        }
    }

    private boolean areNodesEqual(Node a, Node b) {
        return a.getId().equals(b.getId());
    }
}

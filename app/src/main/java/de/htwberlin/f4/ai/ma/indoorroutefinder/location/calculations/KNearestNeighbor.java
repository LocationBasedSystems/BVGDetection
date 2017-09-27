package de.htwberlin.f4.ai.ma.indoorroutefinder.location.calculations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Johann Winter
 *
 * Thanks to Carola Walter
 */


public class KNearestNeighbor {

    /**
     * Calculate k-Nearest-Neighbor algorithm
     * @param knnValue the knn value
     * @param distanceNames names of sorted list
     * @return the name of the node
     */
    public static String calculateKnn(int knnValue, List<String> distanceNames) {
        Map<String, Integer> stringsCount = new HashMap<String, Integer>();

        if (distanceNames.size() >= knnValue && knnValue != 0) {
            for (int i = 0; i < 4*knnValue; i++) {
                if (distanceNames.get(i).length() > 0) {
                    Integer count = stringsCount.get(distanceNames.get(i));
                    if (count == null) count = 0;
                    count++;
                    stringsCount.put(distanceNames.get(i), count);
                }
            }
        }
        Map.Entry<String, Integer> mostRepeated = null;
        for (Map.Entry<String, Integer> e : stringsCount.entrySet()) {
            if (mostRepeated == null || mostRepeated.getValue() < e.getValue())
                mostRepeated = e;
        }
        if (mostRepeated != null) {
            double percent = ((double)mostRepeated.getValue() / ((double)4 * (double)knnValue))*(double)100;

            if (mostRepeated.getKey() != null) {
                return mostRepeated.getKey();
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}
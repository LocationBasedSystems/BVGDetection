package de.htwberlin.f4.ai.ma.indoorroutefinder.gps.calculators;

/**
 * @author Emil Schoenawa (eschoenawa; Matr. Nr.: 554086)
 * @version 05.12.2017
 */

public class LocalGlobalCoordinateCalculatorFactory {

    private static LocalGlobalCoordinateCalculator instance;

    public static LocalGlobalCoordinateCalculator getInstance() {
        if (instance == null) {
            // instance = new SimpleHaversineLocalGlobalCoordinateCalculator();
            instance = new CosineLocalGlobalCoordinateCalculator();
        }
        return instance;
    }
}

package de.htwberlin.f4.ai.ma.indoorroutefinder.gps.updaters;

import android.content.Context;

import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandlerFactory;

/**
 * @author Emil Schoenawa (eschoenawa; Matr. Nr.: 554086)
 * @version 05.12.2017
 */

public class GlobalCoordinateUpdaterFactory {

    private static GlobalCoordinateUpdater instance;

    public static GlobalCoordinateUpdater getInstance(Context context) {
        if (instance == null) {
            instance = new GlobalCoordinateUpdaterImpl(DatabaseHandlerFactory.getInstance(context));
        }
        return instance;
    }

}

package de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.paperchase;

import android.content.Context;

/**
 * Created by Yannik on 05.12.2017.
 */

public class PaperchaseDatabaseHandlerFactory {
    private static PaperchaseDatabaseHandler paperchaseDatabaseHandler;

    public static PaperchaseDatabaseHandler getInstance(Context context) {
        if (paperchaseDatabaseHandler == null) {
            paperchaseDatabaseHandler = new PaperchaseDatabaseHandlerImpl(context);
        }
        return paperchaseDatabaseHandler;
    }
}

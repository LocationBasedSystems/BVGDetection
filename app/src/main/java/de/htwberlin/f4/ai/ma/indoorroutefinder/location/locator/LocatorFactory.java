package de.htwberlin.f4.ai.ma.indoorroutefinder.location.locator;

import android.content.Context;

public class LocatorFactory {

    private static Locator instance;

    public static Locator getInstance(Context context) {
        if (instance == null) {
            instance = new CombinedLocator(context);
        }
        else{
            instance.setContext(context);
        }
        return instance;
    }
}

package de.htwberlin.f4.ai.ma.indoorroutefinder.settings;

import android.content.Context;
import android.util.AttributeSet;


/**
 * Override the standard ListPreference element.
 * It is now possible to see the value of the child element as summary.
 */
public class ListPreference extends android.preference.ListPreference {

    public ListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        setSummary(getSummary());
    }

    @Override
    public CharSequence getSummary() {
        return this.getValue();
    }
}

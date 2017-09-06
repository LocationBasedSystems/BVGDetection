package de.htwberlin.f4.ai.ma.location.locationresult;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

//import com.example.carol.bvg.LocationResultImpl;
import com.example.carol.bvg.R;

import java.util.List;

/**
 * Adapter for the LocationResult listview in LocationDetailedInfoActivity
 */
public class LocationResultAdapter extends ArrayAdapter<LocationResult> {

    public LocationResultAdapter(Context context, List<LocationResult> users) {
        super(context, 0, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Get the data item for this position
        LocationResult locationResult = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_locationresult, parent, false);
        }

        // Lookup view for data population
        TextView settingsTextview = (TextView) convertView.findViewById(R.id.settings_textview);
        TextView resultNodeTextview = (TextView) convertView.findViewById(R.id.result_node_textview);
        TextView measureTimeTextview = (TextView) convertView.findViewById(R.id.measure_time_textview);

        // Populate the data into the template view using the data object
        settingsTextview.setText(locationResult.getSettings());
        resultNodeTextview.setText(locationResult.getMeasuredNode() + "\n" + locationResult.getPercentage() + " %");
        measureTimeTextview.setText(locationResult.getMeasuredTime());

        // Return the completed view to render on screen
        return convertView;
    }
}
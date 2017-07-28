package de.htwberlin.f4.ai.ma.prototype_temp.location_result;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

//import com.example.carol.bvg.LocationResultImpl;
import com.example.carol.bvg.R;

import java.util.ArrayList;

/**
 * adapter for the result list view in locationActivity
 */
class LocationResultAdapter extends ArrayAdapter<LocationResultImpl> {
    public LocationResultAdapter(Context context, ArrayList<LocationResultImpl> users) {
        super(context, 0, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        LocationResultImpl locationResult = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_locationresult, parent, false);
        }
        // Lookup view for data population
        TextView tvLocationSetting = (TextView) convertView.findViewById(R.id.tx_resultsSetting);
        TextView tvPOI = (TextView) convertView.findViewById(R.id.tx_resultPOI);
        TextView tvMeasuredPOI = (TextView) convertView.findViewById(R.id.tx_resultMeasuredPOI);
        TextView tvMeasuredTime = (TextView) convertView.findViewById(R.id.tx_measuredTime);
        // Populate the data into the template view using the data object
        tvLocationSetting.setText(locationResult.getSettings());
        tvPOI.setText(locationResult.getSelectedNode());
        tvMeasuredPOI.setText(locationResult.getMeasuredNode());
        tvMeasuredTime.setText(locationResult.getMeasuredTime());
        // Return the completed view to render on screen
        return convertView;
    }


}
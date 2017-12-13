package de.htwberlin.f4.ai.ma.indoorroutefinder.gps.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.htwberlin.f4.ai.ma.indoorroutefinder.R;
import de.htwberlin.f4.ai.ma.indoorroutefinder.gps.node.GlobalNode;
import de.htwberlin.f4.ai.ma.indoorroutefinder.gps.node.GlobalNodeFactory;
import de.htwberlin.f4.ai.ma.indoorroutefinder.gps.updaters.GlobalCoordinateUpdater;
import de.htwberlin.f4.ai.ma.indoorroutefinder.gps.updaters.GlobalCoordinateUpdaterFactory;
import de.htwberlin.f4.ai.ma.indoorroutefinder.node.Node;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandler;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandlerFactory;

public class GpsTestActivity extends AppCompatActivity {

    private DatabaseHandler databaseHandler;
    private List<Node> allNodes;

    private Spinner nodeSpinner;
    private TextView txtName;
    private TextView txtCoords;
    private TextView txtAccuracy;

    private ArrayList<String> itemsNodeSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps_test);

        databaseHandler = DatabaseHandlerFactory.getInstance(this);
        allNodes = databaseHandler.getAllNodes();
        itemsNodeSpinner = new ArrayList<>();

        nodeSpinner = (Spinner) findViewById(R.id.gps_node_spinner);
        txtName = (TextView) findViewById(R.id.txtNodeName);
        txtCoords = (TextView) findViewById(R.id.txtNodeCoordinates);
        txtAccuracy = (TextView) findViewById(R.id.txtNodeAccuracy);

        for (Node node : allNodes) {
            itemsNodeSpinner.add(node.getId());
        }

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, itemsNodeSpinner);
        nodeSpinner.setAdapter(adapter);

        nodeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                GlobalNode selected = GlobalNodeFactory.createInstance(databaseHandler.getNode(itemsNodeSpinner.get(position)));
                if (selected != null) {
                    txtName.setText(selected.getId());
                    if (selected.hasGlobalCoordinates()) {
                        txtCoords.setText("lat.: " + selected.getLatitude() + "°; long.: " + selected.getLongitude() + "°");
                        txtAccuracy.setText("+/-" + selected.getGlobalCalculationInaccuracyRating() + "m");
                    }
                    else {
                        txtCoords.setText("None");
                        txtAccuracy.setText("-");
                    }
                }
                else {
                    txtName.setText("NULL!");
                    txtCoords.setText("None");
                    txtAccuracy.setText("-");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                txtName.setText("Nothing selected!");
                txtCoords.setText("None");
                txtAccuracy.setText("-");
            }
        });
    }

    public void updateGpsPositions(View view) {
        GlobalCoordinateUpdater updater = GlobalCoordinateUpdaterFactory.getInstance(this);
        updater.updateGlobalCoordinates();

        // Update View
        GlobalNode selected = GlobalNodeFactory.createInstance(databaseHandler.getNode(itemsNodeSpinner.get(nodeSpinner.getSelectedItemPosition())));
        if (selected != null) {
            txtName.setText(selected.getId());
            if (selected.hasGlobalCoordinates()) {
                txtCoords.setText("lat.: " + selected.getLatitude() + "°; long.: " + selected.getLongitude() + "°");
                txtAccuracy.setText("+/-" + selected.getGlobalCalculationInaccuracyRating() + "m");
            }
            else {
                txtCoords.setText("None");
                txtAccuracy.setText("-");
            }
        }
        else {
            txtName.setText("NULL!");
            txtCoords.setText("None");
            txtAccuracy.setText("-");
        }
    }
}

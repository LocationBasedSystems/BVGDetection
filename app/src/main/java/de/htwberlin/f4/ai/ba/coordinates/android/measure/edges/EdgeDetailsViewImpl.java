package de.htwberlin.f4.ai.ba.coordinates.android.measure.edges;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.carol.bvg.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.htwberlin.f4.ai.ba.coordinates.android.BaseActivity;
import de.htwberlin.f4.ai.ba.coordinates.android.measure.StepData;
import de.htwberlin.f4.ai.ma.edge.Edge;
import de.htwberlin.f4.ai.ma.node.Node;
import de.htwberlin.f4.ai.ma.prototype_temp.EditTextPreference;

/**
 * Created by benni on 12.08.2017.
 */

public class EdgeDetailsViewImpl extends BaseActivity implements EdgeDetailsView{

    private ImageView startNodeImage;
    private ImageView targetNodeImage;

    private TextView startNodeCoordsView;
    private TextView targetNodeCoordsView;
    private TextView startNodeIdView;
    private TextView targetNodeIdView;

    private EditText distanceView;
    private EditText infoView;

    private Switch handycapSwitch;

    private Button deleteBtn;
    private Button saveBtn;

    private ListView stepListView;
    private StepListAdapter stepListAdapter;

    private EdgeDetailsController controller;


    public EdgeDetailsViewImpl() {
        controller = new EdgeDetailsControllerImpl();
        controller.setView(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final FrameLayout contentFrameLayout = (FrameLayout) findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_edge_details, contentFrameLayout);

        Bundle bundle = getIntent().getBundleExtra(EDGE_DETAILS_BUNDLE);

        if (bundle != null) {
            String startNodeID = bundle.getString(STARTNODE_KEY);
            String targetNodeID = bundle.getString(TARGETNODE_KEY);

            if (controller != null) {
                controller.setNodes(startNodeID, targetNodeID);
            }
        }

        startNodeImage = (ImageView) findViewById(R.id.edgedetails_start_image);
        targetNodeImage = (ImageView) findViewById(R.id.edgedetails_target_image);

        startNodeCoordsView = (TextView) findViewById(R.id.edgedetails_start_coords);
        targetNodeCoordsView = (TextView) findViewById(R.id.edgedetails_target_coords);
        startNodeIdView = (TextView) findViewById(R.id.edgedetails_start_id);
        targetNodeIdView = (TextView) findViewById(R.id.edgedetails_target_id);

        distanceView = (EditText) findViewById(R.id.edgedetails_distance_edit);
        infoView = (EditText) findViewById(R.id.edgedetails_info_edit);

        handycapSwitch = (Switch) findViewById(R.id.edgedetails_handycap_switch);

        deleteBtn = (Button) findViewById(R.id.edgedetails_delete);
        saveBtn = (Button) findViewById(R.id.edgedetails_save);

        stepListView = (ListView) findViewById(R.id.edgedetails_steplist);


        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (controller != null) {
                    controller.onDeleteClicked();
                }
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (controller != null) {
                    controller.onSaveClicked();
                }
            }
        });

        handycapSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (controller != null) {
                    controller.onHandycapChanged(b);
                }
            }
        });

        infoView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (controller != null) {
                    controller.onEdgeInfoChanged(editable.toString());
                }
            }
        });

        distanceView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (controller != null) {
                    if (editable.toString().length() > 0) {
                        float value = Float.valueOf(editable.toString());
                        controller.onEdgeDistanceChanged(value);
                    }
                }
            }
        });


        stepListAdapter = new StepListAdapter(getContext(), new ArrayList<StepData>());
        stepListView.setAdapter(stepListAdapter);

    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void updateStartNodeInfo(Node node) {
        startNodeIdView.setText(node.getId());

        Uri imageUri = Uri.parse(node.getPicturePath());
        File image = new File(imageUri.getPath());

        if (image.exists()) {
            //using glide to reduce ui lag
            Glide.with(this)
                    .load(node.getPicturePath())
                    .into(startNodeImage);
        }
        // check if the node has coordinates
        if (node.getCoordinates() != null && node.getCoordinates().length() > 0) {
            String[] splitted = node.getCoordinates().split(";");
            float x = Float.valueOf(splitted[0]);
            float y = Float.valueOf(splitted[1]);
            float z = Float.valueOf(splitted[2]);

            float roundX = Math.round(x * 100.0) / 100.0f;
            float roundY = Math.round(y * 100.0) / 100.0f;
            float roundZ = Math.round(z * 100.0) / 100.0f;

            startNodeCoordsView.setText(roundX + " / " + roundY + " / " + roundZ);
        } else {
            startNodeCoordsView.setText("0.0 / 0.0 / 0.0");
        }

    }

    @Override
    public void updateTargetNodeInfo(Node node) {
        targetNodeIdView.setText(node.getId());

        Uri imageUri = Uri.parse(node.getPicturePath());
        File image = new File(imageUri.getPath());

        if (image.exists()) {
            //using glide to reduce ui lag
            Glide.with(this)
                    .load(node.getPicturePath())
                    .into(targetNodeImage);
        }
        // check if the node has coordinates
        if (node.getCoordinates() != null && node.getCoordinates().length() > 0) {
            String[] splitted = node.getCoordinates().split(";");
            float x = Float.valueOf(splitted[0]);
            float y = Float.valueOf(splitted[1]);
            float z = Float.valueOf(splitted[2]);

            float roundX = Math.round(x * 100.0) / 100.0f;
            float roundY = Math.round(y * 100.0) / 100.0f;
            float roundZ = Math.round(z * 100.0) / 100.0f;

            targetNodeCoordsView.setText(roundX + " / " + roundY + " / " + roundZ);
        } else {
            targetNodeCoordsView.setText("0.0 / 0.0 / 0.0");
        }
    }

    @Override
    public void updateEdgeInfo(Edge edge) {
        // edge weight is in cm, but we use meters, so convert it
        float edgeDistance = edge.getWeight() / 100.0f;
        distanceView.setText(String.valueOf(edgeDistance));

        if (edge.getAccessibly()) {
            handycapSwitch.setChecked(true);
        } else {
            handycapSwitch.setChecked(false);
        }

        infoView.setText(edge.getAdditionalInfo());

        // stepdata
        List<StepData> stepDataList = new ArrayList<>();
        List<String> stepCoords = edge.getStepCoordsList();
        for (String coordStr : stepCoords) {
            String[] splitted = coordStr.split(";");
            float x = Float.valueOf(splitted[0]);
            float y = Float.valueOf(splitted[1]);
            float z = Float.valueOf(splitted[2]);
            StepData stepData = new StepData();
            stepData.setCoords(new float[]{x, y, z});
            stepDataList.add(stepData);
        }

        stepListAdapter.addAll(stepDataList);

    }


    @Override
    protected void onResume() {
        super.onResume();
        if (controller != null) {
            controller.onResume();
        }
    }
}
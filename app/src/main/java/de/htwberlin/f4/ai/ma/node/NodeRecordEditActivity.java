package de.htwberlin.f4.ai.ma.node;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.example.carol.bvg.R;
import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import de.htwberlin.f4.ai.ma.android.BaseActivity;
import de.htwberlin.f4.ai.ma.fingerprint.AsyncResponse;
import de.htwberlin.f4.ai.ma.fingerprint.Fingerprint;
import de.htwberlin.f4.ai.ma.fingerprint.FingerprintTask;
import de.htwberlin.f4.ai.ma.fingerprint.show_fingerprint.ShowFingerprintActivity;
import de.htwberlin.f4.ai.ma.nodelist.NodeListActivity;
import de.htwberlin.f4.ai.ma.persistence.DatabaseHandler;
import de.htwberlin.f4.ai.ma.persistence.DatabaseHandlerFactory;
import de.htwberlin.f4.ai.ma.persistence.FileUtilities;
import de.htwberlin.f4.ai.ma.persistence.JSON.JSONWriter;


/**
 * Created by Johann Winter
 *
 * This class handles the recording and editing of Nodes.
 *
 * Icon sources:
 * https://www.iconfinder.com/icons/322425/camera_icon
 * https://www.iconfinder.com/icons/115789/trash_icon
 * https://www.iconfinder.com/icons/809537/diskette_guardar_multimedia_save_save_disk_technology_icon
 * https://www.iconfinder.com/icons/1608681/exchange_icon
 * https://www.iconfinder.com/icons/2135802/communication_network_tower_wifi_wifi_tower_icon
 * https://www.iconfinder.com/icons/492103/directions_location_navigation_search_socialmedia_icon
 * https://www.iconfinder.com/icons/2135924/location_map_navigation_pointer_icon
 * https://www.iconfinder.com/icons/352562/navigation_icon
 * https://www.iconfinder.com/icons/339913/help_info_information_notice_icon
 * https://www.iconfinder.com/icons/2135801/communication_internet_network_wifi_icon
 * https://www.iconfinder.com/icons/2075795/arrow_below_down_low_icon
 * https://www.flaticon.com/free-icon/fingerprint-with-crosshair-focus_25927
 * http://icons.iconarchive.com/icons/custom-icon-design/flatastic-1/48/export-icon.png
 * http://icons.iconarchive.com/icons/custom-icon-design/flatastic-1/48/import-icon.png
 *
 */

public class NodeRecordEditActivity extends BaseActivity implements AsyncResponse {

    private String picturePath;
    private String oldNodeId = null;
    private List<String> oldPicturePaths;
    String[] permissions;
    private int recordTime;
    private int progressStatus = 0;
    private ProgressBar progressBar;
    private JSONWriter JSONWriter;
    private TextView progressTextview;
    TextView initialWifiTextview;
    TextView initialWifiLabelTextview;
    TextView coordinatesLabelTextview;
    private TextView infobox;
    ImageButton showFingerprintButton;
    private ImageButton recordButton;
    ImageButton captureButton;
    ImageButton saveNodeButton;
    private ImageView cameraImageview;
    private EditText nodeIdEdittext;
    private EditText descriptionEdittext;
    private EditText coordinatesEdittext;
    private DatabaseHandler databaseHandler;
    private SharedPreferences sharedPreferences;
    private Spinner minutesDropdown;
    private boolean pictureTaken;
    private boolean takingPictureAtTheMoment;
    private boolean showingBigPictureAtTheMoment;
    private boolean updateMode = false;
    private boolean verboseMode;
    private boolean useSSIDfilter;
    private Node nodeToUpdate;
    private File sdCard = Environment.getExternalStorageDirectory();
    private Context context = this;
    private WifiManager wifiManager;
    private Timestamp timestamp;
    private Fingerprint fingerprint = null;
    private FingerprintTask fingerprintTask;
    private static final int ASK_MULTIPLE_PERMISSION_REQUEST_CODE = 3;
    private static final int CAM_REQUEST = 1;
    RelativeLayout buttonsLayout;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = (FrameLayout) findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_node_record_edit, contentFrameLayout);

        permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.CAMERA};

        // Check permissions
        if (!hasPermissions(this, permissions)) {
            ActivityCompat.requestPermissions(NodeRecordEditActivity.this, permissions, ASK_MULTIPLE_PERMISSION_REQUEST_CODE);
        }

        databaseHandler = DatabaseHandlerFactory.getInstance(this);
        JSONWriter = new JSONWriter();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        oldPicturePaths = new ArrayList<>();


        recordButton = (ImageButton) findViewById(R.id.record_button);
        captureButton  = (ImageButton) findViewById(R.id.capture_button);
        saveNodeButton = (ImageButton) findViewById(R.id.save_node_button);
        showFingerprintButton = (ImageButton) findViewById(R.id.show_fingerprint_button);
        cameraImageview = (ImageView) findViewById(R.id.camera_imageview);
        descriptionEdittext = (EditText) findViewById(R.id.description_edittext);
        nodeIdEdittext = (EditText) findViewById(R.id.record_id_edittext);
        coordinatesEdittext = (EditText) findViewById(R.id.coordinates_edittext);
        progressTextview = (TextView) findViewById(R.id.progress_textview);
        initialWifiTextview = (TextView) findViewById(R.id.initial_wifi_textview);
        initialWifiLabelTextview = (TextView) findViewById(R.id.initial_wifi_label_textview);
        coordinatesLabelTextview = (TextView) findViewById(R.id.coordinates_label_textview_editmode);
        infobox = (TextView) findViewById(R.id.infobox_record_edit);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        minutesDropdown = (Spinner) findViewById(R.id.minutes_dropdown);

        buttonsLayout = (RelativeLayout) findViewById(R.id.buttons_layout_rec_and_edit);

        picturePath = null;

        pictureTaken = false;
        takingPictureAtTheMoment = false;
        showingBigPictureAtTheMoment = false;

        useSSIDfilter = sharedPreferences.getBoolean("use_ssid_filter", false);
        if (useSSIDfilter) {
            String ssidFilter = sharedPreferences.getString("default_wifi_network", null);
            initialWifiTextview.setText(ssidFilter);
        } else {
            initialWifiTextview.setText(getString(R.string.no_ssid_filter));
        }

        recordButton.setImageResource(R.drawable.fingerprint);
        captureButton.setImageResource(R.drawable.camera);

        progressBar.setVisibility(View.INVISIBLE);
        progressTextview.setVisibility(View.INVISIBLE);
        //initialWifiLabelTextview.setVisibility(View.INVISIBLE);
        //initialWifiTextview.setVisibility(View.INVISIBLE);
        coordinatesLabelTextview.setVisibility(View.INVISIBLE);
        coordinatesEdittext.setVisibility(View.INVISIBLE);


        // Minutes selection dropdown
        List<Integer> minutesList = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            minutesList.add(i + 1);
        }
        ArrayAdapter<Integer> minutesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, minutesList);
        minutesDropdown.setAdapter(minutesAdapter);


        // Check if Update-Mode is enabled
        Intent intent = getIntent();
        if (intent.hasExtra("nodeId")) {
            updateMode = true;
            //initialWifiTextview.setVisibility(View.VISIBLE);
            //initialWifiLabelTextview.setVisibility(View.VISIBLE);
            oldNodeId = (String) intent.getExtras().get("nodeId");
            nodeToUpdate = databaseHandler.getNode(oldNodeId);
            nodeIdEdittext.setText(nodeToUpdate.getId());
            descriptionEdittext.setText(nodeToUpdate.getDescription());
            picturePath = nodeToUpdate.getPicturePath();

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.END_OF, R.id.save_node_button);

            ImageButton deleteNodeButton = new ImageButton(this);
            deleteNodeButton.setLayoutParams(params);
            deleteNodeButton.setImageResource(R.drawable.trash_node);
            buttonsLayout.addView(deleteNodeButton);

            if (nodeToUpdate.getFingerprint() != null) {
                recordButton.setImageResource(R.drawable.fingerprint_done);
                showFingerprintButton.setImageResource(R.drawable.info);

                initialWifiTextview.setText(nodeToUpdate.getFingerprint().getSSID());

                if (nodeToUpdate.getFingerprint().getSSID() == null) {
                    initialWifiTextview.setText(getString(R.string.no_ssid_filter));
                }
            } else {
                initialWifiTextview.setText("-");
            }

            if (nodeToUpdate.getCoordinates().length() > 0) {
                coordinatesEdittext.setVisibility(View.VISIBLE);
                coordinatesLabelTextview.setVisibility(View.VISIBLE);
                coordinatesEdittext.setText(nodeToUpdate.getCoordinates());
            }

            if (picturePath == null) {
                Glide.with(this).load(R.drawable.unknown).into(cameraImageview);
            } else {
                Glide.with(this).load(picturePath).into(cameraImageview);
            }

            deleteNodeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(view.getContext())
                            .setTitle(getString(R.string.nodelist_delete_entry_title_question))
                            .setMessage("Soll der Node \"" + oldNodeId + "\" wirklich gelöscht werden?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                    if (nodeToUpdate.getPicturePath() != null) {
                                        File imageFile = new File(nodeToUpdate.getPicturePath());
                                        imageFile.delete();
                                    }
                                    databaseHandler.deleteNode(nodeToUpdate);

                                    finish();
                                    Intent intent = new Intent(context, NodeListActivity.class);
                                    startActivity(intent);
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            });

            showFingerprintButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, ShowFingerprintActivity.class);
                    intent.putExtra("nodeID", nodeToUpdate.getId());
                    startActivity(intent);
                }
            });
        }


        saveNodeButton.setImageResource(R.drawable.save);

        recordButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (nodeIdEdittext.getText().toString().equals("")){
                        Toast.makeText(getApplicationContext(), getString(R.string.please_enter_node_name), Toast.LENGTH_SHORT).show();
                    } else if (databaseHandler.checkIfNodeExists(nodeIdEdittext.getText().toString()) && !updateMode) {
                        Toast.makeText(getApplicationContext(), getString(R.string.node_already_exists_toast), Toast.LENGTH_SHORT).show();
                    } else {
                        recordButton.setEnabled(false);
                        //recordTimeText.setEnabled(false);
                        progressBar.setVisibility(View.VISIBLE);
                        progressTextview.setVisibility(View.VISIBLE);
                        recordButton.setImageResource(R.drawable.fingerprint_low_contrast);

                        //recordTime = Integer.parseInt(recordTimeText.getText().toString());
                        recordTime = minutesDropdown.getSelectedItemPosition() + 1;


                        verboseMode = sharedPreferences.getBoolean("verbose_mode", false);
                        String ssidFilterString = null;

                        if (verboseMode) {
                            if (useSSIDfilter) {
                                ssidFilterString = sharedPreferences.getString("default_wifi_network", null);
                            }
                            fingerprintTask = new FingerprintTask(ssidFilterString, 60 * recordTime, wifiManager, false, progressBar, progressTextview, infobox);
                        } else {
                            if (useSSIDfilter) {
                                ssidFilterString = sharedPreferences.getString("default_wifi_network", null);
                            }
                            fingerprintTask = new FingerprintTask(ssidFilterString, 60 * recordTime, wifiManager, false, progressBar, progressTextview);
                        }

                        fingerprintTask.delegate = NodeRecordEditActivity.this;
                        fingerprintTask.execute();
                    }
                }
        });


        captureButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                if (nodeIdEdittext.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(), getString(R.string.please_enter_node_name), Toast.LENGTH_SHORT).show();
                } else {
                    takingPictureAtTheMoment = true;
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    timestamp = new Timestamp(System.currentTimeMillis());
                    File file = FileUtilities.getFile(nodeIdEdittext.getText().toString(), timestamp);

                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                    startActivityForResult(cameraIntent, CAM_REQUEST);
                }
                }
        });


        cameraImageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showingBigPictureAtTheMoment = true;
                if (pictureTaken) {
                    Intent intent = new Intent(getApplicationContext(), MaxPictureActivity.class);
                    intent.putExtra("picturePath", picturePath);
                    startActivity(intent);
                } else if (nodeToUpdate != null && nodeToUpdate.getPicturePath() != null) {

                    Intent intent = new Intent(getApplicationContext(), MaxPictureActivity.class);
                    intent.putExtra("picturePath", nodeToUpdate.getPicturePath());
                    startActivity(intent);
                }
            }
        });

        saveNodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (updateMode) {
                    checkUpdatedNodeID();
                } else {
                    saveNewNode();
                }
            }
        });
    }


    /**
     * If the fingerprinting background task finished
     * @param fp the Fingerprint from the AsyncTask
     */
    @Override
    public void processFinish(Fingerprint fp, int seconds) {
        fingerprint = fp;

        if (fp == null) {
            recordButton.setImageResource(R.drawable.fingerprint);
        } else {
            recordButton.setImageResource(R.drawable.fingerprint_done);
        }
        progressBar.setVisibility(View.INVISIBLE);
        progressTextview.setVisibility(View.INVISIBLE);
    }


    /**
     * When returning from Camera Activity after taking a picture
     * if a picture was taken (and not just returned),
     * save the path and show the picture in the imageview.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == -1) {
            nodeIdEdittext.setEnabled(false);
            pictureTaken = true;
            takingPictureAtTheMoment = false;

            oldPicturePaths.add(picturePath);

            long realTimestamp = timestamp.getTime();
            picturePath = sdCard.getAbsolutePath() + "/IndoorPositioning/Pictures/" + nodeIdEdittext.getText() + "_" + realTimestamp + ".jpg";
            Glide.with(this).load(picturePath).into(cameraImageview);
        }
    }


    /**
     * Create and persist the new Node.
     */
    private void saveNewNode() {
        if (nodeIdEdittext.getText().toString().equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.please_enter_node_name), Toast.LENGTH_SHORT).show();
        } else {

                final String picPathToSave;
                if (pictureTaken) {
                    long realTimestamp = timestamp.getTime();
                    picPathToSave = sdCard.getAbsolutePath() + "/IndoorPositioning/Pictures/" + nodeIdEdittext.getText() + "_" + realTimestamp + ".jpg";
                } else {
                    picPathToSave = null;
                }

                final String nodeID = nodeIdEdittext.getText().toString();
                final String nodeDescription = descriptionEdittext.getText().toString();

                if (databaseHandler.checkIfNodeExists(nodeIdEdittext.getText().toString())) {
                    Toast.makeText(getApplicationContext(), getString(R.string.node_already_exists_toast), Toast.LENGTH_LONG).show();
                } else {

                    // If no fingerprint has been captured...
                    if (fingerprint == null) {
                        new AlertDialog.Builder(this)
                                .setTitle(getString(R.string.no_fingerprint_title_text))
                                .setMessage("Soll der Ort \"" + nodeIdEdittext.getText().toString() + "\" wirklich ohne Fingerprint erstellt werden?")
                                .setCancelable(false)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        final Node node = NodeFactory.createInstance(nodeID, nodeDescription, null, "", picPathToSave, "");
                                        JSONWriter.writeJSON(node);
                                        databaseHandler.insertNode(node);
                                        Toast.makeText(context, getString(R.string.node_saved_toast), Toast.LENGTH_LONG).show();
                                        deleteOldPictures();
                                        resetUiElements();
                                        askForNewNode();
                                    }
                                })
                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();

                    // If a fingerprint has been captured...
                    } else {
                        final Node node = NodeFactory.createInstance(nodeID, nodeDescription, fingerprint, "", picPathToSave, "");
                        JSONWriter.writeJSON(node);
                        databaseHandler.insertNode(node);
                        progressStatus = 0;
                        progressTextview.setText(String.valueOf(progressStatus));
                        progressBar.setProgress(progressStatus);
                        Toast.makeText(context, getString(R.string.node_saved_toast), Toast.LENGTH_LONG).show();
                        deleteOldPictures();
                        askForNewNode();
                    }
                }
        }
    }


    /**
     * Check if the given nodeID is valid
     */
    private void checkUpdatedNodeID() {
        if (nodeIdEdittext.getText().toString().equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.please_enter_node_name), Toast.LENGTH_SHORT).show();
        } else {
            if (oldNodeId.equals(nodeIdEdittext.getText().toString())) {
                // old id == new id -> update.
               saveUpdatedNode();
            } else {
                if (databaseHandler.checkIfNodeExists(nodeIdEdittext.getText().toString())) {
                    Toast.makeText(getApplicationContext(), getString(R.string.node_already_exists_toast), Toast.LENGTH_LONG).show();
                } else {
                    saveUpdatedNode();
                }
            }
        }
    }


    /**
     * Save an updated Node to database
     */
    private void saveUpdatedNode() {
        final String picPathToSave;
        final String nodeID = nodeIdEdittext.getText().toString();
        final String nodeDescription = descriptionEdittext.getText().toString();
        final String coordinates = coordinatesEdittext.getText().toString();

        if (pictureTaken) {
            long realTimestamp = timestamp.getTime();
            picPathToSave = sdCard.getAbsolutePath() + "/IndoorPositioning/Pictures/" + nodeIdEdittext.getText() + "_" + realTimestamp + ".jpg";
        } else {
            if (picturePath == null) {
                picPathToSave = null;
            } else {
                picPathToSave = picturePath;
            }
        }

        // If no new fingerprint was captured
        if (fingerprint == null) {
            // If an old fingerprint exists
            if (nodeToUpdate.getFingerprint() != null) {
                final Node node = NodeFactory.createInstance(nodeID, nodeDescription, nodeToUpdate.getFingerprint(), coordinates, picPathToSave, nodeToUpdate.getAdditionalInfo());
                JSONWriter.writeJSON(node);
                databaseHandler.updateNode(node, oldNodeId);
                Toast.makeText(context, getString(R.string.node_saved_toast), Toast.LENGTH_LONG).show();

                finish();
                Intent intent = new Intent(context, NodeListActivity.class);
                startActivity(intent);

            // If no new fingerprint was taken and no old exists
            } else {

            //if (nodeToUpdate.getFingerprint() == null) {
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.no_fingerprint_title_text))
                        .setMessage("Soll der Ort \"" + nodeIdEdittext.getText() + "\" wirklich ohne Fingerprint gespeichert werden?")
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                final Node node = NodeFactory.createInstance(nodeID, nodeDescription, null, coordinates, picPathToSave, "");
                                JSONWriter.writeJSON(node);
                                databaseHandler.updateNode(node, oldNodeId);
                                Toast.makeText(context, getString(R.string.node_saved_toast), Toast.LENGTH_LONG).show();
                                deleteOldPictures();
                                resetUiElements();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }

        // If a new fingerprint was taken
        } else {
            final Node node = NodeFactory.createInstance(nodeID, nodeDescription, fingerprint, coordinates, picPathToSave, nodeToUpdate.getAdditionalInfo());
            JSONWriter.writeJSON(node);
            databaseHandler.updateNode(node, oldNodeId);
            Toast.makeText(context, getString(R.string.node_saved_toast), Toast.LENGTH_LONG).show();
            deleteOldPictures();
            finish();
            Intent intent = new Intent(context, NodeListActivity.class);
            startActivity(intent);
        }
    }


    /**
     * Deletes old picture(s) if a new was taken.
     */
    private void deleteOldPictures() {
        for (String picturePath : oldPicturePaths) {
            if (picturePath != null) {
                File imageFile = new File(picturePath);
                imageFile.delete();
            }
        }
    }


    /**
     * Reset buttons. textfields, and progress.
     * Cancel the fingerprinting backgroundtask.
     */
    private void resetUiElements() {
        if (fingerprintTask != null) {
            fingerprintTask.cancel(false);
        }
        progressStatus = 0;
        progressTextview.setText(String.valueOf(progressStatus));
        progressBar.setProgress(progressStatus);
        recordButton.setEnabled(true);
        //recordTimeText.setEnabled(true);
        nodeIdEdittext.setEnabled(true);
        descriptionEdittext.setEnabled(true);
    }


    /**
     * Ask, if new node should be created. If not, finish() and go to NodeListActivity to show all nodes
     */
    private void askForNewNode() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.record_another_node_title_text))
                .setMessage(getString(R.string.record_another_node_question))
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        startActivity(getIntent());
                    }})
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        Intent intent = new Intent(context, NodeListActivity.class);
                        startActivity(intent);
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


    /**
     * Check for permissions
     * @param context the context
     * @return boolean, if all permissions are given
     */
    private boolean hasPermissions(Context context, String[] permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    System.out.println("----- Permission not granted: " + permission);
                    return false;
                }
            }
        }
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check again for permissions if they were not granted
        if (!hasPermissions(this, permissions)) {
            ActivityCompat.requestPermissions(NodeRecordEditActivity.this, permissions, ASK_MULTIPLE_PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * Stop recording thread if the Activity is stopped,
     * except the user left the activity for taking a picture.
     */
    @Override
    protected void onStop() {
        super.onStop();

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isInteractive();

        if (isScreenOn) {
            if (!takingPictureAtTheMoment && !showingBigPictureAtTheMoment) {
                if (fingerprintTask != null) {
                    fingerprintTask.cancel(false);
                }
            }
        }
    }
}
package de.htwberlin.f4.ai.ma.indoorroutefinder.paperchase;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import de.htwberlin.f4.ai.ma.indoorroutefinder.R;
import de.htwberlin.f4.ai.ma.indoorroutefinder.RouteFinderActivity;
import de.htwberlin.f4.ai.ma.indoorroutefinder.location.locator.LocationSource;
import de.htwberlin.f4.ai.ma.indoorroutefinder.location.locator.Locator;
import de.htwberlin.f4.ai.ma.indoorroutefinder.location.locator.LocatorFactory;
import de.htwberlin.f4.ai.ma.indoorroutefinder.location.locator.listeners.LocationChangeListener;
import de.htwberlin.f4.ai.ma.indoorroutefinder.node.Node;
import de.htwberlin.f4.ai.ma.indoorroutefinder.paperchase.models.Paperchase;

import static de.htwberlin.f4.ai.ma.indoorroutefinder.location.locator.LocatorFactory.getInstance;

public class StartPaperchaseActivity extends AppCompatActivity implements LocationChangeListener{

    TextView paperchaseName;
    TextView paperchaseDescription;
    TextView firstClueName;
    TextView firstClueDescription;
    ImageView firstClueImage;
    Button navigateButton;
    Button beginButton;
    Paperchase paperchase;
    private final static int REQUEST_NAV = 2;
    Locator locator;
    private String oldLocId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_paperchase);

        paperchaseName = (TextView) findViewById(R.id.start_paperchase_name);
        paperchaseDescription = (TextView) findViewById(R.id.start_paperchase_description);
        firstClueName = (TextView) findViewById(R.id.start_paperchase_node_name);
        firstClueDescription = (TextView) findViewById(R.id.start_paperchase_node_description);
        firstClueImage = (ImageView) findViewById(R.id.start_paperchase_imageview);
        navigateButton = (Button) findViewById(R.id.start_navigate_to_first_clue);
        beginButton = (Button) findViewById(R.id.start_begin_paperchase);



        paperchase = (Paperchase) getIntent().getSerializableExtra("paperchase");
        if(paperchase == null){
            setResult(RESULT_CANCELED);
            finish();
        }
        else {
            setTitle(paperchase.getName());
            paperchaseDescription.setText(paperchase.getDescription());
            paperchaseDescription.setMovementMethod(new ScrollingMovementMethod());
            firstClueName.setText(paperchase.getClueList().get(0).getLoc().getId());
            firstClueDescription.setText(paperchase.getClueList().get(0).getLoc().getDescription());

            String picturePath = paperchase.getClueList().get(0).getLoc().getPicturePath();
            if (picturePath == null) {
                firstClueImage.setVisibility(View.VISIBLE);
                firstClueImage.setImageResource(R.drawable.unknown);
            } else if (!picturePath.equals("")) {
                firstClueImage.setVisibility(View.VISIBLE);
                firstClueImage.setImageBitmap(BitmapFactory.decodeFile(picturePath));
            } else {
                firstClueImage.setVisibility(View.INVISIBLE);
            }

            beginButton.setEnabled(false);
            navigateButton.setEnabled(false);
            locator = LocatorFactory.getInstance(getApplicationContext());
            locator.registerLocationListener(this);
            locator.startLocationUpdates();


            navigateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), RouteFinderActivity.class);
                    intent.putExtra("paperchase", true);
                    intent.putExtra("nodeId", paperchase.getClueList().get(0).getLoc().getId());
                    locator.unregisterLocationListener(StartPaperchaseActivity.this);
                    locator.stopLocationUpdates();
                    startActivityForResult(intent, REQUEST_NAV);
                }
            });
            beginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    locator.unregisterLocationListener(StartPaperchaseActivity.this);
                    locator.stopLocationUpdates();
                    Intent intent = new Intent(getApplicationContext(), PlayPaperchaseActivity.class);
                    intent.putExtra("paperchase", paperchase);
                    startActivityForResult(intent, 1);
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 1){
            if(resultCode == RESULT_CANCELED){
                setResult(RESULT_CANCELED);
                finish();
            }
            else if(resultCode == RESULT_OK){
                setResult(RESULT_OK);
                finish();
            }
        }
        else if(requestCode == REQUEST_NAV){
            if(resultCode == RESULT_CANCELED){
                Toast.makeText(this, "Keine Route gefunden", Toast.LENGTH_SHORT).show();
                locator.registerLocationListener(this);
                locator.startLocationUpdates();
            }
            else if(resultCode == RESULT_OK){
                locator.registerLocationListener(this);
                locator.startLocationUpdates();
            }

        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        locator.stopLocationUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        locator.startLocationUpdates();
    }

    @Override
    protected void onStop() {
        super.onStop();
        locator.unregisterLocationListener(StartPaperchaseActivity.this);
        locator.stopLocationUpdates();
        setResult(RESULT_CANCELED);
    }

    @Override
    public void onBackPressed() {
        locator.unregisterLocationListener(StartPaperchaseActivity.this);
        locator.stopLocationUpdates();
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onLocationChanged(final Node newLocation, LocationSource source) {
        if(newLocation!=null && paperchase!=null && paperchase.getClueList()!=null) {
            Toast.makeText(StartPaperchaseActivity.this, "Aktuelle Position: " + newLocation.getId(), Toast.LENGTH_SHORT).show();
            navigateButton.setEnabled(true);
            oldLocId=newLocation.getId();
            if (newLocation.getId().equals(paperchase.getClueList().get(0).getLoc().getId())) {
                beginButton.setEnabled(true);
            } else {
                beginButton.setEnabled(false);
            }


        }
    }
}

package de.htwberlin.f4.ai.ma.indoorroutefinder.paperchase;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Vibrator;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;

import de.htwberlin.f4.ai.ma.indoorroutefinder.MaxPictureActivity;
import de.htwberlin.f4.ai.ma.indoorroutefinder.R;
import de.htwberlin.f4.ai.ma.indoorroutefinder.location.locator.LocationSource;
import de.htwberlin.f4.ai.ma.indoorroutefinder.location.locator.Locator;
import de.htwberlin.f4.ai.ma.indoorroutefinder.location.locator.LocatorFactory;
import de.htwberlin.f4.ai.ma.indoorroutefinder.location.locator.listeners.LocationChangeListener;
import de.htwberlin.f4.ai.ma.indoorroutefinder.node.Node;
import de.htwberlin.f4.ai.ma.indoorroutefinder.paperchase.models.Paperchase;

public class PlayPaperchaseActivity extends AppCompatActivity implements LocationChangeListener {

    private int currentClueId = 0;
    Paperchase paperchase;
    TextView hintText;
    TextView currentNodeText;
    Button nextButton;
    ImageView hintImage;
    private long millisAtStart;
    Locator locator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_paperchase);

        millisAtStart = System.currentTimeMillis();
        paperchase = (Paperchase) getIntent().getSerializableExtra("paperchase");
        setTitle(paperchase.getName());
        hintText = (TextView) findViewById(R.id.play_paperchase_hint);
        currentNodeText = (TextView) findViewById(R.id.play_paperchase_current_node_id);

        nextButton = (Button) findViewById(R.id.play_paperchase_next_button);
        hintImage = (ImageView) findViewById(R.id.play_paperchase_hint_image);
        locator = LocatorFactory.getInstance(getApplicationContext());
        locator.registerLocationListener(this);
        locator.startLocationUpdates();
        setFields();


        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                next(true);
            }
        });
        hintImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 String tmpPath = paperchase.getClueList().get(currentClueId).getHintPicturePath();
                 if (tmpPath != null && !tmpPath.equals("")) {
                    Intent intent = new Intent(getApplicationContext(), MaxPictureActivity.class);
                    intent.putExtra("picturePath", tmpPath);
                    intent.putExtra("nodeID", "Hinweisbild");
                    intent.putExtra("isHint", true);
                    startActivity(intent);
                }
            }
        });


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
        locator.unregisterLocationListener(this);
        locator.stopLocationUpdates();
    }


    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        locator.unregisterLocationListener(this);
        locator.stopLocationUpdates();
        finish();
    }

    private void setFields(){
        currentNodeText.setText("placeholder");
        currentNodeText.setText("Current: " + locator.getLastLocation().getId() + "  (Ziel: " + paperchase.getClueList().get(currentClueId+1).getLoc().getId()+")");
        hintText.setText(paperchase.getClueList().get(currentClueId).getClueText());

        try { //This part blurs the image, if it exists
            if (paperchase.getClueList().get(currentClueId).getHintPicturePath() != null && !paperchase.getClueList().get(currentClueId).getHintPicturePath().equals("")) {
                Log.d("PICTUREPATH---------", paperchase.getClueList().get(currentClueId).getHintPicturePath());
                Bitmap image = BitmapFactory.decodeFile(paperchase.getClueList().get(currentClueId).getHintPicturePath());
                float scale = 0.2f;
                Bitmap inputImage = Bitmap.createScaledBitmap(image, Math.round(image.getWidth() * scale), Math.round(image.getHeight() * scale), false);
                Bitmap outputImage = Bitmap.createBitmap(inputImage);
                RenderScript rs = RenderScript.create(getApplication());
                ScriptIntrinsicBlur intrinsicBlur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
                Allocation tmpIn = Allocation.createFromBitmap(rs, inputImage);
                Allocation tmpOut = Allocation.createFromBitmap(rs, outputImage);
                intrinsicBlur.setRadius(25f);
                intrinsicBlur.setInput(tmpIn);
                intrinsicBlur.forEach(tmpOut);
                tmpOut.copyTo(outputImage);

                hintImage.setImageBitmap(outputImage);
            }
            else{
                hintImage.setVisibility(View.INVISIBLE);
            }
        } catch(Exception e){
            e.printStackTrace();
            hintImage.setVisibility(View.INVISIBLE);}

        if(hintText.getText().length() < 100){
            hintText.setTypeface(hintText.getTypeface(), Typeface.BOLD);
        }

    }

    @Override
    public void onLocationChanged(Node newLocation, LocationSource source) {
        if(currentClueId+1 < paperchase.getClueList().size()) {
            currentNodeText.setText("Current: " + newLocation.getId() + "  (Ziel: " + paperchase.getClueList().get(currentClueId + 1).getLoc().getId() + ")");
        }
        if(newLocation!=null) {
            if(currentClueId+1 < paperchase.getClueList().size()) {
                currentNodeText.setText("Current: " + newLocation.getId() + "  (Ziel: " + paperchase.getClueList().get(currentClueId + 1).getLoc().getId() + ")");
                if (newLocation.getId().equals(paperchase.getClueList().get(currentClueId + 1).getLoc().getId())) {
                    next(false);
                }
            }
        }
    }

    private void next(boolean debug){
        if (currentClueId + 2 < paperchase.getClueList().size()) {
            currentClueId++;
            setFields();
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            // Vibrate for 500 milliseconds
            v.vibrate(500);
            if(debug) {
                onLocationChanged(locator.getLastLocation(), null);
        }
        } else if (currentClueId + 2 == paperchase.getClueList().size()) {
            long millis = System.currentTimeMillis() - millisAtStart;
            Intent intent = new Intent(getApplicationContext(), FinishedPaperchaseActivity.class);
            intent.putExtra("time", millis);
            intent.putExtra("text", paperchase.getClueList().get(currentClueId + 1).getClueText());
            new Thread(new Runnable() {
                public void run() {
                    locator.unregisterLocationListener(PlayPaperchaseActivity.this);
                }
            }).start();
            locator.stopLocationUpdates();
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            // Vibrate for 500 milliseconds
            v.vibrate(1500);
            startActivity(intent);
            setResult(RESULT_OK);
            currentClueId++;
            finish();
        } else {
            setResult(RESULT_CANCELED);
            new Thread(new Runnable() {
                public void run() {
                    locator.unregisterLocationListener(PlayPaperchaseActivity.this);
                }
            }).start();
            locator.stopLocationUpdates();
            finish();
        }
    }
}

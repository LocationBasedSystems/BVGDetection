package de.htwberlin.f4.ai.ma.indoorroutefinder.paperchase;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
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
import de.htwberlin.f4.ai.ma.indoorroutefinder.node.Node;
import de.htwberlin.f4.ai.ma.indoorroutefinder.paperchase.models.Paperchase;

public class PlayPaperchaseActivity extends AppCompatActivity {

    private int currentClueId = 0;
    Paperchase paperchase;
    TextView hintText;
    TextView currentNodeText;
    Button nextButton;
    ImageView hintImage;
    private long millisAtStart;

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
        setFields();
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentClueId +2 < paperchase.getClueList().size()){
                    currentClueId++;
                    setFields();
                    //Toast.makeText(PlayPaperchaseActivity.this, "Toll, einen weiteren Ort gefunden", Toast.LENGTH_SHORT).show();
                }
                else if(currentClueId +2 == paperchase.getClueList().size()){
                    long millis = System.currentTimeMillis() - millisAtStart;
                    Intent intent = new Intent(getApplicationContext(), FinishedPaperchaseActivity.class);
                    intent.putExtra("time", millis);
                    intent.putExtra("text", paperchase.getClueList().get(currentClueId+1).getClueText());
                    startActivity(intent);
                    setResult(RESULT_OK);
                    finish();
                }
                else{
                    setResult(RESULT_CANCELED);
                    finish();
                }
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
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }

    private void setFields(){
        currentNodeText.setText(paperchase.getClueList().get(currentClueId).getLoc().getId());
        hintText.setText(paperchase.getClueList().get(currentClueId).getClueText());

        try {
            Log.d("PICTUREPATH---------", paperchase.getClueList().get(currentClueId).getHintPicturePath());
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
}

package de.htwberlin.f4.ai.ma.indoorroutefinder.paperchase;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import de.htwberlin.f4.ai.ma.indoorroutefinder.R;
import de.htwberlin.f4.ai.ma.indoorroutefinder.paperchase.models.Paperchase;

public class StartPaperchaseActivity extends AppCompatActivity {

    TextView paperchaseName;
    TextView paperchaseDescription;
    TextView firstClueName;
    TextView firstClueDescription;
    ImageView firstClueImage;
    Button navigateButton;
    Button beginButton;
    Paperchase paperchase;

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

        //beginButton.setEnabled(false); //TODO wenn emils listener fertig
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
            navigateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(StartPaperchaseActivity.this, "Not yet implemented", Toast.LENGTH_SHORT).show();
                    //TODO routefinder with custom nodes selected
                }
            });
            beginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
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
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }
}
package de.htwberlin.f4.ai.ma.indoorroutefinder.paperchase;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import de.htwberlin.f4.ai.ma.indoorroutefinder.R;

public class FinishedPaperchaseActivity extends AppCompatActivity {

    private Button endButton;
    private TextView timeText;
    private TextView endText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finished_paperchase);
        setTitle("Schnitzeljagd beendet");
        endButton = (Button) findViewById(R.id.finished_end_button);
        timeText = (TextView) findViewById(R.id.paperchase_finished_time);
        endText = (TextView) findViewById(R.id.paperchase_finished_message);
        timeText.setText("Die Schnitzeljagd hat " + getIntent().getLongExtra("time",0) + " ms gedauert!");
        endText.setText(getIntent().getStringExtra("text"));
        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_OK);
                finish();
            }
        });
    }
}

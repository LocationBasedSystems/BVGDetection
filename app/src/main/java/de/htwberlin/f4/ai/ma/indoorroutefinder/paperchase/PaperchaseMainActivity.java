package de.htwberlin.f4.ai.ma.indoorroutefinder.paperchase;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import de.htwberlin.f4.ai.ma.indoorroutefinder.R;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.BaseActivity;

public class PaperchaseMainActivity extends BaseActivity {

    private ArrayList<Paperchase> paperchaseList = new ArrayList<>();
    private ArrayAdapter<Paperchase> arrayAdapter;
    private ListView listView;
    private FloatingActionButton fab;
    private TextView emptyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_paperchase_main);
        setTitle("Schnitzeljagd");

        FrameLayout contentFrameLayout = (FrameLayout) findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_paperchase_main, contentFrameLayout);

        emptyList = (TextView) findViewById(R.id.paperchase_list_empty);
        listView = (ListView) findViewById(R.id.paperchase_list);
        fab = (FloatingActionButton) findViewById(R.id.fab_add_paperchase);

        arrayAdapter = new ArrayAdapter<Paperchase>(getApplicationContext(), android.R.layout.simple_list_item_1, paperchaseList);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Intent intent = new Intent(getApplicationContext(), SchnitzeljagdSpielenActivity.class);
                //intent.putExtra("schnitzeljagd", paperchaseList.get(position));
                //startActivityForResult(intent, 2);
            }
        });
        arrayAdapter.notifyDataSetChanged();
        listView.setEmptyView(emptyList);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 1) {
            if(resultCode == RESULT_OK){
                //paperchaseList.add((Paperchase) data.getParcelableExtra("schnitzeljagd")); //TODO nodes serializable
                //arrayAdapter.notifyDataSetChanged();
            }
            else if(requestCode == RESULT_CANCELED){
                Toast.makeText(getApplicationContext(), "Schnitzeljagderstellung abgebrochen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void fabClicked(View view) {
        final Intent intent = new Intent(this, AddPaperchaseActivity.class);
        startActivityForResult(intent, 1);
    }
}

package de.htwberlin.f4.ai.ma.indoorroutefinder.paperchase;

import android.app.AlertDialog;
import android.content.DialogInterface;
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

import java.io.File;
import java.util.ArrayList;

import de.htwberlin.f4.ai.ma.indoorroutefinder.R;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.BaseActivity;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.paperchase.PaperchaseDatabaseHandler;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.paperchase.PaperchaseDatabaseHandlerFactory;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.paperchase.PaperchaseDatabaseHandlerImpl;

public class PaperchaseMainActivity extends BaseActivity {

    private ArrayList<Paperchase> paperchaseList;
    private ArrayAdapter<Paperchase> arrayAdapter;
    private ListView listView;
    private FloatingActionButton fab;
    private TextView emptyList;
    private PaperchaseDatabaseHandler databaseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_paperchase_main);
        setTitle("Schnitzeljagd");

        FrameLayout contentFrameLayout = (FrameLayout) findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_paperchase_main, contentFrameLayout);

        databaseHandler = PaperchaseDatabaseHandlerFactory.getInstance(getApplicationContext());
        paperchaseList = new ArrayList<>();
        emptyList = (TextView) findViewById(R.id.paperchase_list_empty);
        listView = (ListView) findViewById(R.id.paperchase_list);
        fab = (FloatingActionButton) findViewById(R.id.fab_add_paperchase);
        arrayAdapter = new ArrayAdapter<Paperchase>(this, android.R.layout.simple_list_item_1, paperchaseList);
        listView.setAdapter(arrayAdapter);
        loadDbData();
        listView.setEmptyView(emptyList);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), StartPaperchaseActivity.class);
                intent.putExtra("paperchase", paperchaseList.get(i));
                startActivityForResult(intent, 2);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long l) {
                new AlertDialog.Builder(view.getContext())
                        .setTitle(getString(R.string.delete_entry_title_question))
                        .setMessage("Soll die Schnitzeljagd \"" + paperchaseList.get(position).getName() + "\" wirklich gelöscht werden?")
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                databaseHandler.deletePaperchase(paperchaseList.get(position));
                                loadDbData();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return true;
            }
        });
    }

    private void loadDbData() {
        paperchaseList.clear();
        paperchaseList.addAll(databaseHandler.getAllPaperchases());
        arrayAdapter.notifyDataSetChanged();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 1) {
            if(resultCode == RESULT_OK){
                Paperchase paperchaseTemp = (Paperchase)data.getSerializableExtra("paperchase");
                databaseHandler.insertPaperchase(paperchaseTemp);
                loadDbData();
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

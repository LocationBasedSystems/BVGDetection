package de.htwberlin.f4.ai.ma.indoorroutefinder.paperchase;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.MenuItem;
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
import de.htwberlin.f4.ai.ma.indoorroutefinder.paperchase.models.Paperchase;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.paperchase.PaperchaseDatabaseHandler;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.paperchase.PaperchaseDatabaseHandlerFactory;

public class PaperchaseMainActivity extends BaseActivity {

    private ArrayList<Paperchase> paperchaseList;
    private ArrayAdapter<Paperchase> arrayAdapter;
    private ListView listView;
    private FloatingActionButton fab;
    private TextView emptyList;
    private PaperchaseDatabaseHandler databaseHandler;
    private static final int REQUEST_START = 2;
    private static final int REQUEST_ADD = 1;
    private static final int REQUEST_CHANGE = 3;
    private int latestEditID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Schnitzeljagden");

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
                startActivityForResult(intent, REQUEST_START);
            }
        });

        registerForContextMenu(listView);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if(v.getId() == R.id.paperchase_list){
            getMenuInflater().inflate(R.menu.paperchase_list_longclick_menu, menu);
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if(item.getItemId() == R.id.paperchase_delete){
            new AlertDialog.Builder(getApplicationContext())
                    .setTitle(getString(R.string.delete_entry_title_question))
                    .setMessage("Soll die Schnitzeljagd \"" + paperchaseList.get(info.position).getName() + "\" wirklich gelöscht werden?")
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            databaseHandler.deletePaperchase(paperchaseList.get(info.position));
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
        else if(item.getItemId() == R.id.paperchase_change){
            Intent intent = new Intent(this, AddPaperchaseActivity.class);
            intent.putExtra("paperchase", paperchaseList.get(info.position));
            latestEditID = info.position;
            startActivityForResult(intent, REQUEST_CHANGE);
        }

        return super.onContextItemSelected(item);
    }

    private void loadDbData() {
        paperchaseList.clear();
        paperchaseList.addAll(databaseHandler.getAllPaperchases());
        arrayAdapter.notifyDataSetChanged();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case REQUEST_ADD:
                if(resultCode == RESULT_OK){
                    Paperchase paperchaseTemp = (Paperchase)data.getSerializableExtra("paperchase");
                    databaseHandler.insertPaperchase(paperchaseTemp);
                    loadDbData();
                }
                else if(requestCode == RESULT_CANCELED){
                    Toast.makeText(getApplicationContext(), "Schnitzeljagderstellung abgebrochen", Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_START:
                if(resultCode == RESULT_OK){
                    Toast.makeText(this, "WAHNSINN!!! DIE SCHNITZELJAGD WURDE ERFOLGREICH GESCHAFFT!", Toast.LENGTH_SHORT).show();
                }
                else if(resultCode == RESULT_CANCELED){
                    Toast.makeText(this, "Schade, Schnitzeljagd abgebrochen", Toast.LENGTH_SHORT).show();;
                }
                break;
            case REQUEST_CHANGE:
                if(resultCode == RESULT_OK){
                    databaseHandler.updatePaperchase((Paperchase) data.getSerializableExtra("paperchase"), paperchaseList.get(latestEditID).getName());
                    loadDbData();
                }
                else if(resultCode == RESULT_CANCELED){
                    Toast.makeText(this, "Bearbeiten abgebrochen", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public void fabClicked(View view) {
        final Intent intent = new Intent(this, AddPaperchaseActivity.class);
        startActivityForResult(intent, 1);
    }
}

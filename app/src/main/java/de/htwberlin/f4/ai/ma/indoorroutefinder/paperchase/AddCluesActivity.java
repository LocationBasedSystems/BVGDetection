package de.htwberlin.f4.ai.ma.indoorroutefinder.paperchase;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import de.htwberlin.f4.ai.ma.indoorroutefinder.R;
import de.htwberlin.f4.ai.ma.indoorroutefinder.node.Node;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandler;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandlerFactory;

public class AddCluesActivity extends AppCompatActivity {

    private ListView listView;
    ArrayList<Node> nodeList;
    DatabaseHandler databaseHandler;
    ArrayAdapter<Node> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_clues);
        setTitle("Orte hinzufügen");
        listView = (ListView) findViewById(R.id.clue_list_add);

        databaseHandler = DatabaseHandlerFactory.getInstance(this);
        nodeList = new ArrayList<>();
        nodeList.addAll(databaseHandler.getAllNodes());
        arrayAdapter = new ArrayAdapter<Node>(this, android.R.layout.simple_list_item_multiple_choice, nodeList); //TODO custom item adapter
        listView.setAdapter(arrayAdapter);
    }

    private class ClueAdapter extends ArrayAdapter<Clue>{

        public ClueAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull List<Clue> objects) {
            super(context, resource, textViewResourceId, objects);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_add_clues, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_cart:
                Intent intent = new Intent();
                //intent.putExtra("clues", (Serializable) nodeList.get(0)); //TODO make Nodes Serializable
                setResult(RESULT_OK, intent);
                finish();
                return true;
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                return false;
            default:
                return true;
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }
}

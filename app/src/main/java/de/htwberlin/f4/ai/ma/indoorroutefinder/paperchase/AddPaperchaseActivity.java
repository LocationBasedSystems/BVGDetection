package de.htwberlin.f4.ai.ma.indoorroutefinder.paperchase;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.woxthebox.draglistview.DragListView;
import com.woxthebox.draglistview.swipe.ListSwipeHelper;
import com.woxthebox.draglistview.swipe.ListSwipeItem;

import java.lang.reflect.Array;
import java.util.ArrayList;

import de.htwberlin.f4.ai.ma.indoorroutefinder.R;
import de.htwberlin.f4.ai.ma.indoorroutefinder.node.Node;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandler;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandlerFactory;

public class AddPaperchaseActivity extends AppCompatActivity  implements  RecyclerViewClickListener{
    private TextInputEditText paperchaseName;
    private TextInputEditText paperchaseDescription;
    private Paperchase paperchase;
    private DragListView dragListView;
    private ItemAdapter listAdapter;
    private FloatingActionButton fab;
    private MySwipeRefreshLayout refreshLayout;

    ArrayList<Node> nodeList;
    DatabaseHandler databaseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_paperchase);

        setTitle("Schnitzeljagd hinzufügen");

        databaseHandler = DatabaseHandlerFactory.getInstance(this);
        nodeList = new ArrayList<>();
        nodeList.addAll(databaseHandler.getAllNodes());

        paperchaseName = (TextInputEditText) findViewById(R.id.add_paperchase_name);
        paperchaseDescription = (TextInputEditText) findViewById(R.id.add_paperchase_description);
        paperchase = new Paperchase("empty");
        fab = (FloatingActionButton)findViewById(R.id.fab_add_clue);
        refreshLayout = (MySwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        dragListView = (DragListView) findViewById(R.id.add_paperchase_draglist);
        dragListView.getRecyclerView().setVerticalScrollBarEnabled(true);
        dragListView.setDragListListener(new DragListView.DragListListenerAdapter() {
            @Override
            public void onItemDragStarted(int position) {
                refreshLayout.setEnabled(false);
                super.onItemDragStarted(position);
            }

            @Override
            public void onItemDragEnded(int fromPosition, int toPosition) {
                refreshLayout.setEnabled(true);
                super.onItemDragEnded(fromPosition, toPosition);
            }
        });
        refreshLayout.setScrollingView(dragListView.getRecyclerView());
        refreshLayout.setColorSchemeColors(ContextCompat.getColor(getApplicationContext(), R.color.light_gray));
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshLayout.setRefreshing(false);
                    }
                }, 4000);
            }
        });
        dragListView.setSwipeListener(new ListSwipeHelper.OnSwipeListenerAdapter() {
            @Override
            public void onItemSwipeStarted(ListSwipeItem item) {
                refreshLayout.setEnabled(false);
            }

            @Override
            public void onItemSwipeEnded(ListSwipeItem item, ListSwipeItem.SwipeDirection swipedDirection) {
                refreshLayout.setEnabled(true);
                if(swipedDirection == ListSwipeItem.SwipeDirection.RIGHT || swipedDirection == ListSwipeItem.SwipeDirection.LEFT){
                    Clue swipedItem = (Clue) item.getTag();
                    paperchase.getClueList().remove(swipedItem);
                    listAdapter.notifyDataSetChanged();
                }
            }
        });
        dragListView.setLayoutManager(new LinearLayoutManager(this));
        listAdapter = new ItemAdapter(paperchase.getClueList(), R.layout.clue_list_item, R.id.clue_list_item_name, true,this);
        dragListView.setAdapter(listAdapter,false);
        dragListView.setCanDragHorizontally(false);
    }


    @Override
    public void recyclerViewListClicked(View v, final int position) {
        final View view = v;
        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        final EditText input = new EditText(v.getContext());
        String text = ((TextView)v.findViewById(R.id.clue_list_item_cluetext)).getText().toString();
        if(!text.equals("Klicke hier um Hinweis hinzuzufügen!")) {
            input.setText(text);
        }
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        if(position == nodeList.size() -1){
            builder.setTitle("Nachricht für das Erreichen des Ziels");
        }
        else {
            builder.setTitle("Hinweis - Wie kommt man zum nächsten Ort?");
        }
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(input.getText().toString().length() > 0){
                    paperchase.getClueList().get(position).setClueText(input.getText().toString());
                    listAdapter.notifyDataSetChanged();
                }
                else{
                    dialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Hinweis bearbeiten fehlgeschlagen - leeres Textfeld", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();

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
                boolean err = false;
                if(paperchaseName.getText().toString().length() <2){
                    paperchaseName.setError("Name muss min 2 Zeichen haben");
                    err = true;
                }
                else if(paperchaseDescription.getText().toString().length() <2){
                    paperchaseDescription.setError("Beschreibung muss min 2 Zeichen haben");
                    err = true;
                }
                else if(paperchase.getClueList().size()<2){
                    Toast.makeText(this, "Mindestens 2 Clues nötig!", Toast.LENGTH_SHORT).show();
                    err = true;
            }
                for(Clue c : paperchase.getClueList()){
                    if(c.getClueText()==null || c.getClueText().length() < 1 || c.getClueText().equals("Klicke hier um Hinweis hinzuzufügen!")){
                        err = true;
                        Toast.makeText(getApplicationContext(), "Alle Punkte müssen Hinweise haben!", Toast.LENGTH_SHORT).show();
                    }
                }
                if(!err) {
                    Intent intent = new Intent();
                    paperchase.setName(paperchaseName.getText().toString());
                    paperchase.setDescription(paperchaseDescription.getText().toString());
                    intent.putExtra("paperchase", paperchase);
                    setResult(RESULT_OK, intent);
                    finish();
                    return true;
                }
                else{
                    return false;
                }
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

    public void generateDummyClues(){
        int size = 10;
        if(nodeList.size()< size){
            size = nodeList.size();
        }
        for(int i = 0; i < size ; i++){
            paperchase.addClue(new Clue("Ein Hinweis (" + i + ")", nodeList.get(i)));
        }
    }

    public void fabClicked(View view) {
        final Intent intent = new Intent(this, AddCluesActivity.class);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 1){
            switch (resultCode){
                case RESULT_OK:
                    Bundle clueBundle = data.getBundleExtra("clues");
                    ArrayList<Node> clues = (ArrayList<Node>) clueBundle.getSerializable("clues");
                    for(Node node : clues){
                        Clue clue = new Clue("Klicke hier um Hinweis hinzuzufügen!", node);
                        paperchase.addClue(clue);
                        listAdapter.notifyDataSetChanged();
                    }
                case RESULT_CANCELED:
                    break;
            }
        }
    }

}

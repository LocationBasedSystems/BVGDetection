package de.htwberlin.f4.ai.ma.indoorroutefinder.paperchase;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.woxthebox.draglistview.DragListView;
import com.woxthebox.draglistview.swipe.ListSwipeHelper;
import com.woxthebox.draglistview.swipe.ListSwipeItem;

import java.util.ArrayList;

import de.htwberlin.f4.ai.ma.indoorroutefinder.R;
import de.htwberlin.f4.ai.ma.indoorroutefinder.node.Node;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandler;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandlerFactory;

public class AddPaperchaseActivity extends AppCompatActivity {
    private EditText paperchaseName;
    private EditText paperchaseDescription;
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

        paperchaseName = (EditText) findViewById(R.id.add_paperchase_name);
        paperchaseDescription = (EditText) findViewById(R.id.add_paperchase_description);
        paperchase = new Paperchase("empty");
        fab = (FloatingActionButton)findViewById(R.id.fab_add_clue);
        generateDummyClues();
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
        refreshLayout.setColorSchemeColors(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshLayout.setRefreshing(false);
                    }
                }, 2000);
            }
        });
        dragListView.setSwipeListener(new ListSwipeHelper.OnSwipeListenerAdapter() {
            @Override
            public void onItemSwipeStarted(ListSwipeItem item) {
                refreshLayout.setEnabled(false);
                super.onItemSwipeStarted(item);
            }

            @Override
            public void onItemSwipeEnded(ListSwipeItem item, ListSwipeItem.SwipeDirection swipedDirection) {
                refreshLayout.setEnabled(true);
                super.onItemSwipeEnded(item, swipedDirection);
                if(swipedDirection == ListSwipeItem.SwipeDirection.LEFT || swipedDirection == ListSwipeItem.SwipeDirection.RIGHT){
                    Clue swipedItem = (Clue) item.getTag();
                    paperchase.getClueList().remove(swipedItem);
                    listAdapter.notifyDataSetChanged();
                }


            }
        });
        dragListView.setLayoutManager(new LinearLayoutManager(this));
        listAdapter = new ItemAdapter(paperchase.getClueList(), R.layout.clue_list_item, R.id.clue_list_item_name, false);
        dragListView.setAdapter(listAdapter,false);
        dragListView.setCanDragHorizontally(false);
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
                //intent.putExtra("paperchase", paperchase); //TODO make Nodes Serializable
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
        super.onActivityResult(requestCode, resultCode, data);
    }
}

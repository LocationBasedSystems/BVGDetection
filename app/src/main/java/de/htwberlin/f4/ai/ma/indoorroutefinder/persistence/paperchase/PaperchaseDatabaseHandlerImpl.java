package de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.paperchase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

import de.htwberlin.f4.ai.ma.indoorroutefinder.paperchase.models.Clue;
import de.htwberlin.f4.ai.ma.indoorroutefinder.paperchase.models.Paperchase;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandler;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandlerFactory;

/**
 * Created by Yannik on 05.12.2017.
 */

public class PaperchaseDatabaseHandlerImpl extends SQLiteOpenHelper implements PaperchaseDatabaseHandler {

    private static final String DATABASE_NAME = "paperchase_data.db";
    private static final int DATABASE_VERSION = 2;

    private static final String PAPERCHASES_TABLE = "paperchases";
    private static final String CLUES_TABLE = "clues";

    private static final String PAPERCHASE_NAME = "name"; //PK
    private static final String PAPERCHASE_DESCRIPTION = "description";

    private static final String CLUE_ID = "id"; //PK
    private static final String CLUE_INDEX = "position";
    private static final String CLUE_HINT = "hint";
    private static final String CLUE_PAPERCHASE = "paperchase"; //FK
    private static final String CLUE_NODE = "node"; //FK
    private static final String CLUE_PICTURE = "picture";

    private Context context;

    PaperchaseDatabaseHandlerImpl(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Paperchases table
        String createPaperchaseTableQuery = "CREATE TABLE " + PAPERCHASES_TABLE + " (" +
                PAPERCHASE_NAME + " TEXT PRIMARY KEY," +
                PAPERCHASE_DESCRIPTION + " TEXT)";

        // Clues table
        String createClueTableQuery = "CREATE TABLE " + CLUES_TABLE + " (" +
                CLUE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                CLUE_INDEX + " INTEGER," +
                CLUE_HINT + " TEXT," +
                CLUE_NODE + " TEXT," +
                CLUE_PAPERCHASE + " TEXT," +
                CLUE_PICTURE + " TEXT)";

        db.execSQL(createPaperchaseTableQuery);
        db.execSQL(createClueTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + CLUES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + PAPERCHASES_TABLE);
        onCreate(db);
    }


    @Override
    public void insertPaperchase(Paperchase paperchase) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(PAPERCHASE_NAME, paperchase.getName());
        values.put(PAPERCHASE_DESCRIPTION, paperchase.getDescription());

        database.insert(PAPERCHASES_TABLE, null, values);

        Log.d("DB: insert_paperchase:", paperchase.getName());
        for(Clue clue : paperchase.getClueList()){
            insertClue(clue, paperchase.getName());
        }
        database.close();
    }

    @Override
    public Paperchase getPaperchase(String paperchaseName) {
        ArrayList<Paperchase> paperchaseList = new ArrayList<>();
        ArrayList<Clue> clueList = new ArrayList<>();
        clueList = getAllCluesOfPaperchase(paperchaseName);
        String selectQuery = "SELECT * FROM " + PAPERCHASES_TABLE + " WHERE " + PAPERCHASE_NAME + " ='" + paperchaseName + "'";
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        Paperchase paperchase = null;
        if(cursor.moveToFirst()){
            paperchase = new Paperchase(cursor.getString(0), cursor.getString(1), clueList);
        }
        return paperchase;
    }

    @Override
    public ArrayList<Paperchase> getAllPaperchases() {
        ArrayList<Paperchase> paperchaseList = new ArrayList<>();
        ArrayList<Clue> clueList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + PAPERCHASES_TABLE;
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if(cursor.moveToFirst()){
            do{
                Paperchase paperchase = new Paperchase(cursor.getString(0), cursor.getString(1), getAllCluesOfPaperchase(cursor.getString(0)));
                paperchaseList.add(paperchase);
            }while(cursor.moveToNext());
        }
        return  paperchaseList;
    }

    @Override
    public boolean checkIfPaperchaseExists(String paperchaseName) {
        if(getPaperchase(paperchaseName) != null){
            return true;
        }
        else{ return false; }
    }

    @Override
    public void deletePaperchase(Paperchase paperchase) {
        SQLiteDatabase database = this.getWritableDatabase();
        String deleteQuery = "DELETE FROM " + PAPERCHASES_TABLE + " WHERE " + PAPERCHASE_NAME + "='" + paperchase.getName() + "' AND " +
                PAPERCHASE_DESCRIPTION + " ='" + paperchase.getDescription() + "'";
        Log.d("DB: delete_Paperchase:", paperchase.getName());
        database.execSQL(deleteQuery);
    }

    @Override
    public void insertClue(Clue clue, String paperchaseName) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(CLUE_INDEX, clue.getIdx());
        values.put(CLUE_HINT, clue.getClueText());
        values.put(CLUE_NODE, clue.getLoc().getId());
        values.put(CLUE_PAPERCHASE, paperchaseName);
        values.put(CLUE_PICTURE, clue.getHintPicturePath());

        database.insert(CLUES_TABLE, null, values);

        Log.d("DB: insert_clue:hint:", clue.getClueText());
        //Log.d("DB:PATH-----:" , clue.getHintPicturePath());
        database.close();
    }

    @Override
    public Clue getClue(int clueId) {
        SQLiteDatabase database = this.getReadableDatabase();
        DatabaseHandler databaseHandler = DatabaseHandlerFactory.getInstance(context);
        String selectQuery = "SELECT * FROM " + CLUES_TABLE + " WHERE " + CLUE_ID + "='" + clueId + "'";
        Clue clue  = null;
        Cursor cursor = database.rawQuery(selectQuery, null);
        if(cursor.moveToFirst() && databaseHandler.getNode(cursor.getString(3))!=null){
            clue = new Clue(cursor.getString(2),cursor.getInt(1), databaseHandler.getNode(cursor.getString(3)), cursor.getString(5));
        }
        return clue;
    }

    @Override
    public ArrayList<Clue> getAllCluesOfPaperchase(String paperchaseName) {
        SQLiteDatabase database = this.getReadableDatabase();
        DatabaseHandler databaseHandler = DatabaseHandlerFactory.getInstance(context);
        String selectQuery = "SELECT * FROM " + CLUES_TABLE + " WHERE " + CLUE_PAPERCHASE + "='" + paperchaseName + "'";
        ArrayList<Clue> clueList = new ArrayList<>();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if(cursor.moveToFirst()){
            do {
                if(databaseHandler.getNode(cursor.getString(3))!=null) {
                    clueList.add(new Clue(cursor.getString(2), cursor.getInt(1), databaseHandler.getNode(cursor.getString(3)),cursor.getString(5)));
                    //Log.d("DB:PATH GET ------:", cursor.getString(5));
                }
            }while(cursor.moveToNext());
        }
        return clueList;
    }

    @Override
    public boolean checkIfClueExists(int clueId) {
        if(getClue(clueId) != null){
            return true;
        }
        else{ return false; }
    }

    @Override
    public void deleteCluesByPaperchase(String paperchaseName) {
        SQLiteDatabase database = this.getWritableDatabase();
        String deleteQuery = "DELETE FROM " + CLUES_TABLE + " WHERE " + CLUE_PAPERCHASE + "='" + paperchaseName + "'";
        Log.d("DB: delete_Clues:", paperchaseName);

        database.execSQL(deleteQuery);
        database.close();
    }
}

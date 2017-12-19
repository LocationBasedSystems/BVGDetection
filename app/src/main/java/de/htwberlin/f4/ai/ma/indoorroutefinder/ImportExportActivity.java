package de.htwberlin.f4.ai.ma.indoorroutefinder;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.BaseActivity;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandler;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandlerFactory;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.paperchase.PaperchaseDatabaseHandler;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.paperchase.PaperchaseDatabaseHandlerFactory;

/**
 * Created by Johann Winter
 *
 * Activity for import / export of the database
 */

public class ImportExportActivity extends BaseActivity {

    ImageButton importButton;
    ImageButton exportButton;
    private DatabaseHandler databaseHandler;
    private Context context;
    private static final int PICKFILE_REQUEST_CODE = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.title_activity_importexport));

        final FrameLayout contentFrameLayout = (FrameLayout) findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_import_export, contentFrameLayout);

        context = getApplicationContext();

        importButton = (ImageButton) findViewById(R.id.import_button);
        exportButton = (ImageButton) findViewById(R.id.export_button);

        databaseHandler = DatabaseHandlerFactory.getInstance(this);

        importButton.setImageResource(R.drawable.import_icon);
        exportButton.setImageResource(R.drawable.export_icon);

        importButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                importDatabase();
            }
        });
        exportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exportDatabase();
            }
        });
    }


    /**
     * Ask the user if he or she wants to overwrite the application's database
     * and then copy the file "/IndoorPositioning/Exported/indoor_data.db" over the internal database.
     */
    private void importDatabase() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.import_title_question))
                .setMessage(getString(R.string.import_database_warining))
                .setCancelable(true)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            boolean importSuccessful = DatabaseHandlerFactory.getInstance(context).importDatabase();
                            if (importSuccessful) {
                                boolean paperchaseImportSuccessful = PaperchaseDatabaseHandlerFactory.getInstance(context).importDatabase();
                                if(paperchaseImportSuccessful){
                                    Toast.makeText(context, "Datenbanken importiert", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    Toast.makeText(context, getString(R.string.database_imported_toast), Toast.LENGTH_LONG).show();
                                }

                            }
                        } catch (IOException e) {e.printStackTrace();}
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {}
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


    /**
     * If already existent, ask the user if he wants to overwrite the exported database file "/IndoorPositioning/Exported/indoor_data.db".
     * If the user agrees, or the file is non-existent, it will be overwritten by the database dump which
     * will be created from the application's database.
     */
    private void exportDatabase() {
        File exportFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/IndoorPositioning/Exported/indoor_data.db");

        if (exportFile.exists()) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.export_title_question))
                    .setMessage(getString(R.string.export_database_warning))
                    .setCancelable(true)
                    .setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            boolean exportSuccessful = databaseHandler.exportDatabase();
                            if (exportSuccessful) {
                                boolean paperchaseExportSuccessful = PaperchaseDatabaseHandlerFactory.getInstance(context).exportDatabase();
                                if(paperchaseExportSuccessful){
                                    Toast.makeText(context, "Datenbanken exportiert", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    Toast.makeText(context, getString(R.string.database_exported_toast), Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {}
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

        } else {
            boolean exportSuccessful = databaseHandler.exportDatabase();
            if (exportSuccessful) {
                Toast.makeText(context, getString(R.string.database_exported_toast), Toast.LENGTH_LONG).show();
            }
        }
    }

}

package com.example.casey.altimeter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Zone class will show all the measurements that have been saved
 * for the selected zone. It will allow you to delete and add a new measurement as well.
 */
public class Zone extends AppCompatActivity {

    //Declare the class variables
    private String name;
    private double compAlt = 0.0;
    private int compID = 0;
    private boolean canViewMap;

    private int zoneID;
    private int projectID;
    Context ctx = this;
    TextView name_col, elevation_col, action_col, col0, col1;
    ImageButton add_btn;
    ImageButton deleteButton;
    ImageButton compareButton;
    TableLayout measurementTable;
    TableRow newTR;
    LinearLayout newLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zone);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Get the Project ID from the last activity.
        Bundle extras = getIntent().getExtras();
        zoneID = extras.getInt("ZID");
        projectID = extras.getInt("PID");

        canViewMap = false; //initialize

        //Set up the table variables.
        name_col = (TextView)findViewById(R.id.nameCol);
        elevation_col = (TextView)findViewById(R.id.elevationCol);
        action_col = (TextView)findViewById(R.id.actionCol);
        add_btn = (ImageButton)findViewById(R.id.add_btn);
        measurementTable = (TableLayout)findViewById(R.id.measurementTable);
        measurementTable.setColumnStretchable(0, true);
        measurementTable.setColumnStretchable(1, true);
        measurementTable.setColumnStretchable(2, true);

        //Initialize the measurements table for the current zone.
        try{
            addMeasurements();
            Log.i("Initializing Table","Initilized Measurement table successfully!");
        }catch (Exception ex){
            Logger.getLogger(Zone.class.getName()).log(Level.SEVERE, null, ex);
            Log.e("Init Table Error", "Initilized Measurement table Failed!!!!");
        }
        // Click to add a new measurement.
        final ImageButton btnOpenPopup = (ImageButton)findViewById(R.id.add_btn);
        btnOpenPopup.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // create Intent
                Intent intent = new Intent(getApplicationContext(),
                        Current_Elevation.class);
                // put values to intent
                intent.putExtra("ZID", zoneID);
                intent.putExtra("PID", projectID);
                // Start Activity
                startActivity(intent);

            }
        });
    }

    /**
     * Sets the Zone name for the activity.
     */
    public void setName(String theName) { name = theName; }

    /**
     * Returns the zone name.
     */
    public String getName() { return name; }

    /**
     * Goes to the Mapping activity if the map view button is clicked
     */
    public void mapViewClick(View v){
        if(canViewMap == true) {
            // create Intent
            Intent intent = new Intent(getApplicationContext(),
                    Mapping.class);
            // put values to intent
            intent.putExtra("ZID", zoneID);
            // Start Activity
            startActivity(intent);
        }
        else {
            Toast.makeText(getApplicationContext(), "Can view map only with 2+ Measurements!",
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Add all the measurements to the table for the current zone.
     */
    private void addMeasurements() {
        int count = 0;
        final DatabaseOperations DB = new DatabaseOperations(ctx);
        final Cursor CR = DB.getMeasurement(DB, zoneID);
        final float scale = getResources().getDisplayMetrics().density;
        int padding_15dp = (int) (15 * scale + 0.5f);
        String testName = "";
        String theAltitude = "";
        int theID;
        CR.moveToFirst();
        if(CR == null)
            return;
        //Add all of the measurements to the table from the current zone.
        do{
            count++;
            testName = CR.getString(3);
            theID = CR.getInt(0);
            theAltitude = CR.getString(4)+"\' "+CR.getString(5)+"\"";

            //Name column stuff
            col0 = new TextView(this);
            col0.setGravity(Gravity.LEFT);
            col0.setPadding(padding_15dp,0,0,0);
            col0.setText(testName);
            col0.setTextSize(19);
            col0.setTextColor(Color.BLACK);

            //Elevation stuff
            col1 = new TextView(this);
            col1.setGravity(Gravity.LEFT);
            col1.setPadding(padding_15dp,0,0,0);
            col1.setText(theAltitude);
            col1.setTextSize(19);
            col1.setTextColor(Color.BLACK);

            final int finalTheID = theID;
            final double finalFeet = (double) CR.getInt(4);
            final double finalInches = (double) CR.getFloat(5);

            //compare button stuff
            compareButton = new ImageButton(this);
            compareButton.setId(theID);
            compareButton.setPadding(0, 0, 0, 0);
            compareButton.setBackgroundDrawable(null);
            compareButton.setImageResource(R.mipmap.compare);
            compareButton.setPadding(0, 0, 0, 0);
            compareButton.setOnClickListener(new ImageButton.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (compAlt == 0.0) {
                        //Get the altitude and set those variable for comparison.
                        compAlt = finalFeet + (finalInches / 12);
                        compID = finalTheID;
                        //Gray out this compare button.

                        Toast.makeText(getApplicationContext(), "Select another measurement to compare.",
                                Toast.LENGTH_LONG).show();
                    } else {
                        double theDiff = Math.abs((finalFeet + finalInches / 12) - compAlt);
                        long iPart;
                        double fPart;
                        // Get user input;
                        iPart = (long) theDiff;
                        fPart = theDiff - iPart;
                        fPart = fPart * 12;
                        fPart = Math.floor(fPart * 100) / 100;

                        Toast.makeText(getApplicationContext(), "Difference = " + Long.toString(iPart) + "\' " + fPart + "\"",
                                Toast.LENGTH_LONG).show();
                        compAlt = 0.0;
                        compID = 0;
                        //Ungray out the first compare button.

                    }
                }
            });

            //Delete button stuff
            deleteButton = new ImageButton(this);
            deleteButton.setId(theID);
            deleteButton.setBackgroundDrawable(null);
            deleteButton.setImageResource(R.mipmap.deletebutton);
            deleteButton.setPadding(0, 0, 0, 0);
            final String finalTestName = testName;
            deleteButton.setOnClickListener(new ImageButton.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Zone.this);
                    builder.setTitle("Delete Measurement");
                    builder.setMessage("Are you sure you want to delete this measurement?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Delete the Project and all of the associated files
                            DB.deleteMeasurement(DB, finalTheID);
                            Intent intent = getIntent();
                            finish();
                            startActivity(intent);
                            Toast.makeText(getApplicationContext(), "Deleted: " + finalTestName,
                                    Toast.LENGTH_LONG).show();
                        }
                    });

                    builder.setNegativeButton("No", null);
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            });

            //New layout stuff adding the compare and deletes button to the action column
            newLayout = new LinearLayout(this);
            newLayout.addView(compareButton);
            newLayout.addView(deleteButton);
            newLayout.setOrientation(LinearLayout.HORIZONTAL);
            newLayout.setGravity(Gravity.END);
            newLayout.setPadding(0, 0, padding_15dp, 0);

            //New row stuff
            newTR = new TableRow(this);
            newTR.setGravity(Gravity.CENTER_VERTICAL);
            newTR.addView(col0);
            newTR.addView(col1);
            newTR.addView(newLayout);
            measurementTable.addView(newTR);
        }while(CR.moveToNext());//End of do while loop

        if(count > 1)
            canViewMap = true;
        CR.close();
        DB.close();
    }

    /**
     * Basically connects the zone menu to the layout
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_zone, menu);
        return true;
    }

    /**
     * Tells what happens when a menu item is selected.
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.home_btn:
                // create Intent
                Intent intent = new Intent(getApplicationContext(),
                        Altimeter.class);
                // Start Activity
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

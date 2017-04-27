package com.example.casey.altimeter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Mapping extends AppCompatActivity {

    //Class variables
    private int zoneID;
    Context ctx = this;
    ArrayList<Double> theLats;//This will be manipulated
    ArrayList<Double> theLons;//This will be manipulated
    ArrayList<Double> realLats;
    ArrayList<Double> realLons;
    ArrayList<String> theAlts;
    ArrayList<String> theNames;
    double maxLon;
    double maxLat;
    GridLayout theGrid;

    /**
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapping);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Set up variables
        theGrid = (GridLayout)findViewById(R.id.theGrid);
        theLats = new ArrayList<Double>();
        theLons = new ArrayList<Double>();
        realLats = new ArrayList<Double>();
        realLons = new ArrayList<Double>();
        theAlts = new ArrayList<String>();
        theNames = new ArrayList<String>();

        //Get the Project/Zone ID from the last activity.
        Bundle extras = getIntent().getExtras();
        zoneID = extras.getInt("ZID");

        //get measurements for this zone and set the global lat/lon variables
        setMeasurements();
        //Downsize the measurements to make them easier to work with.
        //It will also call setMaxes() method.
        downsize();
        //Plot the measuremnets that we have.
        plotMeasurements();
    }

    /**
     * Sets the maxLong & maxLat variables.
     */
    private void setMaxes() {
        double tempMax = 0;
        for(int i = 0; i < theLats.size(); i++){
            if(tempMax < theLats.get(i))
                tempMax = theLats.get(i);
        }
        maxLat = tempMax;

        //Do the same thing for the longitudes.
        tempMax = 0;
        for(int i = 0; i < theLons.size(); i++){
            if(tempMax < theLons.get(i))
                tempMax = theLons.get(i);
        }
        maxLon = tempMax;

    }

    /**
     * Resets the lat/lon list variables to make them smaller numbers that are
     * easier to work with.
     */
    private void downsize() {
        //Find the smallest latitude and subtract that number from each latitude.
        double minLat = 999999;
        for(int i = 0; i < theLats.size(); i++){
            if(minLat > theLats.get(i)){
                minLat = theLats.get(i);
            }
        }
        //reset the lat values.
        for(int i = 0; i < theLats.size(); i++)
            theLats.set(i,(theLats.get(i) - minLat));


        //Find the smallest longitude and subtract that number from each longitude.
        double minLon = 999999;
        for(int i = 0; i < theLats.size(); i++){
            if(minLon > theLons.get(i)){
                minLon = theLons.get(i);
            }
        }
        //reset the lon values.
        for(int i = 0; i < theLons.size(); i++)
            theLons.set(i,(theLons.get(i) - minLon));

        //Sets the maxLong & maxLat variables.
        setMaxes();
    }

    /**
     * Plots the measurements on the screen scaled differently
     * depending on the distance between each measurement
     */
    private void plotMeasurements() {
        //Setup the grid with a padding of 2ish
        theGrid.setRowCount((int) maxLon * 1000 + 2);
        theGrid.setColumnCount((int) maxLat * 1000 + 2);

        for(int i = 0; i < theLats.size(); i++) {
            final int iFinal = i;
            ImageButton tempBtn = new ImageButton(this);
            tempBtn.setBackgroundDrawable(null);
            tempBtn.setImageResource(R.mipmap.pin);
            tempBtn.setOnClickListener(new ImageButton.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Mapping.this);
                    builder.setTitle("Measurement Info");
                    String theMessage = "Point Name: "+theNames.get(iFinal)+"\n"+
                                        "Altitude: "+theAlts.get(iFinal)+"\n"+
                                        "Latitude: "+realLats.get(iFinal)+"\n"+
                                        "Longitude: "+realLons.get(iFinal)+"\n";
                    builder.setMessage(theMessage);
                    builder.setPositiveButton("OK", null);

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            });
            GridLayout.LayoutParams param = new GridLayout.LayoutParams();
            param.setGravity(Gravity.CENTER);
            param.columnSpec = GridLayout.spec(theLats.get(i).intValue()*1000);
            param.rowSpec = GridLayout.spec(theLons.get(i).intValue()*1000);
            tempBtn.setLayoutParams (param);

            theGrid.addView(tempBtn);
        }
    }

    /**
     * Sets theLats/theLons lists to all of the zone's measurement variables.
     */
    private void setMeasurements() {

        final DatabaseOperations DB = new DatabaseOperations(ctx);
        final Cursor CR = DB.getMeasurement(DB, zoneID);
        CR.moveToFirst();
        if(CR == null)
            return;
        do{
            realLats.add(CR.getDouble(6));
            realLons.add(CR.getDouble(7));
            theLats.add(CR.getDouble(6)*10000);//Add the measurements to the Latitude List
            theLons.add(CR.getDouble(7)*10000);//Add the measurements to the Longitude List
            theAlts.add(""+CR.getInt(4)+"\' "+CR.getDouble(5)+"\"");//Add the alts to the Altitude List
            theNames.add(CR.getString(3));//Add the names to the List
        }while(CR.moveToNext());
    } // End of onCreate method

    /**
     * Basically connects the Mapping menu to the layout
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_mapping, menu);
        return true;
    }

    /**
     * Decides what happens when a menu item is clicked.
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

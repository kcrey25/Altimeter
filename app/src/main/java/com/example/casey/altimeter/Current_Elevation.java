package com.example.casey.altimeter;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.PopupWindow;

/**
 * This class/Activity will show what the user's current elevation is.
 * It will use the Phone's GPS.
 */
public class Current_Elevation extends AppCompatActivity {
    //Declare the class variables
    private static final double CONV_TO_FEET = 3.280839895;
    TextView theCurElevation;
    View theParent;
    private LocationManager locationManager;
    private LocationListener locationListener;
    protected int altFeet;
    protected double altInches;
    protected double theLat;
    protected double theLon;

    //Used to save elevation data before it updates again after
    //the save button is pressed.
    protected int saveAltFeet;
    protected double saveAltInches;
    protected double saveLat;
    protected double saveLon;

    protected String newMeasurementName;
    private int zoneID;
    private int projectID;
    Context ctx = this;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current__elevation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        theCurElevation = (TextView) findViewById(R.id.curElev);
        //theParent = findViewById(R.layout.content_current__elevation);
        //Get the Project/Zone ID from the last activity.
        Bundle extras = getIntent().getExtras();
        zoneID = extras.getInt("ZID");
        projectID = extras.getInt("PID");


        // Do all of the location stuff here.
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                double num;
                long iPart;
                double fPart;

                // Get user input
                //num = location.getAltitude()*CONV_TO_FEET;
                num = location.getAltitude() * CONV_TO_FEET;
                iPart = (long) num;
                fPart = num - iPart;
                fPart = fPart*12;
                fPart = Math.floor(fPart * 100) / 100;

                //Save the Class Variables
                altFeet = (int)iPart;
                altInches = fPart;
                theLat = location.getLatitude();
                theLon = location.getLongitude();

                //Print out the elevation to the screen in feet/inches
                theCurElevation.setText(iPart + "\' " + fPart + "\"");
                theCurElevation.setTextColor(Color.WHITE);
                theCurElevation.setGravity(Gravity.CENTER_VERTICAL);
            }

            /**
             *
             * @param provider
             * @param status
             * @param extras
             */
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) { }

            /**
             *
             * @param provider
             */
            @Override
            public void onProviderEnabled(String provider) { }

            /**
             * Go to location settings if it is disabled
             * @param provider
             */
            @Override
            public void onProviderDisabled(String provider) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.INTERNET
            }, 10 );
            return;
        }
        locationManager.requestLocationUpdates("gps", 1000, 0, locationListener); // 1000->1 second

        // Save Button opens a popup window.
        final Button btnOpenPopup = (Button)findViewById(R.id.save_btn);
        btnOpenPopup.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                //Save these variables so the user can move the device while entering the name of the measurement.
                saveAltFeet = altFeet;
                saveAltInches = altInches;
                saveLat = theLat;
                saveLon = theLon;

                LayoutInflater layoutInflater
                        = (LayoutInflater) getBaseContext()
                        .getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = layoutInflater.inflate(R.layout.popupwindow, null);
                final PopupWindow popupWindow = new PopupWindow(
                        popupView,
                        LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT);
                final EditText ElevationName = (EditText) popupView.findViewById(R.id.editText);
                //Save button on the popup
                Button btnSave = (Button) popupView.findViewById(R.id.save);
                btnSave.setOnClickListener(new Button.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        newMeasurementName = ElevationName.getText().toString();
                        if(newMeasurementName.equals("")){
                            Toast.makeText(getApplicationContext(), "Please enter a name",
                                    Toast.LENGTH_LONG).show();
                        }
                        else {
                            //This should save the name and the GPS data to storage.
                            //Use the class variables... saveAltFeet, saveAltInches, saveLat, saveLon
                            DatabaseOperations DB = new DatabaseOperations(ctx);
                            DB.saveNewMeasurement(DB, newMeasurementName, projectID, zoneID, saveAltFeet, saveAltInches, saveLat, saveLon);

                            // create Intent
                            Intent intent = new Intent(getApplicationContext(),
                                    Zone.class);
                            // put values to intent
                            intent.putExtra("ZID", zoneID);
                            intent.putExtra("PID", projectID);
                            // Start Activity
                            startActivity(intent);

                            // TODO Auto-generated method stub
                            popupWindow.dismiss();
                        }

                    }
                });

                //Cancel button on the popup
                Button btnCancel = (Button) popupView.findViewById(R.id.dismiss);
                btnCancel.setOnClickListener(new Button.OnClickListener() {

                    @Override
                    public void onClick(View v) { popupWindow.dismiss(); }
                }); // End of popup window stuff.

                popupWindow.setTouchable(true);
                popupWindow.setFocusable(true);
                popupWindow.showAsDropDown(findViewById(R.id.textView), -250, 0);

            }
        });

    }

    /**
     * This Class updates the location data every half second.
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        switch (requestCode){
            case 10:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    locationManager.requestLocationUpdates("gps", 500, 0, locationListener);
                return;
        }
    }

    /**
     * Connects the current elevation menu to its layout
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_current_elevation, menu);
        return true;
    }

    /**
     * This decides what happens when the home button is clicked
     * or any other menu item if we had any.
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

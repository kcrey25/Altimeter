package com.example.casey.altimeter;

import android.app.ActionBar;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class Project extends AppCompatActivity {
    //Declare all the class variables
    protected String newZoneName;
    private int projectID;
    Context ctx = this;
    private String tempStrZoneID;

    TextView name_col, action_col, col0;
    TableLayout ZonesTable;
    LinearLayout newLayout;
    TableRow newTR;
    ImageButton viewButton;
    ImageButton deleteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Get the Project ID from the last activity.
        Bundle extras = getIntent().getExtras();
        String tempStr = extras.getString("ID");
        projectID = Integer.parseInt(tempStr);

        //Set up the table variables.
        name_col = (TextView)findViewById(R.id.nameCol);
        action_col = (TextView)findViewById(R.id.actionCol);
        ZonesTable = (TableLayout)findViewById(R.id.zoneTable);
        ZonesTable.setColumnStretchable(0, true);
        ZonesTable.setColumnStretchable(1, true);

        //Add all of the zones to the table for the current project
        try{
            addZones();
            Log.i("Initializing Table", "Initialized Measurement table successfully!");
        }catch(Exception ex){
            ex.printStackTrace();
            Log.i("Initializing Table", "Failed to initilized table!");
        }

        final ImageButton btnOpenPopup = (ImageButton)findViewById(R.id.add_btn);
        //New project popup
        btnOpenPopup.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                LayoutInflater layoutInflater
                        = (LayoutInflater) getBaseContext()
                        .getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = layoutInflater.inflate(R.layout.popupwindow, null);
                final PopupWindow popupWindow = new PopupWindow(
                        popupView,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                final EditText projectName = (EditText) popupView.findViewById(R.id.editText);
                //Popup save button stuff
                Button btnSave = (Button) popupView.findViewById(R.id.save);
                btnSave.setOnClickListener(new Button.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        newZoneName = projectName.getText().toString();
                        if (newZoneName.equals("")) {
                            Toast.makeText(getApplicationContext(), "Please enter a name",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            //This should save the name and the GPS data to storage.
                            //Use newProjectName to save new Zone Name
                            // Make sure it saves in the correct Project.
                            DatabaseOperations DB = new DatabaseOperations(ctx);
                            DB.saveNewZone(DB, newZoneName, projectID);

                            Intent intent = getIntent();
                            finish();
                            startActivity(intent);

                            Toast.makeText(getApplicationContext(), "Saved as: " + newZoneName,
                                    Toast.LENGTH_LONG).show();
                            // TODO Auto-generated method stub
                            popupWindow.dismiss();
                        }

                    }
                });

                //Popup cancel button stuff.
                Button btnCancel = (Button) popupView.findViewById(R.id.dismiss);
                btnCancel.setOnClickListener(new Button.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        //This should save the name and the GPS data to storage.

                        // TODO Auto-generated method stub
                        popupWindow.dismiss();
                    }
                });
                popupWindow.setTouchable(true);
                popupWindow.setFocusable(true);
                popupWindow.showAsDropDown(findViewById(R.id.blankView), 50, 0);

            }
        });
    }


    private void addZones() {
        int count = 0;
        final DatabaseOperations DB = new DatabaseOperations(ctx);
        //DB.saveNewProject(DB, newProjectName);

        final Cursor CR = DB.getZone(DB, projectID);
        final float scale = getResources().getDisplayMetrics().density;
        int padding_15dp = (int) (15 * scale + 0.5f);
        String testName = "";
        int theID;
        CR.moveToFirst();
        int ct = 0;

        if(CR == null)
            return;
        //Go through all the zones in the current project and list them in the table.
        do {
            testName = CR.getString(2);
            theID = CR.getInt(0);

            //Name column stuff
            col0 = new TextView(this);
            col0.setPadding(padding_15dp,0,0,0);
            col0.setGravity(Gravity.START);
            col0.setText(testName);
            col0.setTextSize(25);

            //View button stuff
            viewButton = new ImageButton(this);
            viewButton.setId(theID);
            viewButton.setPadding(0, 0, 0, 0);
            viewButton.setBackgroundDrawable(null);
            viewButton.setImageResource(R.mipmap.view);
            final int finalTheID = theID;
            viewButton.setOnClickListener(new ImageButton.OnClickListener() {

                @Override
                public void onClick(View v) {
                    //Go into the the correct project
                    // create Intent
                    Intent intent = new Intent(getApplicationContext(),
                            Zone.class);
                    // put values to intent
                    intent.putExtra("ZID", finalTheID);
                    intent.putExtra("PID", projectID);
                    // Start Activity
                    startActivity(intent);

                }
            });

            //Delete button stuff
            deleteButton = new ImageButton(this);
            deleteButton.setId(theID);
            deleteButton.setPadding(0, 0, 0, 0);
            deleteButton.setBackgroundDrawable(null);
            deleteButton.setImageResource(R.mipmap.deletebutton);
            final String finalTestName = testName;
            deleteButton.setOnClickListener(new ImageButton.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Project.this);
                    builder.setTitle("Delete Site Location");
                    builder.setMessage("Are you sure you want to delete this site location?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Delete the Project and all of the associated files
                            DB.deleteZone(DB, finalTheID);
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

            //New layout stuff for the action column
            newLayout = new LinearLayout(this);
            newLayout.addView(viewButton);
            newLayout.addView(deleteButton);
            newLayout.setOrientation(LinearLayout.HORIZONTAL);
            newLayout.setGravity(Gravity.END);
            newLayout.setPadding(0, 0, padding_15dp, 0);

            //New Row Stuff
            newTR = new TableRow(this);
            newTR.setGravity(Gravity.CENTER_VERTICAL);
            newTR.addView(col0);
            newTR.addView(newLayout);
            ZonesTable.addView(newTR);
            ct++;
        }while(CR.moveToNext());//End of while loop
        CR.close();
        DB.close();
    }

    /**
     * Connects the project menu to the layout.
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.project_menu, menu);
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

package com.example.casey.altimeter;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
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

import static android.app.PendingIntent.getActivity;

public class Altimeter extends AppCompatActivity {
    //Declare the class variables
    Context ctx = this;
    protected String newProjectName;
    private String tempStrProjID;
    TextView name_col, action_col, col0;
    TableLayout ProjectsTable;
    LinearLayout newLayout;
    TableRow newTR;
    ImageButton viewButton;
    ImageButton deleteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_altimeter);
        setTitle(R.string.title_activity_altimeter);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Set up the table variables.
        name_col = (TextView)findViewById(R.id.nameCol);
        action_col = (TextView)findViewById(R.id.actionCol);
        ProjectsTable = (TableLayout)findViewById(R.id.projectsTable);
        ProjectsTable.setColumnStretchable(0, true);
        ProjectsTable.setColumnStretchable(1, true);

        // Add all of the projects to the table.
        try{
            addProjects();
            Log.i("Initializing Table", "Initialized Measurement table successfully!");
        }catch(Exception ex){
            ex.printStackTrace();
            Log.i("Initializing Table", "Failed to initilized table!");
        }

        //This is the popup that will allow the user to create a new project.
        final ImageButton btnOpenPopup = (ImageButton)findViewById(R.id.add_btn);
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
                //Save button in create Project popup
                Button btnSave = (Button) popupView.findViewById(R.id.save);
                btnSave.setOnClickListener(new Button.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        newProjectName = projectName.getText().toString();
                        if (newProjectName.equals("")) {
                            Toast.makeText(getApplicationContext(), "Please enter a name",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            //Add the new project to the database.
                            DatabaseOperations DB = new DatabaseOperations(ctx);
                            DB.saveNewProject(DB, newProjectName);

                            Intent intent = getIntent();
                            finish();
                            startActivity(intent);
                            Toast.makeText(getApplicationContext(), "Saved as: " + newProjectName,
                                    Toast.LENGTH_LONG).show();
                            // TODO Auto-generated method stub
                            popupWindow.dismiss();
                            DB.close();
                        }

                    }
                });

                //Cancel button should just get rid of the popup window.
                Button btnCancel = (Button) popupView.findViewById(R.id.dismiss);
                btnCancel.setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) { popupWindow.dismiss();}
                });
                popupWindow.setTouchable(true);
                popupWindow.setFocusable(true);
                popupWindow.showAsDropDown(findViewById(R.id.blankView), 50, 0);

            }
        });
    }

    /**
     * Takes each project listed in the database and displays it
     * on a row in the main table.
     * Each project row will include: Name, view button, and delete button.
     */
    private void addProjects() {
        int count = 0;
        final DatabaseOperations DB = new DatabaseOperations(ctx); //Create object to access the database.
        final Cursor CR = DB.getProject(DB); //Get the projects from the database.
        final float scale = getResources().getDisplayMetrics().density;
        int padding_15dp = (int) (15 * scale + 0.5f);
        String testName = "";
        int theID = 0;
        CR.moveToFirst();
        int ct = 0;

        if(CR == null)
            return;
        // Continue to add project rows until all of them
        // have been added to the table.
        do {
            testName = CR.getString(1);
            theID = CR.getInt(0);

            //Add the name column and format it.
            col0 = new TextView(this);
            col0.setPadding(padding_15dp, 0, 0, 0);
            col0.setGravity(Gravity.START);
            col0.setText(testName);
            col0.setTextSize(25);

            //Add the view button and format it.
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
                    // create Intent and set LoginSuccess Activity
                    Intent intent = new Intent(getApplicationContext(),
                            Project.class);
                    // put values to intent which will get in the LoginSuccess    Activity
                    tempStrProjID = Integer.toString(finalTheID);
                    intent.putExtra("ID", tempStrProjID);
                    // Start LoginSuccess Activity
                    startActivity(intent);

                }
            });

            //Add the delete button and format it.
            deleteButton = new ImageButton(this);
            deleteButton.setId(theID);
            deleteButton.setPadding(0, 0, 0, 0);
            deleteButton.setBackgroundDrawable(null);
            deleteButton.setImageResource(R.mipmap.deletebutton);
            final String finalTestName = testName;
            deleteButton.setOnClickListener(new ImageButton.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Altimeter.this);
                    builder.setTitle("Delete Project");
                    builder.setMessage("Are you sure you want to delete this project?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Delete the Project and all of the associated files
                            DB.deleteProject(DB, finalTheID);
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

            //Add a linear layout to put the two buttons in the same column.
            newLayout = new LinearLayout(this);
            newLayout.addView(viewButton);
            newLayout.addView(deleteButton);
            newLayout.setOrientation(LinearLayout.HORIZONTAL);
            newLayout.setGravity(Gravity.END);
            newLayout.setPadding(0, 0, padding_15dp, 0);

            //Add a row to the table and include the name column and action column.
            newTR = new TableRow(this);
            newTR.setGravity(Gravity.CENTER_VERTICAL);
            newTR.addView(col0);
            newTR.addView(newLayout);

            //Add the row to the table.
            ProjectsTable.addView(newTR);
            ct++;
        }while(CR.moveToNext());//End of while loop
        CR.close(); // Close the cursor object
        DB.close(); // Close the database object

    }
}
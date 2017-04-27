package com.example.casey.altimeter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.design.widget.TabLayout;
import android.util.Log;

/**
 * Created by Casey on 3/14/2016.
 * This allows us to interact with the SQLite Database
 */
public class DatabaseOperations extends SQLiteOpenHelper {
    //The database vaersion
    public static final int database_version = 1;
    // String used to create Projects table query
    public String CREATE_PROJECT_TABLE = "CREATE TABLE "+ TableData.TableInfo.PROJECT_TABLE+
            "("+TableData.TableInfo.ID+" INTEGER PRIMARY KEY ASC, "+
            TableData.TableInfo.PROJECT_NAME+" TEXT);";
    // String used to create Zones table query
    public String CREATE_ZONES_TABLE = "CREATE TABLE "+ TableData.TableInfo.ZONE_TABLE+
            "("+TableData.TableInfo.ID+" INTEGER PRIMARY KEY ASC, "+
            TableData.TableInfo.PROJECT_ID+" INTEGER, "
            +TableData.TableInfo.ZONE_NAME+" TEXT);";
    // String used to create Measurements table query
    public String CREATE_MEASUREMENTS_TABLE = "CREATE TABLE "+ TableData.TableInfo.MEASUREMENTS_TABLE+
            "("+TableData.TableInfo.ID+" INTEGER PRIMARY KEY ASC, "+
            TableData.TableInfo.PROJECT_ID+" INTEGER, "+
            TableData.TableInfo.ZONE_ID+" INTEGER, "+
            TableData.TableInfo.MEASUREMENT_NAME+" TEXT, "+
            TableData.TableInfo.FEET+" INTEGER, "+
            TableData.TableInfo.INCHES+" FLOAT, "+
            TableData.TableInfo.LATITUDE+" FLOAT, "+
            TableData.TableInfo.LONGITUDE+" FLOAT);";

    /**
     * @param context
     */
    public DatabaseOperations(Context context) {
        super(context, TableData.TableInfo.DATABASE_NAME, null, database_version);
        Log.d("Database operations", "Database created!");
    }

    /**
     * Executes the SQL database commands
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_PROJECT_TABLE);
        db.execSQL(CREATE_ZONES_TABLE);
        db.execSQL(CREATE_MEASUREMENTS_TABLE);
        Log.d("Database operations", "Table created!");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }

    /**
     * Saves a new project
     * @param dop
     * @param theName
     */
    public void saveNewProject(DatabaseOperations dop, String theName){
        SQLiteDatabase SQ = dop.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(TableData.TableInfo.PROJECT_NAME, theName);//Change this Value.
        long k = SQ.insert(TableData.TableInfo.PROJECT_TABLE, null, cv);
        Log.d("Database operations", "Project Inserted into DB!");
    }

    /**
     * Saves a new Zone
     * @param dop
     * @param theName
     */
    public void saveNewZone(DatabaseOperations dop, String theName, int projID){
        SQLiteDatabase SQ = dop.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(TableData.TableInfo.ZONE_NAME, theName);
        cv.put(TableData.TableInfo.PROJECT_ID, projID);
        long k = SQ.insert(TableData.TableInfo.ZONE_TABLE, null, cv);
        Log.d("Database operations", "Zone Inserted into DB!");
    }

    /**
     * Saves a new Measurement
     * @param dop
     * @param theName
     */
    public void saveNewMeasurement(DatabaseOperations dop, String theName, int projID, int zoneID, int feet, double inches, double lat, double lon){
        SQLiteDatabase SQ = dop.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(TableData.TableInfo.MEASUREMENT_NAME, theName);
        cv.put(TableData.TableInfo.PROJECT_ID, projID);
        cv.put(TableData.TableInfo.ZONE_ID, zoneID);
        cv.put(TableData.TableInfo.FEET, feet);
        cv.put(TableData.TableInfo.INCHES, inches);
        cv.put(TableData.TableInfo.LATITUDE, lat);
        cv.put(TableData.TableInfo.LONGITUDE, lon);
        long k = SQ.insert(TableData.TableInfo.MEASUREMENTS_TABLE, null, cv);
        Log.d("Database operations", "Measurement Inserted into DB!");
    }

    /**
     * Gets all of the Projects from the projects table.
     * @param dop
     * @return Cursor object
     */
    public Cursor getProject(DatabaseOperations dop){
        SQLiteDatabase SQ = dop.getReadableDatabase();
        String theQuery = "SELECT * FROM "+ TableData.TableInfo.PROJECT_TABLE+" ORDER BY "+TableData.TableInfo.PROJECT_NAME+" ASC;";
        Cursor CR = SQ.rawQuery(theQuery, null);

        return CR;
    }

    /**
     * Gets all of the Zones from the zone table
     * @param dop
     * @return Cursor object
     */
    public Cursor getZone(DatabaseOperations dop, int projID){
        SQLiteDatabase SQ = dop.getReadableDatabase();
        String theQuery = "SELECT * FROM "+ TableData.TableInfo.ZONE_TABLE+
                " WHERE project_id = "+projID+" ORDER BY "+TableData.TableInfo.ZONE_NAME+" ASC;";
        Cursor CR = SQ.rawQuery(theQuery, null);

        return CR;
    }

    /**
     * Gets all of the Measurements from the measurements table
     * @param dop
     * @return Cursor object
     */
    public Cursor getMeasurement(DatabaseOperations dop, int zoneID){
        SQLiteDatabase SQ = dop.getReadableDatabase();
        String theQuery = "SELECT * FROM "+ TableData.TableInfo.MEASUREMENTS_TABLE+
                " WHERE zone_id = "+zoneID+" ORDER BY "+TableData.TableInfo.MEASUREMENT_NAME+" ASC;";
        Cursor CR = SQ.rawQuery(theQuery, null);

        return CR;
    }

    /**
     * Deletes a project from the table given the ID and everything else
     * that goes with that project.
     * @param dop
     * @param id
     */
    public void deleteProject(DatabaseOperations dop, int id){
        SQLiteDatabase SQ = dop.getWritableDatabase();
        // Deletes the project from its table
        SQ.delete(TableData.TableInfo.PROJECT_TABLE, TableData.TableInfo.ID+"=\'"+id+"\'", null);
        // Delete the Zones that have this Project ID
        SQ.delete(TableData.TableInfo.ZONE_TABLE, TableData.TableInfo.PROJECT_ID+"=\'"+id+"\'", null);
        // Delete the Measurements that have this Project ID
        SQ.delete(TableData.TableInfo.MEASUREMENTS_TABLE, TableData.TableInfo.PROJECT_ID+"=\'"+id+"\'", null);
    }

    /**
     * Deletes a zone from the table given the ID and everything else
     * that goes with that zone.
     * @param dop
     * @param id
     */
    public void deleteZone(DatabaseOperations dop, int id){
        SQLiteDatabase SQ = dop.getWritableDatabase();
        // Deletes the zone from its table
        SQ.delete(TableData.TableInfo.ZONE_TABLE, TableData.TableInfo.ID+"=\'"+id+"\'", null);
        // Delete the Measurements that have this Project ID
        SQ.delete(TableData.TableInfo.MEASUREMENTS_TABLE, TableData.TableInfo.ZONE_ID+"=\'"+id+"\'", null);
    }

    /**
     * Deletes a Measurement from the table given the ID.
     * @param dop
     * @param id
     */
    public void deleteMeasurement(DatabaseOperations dop, int id){
        SQLiteDatabase SQ = dop.getWritableDatabase();
        // Delete the Measurements that have this Project ID
        SQ.delete(TableData.TableInfo.MEASUREMENTS_TABLE, TableData.TableInfo.ID+"=\'"+id+"\'", null);
    }
}

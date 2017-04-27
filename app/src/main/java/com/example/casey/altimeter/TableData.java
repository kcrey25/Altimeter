package com.example.casey.altimeter;

import android.provider.BaseColumns;

/**
 * Created by Casey on 3/14/2016.
 * This class holds the strings that will be used
 * to for the database.
 */
public class TableData {

    public static abstract class TableInfo implements BaseColumns{
        public static final String DATABASE_NAME = "altimeter";
        public static final String ID = "id";

        public static final String PROJECT_TABLE = "projects";
        //Table Columns
        public static final String PROJECT_NAME = "project_name";
        public static final String PROJECT_ID = "project_id";

        public static final String ZONE_TABLE = "zones";
        //Table Columns
        public static final String ZONE_NAME = "zone_name";
        public static final String ZONE_ID = "zone_id";

        public static final String MEASUREMENTS_TABLE = "measurements";
        //Table Columns
        public static final String MEASUREMENT_NAME = "measurement_name";
        public static final String MEASUREMENT_ID = "measurement_id";
        public static final String FEET = "feet";
        public static final String INCHES = "inches";
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
    }
}

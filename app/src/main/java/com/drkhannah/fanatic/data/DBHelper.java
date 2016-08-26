package com.drkhannah.fanatic.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by dhannah on 8/25/16.
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG = DBHelper.class.getSimpleName();

    public static final String DATABASE_NAME = "events.db";

    private static final int DATABASE_VERSION = 2;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create a table to hold records of searches.  _id|category|location|keywords
        final String SQL_CREATE_SEARCH_TABLE = "CREATE TABLE " + DBContract.SearchEntry.TABLE_NAME + " (" +
                DBContract.SearchEntry._ID + " INTEGER PRIMARY KEY," +
                DBContract.SearchEntry.CATEGORY + " TEXT NOT NULL, " +
                DBContract.SearchEntry.LOCATION + " TEXT NOT NULL, " +
                DBContract.SearchEntry.KEYWORDS + " TEXT NOT NULL, " +
                //replace duplicate search entries
                " UNIQUE (" + DBContract.SearchEntry.CATEGORY + ", " + DBContract.SearchEntry.LOCATION + ", " + DBContract.SearchEntry.CATEGORY + ") ON CONFLICT REPLACE );";

        // Create a table to hold records of events.  _id|search_id|title|start_time|venue_name|city_name|county_name|performers|longitude|latitude|description|img_url
        final String SQL_CREATE_EVENTS_TABLE = "CREATE TABLE " + DBContract.EventsEntry.TABLE_NAME + " (" +
                DBContract.EventsEntry._ID + " INTEGER PRIMARY KEY," +
                DBContract.EventsEntry.SEARCH_ID + " TEXT NOT NULL, " +
                DBContract.EventsEntry.TITLE + " TEXT NOT NULL, " +
                DBContract.EventsEntry.START_TIME + " TEXT NOT NULL, " +
                DBContract.EventsEntry.VENUE_NAME + " TEXT NOT NULL, " +
                DBContract.EventsEntry.CITY_NAME + " TEXT NOT NULL, " +
                DBContract.EventsEntry.COUNTRY_NAME + " TEXT NOT NULL, " +
                DBContract.EventsEntry.PERFORMERS + " TEXT NOT NULL, " +
                DBContract.EventsEntry.LONGITUDE + " TEXT NOT NULL, " +
                DBContract.EventsEntry.LATITUDE + " TEXT NOT NULL, " +
                DBContract.EventsEntry.DESCRIPTION + " TEXT NOT NULL, " +
                DBContract.EventsEntry.IMG_URL + " TEXT NOT NULL, " +

                // Set up the search_id column as a foreign key to Search table.
                " FOREIGN KEY (" + DBContract.EventsEntry.SEARCH_ID + ") REFERENCES " + DBContract.SearchEntry.TABLE_NAME + " (" + DBContract.SearchEntry._ID + "), " +

                //replace duplicate events
                " UNIQUE (" +DBContract.EventsEntry.TITLE + ", " + DBContract.EventsEntry.START_TIME + ") ON CONFLICT REPLACE );";

        sqLiteDatabase.execSQL(SQL_CREATE_SEARCH_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_EVENTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        int version = oldVersion;
        if(version == 1){
            version = 2;
        }

        if(version != DATABASE_VERSION){
            db.execSQL("DROP TABLE IF EXISTS " + DBContract.SearchEntry.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + DBContract.EventsEntry.TABLE_NAME);
            onCreate(db);
        }
    }
}

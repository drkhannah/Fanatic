
package com.drkhannah.fanatic.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;



import java.util.HashSet;

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    // delete the database
    void deleteTheDatabase() {
        mContext.deleteDatabase(DBHelper.DATABASE_NAME);
    }

    /*
        This function gets called before each test is executed to delete the database.  This makes
        sure that we always have a clean test.
     */
    public void setUp() {
        deleteTheDatabase();
    }

    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(DBContract.SearchEntry.TABLE_NAME);
        tableNameHashSet.add(DBContract.EventsEntry.TABLE_NAME);

        mContext.deleteDatabase(DBHelper.DATABASE_NAME);
        SQLiteDatabase db = new DBHelper(this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );

        // if this fails, it means that your database doesn't contain both the search entry
        // and Evennt entry tables
        assertTrue("Error: Your database was created without both the search entry and event entry tables",
                tableNameHashSet.isEmpty());

        // does search table contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + DBContract.EventsEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> searchColumnHashSet = new HashSet<String>();
        searchColumnHashSet.add(DBContract.SearchEntry._ID);
        searchColumnHashSet.add(DBContract.SearchEntry.CATEGORY);
        searchColumnHashSet.add(DBContract.SearchEntry.LOCATION);
        searchColumnHashSet.add(DBContract.SearchEntry.KEYWORDS);

        int searchColumnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(searchColumnNameIndex);
            searchColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required search
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required search entry columns",
                searchColumnHashSet.isEmpty());

        // does events table contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + DBContract.EventsEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> eventColumnHashSet = new HashSet<String>();
        searchColumnHashSet.add(DBContract.EventsEntry._ID);
        searchColumnHashSet.add(DBContract.EventsEntry.SEARCH_ID);
        searchColumnHashSet.add(DBContract.EventsEntry.TITLE);
        searchColumnHashSet.add(DBContract.EventsEntry.START_TIME);
        searchColumnHashSet.add(DBContract.EventsEntry.VENUE_NAME);
        searchColumnHashSet.add(DBContract.EventsEntry.CITY_NAME);
        searchColumnHashSet.add(DBContract.EventsEntry.COUNTRY_NAME);
        searchColumnHashSet.add(DBContract.EventsEntry.PERFORMERS);
        searchColumnHashSet.add(DBContract.EventsEntry.LONGITUDE);
        searchColumnHashSet.add(DBContract.EventsEntry.LATITUDE);
        searchColumnHashSet.add(DBContract.EventsEntry.DESCRIPTION);
        searchColumnHashSet.add(DBContract.EventsEntry.IMG_URL);

        int eventColumnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(eventColumnNameIndex);
            eventColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required search
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required search entry columns",
                eventColumnHashSet.isEmpty());
        db.close();
    }


    public void testSearchTable() {
        insertSearch();
    }


    public void testEventTable() {
        // First insert the search, and then use the searchRowId to insert
        // the event. Make sure to cover as many failure cases as you can.

        // Instead of rewriting all of the code we've already written in testSearchTable
        // we can move this code to insertSearch and then call insertSearch from both
        // tests. Why move it? We need the code to return the ID of the inserted search
        // and our testSearchTable can only return void because it's a test.

        long searchRowId = insertSearch();

        // Make sure we have a valid row ID.
        assertFalse("Error: Search Not Inserted Correctly", searchRowId == -1L);

        // First step: Get reference to writable database
        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        DBHelper dbHelper = new DBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Second Step (Event): Create event values
        ContentValues eventValues = TestUtilities.createEventValues(searchRowId);

        // Third Step (Event): Insert ContentValues into database and get a row ID back
        long eventRowId = db.insert(DBContract.EventsEntry.TABLE_NAME, null, eventValues);
        assertTrue(eventRowId != -1);

        // Fourth Step: Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor eventCursor = db.query(
                DBContract.EventsEntry.TABLE_NAME,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );

        // Move the cursor to the first valid database row and check to see if we have any rows
        assertTrue( "Error: No Records returned from Search query", eventCursor.moveToFirst() );

        // Fifth Step: Validate the search Query
        TestUtilities.validateCurrentRecord("testInsertReadDb EventsEntry failed to validate",
                eventCursor, eventValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse( "Error: More than one record returned from event query",
                eventCursor.moveToNext() );

        // Sixth Step: Close cursor and database
        eventCursor.close();
        dbHelper.close();
    }

    public long insertSearch() {

        // First step: Get reference to writable database
        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        DBHelper dbHelper = new DBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Second Step: Create ContentValues of what you want to insert
        // (you can use the createSearchValues if you wish)
        ContentValues testValues = TestUtilities.createSearchValues();

        // Third Step: Insert ContentValues into database and get a row ID back
        long searchRowId;
        searchRowId = db.insert(DBContract.SearchEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue(searchRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // Fourth Step: Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                DBContract.SearchEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        // Move the cursor to a valid database row and check to see if we got any records back
        // from the query
        assertTrue( "Error: No Records returned from search query", cursor.moveToFirst() );

        // Fifth Step: Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)
        TestUtilities.validateCurrentRecord("Error: Search Query Validation Failed",
                cursor, testValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse( "Error: More than one record returned from Search query",
                cursor.moveToNext());

        // Sixth Step: Close Cursor and Database
        cursor.close();
        db.close();
        return searchRowId;
    }
}


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
        Delete the database before every test
     */

    @Override
    public void setUp() throws Exception {
        super.setUp();
        deleteTheDatabase();
    }

    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(DBContract.SearchEntry.TABLE_NAME);
        tableNameHashSet.add(DBContract.EventsEntry.TABLE_NAME);

        SQLiteDatabase db = new DBHelper(mContext).getWritableDatabase();
        //is the database open?
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
        // and Event entry tables
        assertTrue("Error: Your database was created without both the search entry and event entry tables",
                tableNameHashSet.isEmpty());

        // does search table contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + DBContract.SearchEntry.TABLE_NAME + ")",
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

        // if this fails, it means that your database doesn't contain all of the required search columns
        assertTrue("Error: The database doesn't contain all of the required search entry columns",
                searchColumnHashSet.isEmpty());

        // does events table contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + DBContract.EventsEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> eventColumnHashSet = new HashSet<String>();
        eventColumnHashSet.add(DBContract.EventsEntry._ID);
        eventColumnHashSet.add(DBContract.EventsEntry.SEARCH_ID);
        eventColumnHashSet.add(DBContract.EventsEntry.TITLE);
        eventColumnHashSet.add(DBContract.EventsEntry.START_TIME);
        eventColumnHashSet.add(DBContract.EventsEntry.VENUE_NAME);
        eventColumnHashSet.add(DBContract.EventsEntry.CITY_NAME);
        eventColumnHashSet.add(DBContract.EventsEntry.COUNTRY_NAME);
        eventColumnHashSet.add(DBContract.EventsEntry.PERFORMERS);
        eventColumnHashSet.add(DBContract.EventsEntry.LONGITUDE);
        eventColumnHashSet.add(DBContract.EventsEntry.LATITUDE);
        eventColumnHashSet.add(DBContract.EventsEntry.DESCRIPTION);
        eventColumnHashSet.add(DBContract.EventsEntry.IMG_URL);

        int eventColumnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(eventColumnNameIndex);
            eventColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required search
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required search entry columns",
                eventColumnHashSet.isEmpty());
        //close the database
        db.close();
    }


    public void testSearchTable() {
        insertSearch();
    }


    public void testEventTable() {
        //insert a search record, then use the returned id as the search_id for your events inserts
        long searchRowId = insertSearch();

        // Make sure we have a valid row ID.
        assertFalse("Error: Search Not Inserted Correctly", searchRowId == -1L);

        //get a writable database
        SQLiteDatabase db = new DBHelper(mContext).getWritableDatabase();
        //is the database open?
        assertEquals(true, db.isOpen());

        // Create event values
        ContentValues eventValues = TestUtilities.createEventValues(searchRowId);

        // Insert ContentValues into events table and get a row ID back
        long eventRowId = db.insert(DBContract.EventsEntry.TABLE_NAME, null, eventValues);
        assertTrue(eventRowId != -1);

        // Query the events table and receive a Cursor back
        Cursor eventTableCursor = db.query(
                DBContract.EventsEntry.TABLE_NAME,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );

        // move the cursor to verify that a record was returned
        assertTrue( "Error: No Records returned from Search query", eventTableCursor.moveToFirst() );

        // Validate the event record
        TestUtilities.validateCurrentRecord("Error: Search Query Validation Failed",
                eventTableCursor, eventValues);

        //move cursor to verify that there is only one record in the database
        assertFalse( "Error: More than one record returned from event query",
                eventTableCursor.moveToNext() );

        //Close cursor and database
        eventTableCursor.close();
        db.close();
    }

    public long insertSearch() {

        // Get reference to writable database
        // SQL table creation String errors will be thrown here when you try to get a writable database.
        SQLiteDatabase db = new DBHelper(mContext).getWritableDatabase();
        //is the database open?
        assertEquals(true, db.isOpen());

        // Create ContentValues of what you want to insert with createSearchValues from TestUtilities
        ContentValues testValues = TestUtilities.createSearchValues();
        //create a different set of searchValues to be inserted
        ContentValues differentValues = TestUtilities.createDifferentSearchValues();

        // Insert testValues into database and get a row ID back
        long searchRowId;
        searchRowId = db.insert(DBContract.SearchEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue(searchRowId != -1);

        //insert a duplicate testValues to make sure the last row gets replaced
        long searchRowId2;
        searchRowId2 = db.insert(DBContract.SearchEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue(searchRowId2 != -1);

        //insert a different search values that wont be replaced
        long searchRowId3;
        searchRowId3 = db.insert(DBContract.SearchEntry.TABLE_NAME, null, differentValues);

        // Verify we got a row back.
        assertTrue(searchRowId3 != -1);

        // Verify it was inserted by querying the database and getting Cursor back
        Cursor searchTableCursor = db.query(
                DBContract.SearchEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        // If searchTableCursor.moveToFirst() returns false, then no records were returned from the search table
        assertTrue( "Error: No Records returned from search query", searchTableCursor.moveToFirst() );

        // use validateCurrentRecord method from TestUtilities
        // to validate the records values match our test ContentValues
        TestUtilities.validateCurrentRecord("Error: Search Query Validation Failed",
                searchTableCursor, testValues);

        // Verify that different search values were inserted
        assertTrue( "Error: Different search values were not returned from Search Table query",
                searchTableCursor.moveToNext());

        // Move the searchTableCursor to verify that there are only two record in the database
        assertFalse( "Error: More than two record returned from Search Table query",
                searchTableCursor.moveToNext());

        //Close Cursor and Database
        searchTableCursor.close();
        db.close();
        return searchRowId2;
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        deleteTheDatabase();
    }


}

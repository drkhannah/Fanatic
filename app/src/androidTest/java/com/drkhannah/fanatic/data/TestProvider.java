
package com.drkhannah.fanatic.data;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.test.AndroidTestCase;
import android.util.Log;

public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    /*
       This helper function deletes all records from both database tables using the Provider.
       It also queries the Provider to make sure that the database has been successfully
       deleted
     */
    public void deleteAllRecordsFromProvider() {
        mContext.getContentResolver().delete(
                DBContract.EventsEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                DBContract.SearchEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                DBContract.EventsEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Events table during delete", 0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                DBContract.SearchEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Search table during delete", 0, cursor.getCount());
        cursor.close();
    }

    public void deleteAllRecords() {
        deleteAllRecordsFromProvider();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }

    /*
        checks to make sure that the content provider is registered correctly.
     */
    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        // We define the component name based on the package name from the context and the
        // Provider class.
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                Provider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: Provider registered with authority: " + providerInfo.authority +
                            " instead of authority: " + DBContract.CONTENT_AUTHORITY,
                    providerInfo.authority, DBContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.
            assertTrue("Error:ConcertsContractProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }

    public void testGetType() {
        // content://com.drkhannah.fanatic.provider/events/
        String type = mContext.getContentResolver().getType(DBContract.EventsEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.drkhannah.fanatic.provider/events/
        assertEquals("Error: the EventsEntry CONTENT_URI should return DBContract.EventsEntry.CONTENT_TYPE", DBContract.EventsEntry.CONTENT_TYPE, type);

        long testSearchId = 1;
        // content://com.drkhannah.fanatic.provider/events/*
        type = mContext.getContentResolver().getType(DBContract.EventsEntry.buildEventListForSearchUri(testSearchId));

        // vnd.android.cursor.dir/com.drkhannah.fanatic.provider/events
        assertEquals("Error: the EventsEntry CONTENT_URI with searchId should return EventsEntry.CONTENT_TYPE", DBContract.EventsEntry.CONTENT_TYPE, type);

        String testDate = "9:00";
        String testTitle = "Title";
        // content://com.drkhannah.fanatic.provider/events/*/*/*
        type = mContext.getContentResolver().getType(DBContract.EventsEntry.buildEventListForSearchWithDateAndTitleUri(testSearchId, testDate, testTitle));

        // vnd.android.cursor.dir/com.drkhannah.fanatic.provider/events
        assertEquals("Error: the EventEntry CONTENT_URI with searchId, date, and title  should return EventsEntry.CONTENT_ITEM_TYPE", DBContract.EventsEntry.CONTENT_ITEM_TYPE, type);

        // content://com.drkhannah.concerts2.provider/search
        type = mContext.getContentResolver().getType(DBContract.SearchEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.drkhannah.fanatic.provider/search
        assertEquals("Error: the SearchEntry CONTENT_URI should return SearchEntry.CONTENT_ITEM_TYPE", DBContract.SearchEntry.CONTENT_ITEM_TYPE, type);
    }

    public void testBasicEventsForSearchQuery() {
        // insert our test records into the database
        SQLiteDatabase db = new DBHelper(mContext).getWritableDatabase();
        //is the database open?
        assertEquals(true, db.isOpen());

        ContentValues testValues = TestUtilities.createSearchValues();
        long searchRowId = TestUtilities.insertTestSearchValues(mContext);

        // Fantastic.  Now that we have a searchId, add some events!
        ContentValues eventValues = TestUtilities.createEventValues(searchRowId);

        long EventRowId = db.insert(DBContract.EventsEntry.TABLE_NAME, null, eventValues);
        assertTrue("Unable to Insert EventEntry into the Database", EventRowId != -1);

        db.close();

        // Test the basic content provider query
        Cursor eventsCursor = mContext.getContentResolver().query(
                DBContract.EventsEntry.buildEventListForSearchUri(searchRowId),
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicEventsForSearchQuery", eventsCursor, eventValues);
    }

    public void testBasicEventsForSearchWithDateAndTitleQuery() {
        // insert our test records into the database
        SQLiteDatabase db = new DBHelper(mContext).getWritableDatabase();
        //is the database open?
        assertEquals(true, db.isOpen());

        ContentValues testValues = TestUtilities.createSearchValues();
        long searchRowId = TestUtilities.insertTestSearchValues(mContext);

        // have a searchId, add some events!
        ContentValues eventValues = TestUtilities.createEventValues(searchRowId);
        String testDate = eventValues.getAsString("start_time");
        String testTitle = eventValues.getAsString("title");

        long eventRowId = db.insert(DBContract.EventsEntry.TABLE_NAME, null, eventValues);
        assertTrue("Unable to Insert EventsEntry into the Database", eventRowId != -1);

        db.close();

        // Test the basic content provider query
        Cursor eventsCursor = mContext.getContentResolver().query(
                DBContract.EventsEntry.buildEventListForSearchWithDateAndTitleUri(searchRowId, testDate, testTitle),
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicEventsForSearchWithDateAndTitleQuery", eventsCursor, eventValues);
    }

    public void testBasicEventsQuery() {
        // insert our test records into the database
        SQLiteDatabase db = new DBHelper(mContext).getWritableDatabase();
        //is the database open?
        assertEquals(true, db.isOpen());

        long searchRowId = TestUtilities.insertTestSearchValues(mContext);

        // we have a event, add some events
        ContentValues eventValues = TestUtilities.createEventValues(searchRowId);

        long eventRowId = db.insert(DBContract.EventsEntry.TABLE_NAME, null, eventValues);
        assertTrue("Unable to Insert EventsEntry into the Database", eventRowId != -1);

        db.close();

        // Test the basic content provider query
        Cursor eventsCursor = mContext.getContentResolver().query(
                DBContract.EventsEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicEventsQuery", eventsCursor, eventValues);
    }

    public void testBasicSearchQueries() {
        // insert our test records into the database
        SQLiteDatabase db = new DBHelper(mContext).getWritableDatabase();
        //is the database open?
        assertEquals(true, db.isOpen());

        ContentValues testValues = TestUtilities.createSearchValues();
        TestUtilities.insertTestSearchValues(mContext);

        // Test the basic content provider query
        Cursor searchCursor = mContext.getContentResolver().query(
                DBContract.SearchEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicSearchQueries, search query", searchCursor, testValues);

        // Has the NotificationUri been set - API level 19.
        if (Build.VERSION.SDK_INT >= 19) {
            assertEquals("Error: Search Query did not properly set NotificationUri",
                    searchCursor.getNotificationUri(), DBContract.SearchEntry.CONTENT_URI);
        }
    }

    //insert and then update the data
    public void testUpdateSearch() {
        ContentValues searchValues = TestUtilities.createSearchValues();

        Uri searchUri = mContext.getContentResolver().insert(DBContract.SearchEntry.CONTENT_URI, searchValues);
        long searchRowId = ContentUris.parseId(searchUri);

        // Verify we got a row back.
        assertTrue(searchRowId != -1);
        Log.d(LOG_TAG, "New row id: " + searchRowId);

        //create updatedValues from searchValues to replace the previously saved searchValues
        ContentValues updatedValues = new ContentValues(searchValues);
        updatedValues.put(DBContract.SearchEntry._ID, searchRowId);
        updatedValues.put(DBContract.SearchEntry.KEYWORDS, "pop");

        // Create a cursor with observer to make sure that the content provider is notifying the observers
        Cursor searchCursor = mContext.getContentResolver().query(DBContract.SearchEntry.CONTENT_URI, null, null, null, null);

        TestUtilities.TestContentObserver testContentObserver = TestUtilities.getTestContentObserver();
        searchCursor.registerContentObserver(testContentObserver);

        int count = mContext.getContentResolver().update(
                DBContract.SearchEntry.CONTENT_URI,
                updatedValues,
                DBContract.SearchEntry._ID + "= ?",
                new String[]{Long.toString(searchRowId)});

        assertEquals(count, 1);

        // Test to make sure our observer is called.  If not, we throw an assertion.
        //If your code is failing here, it means that your content provider
        // isn't calling getContext().getContentResolver().notifyChange(uri, null);
        testContentObserver.waitForNotificationOrFail();

        searchCursor.unregisterContentObserver(testContentObserver);
        searchCursor.close();

        // get a cursor to see the updated record
        Cursor cursor = mContext.getContentResolver().query(
                DBContract.EventsEntry.CONTENT_URI,
                null,   // projection
                DBContract.SearchEntry._ID + " = " + searchRowId,
                null,   // Values for the "where" clause
                null    // sort order
        );

        TestUtilities.validateCursor("testUpdateSearch.  Error validating Search entry update.",
                cursor, updatedValues);

        cursor.close();
    }


    // Make sure we can still delete after adding/updating stuff
    public void testInsertReadProvider() {
        ContentValues searchValues = TestUtilities.createSearchValues();

        // Register a content observer for our insert.  This time, directly with the content resolver
        TestUtilities.TestContentObserver testContentObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(DBContract.SearchEntry.CONTENT_URI, true, testContentObserver);
        Uri searchUri = mContext.getContentResolver().insert(DBContract.SearchEntry.CONTENT_URI, searchValues);

        // Did the content observer get called?
        testContentObserver.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(testContentObserver);

        long searchRowId = ContentUris.parseId(searchUri);

        // Verify we got a row back.
        assertTrue(searchRowId != -1);

        // query results.
        Cursor cursor = mContext.getContentResolver().query(
                DBContract.SearchEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating SearchEntry.", cursor, searchValues);

        //add some events to the search
        ContentValues eventValues = TestUtilities.createEventValues(searchRowId);
        // The TestContentObserver is a one-shot class
        testContentObserver = TestUtilities.getTestContentObserver();

        mContext.getContentResolver().registerContentObserver(DBContract.SearchEntry.CONTENT_URI, true, testContentObserver);

        Uri eventInsertUri = mContext.getContentResolver().insert(DBContract.EventsEntry.CONTENT_URI, eventValues);
        assertTrue(eventInsertUri != null);

        // Did our content observer get called?
        testContentObserver.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(testContentObserver);

        // query results.
        Cursor eventCursor = mContext.getContentResolver().query(
                DBContract.EventsEntry.CONTENT_URI,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // columns to group by
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating EventEntry insert.", eventCursor, eventValues);

        // Add the search values in with the event data so that we can make
        // sure that the join worked and we actually get all the values back
        eventValues.putAll(searchValues);

        // Get the joined Event and Search data
        eventCursor = mContext.getContentResolver().query(
                DBContract.EventsEntry.buildEventListForSearchUri(searchRowId),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        TestUtilities.validateCursor("testInsertReadProvider.  Error validating joined Event and Search Data.", eventCursor, eventValues);

    }

    // Make sure we can still delete after adding/updating stuff
    public void testDeleteRecords() {
        testInsertReadProvider();

        // Register a content observer for a search delete.
        TestUtilities.TestContentObserver searchObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(DBContract.SearchEntry.CONTENT_URI, true, searchObserver);

        // Register a content observer for a event delete.
        TestUtilities.TestContentObserver eventObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(DBContract.EventsEntry.CONTENT_URI, true, eventObserver);

        deleteAllRecordsFromProvider();

        // Did the content observer get called?
        searchObserver.waitForNotificationOrFail();
        eventObserver.waitForNotificationOrFail();

        mContext.getContentResolver().unregisterContentObserver(searchObserver);
        mContext.getContentResolver().unregisterContentObserver(eventObserver);
    }


    static private final int BULK_INSERT_RECORDS_TO_INSERT = 10;

    static ContentValues[] createBulkInsertEventValues(long searchRowId) {
        ContentValues[] returnContentValues = new ContentValues[BULK_INSERT_RECORDS_TO_INSERT];

        for (int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++) {
            ContentValues eventValues = new ContentValues();
            eventValues.put(DBContract.EventsEntry.SEARCH_ID, searchRowId);
            eventValues.put(DBContract.EventsEntry.TITLE, "title " + i);
            eventValues.put(DBContract.EventsEntry.START_TIME, "start time " + i);
            eventValues.put(DBContract.EventsEntry.VENUE_NAME, "venue name");
            eventValues.put(DBContract.EventsEntry.CITY_NAME, "city name");
            eventValues.put(DBContract.EventsEntry.COUNTRY_NAME, "country name");
            eventValues.put(DBContract.EventsEntry.PERFORMERS, "performers");
            eventValues.put(DBContract.EventsEntry.LONGITUDE, "longitude");
            eventValues.put(DBContract.EventsEntry.LATITUDE, "latitude");
            eventValues.put(DBContract.EventsEntry.DESCRIPTION, "description");
            eventValues.put(DBContract.EventsEntry.IMG_URL, "image url");
            returnContentValues[i] = eventValues;
        }
        return returnContentValues;
    }

    // Test the bulk insert
    public void testBulkInsert() {
        //create a search record
        ContentValues searchValues = TestUtilities.createSearchValues();
        Uri searchUri = mContext.getContentResolver().insert(DBContract.SearchEntry.CONTENT_URI, searchValues);
        long searchRowId = ContentUris.parseId(searchUri);

        // Verify we got a row back.
        assertTrue(searchRowId != -1);

        // query results.
        Cursor cursor = mContext.getContentResolver().query(
                DBContract.SearchEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtilities.validateCursor("testBulkInsert. Error validating SearchEntry.", cursor, searchValues);

        //bulkInsert  10 events.
        ContentValues[] bulkInsertContentValues = createBulkInsertEventValues(searchRowId);

        // Register a content observer for our bulk insert.
        TestUtilities.TestContentObserver testContentObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(DBContract.EventsEntry.CONTENT_URI, true, testContentObserver);

        int insertCount = mContext.getContentResolver().bulkInsert(DBContract.EventsEntry.CONTENT_URI, bulkInsertContentValues);

        //Did the observer get called?
        testContentObserver.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(testContentObserver);

        assertEquals(insertCount, BULK_INSERT_RECORDS_TO_INSERT);

        // query the results.
        cursor = mContext.getContentResolver().query(
                DBContract.EventsEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                DBContract.EventsEntry.START_TIME + " ASC"  // sort order == by DATE ASCENDING
        );

        // are there as many records in the database as we've inserted
        assertEquals(cursor.getCount(), BULK_INSERT_RECORDS_TO_INSERT);

        //make sure they match the ones we created
        cursor.moveToFirst();
        for (int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, cursor.moveToNext()) {
            TestUtilities.validateCurrentRecord("testBulkInsert.  Error validating EventEntry " + i, cursor, bulkInsertContentValues[i]);
        }
        cursor.close();
    }
}

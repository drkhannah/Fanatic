///*
// * Copyright (C) 2014 The Android Open Source Project
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.drkhannah.fanatic.data;
//
//import android.content.ComponentName;
//import android.content.ContentUris;
//import android.content.ContentValues;
//import android.content.pm.PackageManager;
//import android.content.pm.ProviderInfo;
//import android.database.Cursor;
//import android.database.sqlite.SQLiteDatabase;
//import android.net.Uri;
//import android.os.Build;
//import android.test.AndroidTestCase;
//import android.util.Log;
//
//import com.drkhannah.concerts2.database.ConcertsContentProvider;
//import com.drkhannah.concerts2.database.ConcertsContract;
//import com.drkhannah.concerts2.database.ConcertsDatabase;
//
///*
//    Note: This is not a complete set of tests of the Sunshine ContentProvider, but it does test
//    that at least the basic functionality has been implemented correctly.
//
//    Students: Uncomment the tests in this class as you implement the functionality in your
//    ContentProvider to make sure that you've implemented things reasonably correctly.
// */
//public class TestProvider extends AndroidTestCase {
//
//    public static final String LOG_TAG = TestProvider.class.getSimpleName();
//
//    /*
//       This helper function deletes all records from both database tables using the ContentProvider.
//       It also queries the ContentProvider to make sure that the database has been successfully
//       deleted, so it cannot be used until the Query and Delete functions have been written
//       in the ContentProvider.
//
//       Students: Replace the calls to deleteAllRecordsFromDB with this one after you have written
//       the delete functionality in the ContentProvider.
//     */
//    public void deleteAllRecordsFromProvider() {
//        mContext.getContentResolver().delete(
//                ConcertsContract.ConcertEntry.CONTENT_URI,
//                null,
//                null
//        );
//        mContext.getContentResolver().delete(
//                ConcertsContract.ArtistEntry.CONTENT_URI,
//                null,
//                null
//        );
//
//        Cursor cursor = mContext.getContentResolver().query(
//                ConcertsContract.ConcertEntry.CONTENT_URI,
//                null,
//                null,
//                null,
//                null
//        );
//        assertEquals("Error: Records not deleted from Concert table during delete", 0, cursor.getCount());
//        cursor.close();
//
//        cursor = mContext.getContentResolver().query(
//                ConcertsContract.ArtistEntry.CONTENT_URI,
//                null,
//                null,
//                null,
//                null
//        );
//        assertEquals("Error: Records not deleted from Artist table during delete", 0, cursor.getCount());
//        cursor.close();
//    }
//
//    /*
//       This helper function deletes all records from both database tables using the database
//       functions only.  This is designed to be used to reset the state of the database until the
//       delete functionality is available in the ContentProvider.
//     */
//    public void deleteAllRecordsFromDB() {
//        ConcertsDatabase dbHelper = new ConcertsDatabase(mContext);
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
//
//        db.delete(ConcertsContract.ConcertEntry.TABLE_NAME, null, null);
//        db.delete(ConcertsContract.ArtistEntry.TABLE_NAME, null, null);
//        db.close();
//    }
//
//    /*
//        Student: Refactor this function to use the deleteAllRecordsFromProvider functionality once
//        you have implemented delete functionality there.
//     */
//    public void deleteAllRecords() {
//        deleteAllRecordsFromDB();
//    }
//
//    // Since we want each test to start with a clean slate, run deleteAllRecords
//    // in setUp (called by the test runner before each test).
//    @Override
//    protected void setUp() throws Exception {
//        super.setUp();
//        deleteAllRecords();
//    }
//
//    /*
//        This test checks to make sure that the content provider is registered correctly.
//        Students: Uncomment this test to make sure you've correctly registered the ConcertContentProvider.
//     */
//    public void testProviderRegistry() {
//        PackageManager pm = mContext.getPackageManager();
//
//        // We define the component name based on the package name from the context and the
//        // ConcertsContentProvider class.
//        ComponentName componentName = new ComponentName(mContext.getPackageName(),
//                ConcertsContentProvider.class.getName());
//        try {
//            // Fetch the provider info using the component name from the PackageManager
//            // This throws an exception if the provider isn't registered.
//            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);
//
//            // Make sure that the registered authority matches the authority from the Contract.
//            assertEquals("Error: ConcertContentProvider registered with authority: " + providerInfo.authority +
//                    " instead of authority: " + ConcertsContract.CONTENT_AUTHORITY,
//                    providerInfo.authority, ConcertsContract.CONTENT_AUTHORITY);
//        } catch (PackageManager.NameNotFoundException e) {
//            // I guess the provider isn't registered correctly.
//            assertTrue("Error:ConcertsContractProvider not registered at " + mContext.getPackageName(),
//                    false);
//        }
//    }
//
//    /*
//            This test doesn't touch the database.  It verifies that the ContentProvider returns
//            the correct type for each type of URI that it can handle.
//            Students: Uncomment this test to verify that your implementation of GetType is
//            functioning correctly.
//         */
//    public void testGetType() {
//        // content://com.drkhannah.concerts2.provider/concerts/
//        String type = mContext.getContentResolver().getType(ConcertsContract.ConcertEntry.CONTENT_URI);
//        // vnd.android.cursor.dir/com.drkhannah.concerts2.provider/concerts
//        assertEquals("Error: the ConcertEntry CONTENT_URI should return ConcertsContract.ConcertEntry.CONTENT_TYPE",
//                ConcertsContract.ConcertEntry.CONTENT_TYPE, type);
//
//        String testArtist = "nofx";
//        // content://com.drkhannah.concerts2.provider/concerts/nofx
//        type = mContext.getContentResolver().getType(
//                ConcertsContract.ConcertEntry.buildConcertsListForArtist(testArtist));
//        // vnd.android.cursor.dir/com.drkhannah.concerts2.provider/concerts
//        assertEquals("Error: the ConcertEntry CONTENT_URI with artist should return ConcertEntry.CONTENT_TYPE",
//                ConcertsContract.ConcertEntry.CONTENT_TYPE, type);
//
//        String date = "Date of Concert";
//        // content://com.drkhannah.concerts2.provider/concerts/nofx/date
//        type = mContext.getContentResolver().getType(
//                ConcertsContract.ConcertEntry.buildConcertForArtistWithDate(testArtist, date));
//        // vnd.android.cursor.dir/com.drkhannah.concerts2.provider/concerts
//        assertEquals("Error: the ConcertEntry CONTENT_URI with artist should return ConcertEntry.CONTENT_ITEM_TYPE",
//                ConcertsContract.ConcertEntry.CONTENT_ITEM_TYPE, type);
//
//        // content://com.drkhannah.concerts2.provider/artist/
//        type = mContext.getContentResolver().getType(ConcertsContract.ArtistEntry.CONTENT_URI);
//        // vnd.android.cursor.dir/com.drkhannah.concerts2.provider/artist
//        assertEquals("Error: the ArtistEntry CONTENT_URI should return ConcertsContract.ArtistEntry.CONTENT_ITEM_TYPE",
//                ConcertsContract.ArtistEntry.CONTENT_ITEM_TYPE, type);
//    }
//
//
//    /*
//        This test uses the database directly to insert and then uses the ContentProvider to
//        read out the data.  Uncomment this test to see if the basic concerts query functionality
//        given in the ContentProvider is working correctly.
//     */
//    public void testBasicConcertsForArtistQuery() {
//        // insert our test records into the database
//        ConcertsDatabase dbHelper = new ConcertsDatabase(mContext);
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
//
//        ContentValues testValues = TestUtilities.createNofxArtistValues();
//        String artist = testValues.getAsString("name");
//        long artistRowId = TestUtilities.insertTestSearchValues(mContext);
//
//        // Fantastic.  Now that we have a artist, add some concerts!
//        ContentValues concertValues = TestUtilities.createConcertValues(artistRowId);
//
//        long concertRowId = db.insert(ConcertsContract.ConcertEntry.TABLE_NAME, null, concertValues);
//        assertTrue("Unable to Insert ConcertEntry into the Database", concertRowId != -1);
//
//        db.close();
//
//        // Test the basic content provider query
//        Cursor concertCursor = mContext.getContentResolver().query(
//                ConcertsContract.ConcertEntry.buildConcertsListForArtist(artist),
//                null,
//                null,
//                null,
//                null
//        );
//
//        // Make sure we get the correct cursor out of the database
//        TestUtilities.validateCursor("testBasicConcertsForArtistQuery", concertCursor, concertValues);
//    }
//
//    public void testBasicConcertForArtistWithDateQuery() {
//        // insert our test records into the database
//        ConcertsDatabase dbHelper = new ConcertsDatabase(mContext);
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
//
//        ContentValues testValues = TestUtilities.createNofxArtistValues();
//        String artist = testValues.getAsString("name");
//        long artistRowId = TestUtilities.insertTestSearchValues(mContext);
//
//        // Fantastic.  Now that we have a artist, add some concerts!
//        ContentValues concertValues = TestUtilities.createConcertValues(artistRowId);
//        String date = concertValues.getAsString("date");
//
//        long concertRowId = db.insert(ConcertsContract.ConcertEntry.TABLE_NAME, null, concertValues);
//        assertTrue("Unable to Insert ConcertEntry into the Database", concertRowId != -1);
//
//        db.close();
//
//        // Test the basic content provider query
//        Cursor concertCursor = mContext.getContentResolver().query(
//                ConcertsContract.ConcertEntry.buildConcertForArtistWithDate(artist, date),
//                null,
//                null,
//                null,
//                null
//        );
//
//        // Make sure we get the correct cursor out of the database
//        TestUtilities.validateCursor("testBasicConcertForArtistWithDateQuery", concertCursor, concertValues);
//    }
//
//    public void testBasicConcertsQuery() {
//        // insert our test records into the database
//        ConcertsDatabase dbHelper = new ConcertsDatabase(mContext);
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
//
//        ContentValues testValues = TestUtilities.createNofxArtistValues();
//        long artistRowId = TestUtilities.insertTestSearchValues(mContext);
//
//        // Fantastic.  Now that we have a artist, add some concerts!
//        ContentValues concertValues = TestUtilities.createConcertValues(artistRowId);
//
//        long concertRowId = db.insert(ConcertsContract.ConcertEntry.TABLE_NAME, null, concertValues);
//        assertTrue("Unable to Insert ConcertEntry into the Database", concertRowId != -1);
//
//        db.close();
//
//        // Test the basic content provider query
//        Cursor concertCursor = mContext.getContentResolver().query(
//                ConcertsContract.ConcertEntry.CONTENT_URI,
//                null,
//                null,
//                null,
//                null
//        );
//
//        // Make sure we get the correct cursor out of the database
//        TestUtilities.validateCursor("testBasicConcertsQuery", concertCursor, concertValues);
//    }
//
//    /*
//        This test uses the database directly to insert and then uses the ContentProvider to
//        read out the data.  Uncomment this test to see if your artist queries are
//        performing correctly.
//     */
//    public void testBasicArtistQueries() {
//        // insert our test records into the database
//        ConcertsDatabase dbHelper = new ConcertsDatabase(mContext);
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
//
//        ContentValues testValues = TestUtilities.createNofxArtistValues();
//        long artistRowId = TestUtilities.insertTestSearchValues(mContext);
//
//        // Test the basic content provider query
//        Cursor artistCursor = mContext.getContentResolver().query(
//                ConcertsContract.ArtistEntry.CONTENT_URI,
//                null,
//                null,
//                null,
//                null
//        );
//
//        // Make sure we get the correct cursor out of the database
//        TestUtilities.validateCursor("testBasicArtistQueries, artist query", artistCursor, testValues);
//
//        // Has the NotificationUri been set correctly? --- we can only test this easily against API
//        // level 19 or greater because getNotificationUri was added in API level 19.
//        if ( Build.VERSION.SDK_INT >= 19 ) {
//            assertEquals("Error: Artist Query did not properly set NotificationUri",
//                    artistCursor.getNotificationUri(), ConcertsContract.ArtistEntry.CONTENT_URI);
//        }
//    }
//
//    /*
//        This test uses the provider to insert and then update the data. Uncomment this test to
//        see if your update artist is functioning correctly.
//     */
//    public void testUpdateArtist() {
//        // Create a new map of values, where column names are the keys
//        ContentValues values = TestUtilities.createNofxArtistValues();
//
//        Uri artistUri = mContext.getContentResolver().
//                insert(ConcertsContract.ArtistEntry.CONTENT_URI, values);
//        long artistRowId = ContentUris.parseId(artistUri);
//
//        // Verify we got a row back.
//        assertTrue(artistRowId != -1);
//        Log.d(LOG_TAG, "New row id: " + artistRowId);
//
//        ContentValues updatedValues = new ContentValues(values);
//        updatedValues.put(ConcertsContract.ArtistEntry._ID, artistRowId);
//        updatedValues.put(ConcertsContract.ArtistEntry.NAME, "nfx");
//
//        // Create a cursor with observer to make sure that the content provider is notifying
//        // the observers as expected
//        Cursor artistCursor = mContext.getContentResolver().query(ConcertsContract.ArtistEntry.CONTENT_URI, null, null, null, null);
//
//        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
//        artistCursor.registerContentObserver(tco);
//
//        int count = mContext.getContentResolver().update(
//                ConcertsContract.ArtistEntry.CONTENT_URI, updatedValues, ConcertsContract.ArtistEntry._ID + "= ?",
//                new String[] { Long.toString(artistRowId)});
//        assertEquals(count, 1);
//
//        // Test to make sure our observer is called.  If not, we throw an assertion.
//        //
//        // Students: If your code is failing here, it means that your content provider
//        // isn't calling getContext().getContentResolver().notifyChange(uri, null);
//        tco.waitForNotificationOrFail();
//
//        artistCursor.unregisterContentObserver(tco);
//        artistCursor.close();
//
//        // A cursor is your primary interface to the query results.
//        Cursor cursor = mContext.getContentResolver().query(
//                ConcertsContract.ArtistEntry.CONTENT_URI,
//                null,   // projection
//                ConcertsContract.ArtistEntry._ID + " = " + artistRowId,
//                null,   // Values for the "where" clause
//                null    // sort order
//        );
//
//        TestUtilities.validateCursor("testUpdateArtist.  Error validating Artist entry update.",
//                cursor, updatedValues);
//
//        cursor.close();
//    }
//
//
//    // Make sure we can still delete after adding/updating stuff
//    //
//    // Student: Uncomment this test after you have completed writing the insert functionality
//    // in your provider.  It relies on insertions with testInsertReadProvider, so insert and
//    // query functionality must also be complete before this test can be used.
//    public void testInsertReadProvider() {
//        ContentValues testValues = TestUtilities.createNofxArtistValues();
//
//        // Register a content observer for our insert.  This time, directly with the content resolver
//        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
//        mContext.getContentResolver().registerContentObserver(ConcertsContract.ArtistEntry.CONTENT_URI, true, tco);
//        Uri artistUri = mContext.getContentResolver().insert(ConcertsContract.ArtistEntry.CONTENT_URI, testValues);
//
//        // Did our content observer get called?  Students:  If this fails, your insert artist
//        // isn't calling getContext().getContentResolver().notifyChange(uri, null);
//        tco.waitForNotificationOrFail();
//        mContext.getContentResolver().unregisterContentObserver(tco);
//
//        long artistRowId = ContentUris.parseId(artistUri);
//
//        // Verify we got a row back.
//        assertTrue(artistRowId != -1);
//
//        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
//        // the round trip.
//
//        // A cursor is your primary interface to the query results.
//        Cursor cursor = mContext.getContentResolver().query(
//                ConcertsContract.ArtistEntry.CONTENT_URI,
//                null, // leaving "columns" null just returns all the columns.
//                null, // cols for "where" clause
//                null, // values for "where" clause
//                null  // sort order
//        );
//
//        TestUtilities.validateCursor("testInsertReadProvider. Error validating ArtistEntry.",
//                cursor, testValues);
//
//        // Fantastic.  Now that we have a artist, add some concerts!
//        ContentValues concertValues = TestUtilities.createConcertValues(artistRowId);
//        // The TestContentObserver is a one-shot class
//        tco = TestUtilities.getTestContentObserver();
//
//        mContext.getContentResolver().registerContentObserver(ConcertsContract.ConcertEntry.CONTENT_URI, true, tco);
//
//        Uri concertInsertUri = mContext.getContentResolver()
//                .insert(ConcertsContract.ConcertEntry.CONTENT_URI, concertValues);
//        assertTrue(concertInsertUri != null);
//
//        // Did our content observer get called?  Students:  If this fails, your insert concerts
//        // in your ContentProvider isn't calling
//        // getContext().getContentResolver().notifyChange(uri, null);
//        tco.waitForNotificationOrFail();
//        mContext.getContentResolver().unregisterContentObserver(tco);
//
//        // A cursor is your primary interface to the query results.
//        Cursor concertCursor = mContext.getContentResolver().query(
//                ConcertsContract.ConcertEntry.CONTENT_URI,  // Table to Query
//                null, // leaving "columns" null just returns all the columns.
//                null, // cols for "where" clause
//                null, // values for "where" clause
//                null // columns to group by
//        );
//
//        TestUtilities.validateCursor("testInsertReadProvider. Error validating ConcertEntry insert.",
//                concertCursor, concertValues);
//
//        // Add the artist values in with the concert data so that we can make
//        // sure that the join worked and we actually get all the values back
//        concertValues.putAll(testValues);
//
//        // Get the joined Concert and Artist data
//        concertCursor = mContext.getContentResolver().query(
//                ConcertsContract.ConcertEntry.buildConcertsListForArtist(TestUtilities.TEST_ARTIST),
//                null, // leaving "columns" null just returns all the columns.
//                null, // cols for "where" clause
//                null, // values for "where" clause
//                null  // sort order
//        );
//        TestUtilities.validateCursor("testInsertReadProvider.  Error validating joined Concert and Artist Data.",
//                concertCursor, concertValues);
//
//    }
//
//    // Make sure we can still delete after adding/updating stuff
//    //
//    // Student: Uncomment this test after you have completed writing the delete functionality
//    // in your provider.  It relies on insertions with testInsertReadProvider, so insert and
//    // query functionality must also be complete before this test can be used.
//    public void testDeleteRecords() {
//        testInsertReadProvider();
//
//        // Register a content observer for our artist delete.
//        TestUtilities.TestContentObserver artistObserver = TestUtilities.getTestContentObserver();
//        mContext.getContentResolver().registerContentObserver(ConcertsContract.ArtistEntry.CONTENT_URI, true, artistObserver);
//
//        // Register a content observer for our concert delete.
//        TestUtilities.TestContentObserver concertObserver = TestUtilities.getTestContentObserver();
//        mContext.getContentResolver().registerContentObserver(ConcertsContract.ConcertEntry.CONTENT_URI, true, concertObserver);
//
//        deleteAllRecordsFromProvider();
//
//        // Students: If either of these fail, you most-likely are not calling the
//        // getContext().getContentResolver().notifyChange(uri, null); in the ContentProvider
//        // delete.  (only if the insertReadProvider is succeeding)
//        artistObserver.waitForNotificationOrFail();
//        concertObserver.waitForNotificationOrFail();
//
//        mContext.getContentResolver().unregisterContentObserver(artistObserver);
//        mContext.getContentResolver().unregisterContentObserver(concertObserver);
//    }
//
//
//    static private final int BULK_INSERT_RECORDS_TO_INSERT = 10;
//    static ContentValues[] createBulkInsertConcertValues(long artistRowId) {
//        ContentValues[] returnContentValues = new ContentValues[BULK_INSERT_RECORDS_TO_INSERT];
//
//        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++ ) {
//            ContentValues concertValues = new ContentValues();
//            concertValues.put(ConcertsContract.ConcertEntry.ARTIST_ID, artistRowId);
//            concertValues.put(ConcertsContract.ConcertEntry.TITLE, "Title of Concert");
//            concertValues.put(ConcertsContract.ConcertEntry.DATE, "Date of Concert: " + i);
//            concertValues.put(ConcertsContract.ConcertEntry.LOCATION, "Location of Concert");
//            concertValues.put(ConcertsContract.ConcertEntry.PLACE, "Place of Concert");
//            concertValues.put(ConcertsContract.ConcertEntry.LONGITUDE, "75");
//            concertValues.put(ConcertsContract.ConcertEntry.LATITUDE, "65");
//            concertValues.put(ConcertsContract.ConcertEntry.TICKET_STATUS, "Available");
//            returnContentValues[i] = concertValues;
//        }
//        return returnContentValues;
//    }
//
//    // Student: Uncomment this test after you have completed writing the BulkInsert functionality
//    // in your provider.  Note that this test will work with the built-in (default) provider
//    // implementation, which just inserts records one-at-a-time, so really do implement the
//    // BulkInsert ContentProvider function.
//    public void testBulkInsert() {
//        // first, let's create a artist value
//        ContentValues testValues = TestUtilities.createNofxArtistValues();
//        Uri artistUri = mContext.getContentResolver().insert(ConcertsContract.ArtistEntry.CONTENT_URI, testValues);
//        long artistRowId = ContentUris.parseId(artistUri);
//
//        // Verify we got a row back.
//        assertTrue(artistRowId != -1);
//
//        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
//        // the round trip.
//
//        // A cursor is your primary interface to the query results.
//        Cursor cursor = mContext.getContentResolver().query(
//                ConcertsContract.ArtistEntry.CONTENT_URI,
//                null, // leaving "columns" null just returns all the columns.
//                null, // cols for "where" clause
//                null, // values for "where" clause
//                null  // sort order
//        );
//
//        TestUtilities.validateCursor("testBulkInsert. Error validating ArtistEntry.",
//                cursor, testValues);
//
//        // Now we can bulkInsert some concerts.  In fact, we only implement BulkInsert for concert
//        // entries.  With ContentProviders, you really only have to implement the features you
//        // use, after all.
//        ContentValues[] bulkInsertContentValues = createBulkInsertConcertValues(artistRowId);
//
//        // Register a content observer for our bulk insert.
//        TestUtilities.TestContentObserver concertObserver = TestUtilities.getTestContentObserver();
//        mContext.getContentResolver().registerContentObserver(ConcertsContract.ConcertEntry.CONTENT_URI, true, concertObserver);
//
//        int insertCount = mContext.getContentResolver().bulkInsert(ConcertsContract.ConcertEntry.CONTENT_URI, bulkInsertContentValues);
//
//        // Students:  If this fails, it means that you most-likely are not calling the
//        // getContext().getContentResolver().notifyChange(uri, null); in your BulkInsert
//        // ContentProvider method.
//        concertObserver.waitForNotificationOrFail();
//        mContext.getContentResolver().unregisterContentObserver(concertObserver);
//
//        assertEquals(insertCount, BULK_INSERT_RECORDS_TO_INSERT);
//
//        // A cursor is your primary interface to the query results.
//        cursor = mContext.getContentResolver().query(
//                ConcertsContract.ConcertEntry.CONTENT_URI,
//                null, // leaving "columns" null just returns all the columns.
//                null, // cols for "where" clause
//                null, // values for "where" clause
//                ConcertsContract.ConcertEntry.DATE + " ASC"  // sort order == by DATE ASCENDING
//        );
//
//        // we should have as many records in the database as we've inserted
//        assertEquals(cursor.getCount(), BULK_INSERT_RECORDS_TO_INSERT);
//
//        // and let's make sure they match the ones we created
//        cursor.moveToFirst();
//        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, cursor.moveToNext() ) {
//            TestUtilities.validateCurrentRecord("testBulkInsert.  Error validating ConcertEntry " + i,
//                    cursor, bulkInsertContentValues[i]);
//        }
//        cursor.close();
//    }
//}

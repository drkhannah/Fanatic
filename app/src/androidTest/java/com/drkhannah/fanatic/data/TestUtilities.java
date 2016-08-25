package com.drkhannah.fanatic.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import com.drkhannah.fanatic.utils.PollingCheck;

import java.util.Map;
import java.util.Set;


public class TestUtilities extends AndroidTestCase {
    static final String TEST_CATEGORY = "music";
    static final String TEST_LOCATION = "44107";
    static final String TEST_KEYWORDS = "rock";

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    /*
        create some default event values for your database tests.
     */
    static ContentValues createEventValues(long searchRowId) {
        ContentValues eventValues = new ContentValues();
        eventValues.put(DBContract.EventsEntry.SEARCH_ID, searchRowId);
        eventValues.put(DBContract.EventsEntry.TITLE, "Test Title");
        eventValues.put(DBContract.EventsEntry.START_TIME, "Test time");
        eventValues.put(DBContract.EventsEntry.VENUE_NAME, "Test venue name");
        eventValues.put(DBContract.EventsEntry.CITY_NAME, "Test city name");
        eventValues.put(DBContract.EventsEntry.COUNTRY_NAME, "Test county name");
        eventValues.put(DBContract.EventsEntry.PERFORMERS, "Test performers");
        eventValues.put(DBContract.EventsEntry.LONGITUDE, "Test longitude");
        eventValues.put(DBContract.EventsEntry.LATITUDE, "Test latitude");
        eventValues.put(DBContract.EventsEntry.DESCRIPTION, "Test description");
        eventValues.put(DBContract.EventsEntry.IMG_URL, "Test image url");

        return eventValues;
    }

    static ContentValues createSearchValues() {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(DBContract.SearchEntry.CATEGORY, TEST_CATEGORY);
        testValues.put(DBContract.SearchEntry.LOCATION, TEST_LOCATION);
        testValues.put(DBContract.SearchEntry.KEYWORDS, TEST_KEYWORDS);

        return testValues;
    }


    static long insertTestSearchValues(Context context) {
        // insert our test records into the database
        DBHelper dbHelper = new DBHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = TestUtilities.createSearchValues();

        long searchRowId;
        searchRowId = db.insert(DBContract.SearchEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue("Error: Failure to insert Test Search Values", searchRowId != -1);

        return searchRowId;
    }

    /*
        tests that the onChange function is called
     */
    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }
}

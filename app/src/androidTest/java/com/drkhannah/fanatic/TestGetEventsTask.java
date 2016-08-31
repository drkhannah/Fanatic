/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.drkhannah.fanatic;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.test.AndroidTestCase;

import com.drkhannah.fanatic.data.DBContract;

public class TestGetEventsTask extends AndroidTestCase{
    static final String ADD_SEARCH_CATEGORY = "music";
    static final String ADD_SEARCH_LOCATION = "44107";
    static final String ADD_SEARCH_KEYWORDS = "rock";

    @TargetApi(11)
    public void testAddArtist() {
        // start from a clean state
        getContext().getContentResolver().delete(
                DBContract.SearchEntry.CONTENT_URI,
                DBContract.SearchEntry.CATEGORY + " = ? AND " + DBContract.SearchEntry.LOCATION + " = ? AND " + DBContract.SearchEntry.KEYWORDS + " = ? ",
                new String[]{ADD_SEARCH_CATEGORY, ADD_SEARCH_LOCATION, ADD_SEARCH_KEYWORDS});

        GetEventsTask getEventsTask = new GetEventsTask(getContext());
        long searchId = getEventsTask.addSearch(ADD_SEARCH_CATEGORY, ADD_SEARCH_LOCATION, ADD_SEARCH_KEYWORDS);

        // does addSearch return a valid record ID?
        assertFalse("Error: addSearch returned an invalid ID on insert", searchId == -1);

        // test all this twice
        for ( int i = 0; i < 2; i++ ) {

            // does the ID point to our Artist?
            Cursor searchCursor = getContext().getContentResolver().query(
                    DBContract.SearchEntry.CONTENT_URI,
                    null,
                    DBContract.SearchEntry.CATEGORY + " = ? AND " + DBContract.SearchEntry.LOCATION + " = ? AND " + DBContract.SearchEntry.KEYWORDS + " = ? ",
                    new String[]{ADD_SEARCH_CATEGORY, ADD_SEARCH_LOCATION, ADD_SEARCH_KEYWORDS},
                    null);

            // these match the indices of the projection
            if (searchCursor.moveToFirst()) {
                assertEquals("Error: the queried value of searchId does not match the returned value" + "from addSearch", searchCursor.getLong(0), searchId);
                assertEquals("Error: the queried value of Search setting is incorrect", searchCursor.getString(1), ADD_SEARCH_CATEGORY);
                assertEquals("Error: the queried value of Search setting is incorrect", searchCursor.getString(2), ADD_SEARCH_LOCATION);
                assertEquals("Error: the queried value of Search setting is incorrect", searchCursor.getString(3), ADD_SEARCH_KEYWORDS);
            } else {
                fail("Error: the id you used to query returned an empty cursor");
            }

            // there should be no more records
            assertFalse("Error: there should be only one record returned from a search query", searchCursor.moveToNext());

            // add the search again
            long newSearchId = getEventsTask.addSearch(ADD_SEARCH_CATEGORY, ADD_SEARCH_LOCATION, ADD_SEARCH_KEYWORDS);

            assertEquals("Error: inserting a search again should return the same ID", searchId, newSearchId);
        }
        // reset our state back to normal
        getContext().getContentResolver().delete(
                DBContract.SearchEntry.CONTENT_URI,
                DBContract.SearchEntry.CATEGORY + " = ? AND " + DBContract.SearchEntry.LOCATION + " = ? AND " + DBContract.SearchEntry.KEYWORDS + " = ? ",
                new String[]{ADD_SEARCH_CATEGORY, ADD_SEARCH_LOCATION, ADD_SEARCH_KEYWORDS});

        // clean up the test so that other tests can use the content provider
        getContext().getContentResolver().
                acquireContentProviderClient(DBContract.SearchEntry.CONTENT_URI).
                getLocalContentProvider().shutdown();
    }
}

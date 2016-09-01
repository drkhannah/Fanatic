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
package com.drkhannah.fanatic.data;

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;

public class TestUriMatcher extends AndroidTestCase {
    private static final String TEST_CATEGORY = "music";
    private static final String TEST_LOCATION = "44107";
    private static final String TEST_KEYWORDS = "rock";
    private static final String TEST_START_TIME = "9:00";
    private static final String TEST_TITLE = "Title";
;

    // content://com.drkhannah.fanatic.provider/events"
    private static final Uri TEST_EVENTS_DIR = DBContract.EventsEntry.CONTENT_URI;

    // content://com.drkhannah.fanatic.provider/search"
    private static final Uri TEST_SEARCH_DIR = DBContract.SearchEntry.CONTENT_URI;

    // content://com.drkhannah.fanatic.provider/events/category/location/keywords"
    private static final Uri TEST_EVENTS_FOR_SEARCH_DIR = DBContract.EventsEntry.buildEventListForSearchUri(TEST_CATEGORY, TEST_LOCATION, TEST_KEYWORDS);

    // content://com.drkhannah.fanatic.provider/events/category/location/keywords/date/time"
    private static final Uri TEST_EVENT_FOR_DATE_AND_TITLE_DIR = DBContract.EventsEntry.buildEventForSearchWithDateAndTitleUri(TEST_CATEGORY, TEST_LOCATION, TEST_KEYWORDS, TEST_START_TIME, TEST_TITLE);


    /*
        This function tests that your UriMatcher returns the correct integer value
        for each of the Uri types that our Provider can handle.
     */
    public void testUriMatcher() {
        UriMatcher testMatcher = Provider.buildUriMatcher();

        assertEquals("Error: The EVENTS_URI was matched incorrectly.", testMatcher.match(TEST_EVENTS_DIR), Provider.EVENTS_URI);

        assertEquals("Error: The EVENTS_FOR_SEARCH_URI was matched incorrectly.", testMatcher.match(TEST_EVENTS_FOR_SEARCH_DIR), Provider.EVENTS_FOR_SEARCH_URI);

        assertEquals("Error: The SEARCH_URI was matched incorrectly.", testMatcher.match(TEST_SEARCH_DIR), Provider.SEARCH_URI);

        assertEquals("Error: The EVENT_FOR_DATE_AND_TITLE_URI was matched incorrectly.", testMatcher.match(TEST_EVENT_FOR_DATE_AND_TITLE_DIR), Provider.EVENT_FOR_DATE_AND_TITLE_URI);
    }
}

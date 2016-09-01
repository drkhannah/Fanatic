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

import android.net.Uri;
import android.test.AndroidTestCase;


public class TestDBContract extends AndroidTestCase {

    private static final String TEST_CATEGORY = "music";
    private static final String TEST_LOCATION = "44107";
    private static final long TEST_SEARCH_ID = 1;
    private static final String TEST_KEYWORDS = "rock";
    private static final String TEST_START_TIME = "9:00";
    private static final String TEST_TITLE = "Title";

    public void testBuildEventListForSearchUri() {

        Uri eventsForSearchUri = DBContract.EventsEntry.buildEventListForSearchUri(TEST_CATEGORY, TEST_LOCATION, TEST_KEYWORDS);

        assertNotNull("Error: Null Uri returned.  You must fill-in buildEventListForSearchUri in " + "DBContract.", eventsForSearchUri);

        assertEquals("Error: Events for Search not properly appended to the end of the Uri", String.valueOf(TEST_KEYWORDS), eventsForSearchUri.getLastPathSegment().toString());

        assertEquals("Error: Events for Search Uri doesn't match our expected result", "content://com.drkhannah.fanatic.provider/events/music/44107/rock",  eventsForSearchUri.toString());
    }

    public void testbuildEventForSearchWithDateUri() {

        Uri eventForSearchUri = DBContract.EventsEntry.buildEventForSearchWithDateAndTitleUri(TEST_CATEGORY, TEST_LOCATION, TEST_KEYWORDS, TEST_START_TIME, TEST_TITLE);

        assertNotNull("Error: Null Uri returned.  You must fill-in buildEventListForSearchUri in " + "DBContract.", eventForSearchUri);

        assertEquals("Error: Events for Search not properly appended to the end of the Uri", TEST_TITLE, eventForSearchUri.getLastPathSegment());

        assertEquals("Error: Events for Search Uri doesn't match our expected result", "content://com.drkhannah.fanatic.provider/events/music/44107/rock/9%3A00/Title" , eventForSearchUri.toString());
    }


}

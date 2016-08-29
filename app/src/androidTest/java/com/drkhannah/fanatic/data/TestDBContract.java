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

    private static final long TEST_SEARCH_ID = 1;
    private static final String TEST_START_TIME = "9:00";
    private static final String TEST_TITLE = "Title";

    public void testBuildEventListForSearchUri() {

        Uri searchUri = DBContract.EventsEntry.buildEventListForSearchUri(TEST_SEARCH_ID);

        assertNotNull("Error: Null Uri returned.  You must fill-in buildEventListForSearchUri in " + "DBContract.", searchUri);

        assertEquals("Error: Events for Search not properly appended to the end of the Uri", String.valueOf(TEST_SEARCH_ID), searchUri.getLastPathSegment().toString());

        assertEquals("Error: Events for Search Uri doesn't match our expected result", searchUri.toString(), "content://com.drkhannah.fanatic.provider/events/1");
    }

    public void testbuildEventForSearchWithDateUri() {

        Uri searchUri = DBContract.EventsEntry.buildEventListForSearchWithDateAndTitleUri(TEST_SEARCH_ID, TEST_START_TIME, TEST_TITLE);

        assertNotNull("Error: Null Uri returned.  You must fill-in buildEventListForSearchUri in " + "DBContract.", searchUri);

        assertEquals("Error: Events for Search not properly appended to the end of the Uri", TEST_TITLE, searchUri.getLastPathSegment());

        assertEquals("Error: Events for Search Uri doesn't match our expected result", searchUri.toString(), "content://com.drkhannah.fanatic.provider/events/1/9%3A00/Title");
    }


}

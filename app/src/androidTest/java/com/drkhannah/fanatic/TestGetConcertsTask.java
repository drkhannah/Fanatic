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
//package com.drkhannah.concerts2;
//
//import android.annotation.TargetApi;
//import android.database.Cursor;
//import android.test.AndroidTestCase;
//
//import com.drkhannah.concerts2.database.ConcertsContract;
//import com.drkhannah.concerts2.network.GetConcertsTask;
//
//public class TestGetConcertsTask extends AndroidTestCase{
//    static final String ADD_ARTIST = "nofx";
//    static final String ADD_IMG_URL = "https://s3.amazonaws.com/bit-photos/thumb/6739767.jpeg";
//
//    /*
//        Students: uncomment testAddArtist after you have written the AddArtist function.
//        This test will only run on API level 11 and higher because of a requirement in the
//        content provider.
//     */
//    @TargetApi(11)
//    public void testAddArtist() {
//        // start from a clean state
//        getContext().getContentResolver().delete(ConcertsContract.ArtistEntry.CONTENT_URI,
//                ConcertsContract.ArtistEntry.NAME + " = ?",
//                new String[]{ADD_ARTIST});
//
//        GetConcertsTask fwt = new GetConcertsTask(getContext(), null, null, null);
//        long artistId = fwt.addArtist(ADD_ARTIST, ADD_IMG_URL);
//
//        // does addArtist return a valid record ID?
//        assertFalse("Error: addArtist returned an invalid ID on insert",
//                artistId == -1);
//
//        // test all this twice
//        for ( int i = 0; i < 2; i++ ) {
//
//            // does the ID point to our Artist?
//            Cursor artistCursor = getContext().getContentResolver().query(
//                    ConcertsContract.ArtistEntry.CONTENT_URI,
//                    new String[]{
//                            ConcertsContract.ArtistEntry._ID,
//                            ConcertsContract.ArtistEntry.NAME,
//                            ConcertsContract.ArtistEntry.IMG_URL
//                    },
//                    ConcertsContract.ArtistEntry.NAME + " = ?",
//                    new String[]{ADD_ARTIST},
//                    null);
//
//            // these match the indices of the projection
//            if (artistCursor.moveToFirst()) {
//                assertEquals("Error: the queried value of artistId does not match the returned value" +
//                        "from addArtist", artistCursor.getLong(0), artistId);
//                assertEquals("Error: the queried value of Artist setting is incorrect",
//                        artistCursor.getString(1), ADD_ARTIST);
//                assertEquals("Error: the queried value of Artist setting is incorrect",
//                        artistCursor.getString(2), ADD_IMG_URL);
//            } else {
//                fail("Error: the id you used to query returned an empty cursor");
//            }
//
//            // there should be no more records
//            assertFalse("Error: there should be only one record returned from a artist query",
//                    artistCursor.moveToNext());
//
//            // add the artist again
//            long newArtistId = fwt.addArtist(ADD_ARTIST, ADD_IMG_URL);
//
//            assertEquals("Error: inserting a artist again should return the same ID",
//                    artistId, newArtistId);
//        }
//        // reset our state back to normal
//        getContext().getContentResolver().delete(ConcertsContract.ArtistEntry.CONTENT_URI,
//                ConcertsContract.ArtistEntry.NAME + " = ?",
//                new String[]{ADD_ARTIST});
//
//        // clean up the test so that other tests can use the content provider
//        getContext().getContentResolver().
//                acquireContentProviderClient(ConcertsContract.ArtistEntry.CONTENT_URI).
//                getLocalContentProvider().shutdown();
//    }
//}

package com.drkhannah.fanatic.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by dhannah on 8/25/16.
 */
public class DBContract {

    //Search Table columns
    public interface SearchColumns {
        String CATEGORY = "category";
        String LOCATION = "location";
        String KEYWORDS = "keywords";
    }

    //Events Table columns
    public interface EventsColumns {
        String SEARCH_ID = "search_id";
        String TITLE = "title";
        String START_TIME = "start_time";
        String VENUE_NAME = "venue_name";
        String CITY_NAME = "city_name";
        String COUNTRY_NAME = "county_name";
        String PERFORMERS = "performers";
        String LONGITUDE = "longitude";
        String LATITUDE = "latitude";
        String DESCRIPTION = "description";
        String IMG_URL = "img_url";
    }

    //content authority
    public static final String CONTENT_AUTHORITY = "com.drkhannah.fanatic.provider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_SEARCH = "search";
    public static final String PATH_EVENT = "events";

    public static final class SearchEntry implements SearchColumns, BaseColumns {
        public static final String TABLE_NAME = "search";
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendEncodedPath(PATH_SEARCH)
                .build();

        //MIME TYPE to return more than one row from the search table
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SEARCH;
        //MIME TYPE to return a single row from the search table
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SEARCH;

        //builds a URI from a search id
        public static Uri buildSearchUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class EventsEntry implements EventsColumns, BaseColumns {
        public static final String TABLE_NAME = "events";
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendEncodedPath(PATH_EVENT)
                .build();

        //MIME TYPE to return more than one row from the event table
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_EVENT;
        //MIME TYPE to return a single row from the event table
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_EVENT;

        //builds a URI from a search_id
        public static Uri buildEventListForSearchUri(String searchId) {
            return CONTENT_URI.buildUpon().appendPath(searchId).build();
        }

        //builds a URI from a search_id and date
        public static Uri buildEventForSearchWithDate(String searchId, String date) {
            return CONTENT_URI.buildUpon().appendPath(searchId).appendPath(date).build();
        }

        public static String getEventFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static String getDateFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }
    }
}

package com.drkhannah.fanatic.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by dhannah on 8/25/16.
 */
public class Provider extends ContentProvider {

    private static final String LOG_TAG = Provider.class.getSimpleName();

    public static final UriMatcher sUriMatcher = buildUriMatcher();
    private DBHelper mDBHelper;

    public static final int SEARCH_URI = 100;
    public static final int EVENTS_URI = 101;
    public static final int EVENTS_FOR_SEARCH_URI = 102;
    public static final int EVENT_FOR_DATE_AND_TITLE_URI = 103;

    private static final SQLiteQueryBuilder sEventsBySearchQueryBuilder;

    static {
        sEventsBySearchQueryBuilder = new SQLiteQueryBuilder();
        //events INNER JOIN search ON event.search_id = search._id
        sEventsBySearchQueryBuilder.setTables(
                DBContract.EventsEntry.TABLE_NAME + " INNER JOIN " +
                        DBContract.SearchEntry.TABLE_NAME +
                        " ON " + DBContract.EventsEntry.TABLE_NAME +
                        "." + DBContract.EventsEntry.SEARCH_ID +
                        " = " + DBContract.SearchEntry.TABLE_NAME +
                        "." + DBContract.SearchEntry._ID);
    }

    //search.category = ? AND location = ? and keywords = ?
    private static final String sSearchSelection = DBContract.SearchEntry.TABLE_NAME + "." + DBContract.SearchEntry.CATEGORY + " = ? AND " + DBContract.SearchEntry.LOCATION + " = ? AND " + DBContract.SearchEntry.KEYWORDS + " = ? ";

    private Cursor getEventsForSearch(Uri uri, String[] projection, String sortOrder) {
        String searchCategory = DBContract.EventsEntry.getCategoryFromUri(uri);
        String searchLocation = DBContract.EventsEntry.getLocationFromUri(uri);
        String searchKeywords = DBContract.EventsEntry.getKeywordsFromUri(uri);

        String[] selectionArgs = new String[]{searchCategory, searchLocation, searchKeywords};
        String selection = sSearchSelection;

        return sEventsBySearchQueryBuilder.query(mDBHelper.getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);
    }

    //search.category = ? AND search.location = ? and search.keywords = ? AND event.start_time = ? AND event.title = ?
    private static final String sSearchAndDateSelection = DBContract.SearchEntry.TABLE_NAME + "." + DBContract.SearchEntry.CATEGORY + " = ? AND " + DBContract.SearchEntry.LOCATION + " = ? AND " + DBContract.SearchEntry.KEYWORDS + " = ? AND "+ DBContract.EventsEntry.START_TIME + " = ? AND " + DBContract.EventsEntry.TITLE + " = ? ";

    private Cursor getEventsForSearchWithDateAndTitle(Uri uri, String[] projection, String sortOrder) {
        String categoryFromUri = DBContract.EventsEntry.getCategoryFromUri(uri);
        String locationFromUri = DBContract.EventsEntry.getLocationFromUri(uri);
        String keywordsFromUri = DBContract.EventsEntry.getKeywordsFromUri(uri);
        String startTimeFromUri = DBContract.EventsEntry.getStartTimeFromUri(uri);
        String titleFromUri = DBContract.EventsEntry.getTitleFromUri(uri);

        String[] selectionArgs = new String[]{categoryFromUri, locationFromUri, keywordsFromUri, startTimeFromUri, titleFromUri};
        String selection = sSearchAndDateSelection;

        return sEventsBySearchQueryBuilder.query(mDBHelper.getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);
    }

    //Declaire valid uri paths in UriMatcher
    public static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DBContract.CONTENT_AUTHORITY;

        //Each Uri needs a unique code
        matcher.addURI(authority, DBContract.PATH_SEARCH, SEARCH_URI);
        matcher.addURI(authority, DBContract.PATH_EVENTS, EVENTS_URI);
        matcher.addURI(authority, DBContract.PATH_EVENTS + "/*/*/*", EVENTS_FOR_SEARCH_URI);
        matcher.addURI(authority, DBContract.PATH_EVENTS + "/*/*/*/*/*", EVENT_FOR_DATE_AND_TITLE_URI);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mDBHelper = new DBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        //use UriMatcher to match the uri to a MIME TYPE
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case SEARCH_URI: //can only ever return a single record
                return DBContract.SearchEntry.CONTENT_ITEM_TYPE;
            case EVENTS_URI: //can return multiple records
                return DBContract.EventsEntry.CONTENT_TYPE;
            case EVENTS_FOR_SEARCH_URI: //can return multiple records
                return DBContract.EventsEntry.CONTENT_TYPE;
            case EVENT_FOR_DATE_AND_TITLE_URI: //can only return a single record
                return DBContract.EventsEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("content provider getType() Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        //switch on a given Uri, and query the database
        Cursor cursor;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            //content://com.drkhannah.fanatic.provider/search
            case SEARCH_URI: {
                cursor = mDBHelper.getReadableDatabase().query(
                        DBContract.SearchEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            // "content://com.drkhannah.fanatic.provider/events"
            case EVENTS_URI: {
                cursor = mDBHelper.getReadableDatabase().query(
                        DBContract.EventsEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            //content://com.drkhannah.fanatic.provider/events/#
            case EVENTS_FOR_SEARCH_URI: {
                cursor = getEventsForSearch(uri, projection, sortOrder);
                break;
            }
            //content://com.drkhannah.fanatic.provider/events/#/*/*
            case EVENT_FOR_DATE_AND_TITLE_URI: {
                cursor = getEventsForSearchWithDateAndTitle(uri, projection, sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("content provider query() Unknown uri: " + uri);
        }
        //watch for changes on a URI and any of its decendents
        //this allows the content provider to alert the UI when a cursor changes
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final SQLiteDatabase db = mDBHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case SEARCH_URI: {
                long _id = db.insert(DBContract.SearchEntry.TABLE_NAME, null, contentValues);
                if (_id > 0) {
                    returnUri = DBContract.SearchEntry.buildSearchUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case EVENTS_URI: {
                long _id = db.insert(DBContract.EventsEntry.TABLE_NAME, null, contentValues);
                if (_id > 0) {
                    returnUri = DBContract.EventsEntry.buildEventUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Content Provider insert() Unknown uri: " + uri);
        }
        //notify that there was a database change
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDBHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if (null == selection) selection = "1";
        switch (match) {
            case SEARCH_URI:
                rowsDeleted = db.delete(DBContract.SearchEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case EVENTS_URI:
                rowsDeleted = db.delete(DBContract.EventsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Content Provider delete() Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            //notify that there was a database change
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;

    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDBHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case SEARCH_URI:
                rowsUpdated = db.update(DBContract.SearchEntry.TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            case EVENTS_URI:
                rowsUpdated = db.update(DBContract.EventsEntry.TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Content Provider update() Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            //notify that there was a database change
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mDBHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case EVENTS_URI:
                //start a database transaction to insert multiple items at once
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(DBContract.EventsEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    //set database transaction as successful after insert loop finishes
                    db.setTransactionSuccessful();
                } finally {
                    //end the database transaction
                    db.endTransaction();
                }
                //notify that there was a database change
                getContext().getContentResolver().notifyChange(uri, null);

                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mDBHelper.close();
        super.shutdown();
    }

}

package com.drkhannah.fanatic;

import android.app.IntentService;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.drkhannah.fanatic.data.DBContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

public class EventIntentService extends IntentService {
    private static final String LOG_TAG = EventIntentService.class.getSimpleName();

    private static final String ACTION_GET_EVENTS = "com.drkhannah.fanatic.action.EVENTS";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.drkhannah.fanatic.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.drkhannah.fanatic.extra.PARAM2";
    private static final String EXTRA_PARAM3 = "com.drkhannah.fanatic.extra.PARAM3";

    public EventIntentService() {
        super("EventIntentService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionGetEvents(Context context, String category, String location, String keywords) {
        Intent intent = new Intent(context, EventIntentService.class);
        intent.setAction(ACTION_GET_EVENTS);
        intent.putExtra(EXTRA_PARAM1, category);
        intent.putExtra(EXTRA_PARAM2, location);
        intent.putExtra(EXTRA_PARAM3, keywords);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_GET_EVENTS.equals(action)) {
                final String category = intent.getStringExtra(EXTRA_PARAM1);
                final String location = intent.getStringExtra(EXTRA_PARAM2);
                final String keywords = intent.getStringExtra(EXTRA_PARAM3);
                handleActionGetEvents(category, location, keywords);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionGetEvents(String category, String location, String keywords) {
        String eventsJsonString = null;

        //get what was passed to GetEventsTask.execute();
        final String CATEGORY_TO_SEARCH = category;
        final String LOCATION_TO_SEARCH = location;
        final String KEYWORDS_TO_SEARCH = keywords;

        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {

            // URL for bands api query
            //Build a uri to construct a valid url
            final String BASE_URL = "http://api.eventful.com/json/events/search?";
            final String API_KEY = "app_key";
            final String CATEGORY = "category";
            final String LOCATION = "location";
            final String KEYWORDS = "keywords";
            final String PAGE_SIZE = "page_size";

            //build a valid URI
            Uri validUri = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter(API_KEY, this.getString(R.string.api_key))
                    .appendQueryParameter(CATEGORY, CATEGORY_TO_SEARCH)
                    .appendQueryParameter(LOCATION, LOCATION_TO_SEARCH)
                    .appendQueryParameter(KEYWORDS, KEYWORDS_TO_SEARCH)
                    .appendQueryParameter(PAGE_SIZE, this.getString(R.string.response_page_size))
                    .build();

            URL url = new URL(validUri.toString());
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.e(LOG_TAG, "HTTP ERROR RESPONSE CODE" + connection.getResponseCode());
                return;
            }

            // Read the input stream into a String
            InputStream inputStream = connection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // there is nothing in the inputStream so return null
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // append a line break to each line read
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return;
            }

            eventsJsonString = buffer.toString();

            Log.v(LOG_TAG, "RESPONSE FROM BANDSINTOWN: " + eventsJsonString);

        } catch (IOException e) {
            Log.e(LOG_TAG, "ERROR GETTING RESPONSE: " + e);
            return;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "ERROR CLOSING READER: " + e);
                }
            }
        }
        try {
            parseJson(eventsJsonString, CATEGORY_TO_SEARCH, LOCATION_TO_SEARCH, KEYWORDS_TO_SEARCH);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return;
    }

    private void parseJson(String responseString, String category, String location, String keywords) throws JSONException {

        //these are the properties that we need to get from the JSON response
        final String EVENTS = "events";
        final String EVENT = "event";
        final String TITLE = "title";
        final String START_TIME = "start_time";
        final String VENUE_NAME = "venue_name";
        final String CITY_NAME = "city_name";
        final String COUNTRY_NAME = "country_name";
        final String PERFORMERS = "performers";
        final String PERFORMER = "performer";
        final String PERFORMER_NAME = "name";
        final String LONGITUDE = "longitude";
        final String LATITUDE = "latitude";
        final String IMAGE = "image";
        final String IMAGE_SIZE = "medium";
        final String IMAGE_URL = "url";
        final String DESCRIPTION = "description";

        long searchId = addSearch(category, location, keywords);

        //get to the event array in the responseString
        JSONObject eventsJson = new JSONObject(responseString);
        JSONObject events = eventsJson.optJSONObject(EVENTS);
        if (events != null) {
            JSONArray eventArray = events.optJSONArray(EVENT);

            if (eventArray != null) {
                //Vector of Content Values that will be inserted into the events table of the database
                Vector<ContentValues> valuesVector = new Vector<ContentValues>(eventArray.length());

                //loop through events
                if (eventArray.length() > 0) {
                    for (int i = 0; i < eventArray.length(); i++) {
                        JSONObject jsonEvent = eventArray.getJSONObject(i);

                        //event info we want to extra from json
                        String title = jsonEvent.getString(TITLE);
                        String startTime = getReadableDateString(jsonEvent.getString(START_TIME));
                        String venueName = jsonEvent.getString(VENUE_NAME);
                        String cityName = jsonEvent.getString(CITY_NAME);
                        String countryName = jsonEvent.getString(COUNTRY_NAME);
                        String longitude = jsonEvent.getString(LONGITUDE);
                        String latitude = jsonEvent.getString(LATITUDE);
                        String description = android.text.Html.fromHtml(jsonEvent.getString(DESCRIPTION)).toString();

                        //loop through performers to find all performers
                        List<String> performers = new ArrayList<>();
                        JSONObject jsonPerformersObject = jsonEvent.optJSONObject(PERFORMERS);
                        if (jsonPerformersObject != null) {
                            JSONArray jsonPerformerArray = jsonPerformersObject.optJSONArray(PERFORMER);
                            if (jsonPerformerArray != null) {
                                for (int j = 0; j < jsonPerformerArray.length(); j++) {
                                    JSONObject jsonPerformer = jsonPerformerArray.getJSONObject(j);
                                    String performerName = jsonPerformer.getString(PERFORMER_NAME);
                                    performers.add(performerName);
                                }
                            }
                        }

                        //get image url
                        JSONObject jsonImage = jsonEvent.optJSONObject(IMAGE);
                        String imageUrl;
                        if (jsonImage != null) {
                            JSONObject jsonImageSmall = jsonImage.optJSONObject(IMAGE_SIZE);
                            imageUrl = jsonImageSmall.optString(IMAGE_URL);
                        } else {
                            imageUrl= null;
                        }

                        //create ContentValues for this event
                        ContentValues eventValues = new ContentValues();
                        eventValues.put(DBContract.EventsEntry.SEARCH_ID, searchId);
                        eventValues.put(DBContract.EventsEntry.TITLE, title);
                        eventValues.put(DBContract.EventsEntry.START_TIME, startTime);
                        eventValues.put(DBContract.EventsEntry.VENUE_NAME, venueName);
                        eventValues.put(DBContract.EventsEntry.CITY_NAME, cityName);
                        eventValues.put(DBContract.EventsEntry.COUNTRY_NAME, countryName);
                        eventValues.put(DBContract.EventsEntry.PERFORMERS, performers.toString().replace("[", "").replace("]", ""));
                        eventValues.put(DBContract.EventsEntry.LONGITUDE, longitude);
                        eventValues.put(DBContract.EventsEntry.LATITUDE, latitude);
                        eventValues.put(DBContract.EventsEntry.DESCRIPTION, description);
                        eventValues.put(DBContract.EventsEntry.IMG_URL, imageUrl);

                        //add the eventValues to the Vector<ContentValues>
                        valuesVector.add(eventValues);
                    }
                }
                //bulk insert valuesVector into the events table of the database
                int inserted = 0;
                if (valuesVector.size() > 0) {
                    ContentValues[] cvArray = new ContentValues[valuesVector.size()];
                    valuesVector.toArray(cvArray);
                    inserted = this.getContentResolver().bulkInsert(DBContract.EventsEntry.CONTENT_URI, cvArray);
                }

                Log.d(LOG_TAG, "EventIntentService Complete. " + inserted + " Inserted");
            }
        }
        return;
    }

    private String getReadableDateString(String time) {
        // format the start_time that was returned from json
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd h:mm").parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new SimpleDateFormat("MM-dd-yyy h:mm").format(date);
    }

    //add a search to the database, or retrieve a search from the database
    long addSearch(String category, String location, String keywords) {
        long searchId;

        // First, check if a search with this category, location, and keywords exists in the database
        Cursor searchCursor = this.getContentResolver().query(
                DBContract.SearchEntry.CONTENT_URI,
                new String[]{DBContract.SearchEntry._ID},
                DBContract.SearchEntry.CATEGORY + " = ? AND " + DBContract.SearchEntry.LOCATION + " = ? AND " + DBContract.SearchEntry.KEYWORDS + " = ? ",
                new String[]{category, location, keywords},
                null);

        if (searchCursor.moveToFirst()) {
            int searchIdIndex = searchCursor.getColumnIndex(DBContract.SearchEntry._ID);
            searchId = searchCursor.getLong(searchIdIndex);
        } else {
            // Now that the content provider is set up, inserting rows of data is pretty simple.
            // First create a ContentValues object to hold the data you want to insert.
            ContentValues searchValues = new ContentValues();

            // Then add the data, along with the corresponding name of the data type,
            // so the content provider knows what kind of value is being inserted.
            searchValues.put(DBContract.SearchEntry.CATEGORY, category);
            searchValues.put(DBContract.SearchEntry.LOCATION, location);
            searchValues.put(DBContract.SearchEntry.KEYWORDS, keywords);

            // Finally, insert search data into the database.
            Uri insertedUri = this.getContentResolver().insert(
                    DBContract.SearchEntry.CONTENT_URI,
                    searchValues
            );

            // The resulting URI contains the ID for the row.  Extract the searchId from the Uri.
            searchId = ContentUris.parseId(insertedUri);
        }

        searchCursor.close();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPref.edit().putLong(this.getString(R.string.last_search_id), searchId).commit();

        return searchId;
    }

}

package com.drkhannah.fanatic;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
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

/**
 * Created by dhannah on 8/23/16.
 */
public class GetEventsTask extends AsyncTask<String, Void, Void> {

    private final String LOG_TAG = GetEventsTask.class.getSimpleName();

    private Context mContext;

    public GetEventsTask(Context context) {
        mContext = context;
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
        Cursor searchCursor = mContext.getContentResolver().query(
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
            Uri insertedUri = mContext.getContentResolver().insert(
                    DBContract.SearchEntry.CONTENT_URI,
                    searchValues
            );

            // The resulting URI contains the ID for the row.  Extract the searchId from the Uri.
            searchId = ContentUris.parseId(insertedUri);
        }

        searchCursor.close();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        sharedPref.edit().putLong(mContext.getString(R.string.last_search_id), searchId).commit();

        return searchId;
    }

    private Void parseJson(String responseString, String category, String location, String keywords) throws JSONException {

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
        final String IMAGE_SIZE = "large";
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
                            JSONObject jsonImageLarge = jsonImage.optJSONObject(IMAGE_SIZE);
                            imageUrl = jsonImageLarge.optString(IMAGE_URL);
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
                    inserted = mContext.getContentResolver().bulkInsert(DBContract.EventsEntry.CONTENT_URI, cvArray);
                }

                Log.d(LOG_TAG, "GetEventsTask Complete. " + inserted + " Inserted");
            }
        }
        return null;
    }


    @Override
    protected Void doInBackground(String... params) {

        String eventsJsonString = null;

        //get what was passed to GetEventsTask.execute();
        final String CATEGORY_TO_SEARCH = params[0];
        final String LOCATION_TO_SEARCH = params[1];
        final String KEYWORDS_TO_SEARCH = params[2];

        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {

            //Build a uri to construct a valid url
            final String BASE_URL = "http://api.eventful.com/json/events/search?";
            final String API_KEY = "app_key";
            final String CATEGORY = "category";
            final String LOCATION = "location";
            final String KEYWORDS = "keywords";
            final String PAGE_SIZE = "page_size";
            final String IMAGE_SIZE = "large";

            //build a valid URI
            Uri validUri = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter(API_KEY, mContext.getString(R.string.api_key))
                    .appendQueryParameter(CATEGORY, CATEGORY_TO_SEARCH)
                    .appendQueryParameter(LOCATION, LOCATION_TO_SEARCH)
                    .appendQueryParameter(KEYWORDS, KEYWORDS_TO_SEARCH)
                    .appendQueryParameter(PAGE_SIZE, mContext.getString(R.string.response_page_size))
                    .appendQueryParameter(IMAGE_SIZE, mContext.getString(R.string.response_image_size))
                    .build();

            URL url = new URL(validUri.toString());
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.e(LOG_TAG, "HTTP ERROR RESPONSE CODE" + connection.getResponseCode());
                return null;
            }

            // Read the input stream into a String
            InputStream inputStream = connection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // there is nothing in the inputStream so return null
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // append a line break to each line read
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }

            eventsJsonString = buffer.toString();

            Log.v(LOG_TAG, "RESPONSE FROM Eventful: " + eventsJsonString);

        } catch (IOException e) {
            Log.e(LOG_TAG, "ERROR GETTING RESPONSE: " + e);
            return null;
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
            return parseJson(eventsJsonString, CATEGORY_TO_SEARCH, LOCATION_TO_SEARCH, KEYWORDS_TO_SEARCH);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}

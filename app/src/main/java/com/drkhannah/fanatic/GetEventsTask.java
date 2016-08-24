package com.drkhannah.fanatic;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.drkhannah.fanatic.adapters.RecyclerViewAdapter;
import com.drkhannah.fanatic.models.Event;

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

/**
 * Created by dhannah on 8/23/16.
 */
public class GetEventsTask extends AsyncTask<String, Void, List<Event>> {

    private final String LOG_TAG = GetEventsTask.class.getSimpleName();

    private Context mContext;
    private RecyclerViewAdapter mRecyclerViewAdapter;

    public GetEventsTask(Context context, RecyclerViewAdapter recyclerViewAdapter) {
        mContext = context;
        mRecyclerViewAdapter = recyclerViewAdapter;
    }

    /* The date/time conversion code is going to be moved outside the asynctask later,
         * so for convenience we're breaking it out into its own method now.
         */
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

    private List<Event> parseJson(String responseString) throws JSONException {

        List<Event> eventsList = new ArrayList<>();

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
        final String IMAGE_SIZE = "small";
        final String IMAGE_URL = "url";
        final String DESCRIPTION = "description";

        //get to the event array in the responseString
        JSONObject eventsJson = new JSONObject(responseString);
        JSONObject events = eventsJson.getJSONObject(EVENTS);
        JSONArray eventArray = events.getJSONArray(EVENT);

        //loop through events and create a List<Event>
        if (eventArray.length() > 0) {
            for (int i = 0; i < eventArray.length(); i++) {
                JSONObject jsonEvent = eventArray.getJSONObject(i);

                //event info we want to extra from json
                String title = jsonEvent.getString(TITLE);
                String startTime = getReadableDateString(jsonEvent.getString(START_TIME));
                String venuName = jsonEvent.getString(VENUE_NAME);
                String cityName = jsonEvent.getString(CITY_NAME);
                String countryName = jsonEvent.getString(COUNTRY_NAME);
                String longitude = jsonEvent.getString(LONGITUDE);
                String latitude = jsonEvent.getString(LATITUDE);
                String description = jsonEvent.getString(DESCRIPTION);

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
                String jsonImageUrl;
                if (jsonImage != null) {
                    JSONObject jsonImageSize = jsonImage.getJSONObject(IMAGE_SIZE);
                    jsonImageUrl = jsonImageSize.getString(IMAGE_URL);
                } else {
                    jsonImageUrl = null;
                }


                Event event = new Event(title, startTime, venuName, cityName, countryName, performers, longitude, latitude, description, jsonImageUrl);
                eventsList.add(event);
            }
        }

        return eventsList;
    }

    @Override
    protected List<Event> doInBackground(String... params) {

        String eventsJsonString = null;

        //get what was passed to GetEventsTask.execute();
        final String CATEGORY_TO_SEARCH = params[0];
        final String LOCATION_TO_SEARCH = params[1];

        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {

            // URL for bands api query
            //Build a uri to construct a valid url
            final String BASE_URL = "http://api.eventful.com/json/events/search?";
            final String API_KEY = "app_key";
            final String CATEGORY = "category";
            final String LOCATION = "location";
            final String PAGE_SIZE = "page_size";

            //build a valid URI
            Uri validUri = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter(API_KEY, mContext.getString(R.string.api_key))
                    .appendQueryParameter(CATEGORY, CATEGORY_TO_SEARCH)
                    .appendQueryParameter(LOCATION, LOCATION_TO_SEARCH)
                    .appendQueryParameter(PAGE_SIZE, mContext.getString(R.string.response_page_size))
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

            Log.v(LOG_TAG, "RESPONSE FROM BANDSINTOWN: " + eventsJsonString);

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
            return parseJson(eventsJsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(List<Event> result) {
        super.onPostExecute(result);
        mRecyclerViewAdapter.swapData(result);
    }
}

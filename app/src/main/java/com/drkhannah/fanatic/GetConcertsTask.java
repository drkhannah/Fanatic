package com.drkhannah.fanatic;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.drkhannah.fanatic.adapters.RecyclerViewAdapter;
import com.drkhannah.fanatic.models.Concert;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dhannah on 8/23/16.
 */
public class GetConcertsTask extends AsyncTask<String, Void, List<Concert>> {

    private final String LOG_TAG = GetConcertsTask.class.getSimpleName();

    private Context mContext;
    private RecyclerViewAdapter mRecyclerViewAdapter;

    public GetConcertsTask(Context context, RecyclerViewAdapter recyclerViewAdapter) {
        mContext = context;
        mRecyclerViewAdapter = recyclerViewAdapter;
    }

    /* The date/time conversion code is going to be moved outside the asynctask later,
         * so for convenience we're breaking it out into its own method now.
         */
    private String getReadableDateString(long time) {
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
        return shortenedDateFormat.format(time);
    }

    private List<Concert> parseJson(String responseString, String artistToSearch) throws JSONException {

        List<Concert> concertsList = new ArrayList<>();

        //these are the properties that we need to get from the JSON response
        final String EVENTS = "events";
        final String EVENT = "event";
        final String TITLE = "title";
        final String START_TIME = "start_time";
        final String VENUE_NAME = "venue_name";
        final String CITY_NAME = "city_name";
        final String COUNTRY_NAME = "country_name";
        final String PREFORMERS = "performers";
        final String PREFORMER = "performer";
        final String PERFORMER_NAME = "name";
        final String LONGITUDE = "longitude";
        final String LATITUDE = "latitude";
        final String IMAGE = "image";
        final String IMAGE_SIZE = "small";
        final String IMAGE_URL = "url";
        final String DESCRIPTION = "description";

        //get to the event array in the responseString
        JSONObject concertsJson = new JSONObject(responseString);
        JSONObject events = concertsJson.getJSONObject(EVENTS);
        JSONArray eventArray = events.getJSONArray(EVENT);

        //loop through events and create a List<Concert>
        if (eventArray.length() > 0) {
            for (int i = 0; i < eventArray.length(); i++) {
                JSONObject jsonConcert = eventArray.getJSONObject(i);

                //concert info we want to extra from json
                String title = jsonConcert.getString(TITLE);
                String startTime = jsonConcert.getString(START_TIME);
                String venuName = jsonConcert.getString(VENUE_NAME);
                String cityName = jsonConcert.getString(CITY_NAME);
                String countryName = jsonConcert.getString(COUNTRY_NAME);
                String longitude = jsonConcert.getString(LONGITUDE);
                String latitude = jsonConcert.getString(LATITUDE);
                String description = jsonConcert.getString(DESCRIPTION);

                //loop through performers to find all performers
                List<String> performers = new ArrayList<>();
                JSONObject jsonPerformersObject = jsonConcert.optJSONObject(PREFORMERS);
                if (jsonPerformersObject == null) {
                    performers.add(artistToSearch);
                } else {
                    JSONArray jsonPerformerArray = jsonPerformersObject.optJSONArray(PREFORMER);
                    if (jsonPerformerArray == null) {
                        performers.add(artistToSearch);
                    } else {
                        for (int j = 0; j < jsonPerformerArray.length(); j++) {
                            JSONObject jsonPerformer = jsonPerformerArray.getJSONObject(j);
                            String performerName = jsonPerformer.getString(PERFORMER_NAME);
                            performers.add(performerName);
                        }
                    }
                }

                //get image url
                JSONObject jsonImage = jsonConcert.optJSONObject(IMAGE);
                String jsonImageUrl;
                if (jsonImage != null) {
                    JSONObject jsonImageSize = jsonImage.getJSONObject(IMAGE_SIZE);
                    jsonImageUrl = jsonImageSize.getString(IMAGE_URL);
                } else {
                    jsonImageUrl = null;
                }


                Concert concert = new Concert(artistToSearch, title, startTime, venuName, cityName, countryName, performers, longitude, latitude, description, jsonImageUrl);
                concertsList.add(concert);
            }
        }

        return concertsList;
    }

    @Override
    protected List<Concert> doInBackground(String... params) {

        String concertsJsonString = null;

        //get what was passed to GetConcertsTask.execute();
        String artistToSearch = params[0];

        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {

            // URL for bands api query
            //Build a uri to construct a valid url
            final String BASE_URL = "http://api.eventful.com/json/events/search?";
            final String API_KEY = "app_key";
            final String CATEGORY = "category";
            final String KEYWORDS = "keywords";


            final String ARTIST = artistToSearch;

            //build a valid URI
            Uri validUri = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter(API_KEY, mContext.getString(R.string.api_key))
                    .appendQueryParameter(CATEGORY, mContext.getString(R.string.category_music))
                    .appendQueryParameter(KEYWORDS, ARTIST)
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

            concertsJsonString = buffer.toString();

            Log.v(LOG_TAG, "RESPONSE FROM BANDSINTOWN: " + concertsJsonString);

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
            return parseJson(concertsJsonString, artistToSearch);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(List<Concert> result) {
        super.onPostExecute(result);
        mRecyclerViewAdapter.swapData(result);
    }
}

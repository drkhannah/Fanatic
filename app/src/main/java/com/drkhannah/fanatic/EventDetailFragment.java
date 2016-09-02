package com.drkhannah.fanatic;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.drkhannah.fanatic.data.DBContract;
import com.squareup.picasso.Picasso;


/**
 * A fragment representing a single Event detail screen.
 * This fragment is either contained in a {@link EventListActivity}
 * in two-pane mode (on tablets) or a {@link EventDetailActivity}
 * on handsets.
 */
public class EventDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    /**
     * The fragment argument representing the event ID that this fragment
     * represents.
     */
    public static final String CATEGORY_ARG = "category_arg";
    public static final String LOCATION_ARG = "location_arg";
    public static final String KEYWORDS_ARG = "keywords_arg";
    public static final String TITLE_ARG = "title_arg";
    public static final String START_TIME_ARG = "start_time_arg";

    private String mCategory;
    private String mLocation;
    private String mKeywords;
    private String mTitle;
    private String mStartTime;
    private String mLongitude;
    private String mLatitude;
    private String mGeo;
    private String mLocationQuery;

    private TextView mTitleTextView;
    private TextView mStartTimeTextView;
    private TextView mVenueNameTextView;
    private TextView mDescriptionTextView;
    private TextView mPerformersTextView;

    private ImageView mEventImageView;

    private CollapsingToolbarLayout mAppBarLayout;

    private static final int DETAIL_LOADER = 2;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public EventDetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Activity activity = this.getActivity();
        mAppBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);

        if (getArguments().containsKey(CATEGORY_ARG)) {
            mCategory = getArguments().getString(CATEGORY_ARG);
        }
        if (getArguments().containsKey(CATEGORY_ARG)) {
            mLocation = getArguments().getString(LOCATION_ARG);
        }
        if (getArguments().containsKey(CATEGORY_ARG)) {
            mKeywords = getArguments().getString(KEYWORDS_ARG);
        }
        if (getArguments().containsKey(TITLE_ARG)) {
            mTitle = getArguments().getString(TITLE_ARG);
        }
        if (getArguments().containsKey(START_TIME_ARG)) {
            mStartTime = getArguments().getString(START_TIME_ARG);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.event_detail, container, false);

        mEventImageView = (ImageView) rootView.findViewById(R.id.event_imageview);

        mTitleTextView = (TextView) rootView.findViewById(R.id.detail_title_textview);
        mStartTimeTextView = (TextView) rootView.findViewById(R.id.detail_start_time_textview);
        mVenueNameTextView = (TextView) rootView.findViewById(R.id.detail_venue_name_textview);
        mDescriptionTextView = (TextView) rootView.findViewById(R.id.detail_description_textview);
        mPerformersTextView = (TextView) rootView.findViewById(R.id.detail_performers_textview);

        getLoaderManager().initLoader(DETAIL_LOADER, null, this);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_detail, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_map:
                if (mGeo != null || mLocationQuery != null) {
                    //implicit intent to open up the concerts location in a map app
                    Uri gmmIntentUri = Uri.parse(mGeo).buildUpon()
                            .appendQueryParameter("q", mLocationQuery)
                            .build();
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                        startActivity(mapIntent);
                    }
                }
                break;
            case R.id.action_share:
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, mTitleTextView.getText());
                //create a chooser and set the title so users can pick the app they want to share with
                startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.share_event)));
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri singleEventUri = DBContract.EventsEntry.buildEventForSearchWithDateAndTitleUri(mCategory, mLocation, mKeywords, mStartTime, mTitle);
        return new CursorLoader(getContext(), singleEventUri, null, null, null, null);
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            final String title = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.EventsEntry.TITLE));
            final String startTime = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.EventsEntry.START_TIME));
            final String venueName = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.EventsEntry.VENUE_NAME));
            final String description = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.EventsEntry.DESCRIPTION));
            final String performers = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.EventsEntry.PERFORMERS));
            final String longitude = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.EventsEntry.LONGITUDE));
            final String latitude = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.EventsEntry.LATITUDE));
            final String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.EventsEntry.IMG_URL));

            if (mAppBarLayout != null) {
                mAppBarLayout.setTitle(title);
            }

            mStartTimeTextView.setText(startTime);
            mVenueNameTextView.setText(venueName);

            if (imageUrl != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTitleTextView.setText(title);
                        Picasso.with(getContext()).load(imageUrl).into(mEventImageView);
                    }
                });
            }

            if (!description.equalsIgnoreCase(getActivity().getString(R.string.null_string))) {
                mDescriptionTextView.setText(description);
            }

            if (!performers.equalsIgnoreCase(getActivity().getString(R.string.null_string))) {
                mPerformersTextView.setText(performers);
            }

            mLongitude = longitude;
            mLatitude = latitude;
            mLocationQuery = venueName;
            StringBuilder geoString = new StringBuilder()
                    .append("geo:")
                    .append(mLatitude)
                    .append(",")
                    .append(mLongitude)
                    .append("?");
            mGeo = geoString.toString();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}

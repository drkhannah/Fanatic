package com.drkhannah.fanatic;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
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
    public static final String SEARCH_ID_ARG = "search_id_arg";
    public static final String TITLE_ARG = "title_arg";
    public static final String START_TIME_ARG = "start_time_arg";

    private long mSearchId;
    private String mTitle;
    private String mStartTime;

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
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Activity activity = this.getActivity();
        mAppBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);

        if (getArguments().containsKey(SEARCH_ID_ARG)) {
            mSearchId = getArguments().getLong(SEARCH_ID_ARG);
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
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri singleEventUri = DBContract.EventsEntry.buildEventListForSearchWithDateAndTitleUri(mSearchId, mStartTime, mTitle);
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
            final String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.EventsEntry.IMG_URL));

            if (mAppBarLayout != null) {
                mAppBarLayout.setTitle(title);
            } else {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTitleTextView.setText(title);
                        Picasso.with(getContext()).load(imageUrl).into(mEventImageView);
                    }
                });
            }

            mStartTimeTextView.setText(startTime);
            mVenueNameTextView.setText(venueName);

            if (!description.equalsIgnoreCase(getActivity().getString(R.string.null_string))) {
                mDescriptionTextView.setText(description);
            }

            if (!performers.equalsIgnoreCase(getActivity().getString(R.string.null_string))) {
                mPerformersTextView.setText(performers);
            }


        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}

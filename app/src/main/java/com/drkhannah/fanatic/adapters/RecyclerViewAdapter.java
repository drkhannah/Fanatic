package com.drkhannah.fanatic.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.drkhannah.fanatic.EventDetailActivity;
import com.drkhannah.fanatic.EventDetailFragment;
import com.drkhannah.fanatic.R;
import com.drkhannah.fanatic.data.DBContract;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private Cursor mCursor;
    private FragmentManager mFragmentManager;
    private boolean mTwoPane;

    public RecyclerViewAdapter(Cursor cursor, FragmentManager fragmentManager, boolean twoPaneMode) {
        mCursor = cursor;
        mFragmentManager = fragmentManager;
        mTwoPane = twoPaneMode;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_list_content, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        final long search_id = mCursor.getLong(mCursor.getColumnIndexOrThrow(DBContract.EventsEntry.SEARCH_ID));
        final String title = mCursor.getString(mCursor.getColumnIndexOrThrow(DBContract.EventsEntry.TITLE));
        final String startTime = mCursor.getString(mCursor.getColumnIndexOrThrow(DBContract.EventsEntry.START_TIME));
        String venueName = mCursor.getString(mCursor.getColumnIndexOrThrow(DBContract.EventsEntry.VENUE_NAME));
        holder.mEventTitleTextView.setText(title);
        holder.mStartTimeTextView.setText(startTime);
        holder.mVenueNameTextView.setText(venueName);


        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putLong(EventDetailFragment.SEARCH_ID_ARG, search_id);
                    arguments.putString(EventDetailFragment.TITLE_ARG, title);
                    arguments.putString(EventDetailFragment.START_TIME_ARG, startTime);
                    EventDetailFragment fragment = new EventDetailFragment();
                    fragment.setArguments(arguments);
                    mFragmentManager.beginTransaction()
                            .replace(R.id.event_detail_container, fragment)
                            .commit();
                } else {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, EventDetailActivity.class);
                    intent.putExtra(EventDetailFragment.SEARCH_ID_ARG, search_id);
                    intent.putExtra(EventDetailFragment.TITLE_ARG, title);
                    intent.putExtra(EventDetailFragment.START_TIME_ARG, startTime);
                    context.startActivity(intent);
                }
            }
        });
    }

    public void swapCursor(Cursor newCursor){
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    public Cursor getCursor() {
        return mCursor;
    }

    @Override
    public int getItemCount() {
        return (null != mCursor ? mCursor.getCount() : 0);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mEventTitleTextView;
        public final TextView mStartTimeTextView;
        public final TextView mVenueNameTextView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mEventTitleTextView = (TextView) view.findViewById(R.id.event_title_textview);
            mStartTimeTextView = (TextView) view.findViewById(R.id.detail_start_time_textview);
            mVenueNameTextView = (TextView) view.findViewById(R.id.detail_venue_name_textview);
        }

        @Override
        public String toString() {
            return "ViewHolder{" +
                    "mEventTitleTextView=" + mEventTitleTextView +
                    ", mStartTimeTextView=" + mStartTimeTextView +
                    ", mVenueNameTextView=" + mVenueNameTextView +
                    '}';
        }
    }
}
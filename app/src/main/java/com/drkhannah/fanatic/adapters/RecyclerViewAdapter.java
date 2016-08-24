package com.drkhannah.fanatic.adapters;

import android.content.Context;
import android.content.Intent;
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
import com.drkhannah.fanatic.models.Event;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private List<Event> mEvents;
    private FragmentManager mFragmentManager;
    private boolean mTwoPane;

    public RecyclerViewAdapter(List<Event> items, FragmentManager fragmentManager, boolean twoPaneMode) {
        mEvents = items;
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
        holder.mEvent = mEvents.get(position);
        holder.mEventTitleTextView.setText(holder.mEvent.getTitle());
        holder.mStartTimeTextView.setText(holder.mEvent.getStartTime());
        holder.mVenueNameTextView.setText(holder.mEvent.getVenueName());


        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putParcelable(EventDetailFragment.PARCELABLE_EVENT, holder.mEvent);
                    EventDetailFragment fragment = new EventDetailFragment();
                    fragment.setArguments(arguments);
                    mFragmentManager.beginTransaction()
                            .replace(R.id.event_detail_container, fragment)
                            .commit();
                } else {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, EventDetailActivity.class);
                    intent.putExtra(EventDetailFragment.PARCELABLE_EVENT, holder.mEvent);

                    context.startActivity(intent);
                }
            }
        });
    }

    public void swapData(List<Event> newEvents){
        mEvents = newEvents;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return (null != mEvents ? mEvents.size() : 0);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mEventTitleTextView;
        public final TextView mStartTimeTextView;
        public final TextView mVenueNameTextView;
        public Event mEvent;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mEventTitleTextView = (TextView) view.findViewById(R.id.event_title_textview);
            mStartTimeTextView = (TextView) view.findViewById(R.id.start_time_textview);
            mVenueNameTextView = (TextView) view.findViewById(R.id.venue_name_textview);
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
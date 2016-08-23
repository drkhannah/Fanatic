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

import com.drkhannah.fanatic.ConcertDetailActivity;
import com.drkhannah.fanatic.ConcertDetailFragment;
import com.drkhannah.fanatic.R;
import com.drkhannah.fanatic.models.Concert;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private List<Concert> mConcerts;
    private FragmentManager mFragmentManager;
    private boolean mTwoPane;

    public RecyclerViewAdapter(List<Concert> items, FragmentManager fragmentManager, boolean twoPaneMode) {
        mConcerts = items;
        mFragmentManager = fragmentManager;
        mTwoPane = twoPaneMode;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.concert_list_content, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mConcert = mConcerts.get(position);


        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    //arguments.putString(ConcertDetailFragment.ARG_ITEM_ID, holder.mConcert.id);
                    ConcertDetailFragment fragment = new ConcertDetailFragment();
                    fragment.setArguments(arguments);
                    mFragmentManager.beginTransaction()
                            .replace(R.id.concert_detail_container, fragment)
                            .commit();
                } else {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, ConcertDetailActivity.class);
                    //intent.putExtra(ConcertDetailFragment.ARG_ITEM_ID, holder.mConcert.id);

                    context.startActivity(intent);
                }
            }
        });
    }

    public void swapData(List<Concert> newConcerts){
        mConcerts = newConcerts;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mConcerts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public Concert mConcert;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.id);
            mContentView = (TextView) view.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
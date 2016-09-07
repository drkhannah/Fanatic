package com.drkhannah.fanatic;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.drkhannah.fanatic.adapters.RecyclerViewAdapter;
import com.drkhannah.fanatic.data.DBContract;
import com.drkhannah.fanatic.settings.SettingsActivity;
import com.drkhannah.fanatic.sync.SyncAdapter;

/**
 * An activity representing a list of Event. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link EventDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class EventListActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = EventListActivity.class.getSimpleName();

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private RecyclerViewAdapter mRecyclerViewAdapter;
    private TextView mEmptyListTextView;

    //loader identifier number
    public static final int EVENTS_LOADER = 0;

    private JobScheduler mJobScheduler;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_drawer_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "fetching event data", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                Intent intent = new Intent(EventListActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });

        //nav drawer setup
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.open_navigation_drawer, R.string.close_navitagion_drawer);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (findViewById(R.id.event_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        //recyclerView setup
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.event_list);
        assert recyclerView != null;
        setupRecyclerView(recyclerView);

        mEmptyListTextView = (TextView) findViewById(R.id.empty_events_textview);
        mEmptyListTextView.setText(R.string.prompt_search_events);
        setAlarm();

        //job scheduler
        mJobScheduler = (JobScheduler) getSystemService( Context.JOB_SCHEDULER_SERVICE );
        JobInfo.Builder builder = new JobInfo.Builder( 1, new ComponentName( getPackageName(), JobSchedulerService.class.getName() ) );
        builder.setMinimumLatency(1000);
        mJobScheduler.schedule(builder.build());
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportLoaderManager().restartLoader(EVENTS_LOADER, null, this);
        SyncAdapter.initSyncAdapter(this);
    }

    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        mRecyclerViewAdapter = new RecyclerViewAdapter(this, null, getSupportFragmentManager(), mTwoPane);
        recyclerView.setAdapter(mRecyclerViewAdapter);
    }

    public boolean getMode() {
        return mTwoPane;
    }

    /**
     * Called when an item in the navigation menu is selected.
     *
     * @param item The selected item
     * @return true to display the item as the selected item
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_settings) {
            // Handle the activity_settings action
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String category = Utils.getCategoryFromSharedPref(this);
        String location = Utils.getLocationFromSharedPref(this);
        String keywords = Utils.getKeywordsFromSharedPref(this);
        String sortOrder = DBContract.EventsEntry.START_TIME + " ASC";
        Uri eventsForSearchUri = DBContract.EventsEntry.buildEventListForSearchUri(category, location, keywords);
        return new CursorLoader(EventListActivity.this, eventsForSearchUri, null, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.getCount() != 0) {
            mRecyclerViewAdapter.swapCursor(cursor);
            mEmptyListTextView.setText(null);
        } else {
            Log.d(LOG_TAG, "cursor returned 0");
            SyncAdapter.syncNow(this);
            mEmptyListTextView.setText(R.string.no_events_returned_label);
            mRecyclerViewAdapter.swapCursor(null);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mRecyclerViewAdapter.swapCursor(null);
    }

    public void setAlarm() {
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000, pendingIntent);
    }
}

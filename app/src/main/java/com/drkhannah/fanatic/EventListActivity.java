package com.drkhannah.fanatic;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.drkhannah.fanatic.adapters.RecyclerViewAdapter;
import com.drkhannah.fanatic.models.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * An activity representing a list of Event. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link EventDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class EventListActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private RecyclerViewAdapter mRecyclerViewAdapter;
    private List<Event> mEventList = new ArrayList<>();

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

                //read location from DefaultSharedPreferences
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(EventListActivity.this);
                String location = sharedPreferences.getString(getString(R.string.pref_location_key), getString(R.string.pref_default_location));
                String category = sharedPreferences.getString(getString(R.string.pref_category_key), getString(R.string.pref_default_location));
                GetEventsTask getEventsTask = new GetEventsTask(EventListActivity.this, mRecyclerViewAdapter);
                getEventsTask.execute(category, location);
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
        mRecyclerViewAdapter = new RecyclerViewAdapter(mEventList, getSupportFragmentManager(), mTwoPane);
        recyclerView.setAdapter(mRecyclerViewAdapter);
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
            // Handle the settings action
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}

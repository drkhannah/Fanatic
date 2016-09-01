package com.drkhannah.fanatic;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by dhannah on 9/1/16.
 */
public class Utils {

    public static String getCategoryFromSharedPref(Context context) {
        SharedPreferences defaultPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return defaultPreferences.getString(context.getString(R.string.pref_category_key), "music");
    }

    public static String getLocationFromSharedPref(Context context) {
        SharedPreferences defaultPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return defaultPreferences.getString(context.getString(R.string.pref_location_key), "44107");
    }

    public static String getKeywordsFromSharedPref(Context context) {
        SharedPreferences defaultPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return defaultPreferences.getString(context.getString(R.string.pref_keywords_key), "rock");
    }
}

package com.drkhannah.fanatic.models;

import java.util.List;

/**
 * Created by dhannah on 8/23/16.
 */
public class Concert {

    private String mArtistName;
    private String mTitle;
    private String mStartTime;
    private String mVenueName;
    private String mCityName;
    private String mCountryName;
    private List<String> mPerformers;
    private String mLongitude;
    private String mLatitude;
    private String mDescription;
    private String mImageUrl;

    public Concert(String artistName, String title, String startTime, String venueName, String cityName, String countryName, List<String> performers, String longitude, String latitude, String description, String imageUrl) {
        mArtistName = artistName;
        mTitle = title;
        mStartTime = startTime;
        mVenueName = venueName;
        mCityName = cityName;
        mCountryName = countryName;
        mPerformers = performers;
        mLongitude = longitude;
        mLatitude = latitude;
        mDescription = description;
        mImageUrl = imageUrl;
    }

    public String getArtistName() {
        return mArtistName;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getStartTime() {
        return mStartTime;
    }

    public String getVenueName() {
        return mVenueName;
    }

    public String getCityName() {
        return mCityName;
    }

    public String getCountryName() {
        return mCountryName;
    }

    public List<String> getPerformers() {
        return mPerformers;
    }

    public String getLongitude() {
        return mLongitude;
    }

    public String getLatitude() {
        return mLatitude;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    @Override
    public String toString() {
        return "Concert{" +
                "mArtistName='" + mArtistName + '\'' +
                ", mTitle='" + mTitle + '\'' +
                ", mStartTime='" + mStartTime + '\'' +
                ", mVenueName='" + mVenueName + '\'' +
                ", mCityName='" + mCityName + '\'' +
                ", mCountryName='" + mCountryName + '\'' +
                ", mPerformers=" + mPerformers +
                ", mLongitude='" + mLongitude + '\'' +
                ", mLatitude='" + mLatitude + '\'' +
                ", mDescription='" + mDescription + '\'' +
                ", mImageUrl='" + mImageUrl + '\'' +
                '}';
    }
}

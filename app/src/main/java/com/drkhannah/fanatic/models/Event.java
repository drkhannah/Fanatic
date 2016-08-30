package com.drkhannah.fanatic.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by dhannah on 8/23/16.
 */
public class Event implements Parcelable{

    private String mTitle;
    private String mStartTime;
    private String mVenueName;
    private String mCityName;
    private String mCountryName;
    private String mPerformers;
    private String mLongitude;
    private String mLatitude;
    private String mDescription;
    private String mImageUrl;

    public Event(String title, String startTime, String venueName, String cityName, String countryName, String performers, String longitude, String latitude, String description, String imageUrl) {
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

    //Parcelable code
    protected Event(Parcel in) {
        mTitle = in.readString();
        mStartTime = in.readString();
        mVenueName = in.readString();
        mCityName = in.readString();
        mCountryName = in.readString();
        mPerformers = in.readString();
        mLongitude = in.readString();
        mLatitude = in.readString();
        mDescription = in.readString();
        mImageUrl = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTitle);
        dest.writeString(mStartTime);
        dest.writeString(mVenueName);
        dest.writeString(mCityName);
        dest.writeString(mCountryName);
        dest.writeString(mPerformers);
        dest.writeString(mLongitude);
        dest.writeString(mLatitude);
        dest.writeString(mDescription);
        dest.writeString(mImageUrl);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };
    //end Parcelable code

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

    public String getPerformers() {
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

    public void setTitle(String title) {
        mTitle = title;
    }

    @Override
    public String toString() {
        return "Event{" +
                "mTitle='" + mTitle + '\'' +
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

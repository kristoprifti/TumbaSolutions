package me.kristoprifti.android.tumbasolutions.models;

import android.os.Parcel;
import android.os.Parcelable;

import io.realm.RealmObject;

/**
 * Created by Kristi on 1/4/2017.
 */

public class Picture extends RealmObject implements Parcelable{

    private String title;
    private String pictureUrl;
    private double latitude;
    private double longitude;
    private String addressLocation;
    private String weatherCondition;
    private String weatherImageUri;

    public Picture() {
        // default values possible since 2.0.2
    }

    public String getTitle() {
        return title;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getAddressLocation() {
        return addressLocation;
    }

    public String getWeatherCondition() {
        return weatherCondition;
    }

    public String getWeatherImageUri() {
        return weatherImageUri;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setAddressLocation(String addressLocation) {
        this.addressLocation = addressLocation;
    }

    public void setWeatherCondition(String weatherCondition) {
        this.weatherCondition = weatherCondition;
    }

    public void setWeatherImageUri(String weatherImageUri) {
        this.weatherImageUri = weatherImageUri;
    }

    @Override
    public String toString() {
        return "Picture{" +
                "title='" + title + '\'' +
                ", pictureUrl='" + pictureUrl + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", addressLocation='" + addressLocation + '\'' +
                ", weatherCondition='" + weatherCondition + '\'' +
                ", weatherImageUri='" + weatherImageUri + '\'' +
                '}';
    }

    private Picture(Parcel in) {
        title = in.readString();
        pictureUrl = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        addressLocation = in.readString();
        weatherCondition = in.readString();
        weatherImageUri = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(pictureUrl);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(addressLocation);
        dest.writeString(weatherCondition);
        dest.writeString(weatherImageUri);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Picture> CREATOR = new Parcelable.Creator<Picture>() {
        @Override
        public Picture createFromParcel(Parcel in) {
            return new Picture(in);
        }

        @Override
        public Picture[] newArray(int size) {
            return new Picture[size];
        }
    };
}

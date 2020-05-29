package com.ozurak.tiktokfetch.Model;

import android.os.Parcel;
import android.os.Parcelable;
/**
 * Stefan Najdovski
 * 5/21/2020
 */
public class DataModel implements Parcelable {
    public static final Creator<DataModel> CREATOR = new Creator<DataModel>() {
        public DataModel createFromParcel(Parcel parcel) {
            return new DataModel(parcel);
        }

        public DataModel[] newArray(int i) {
            return new DataModel[i];
        }
    };
    private String filename;
    private String filepath;

    public int describeContents() {
        return 0;
    }


    protected DataModel(Parcel parcel) {
        this.filename = parcel.readString();
        this.filepath = parcel.readString();
    }


    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.filename);
        parcel.writeString(this.filepath);
    }
}


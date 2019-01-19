package com.example.a00382071.parcelableknife.parcel;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by 00382071 on 2019/1/19.
 */

public class TestModel implements Parcelable {
    int a;

    protected TestModel(Parcel in) {
        a = in.readInt();
    }

    public static final Creator<TestModel> CREATOR = new Creator<TestModel>() {
        @Override
        public TestModel createFromParcel(Parcel in) {
            return new TestModel(in);
        }

        @Override
        public TestModel[] newArray(int size) {
            return new TestModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(a);
    }
}

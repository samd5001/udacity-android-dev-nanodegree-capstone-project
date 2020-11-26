package com.sdunk.jiraestimator.model;

import android.os.Parcel;
import android.os.Parcelable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
public class EstimateUser implements Parcelable {

    public static final Creator<EstimateUser> CREATOR = new Creator<EstimateUser>() {
        @Override
        public EstimateUser createFromParcel(Parcel in) {
            return new EstimateUser(in);
        }

        @Override
        public EstimateUser[] newArray(int size) {
            return new EstimateUser[size];
        }
    };
    private final String endpointId;
    private final String email;

    protected EstimateUser(Parcel in) {
        endpointId = in.readString();
        email = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(endpointId);
        dest.writeString(email);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}

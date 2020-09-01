package com.sdunk.jiraestimator.model;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import lombok.Data;

@Data
@Entity(tableName = "issues")
public class JiraIssue implements Parcelable {

    @PrimaryKey
    @NonNull
    private String id;

    private String self;

    private String key;

    private String description;

    private ArrayList<String> comment;

    protected JiraIssue(Parcel in) {
        id = in.readString();
        self = in.readString();
        key = in.readString();
        description = in.readString();
        comment = in.createStringArrayList();
    }

    public static final Creator<JiraIssue> CREATOR = new Creator<JiraIssue>() {
        @Override
        public JiraIssue createFromParcel(Parcel in) {
            return new JiraIssue(in);
        }

        @Override
        public JiraIssue[] newArray(int size) {
            return new JiraIssue[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(self);
        parcel.writeString(key);
        parcel.writeString(description);
        parcel.writeStringList(comment);
    }
}

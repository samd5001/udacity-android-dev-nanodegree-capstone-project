package com.sdunk.jiraestimator.data.model;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "issues")
public class JiraIssue implements Parcelable {

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
    @PrimaryKey
    @NonNull
    private String id;
    private String self;
    private String key;
    private String description;
    private List<String> comment;

    public JiraIssue(@NonNull String id, String self, String key, String description, List<String> comment) {
        this.id = id;
        this.self = self;
        this.key = key;
        this.description = description;
        this.comment = comment;
    }

    protected JiraIssue(Parcel in) {
        id = Objects.requireNonNull(in.readString());
        self = in.readString();
        key = in.readString();
        description = in.readString();
        comment = in.createStringArrayList();
    }

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

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getComment() {
        return comment;
    }

    public void setComment(ArrayList<String> comment) {
        this.comment = comment;
    }
}

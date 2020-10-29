package com.sdunk.jiraestimator.model;


import android.os.Parcel;
import android.os.Parcelable;

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
    private String url;
    private String key;
    private String name;
    private String description;
    private Double storyPoints;

    protected JiraIssue(Parcel in) {
        id = in.readString();
        url = in.readString();
        key = in.readString();
        name = in.readString();
        description = in.readString();
        if (in.readByte() == 0) {
            storyPoints = null;
        } else {
            storyPoints = in.readDouble();
        }
    }

    public JiraIssue(@NonNull String id, String url, String key, String name, String description, Double storyPoints) {
        this.id = id;
        this.url = url;
        this.key = key;
        this.name = name;
        this.description = description;
        this.storyPoints = storyPoints;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getStoryPoints() {
        return storyPoints;
    }

    public void setStoryPoints(Double storyPoints) {
        this.storyPoints = storyPoints;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(url);
        dest.writeString(key);
        dest.writeString(name);
        dest.writeString(description);
        if (storyPoints == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(storyPoints);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }
}

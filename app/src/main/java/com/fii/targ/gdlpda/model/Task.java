package com.fii.targ.gdlpda.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Task implements Parcelable {
    private final String taskNumber;
    private final String status;
    private final String stage;
    private final String startPoint;
    private final String endPoint;
    private final String trolleyNumber;
    private final String agvNumber;

    public Task(String taskNumber, String status, String stage, String startPoint, String endPoint, String trolleyNumber, String agvNumber) {
        this.taskNumber = taskNumber;
        this.status = status;
        this.stage = stage;
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.trolleyNumber = trolleyNumber;
        this.agvNumber = agvNumber;
    }

    protected Task(Parcel in) {
        taskNumber = in.readString();
        status = in.readString();
        stage = in.readString();
        startPoint = in.readString();
        endPoint = in.readString();
        trolleyNumber = in.readString();
        agvNumber = in.readString();
    }

    public static final Creator<Task> CREATOR = new Creator<Task>() {
        @Override
        public Task createFromParcel(Parcel in) {
            return new Task(in);
        }

        @Override
        public Task[] newArray(int size) {
            return new Task[size];
        }
    };

    public String getTaskNumber() {
        return taskNumber;
    }

    public String getStatus() {
        return status;
    }

    public String getStage() {
        return stage;
    }

    public String getStartPoint() {
        return startPoint;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public String getTrolleyNumber() {
        return trolleyNumber;
    }

    public String getAgvNumber() {
        return agvNumber;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(taskNumber);
        dest.writeString(status);
        dest.writeString(stage);
        dest.writeString(startPoint);
        dest.writeString(endPoint);
        dest.writeString(trolleyNumber);
        dest.writeString(agvNumber);
    }
}
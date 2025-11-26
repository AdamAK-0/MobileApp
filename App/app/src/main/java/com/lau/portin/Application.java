package com.lau.portin;

import java.io.Serializable;

public class Application implements Serializable {
    private int applicationId;
    private String userName;
    private String userEmail;
    private String status;
    private String appliedAt;
    private String userPhoto;

    public Application(int applicationId, String userName, String userEmail,
                       String status, String appliedAt, String userPhoto) {
        this.applicationId = applicationId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.status = status;
        this.appliedAt = appliedAt;
        this.userPhoto = userPhoto;
    }

    public int getApplicationId() {
        return applicationId;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAppliedAt() {
        return appliedAt;
    }

    public String getUserPhoto() {
        return userPhoto;
    }
}

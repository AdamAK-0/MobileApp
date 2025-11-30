package com.lau.portin;

import java.io.Serializable;

public class Application implements Serializable {
    private int applicationId;
    private int userId;
    private String userName;
    private String userEmail;
    private String status;
    private String appliedAt;
    private String userPhoto;
    private String cvUrl;

    public Application(int applicationId, int userId, String userName, String userEmail,
                       String status, String appliedAt, String userPhoto, String cvUrl) {
        this.applicationId = applicationId;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.status = status;
        this.appliedAt = appliedAt;
        this.userPhoto = userPhoto;
        this.cvUrl = cvUrl;
    }

    public int getApplicationId() {
        return applicationId;
    }

    public int getUserId() {
        return userId;
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

    public String getCvUrl() {
        return cvUrl;
    }
}

package com.lau.portin;

import java.io.Serializable;

public class User implements Serializable {

    private int user_id;
    private String first_name;
    private String middle_name;
    private String last_name;
    private int birth_year;
    private String email;
    private String password;
    private String photo;
    private String transcript;
    private String created_at;

    public User() {
    }

    public User(int user_id, String first_name, String middle_name, String last_name, int birth_year,
                String email, String password, String photo, String transcript, String created_at) {
        this.user_id = user_id;
        this.first_name = first_name;
        this.middle_name = middle_name;
        this.last_name = last_name;
        this.birth_year = birth_year;
        this.email = email;
        this.password = password;
        this.photo = photo;
        this.transcript = transcript;
        this.created_at = created_at;
    }

    // Getters & Setters

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getMiddle_name() {
        return middle_name;
    }

    public void setMiddle_name(String middle_name) {
        this.middle_name = middle_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public int getBirth_year() {
        return birth_year;
    }

    public void setBirth_year(int birth_year) {
        this.birth_year = birth_year;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getTranscript() {
        return transcript;
    }

    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }
}

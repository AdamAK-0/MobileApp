package com.lau.portin;

import java.io.Serializable;

public class Company implements Serializable {

    private int company_id;
    private String name;
    private String email;
    private String password;
    private String photo;
    private float rating;
    private String description;
    private String created_at;

    public Company() {
    }

    public Company(int company_id, String name, String email, String password,
                   String photo, float rating, String description, String created_at) {
        this.company_id = company_id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.photo = photo;
        this.rating = rating;
        this.description = description;
        this.created_at = created_at;
    }

    // Getters & Setters

    public int getCompany_id() {
        return company_id;
    }

    public void setCompany_id(int company_id) {
        this.company_id = company_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }
}

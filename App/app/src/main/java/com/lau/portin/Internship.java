package com.lau.portin;

import java.io.Serializable;

public class Internship implements Serializable {
    public int id;
    public String companyName;
    public String name;
    public String description;
    public String type;
    public String photo;

    public Internship(int id, int company_id, String companyName, String name, String description, String type, String photo) {
        this.id = id;
        this.companyName = companyName;
        this.name = name;
        this.description = description;
        this.type = type;
        this.photo = photo;
    }

    public String getPhoto() {
        return photo;
    }

    public int getId() {
        return id;
    }
    public String getCompanyName() {
        return companyName;
    }
    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public String getType() {
        return type;
    }
}

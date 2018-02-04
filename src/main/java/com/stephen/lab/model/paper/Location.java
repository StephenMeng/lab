package com.stephen.lab.model.paper;

import java.io.Serializable;

public class Location implements Serializable {
    private String country;
    private String countryCode;
    private String town;

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }
}

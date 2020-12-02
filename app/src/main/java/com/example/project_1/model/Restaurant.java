package com.example.project_1.model;

import java.io.Serializable;

/**
 * Restaurant class models the information about a restaurant.
 */
public class Restaurant implements Serializable {
    private String trackingNumber;
    private String name;
    private String physicalAddress;
    private String physicalCity;
    private String factType;
    private double latitude;
    private double longitude;
    private boolean favorite = false;

    public Restaurant(String trackingNumber, String name, String physicalAddress, String physicalCity, String factType, double latitude, double longitude) {
        this.trackingNumber = trackingNumber;
        this.name = name;
        this.physicalAddress = physicalAddress;
        this.physicalCity = physicalCity;
        this.factType = factType;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Default constructor to read cvs file
    public Restaurant() {
        this.trackingNumber = null;
        this.name = null;
        this.physicalAddress = null;
        this.physicalCity = null;
        this.factType = null;
        this.latitude = 0;
        this.longitude = 0;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhysicalAddress() {
        return physicalAddress;
    }

    public void setPhysicalAddress(String physicalAddress) {
        this.physicalAddress = physicalAddress;
    }

    public String getPhysicalCity() {
        return physicalCity;
    }

    public void setPhysicalCity(String physicalCity) {
        this.physicalCity = physicalCity;
    }

    public String getFactType() {
        return factType;
    }

    public void setFactType(String factType) {
        this.factType = factType;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    @Override
    public String toString() {
        return "Restaurant{" +
                "trackingNumber='" + trackingNumber + '\'' +
                ", name='" + name + '\'' +
                ", physicalAddress='" + physicalAddress + '\'' +
                ", physicalCity='" + physicalCity + '\'' +
                ", factType='" + factType + '\'' +
                ", latitude=" + latitude +
                ", altitude=" + longitude +
                '}';
    }
}

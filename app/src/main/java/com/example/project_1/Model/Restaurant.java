package com.example.project_1.Model;

/**
 * Restaurant information
 */

public class Restaurant {
    private String trackingNumber;
    private String name;
    private String physicalAddress;
    private String physicalCity;
    private String factType;
    private double latitude;
    private double altitude;

    private int restaurantIcon;

    public Restaurant(String trackingNumber, String name, String physicalAddress, String physicalCity, String factType, double latitude, double altitude) {
        this.trackingNumber = trackingNumber;
        this.name = name;
        this.physicalAddress = physicalAddress;
        this.physicalCity = physicalCity;
        this.factType = factType;
        this.latitude = latitude;
        this.altitude = altitude;
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

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public int getRestaurantIcon() {
        return restaurantIcon;
    }

    public void setRestaurantIcon(int restaurantIcon) {
        this.restaurantIcon = restaurantIcon;
    }
}

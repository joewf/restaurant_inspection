package com.example.project_1.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.util.List;

/**
 * ClusterMarker class models the information about a ClusterMarker.
 */
public class ClusterMarker implements ClusterItem {

    private LatLng position;
    private String title;
    private String snippet;
    private int icon;
    private HazardRating hazardRating;
    private List<Inspection> inspectionList;
    private Restaurant restaurant;


    public ClusterMarker(String title, String snippet, LatLng position, HazardRating hazardRating) {

        this.position = position;
        this.title = title;
        this.snippet = snippet;
        setHazardRating(hazardRating);
    }

    public ClusterMarker(String title, String snippet, LatLng position, HazardRating hazardRating,
                         int icon, List<Inspection> inspectionList) {

        this.position = position;
        this.title = title;
        this.snippet = snippet;
        this.hazardRating = hazardRating;
        this.icon = icon;
        this.inspectionList = inspectionList;
    }

    public ClusterMarker(String title, String snippet, LatLng position, HazardRating hazardRating,
                         int icon, List<Inspection> inspectionList, Restaurant restaurant) {

        this.position = position;
        this.title = title;
        this.snippet = snippet;
        this.hazardRating = hazardRating;
        this.icon = icon;
        this.inspectionList = inspectionList;
        this.restaurant = restaurant;
    }


    public ClusterMarker() {

    }

    @NonNull
    @Override
    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    @Nullable
    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Nullable
    @Override
    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public int getIcon() {
        return icon;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public HazardRating getHazard(){
        return hazardRating;
    }

    public void setHazardRating(HazardRating hazardRating) {
        if(this.hazardRating == HazardRating.LOW) {
            this.hazardRating = HazardRating.LOW;
        }else if(this.hazardRating == HazardRating.MODERATE) {
            this.hazardRating = hazardRating.MODERATE;
        }else{
            this.hazardRating = hazardRating.HIGH;
        }
    }

    public HazardRating getHazardRating() {
        return hazardRating;
    }

    public List<Inspection> getInspectionList() {
        return inspectionList;
    }

    public void setInspectionList(List<Inspection> inspectionList) {
        this.inspectionList = inspectionList;
    }
}

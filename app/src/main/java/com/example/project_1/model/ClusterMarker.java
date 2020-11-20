package com.example.project_1.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterItem;

/**
 * ClusterMarker class models the information about a ClusterMarker.
 */
public class ClusterMarker implements ClusterItem {

    private LatLng position;
    private String title;
    private String snippet;
    private BitmapDescriptor mIcon;
    private int icon;
    private HazardRating hazardRating;

    public ClusterMarker(String title, String snippet, LatLng position, HazardRating hazardRating) {

        this.position = position;
        this.title = title;
        this.snippet = snippet;
        setHazardRating(hazardRating);
    }

    public ClusterMarker(String title, String snippet, LatLng position, int icon) {

        this.position = position;
        this.title = title;
        this.snippet = snippet;
        this.icon = icon;
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
}

package com.example.project_1.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.project_1.R;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.ui.IconGenerator;

public class ClusterMarker implements ClusterItem {

    private LatLng position;
    private String title;
    private String snippet;
    private BitmapDescriptor icon;
    private IconGenerator iconGenerator;
    private int mIcon;

    public ClusterMarker(LatLng position) {
        this.position = position;
    }

    public ClusterMarker(String title, String snippet, LatLng position, BitmapDescriptor icon ) {

        this.position = position;
        this.title = title;
        this.snippet = snippet;
        this.icon = icon;
    }

    public ClusterMarker(MarkerOptions marker) {

        this.position = marker.getPosition();
        this.title = marker.getTitle();
        this.snippet = marker.getSnippet();
        this.icon = getIcon();

    }

    public ClusterMarker(LatLng position, String title, String snippet) {

        this.position = position;
        this.title = title;
        this.snippet = snippet;
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

    public BitmapDescriptor getIcon() {
        return icon;
    }

    public void setIcon(BitmapDescriptor icon) {
        this.icon = icon;
    }
}

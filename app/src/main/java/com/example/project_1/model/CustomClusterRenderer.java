package com.example.project_1.model;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.project_1.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

public class CustomClusterRenderer extends DefaultClusterRenderer<ClusterMarker> {

    private final IconGenerator mClusterIconGenerator;
    private final Context mContext;
    private BitmapDescriptor markerDescriptor;

    public CustomClusterRenderer(Context context, GoogleMap map, ClusterManager<ClusterMarker> clusterManager) {
        super(context, map, clusterManager);

        mContext = context;
        mClusterIconGenerator = new IconGenerator(mContext);
    }

    @Override
    protected void onBeforeClusterItemRendered(@NonNull ClusterMarker item, @NonNull MarkerOptions markerOptions) {
        if(item.getIcon() == 1) {
            markerDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.green_hazard_20);
            markerOptions.icon(markerDescriptor);
        }else if(item.getIcon() == 2) {
            markerDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.yellow_hazard_20);
            markerOptions.icon(markerDescriptor);
        }else {
            markerDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.red_hazard_20);
            markerOptions.icon(markerDescriptor);
        }
    }

    /*@Override
    protected void onBeforeClusterRendered(@NonNull Cluster<ClusterMarker> cluster, @NonNull MarkerOptions markerOptions) {
        if(cluster.getSize() > 6) {
            mClusterIconGenerator.setBackground(ContextCompat.getDrawable(mContext, R.drawable.ic_baseline_zoom_in_black_24));
        }else {
            mClusterIconGenerator.setBackground(ContextCompat.getDrawable(mContext, R.drawable.ic_baseline_zoom_in_24));
        }

        mClusterIconGenerator.setTextAppearance(R.style.AppTheme_WhiteTextAppearance);
        String clusterTitle = String.valueOf(cluster.getSize());
        Bitmap icon = mClusterIconGenerator.makeIcon(clusterTitle);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));

    }*/
}

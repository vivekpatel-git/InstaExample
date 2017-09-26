package com.example.wmtandroid5.instaexample;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.example.wmtandroid5.instaexample.db.DatabaseHandler;
import com.example.wmtandroid5.instaexample.utils.Constant;
import com.example.wmtandroid5.instaexample.utils.Utils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import butterknife.BindView;

public class LocationMapActivity extends BaseActivity {

    @BindView(R.id.mapView)
    MapView mMapView;

    private GoogleMap googleMap;
    private int cellSize;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.insta_map_fragment);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        cellSize = Utils.getScreenWidth(this) / 3;
        try {
            MapsInitializer.initialize(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Constant.datalist = DatabaseHandler.getInstance(this).getAllUserMediaLocationList();

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;
                if (Constant.datalist != null) {
                    for (int i = 0; i < Constant.datalist.size(); i++) {
                        setUpMap(Constant.datalist.get(i).getLatituted(), Constant.datalist.get(i).getLongituted(), Constant.datalist.get(i).getUrl());
                    }
                }
            }
        });
    }

    private void setUpMap(final double latitude, final double longitude, String url) {
        final View marker = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_info_window, null);
        final ImageView profile_image = (ImageView) marker.findViewById(R.id.profile_image);
        Picasso.with(this)
                .load(url)
                .resize(cellSize, cellSize)
                .centerCrop()
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        profile_image.setImageBitmap(bitmap);
                        googleMap.addMarker(new MarkerOptions()
                                .position(new LatLng(latitude, longitude))
                                .title("Title")
                                .snippet("Description")
                                .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(marker))));
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });


    }

    private Bitmap getMarkerBitmapFromView(View view) {
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = view.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        view.draw(canvas);
        return returnedBitmap;
    }

}

package com.example.qrcodescanner2.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.qrcodescanner2.BaseFragment;
import com.example.qrcodescanner2.GlobalVariables;
import com.example.qrcodescanner2.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapFragment extends BaseFragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final LatLng DEFAULT_LOCATION = new LatLng(12.9716, 77.5946); // Bangalore

    public MapFragment() {
        // Required empty public constructor
    }

    public static MapFragment newInstance(String param1, String param2) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // Handle any arguments passed to the fragment here
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh the map when the fragment is resumed
        if (mMap != null) {
            updateMap();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        updateMap();
    }

    private void updateMap() {
        if (mMap == null) return;

        GlobalVariables globalVariables = GlobalVariables.getInstance();
        LatLng fetchme;

        if (!Double.isNaN(globalVariables.getCurrentLatitude()) &&
                !Double.isNaN(globalVariables.getCurrentLongitude())) {
            fetchme = new LatLng(globalVariables.getCurrentLatitude(), globalVariables.getCurrentLongitude());
        } else {
            fetchme = DEFAULT_LOCATION;
        }

        // Clear any existing markers
        mMap.clear();

        // Add a new marker
        MarkerOptions options = new MarkerOptions()
                .position(fetchme)
                .title("fetchmee")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
        mMap.addMarker(options);

        // Move the camera to the specified location and apply the zoom level
        float zoomLevel = 18.0f; // Adjust zoom level (1.0f = world, 21.0f = street)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(fetchme, zoomLevel));

        // Enable zoom controls and compass
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
    }
}

package com.example.piCarCustomer;

import android.annotation.SuppressLint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.compat.Place;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    private final static String TAG = "MapFragment";
    private FusedLocationProviderClient locationProviderClient;
    private Location location;
    private Place endLoc;
    private GoogleMap map;

    @SuppressLint("MissingPermission")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        TextView whereGo = view.findViewById(R.id.whereGo);
        whereGo.setOnClickListener(view1 -> getActivity().getSupportFragmentManager()
                                                         .beginTransaction()
                                                         .replace(R.id.frameLayout, new LocationInputFragment(), "locationInput")
                                                         .addToBackStack("locationInput")
                                                         .commit());
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager().beginTransaction()
                                     .replace(R.id.map, mapFragment, "map")
                                     .commit();
        }

        NestedScrollView bottomSheet = view.findViewById(R.id.bottomSheet);
        if (endLoc != null) {
            Log.i(TAG, "success get endLoc");
            bottomSheet.setVisibility(View.VISIBLE);
            bindBottomSheet(view);
        } else
            bottomSheet.setVisibility(View.GONE);

        mapFragment.getMapAsync(this);
        locationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        return view;
    }

    private void bindBottomSheet(View view) {
        ImageView callNormal = view.findViewById(R.id.callNormal);
        ImageView drunk = view.findViewById(R.id.drunk);
        final Button callCar = view.findViewById(R.id.callCar);
        final SingleOrder singleOrder = new SingleOrder();
        callNormal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                singleOrder.setOrderType(Util.NORMAL);
                callCar.setText("一般叫車");
                callCar.setVisibility(View.VISIBLE);
            }
        });
        drunk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                singleOrder.setOrderType(Util.DRUNK);
                callCar.setText("代駕");
                callCar.setVisibility(View.VISIBLE);
            }
        });

        callCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Geocoder geocoder = new Geocoder(getActivity());
                try {
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (!addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        singleOrder.setMemID("M001");
                        singleOrder.setStartLoc(address.getAddressLine(0));
                        singleOrder.setStartLat(address.getLatitude());
                        singleOrder.setStartLng(address.getLongitude());
                    } else {
                        singleOrder.setStartLat(location.getLatitude());
                        singleOrder.setStartLng(location.getLongitude());
                    }

                    singleOrder.setEndLoc((String) endLoc.getAddress());
                    singleOrder.setEndLat(endLoc.getLatLng().latitude);
                    singleOrder.setEndLng(endLoc.getLatLng().longitude);
                    singleOrder.setState(0);
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("action", "insert");
                    jsonObject.addProperty("singleOrder", new Gson().toJson(singleOrder));
                    try {
                        new JsonTask().execute("/singleOrderApi", jsonObject.toString()).get();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.i(TAG, jsonObject.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        locationProviderClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                MapFragment.this.location = location;
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(latLng)
                        .zoom(15)
                        .build();
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });
        map.setMyLocationEnabled(true);
    }

    public void onPlaceInputCallBack(Place place) {
        this.endLoc = place;
    }
}

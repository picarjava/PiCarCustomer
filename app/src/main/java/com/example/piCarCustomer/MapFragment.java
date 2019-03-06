package com.example.piCarCustomer;

import android.annotation.SuppressLint;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
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
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.compat.Place;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    private final static String TAG = "MapFragment";
    private static final int REQUEST_CHECK_SETTINGS = 1;
    private FusedLocationProviderClient locationProviderClient;
    private SettingsClient settingsClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private LocationSettingsRequest locationSettingsRequest;
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
        whereGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager()
                             .beginTransaction()
                             .replace(R.id.frameLayout, new LocationInputFragment(), "locationInput")
                             .addToBackStack("locationInput")
                             .commit();
            }
        });
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
                            new SingleOrderTask().execute("/singleOrderApi", jsonObject.toString()).get();
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
        } else
            bottomSheet.setVisibility(View.GONE);

        mapFragment.getMapAsync(this);
        locationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        settingsClient = LocationServices.getSettingsClient(getActivity());
//        createLocationCallback();
//        buildSettingLocationRequest();
        return view;
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

    private void createLocationCallback() {
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                location = locationResult.getLastLocation();
            }
        };
    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void buildSettingLocationRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        locationSettingsRequest = builder.build();
    }

    private void startLocationUpdate() {
        settingsClient.checkLocationSettings(locationSettingsRequest).addOnSuccessListener(getActivity(), new OnSuccessListener<LocationSettingsResponse>() {
            @SuppressLint("MissingPermission")
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                locationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            }}).addOnFailureListener(getActivity(), new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int state = ((ApiException) e).getStatusCode();
                switch (state) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.e(TAG, "Location settings are not satisfied. Attempting to upgrade location settings ");
                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the
                            // result in onActivityResult().
                            ResolvableApiException rae = (ResolvableApiException) e;
                            rae.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException sie) {
                            Log.e(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        String errorMessage = "Location settings are inadequate, and cannot be " +
                                "fixed here. Fix in Settings.";
                        Log.e(TAG, errorMessage);
                        Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        locationProviderClient.removeLocationUpdates(locationCallback)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        Log.e(TAG, "Cancel location updates requested");
                    }
                });
    }

    public void onPlaceInputCallBack(Place place) {
        this.endLoc = place;
    }

    private static class SingleOrderTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(Util.URL + strings[0]).openConnection();
                connection.setDoInput(true);
                connection.setDoInput(true);
                connection.setUseCaches(false);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("content-type", "charset=utf-8;");
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
                bufferedWriter.write(strings[1]);
                bufferedWriter.close();
                connection.getResponseCode();
                connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}

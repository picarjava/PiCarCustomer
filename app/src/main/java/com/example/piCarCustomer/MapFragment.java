package com.example.piCarCustomer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.piCarCustomer.task.CommonTask;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.compat.Place;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.maps.android.PolyUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    private final static String TAG = "MapFragment";
    private FusedLocationProviderClient locationProviderClient;
    private AppCompatActivity activity;
    private Location location;
    private Place endLoc;
    private GoogleMap map;
    private NestedScrollView bottomSheet;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (AppCompatActivity) context;
    }

    @SuppressLint("MissingPermission")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        bottomSheet = view.findViewById(R.id.bottomSheet);
        TextView whereGo = view.findViewById(R.id.whereGo);
        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(v -> Snackbar.make(v, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());
        whereGo.setOnClickListener(view1 -> activity.getSupportFragmentManager()
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
            try {
                LinearLayout linearLayout = view.findViewById(R.id.linearLayout);
                linearLayout.setVisibility(View.GONE);
                String jsonIn = new DirectionTask().execute(Util.GOOGLE_DIRECTION_URL + "origin=" + location.getLatitude() + "," + location.getLongitude() +
                                                            "&destination=place_id:" + endLoc.getId() + "&key=" + getString(R.string.direction_key)).get();
                JsonObject jsonObject = new Gson().fromJson(jsonIn, JsonObject.class);
                String encodeLine = jsonObject.get("routes").getAsJsonArray().get(0).getAsJsonObject().get("overview_polyline").getAsJsonObject().get("points").getAsString();
                List<LatLng> latLngs = PolyUtil.decode(encodeLine);
                map.addPolyline(new PolylineOptions().color(Color.DKGRAY).width(10).addAll(latLngs));
                Log.d(TAG, latLngs.toString());
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else
            bottomSheet.setVisibility(View.GONE);

        mapFragment.getMapAsync(this);
        locationProviderClient = LocationServices.getFusedLocationProviderClient(activity);
        return view;
    }

    private void bindBottomSheet(View view) {
        ImageView callNormal = view.findViewById(R.id.callNormal);
        ImageView drunk = view.findViewById(R.id.drunk);
        final Button callCar = view.findViewById(R.id.callCar);
        final SingleOrder singleOrder = new SingleOrder();
        callNormal.setOnClickListener(view1 -> {
            singleOrder.setOrderType(Util.NORMAL);
            callCar.setText("一般叫車");
            callCar.setVisibility(View.VISIBLE);
        });
        drunk.setOnClickListener(view12 -> {
            singleOrder.setOrderType(Util.DRUNK);
            callCar.setText("代駕");
            callCar.setVisibility(View.VISIBLE);
        });

        callCar.setOnClickListener(view13 -> {
            Geocoder geocoder = new Geocoder(getActivity());
            try {
                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (!addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    singleOrder.setMemID("M001");
                    singleOrder.setStartLoc(address.getAddressLine(0));
                }

                singleOrder.setStartLat(location.getLatitude());
                singleOrder.setStartLng(location.getLongitude());
                singleOrder.setEndLoc((String) endLoc.getAddress());
                singleOrder.setEndLat(endLoc.getLatLng().latitude);
                singleOrder.setEndLng(endLoc.getLatLng().longitude);
                singleOrder.setState(0);
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("action", "insert");
                jsonObject.addProperty("singleOrder", new Gson().toJson(singleOrder));
                bottomSheet.setVisibility(View.GONE);
                String jsonIn;
                try {
                    jsonIn = new CommonTask().execute("/singleOrderApi", jsonObject.toString()).get();
                    Gson gson = new Gson();
                    jsonObject = gson.fromJson(jsonIn, JsonObject.class);
                    String driverName = jsonObject.get("driverName").getAsString();
                    String plateNum = jsonObject.get("plateNum").getAsString();
                    String carType = jsonObject.get("carType").getAsString();
                    BottomSheetDialogFragment bottomSheetDialogFragment = new BottomSheetFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("driverName", driverName);
                    bundle.putString("plateNum", plateNum);
                    bundle.putString("carType", carType);
                    bottomSheetDialogFragment.setArguments(bundle);
                    bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.i(TAG, jsonObject.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        locationProviderClient.getLastLocation().addOnSuccessListener(activity, location -> {
            MapFragment.this.location = location;
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraPosition cameraPosition;
            if (endLoc == null) {
                cameraPosition = new CameraPosition.Builder()
                                                   .target(latLng)
                                                   .zoom(15)
                                                   .build();
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            } else {
                LatLngBounds latLngBounds = LatLngBounds.builder()
                                                        .include(new LatLng(location.getLatitude(), location.getLongitude()))
                                                        .include(endLoc.getLatLng())
                                                        .build();
                LatLng center = latLngBounds.getCenter();
                center = new LatLng(center.latitude - Math.abs(endLoc.getLatLng().latitude - location.getLatitude()), center.longitude);
                latLngBounds = latLngBounds.including(center);
                Log.d(TAG, center.toString());
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 100));
                map.setMyLocationEnabled(true);
            }
        });
    }

    public void onPlaceInputCallBack(Place place) {
        this.endLoc = place;
    }

    private static class DirectionTask extends AsyncTask<String, Void, String> {
        private final static String TAG = "CommonTask";

        @Override
        protected String doInBackground(String... strings) {
            Log.d(TAG, strings[0]);
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(strings[0]).openConnection();
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setUseCaches(false);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("content-type", "charset=utf-8;");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder jsonIn = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null)
                    jsonIn.append(line);

                connection.disconnect();
                bufferedReader.close();
                return jsonIn.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}

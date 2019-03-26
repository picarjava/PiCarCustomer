package com.example.piCarCustomer;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.piCarCustomer.task.CommonTask;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.compat.Place;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.maps.android.PolyUtil;

import org.java_websocket.client.WebSocketClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MapFragment extends Fragment implements OnMapReadyCallback, WebSocketHandler.WebSocketCallBack {
    private final static String TAG = "MapFragment";
    private final static String SCANNER_PACKAGE = "com.google.zxing.client.android";
    private final static int RES_SCANNER = 1;
    private FusedLocationProviderClient locationProviderClient;
    private MainActivity activity;
    private Location callCarLocation;
    private Place takeOffLoc;
    private LatLng startLatLng, endLatLng;
    private GoogleMap map;
    private LinearLayout linearLayout;
    private NestedScrollView bottomSheet;
    private Member member;
    private WebSocketClient orderBroadcastWebSocket;
    private int distance;
    private AsyncTask directionTask;
    private Handler handler;
    private boolean init;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;
    }

    @SuppressLint("MissingPermission")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        handler = new WebSocketHandler(this);
        member = activity.memberCallBack();
        bottomSheet = view.findViewById(R.id.bottomSheet);
        TextView whereGo = view.findViewById(R.id.whereGo);
        linearLayout = view.findViewById(R.id.linearLayout);
        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(SCANNER_PACKAGE + ".SCAN");
            try {
                activity.startActivityForResult(intent, RES_SCANNER);
            } catch (ActivityNotFoundException e) {
                Log.d(TAG, "not find scanner package");
                showDownloadDialog();
            }
        });
        whereGo.setOnClickListener(v -> activity.getSupportFragmentManager()
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

        mapFragment.getMapAsync(this);
        NestedScrollView bottomSheet = view.findViewById(R.id.bottomSheet);
        if (takeOffLoc != null) {
            Log.i(TAG, "success get takeOffLoc");
            bottomSheet.setVisibility(View.VISIBLE);
            bindBottomSheet(view);
            linearLayout.setVisibility(View.GONE);
            directionTask = new DirectionTask(this).execute(Constant.GOOGLE_DIRECTION_URL + "origin=" + callCarLocation.getLatitude() + "," + callCarLocation.getLongitude() +
                                                                         "&destination=place_id:" + takeOffLoc.getId() + "&key=" + getString(R.string.direction_key));
        } else
            bottomSheet.setVisibility(View.GONE);

        locationProviderClient = LocationServices.getFusedLocationProviderClient(activity);
        Button button = view.findViewById(R.id.button);
        button.setOnClickListener(v -> {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getInPiCar");
            jsonObject.addProperty("driverID", "D004");
            jsonObject.addProperty("orderID", "SODR066");
            new CommonTask().execute("/singleOrderApi", jsonObject.toString());
        });
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        closeWebSocket();
        if (directionTask != null)
            directionTask.cancel(true);
    }

    private void bindBottomSheet(View view) {
        ImageView callNormal = view.findViewById(R.id.callNormal);
        ImageView drunk = view.findViewById(R.id.drunk);
        final Button callCar = view.findViewById(R.id.callCar);
        final SingleOrder singleOrder = new SingleOrder();
        callNormal.setOnClickListener(view1 -> {
            singleOrder.setOrderType(Constant.NORMAL);
            callCar.setText("一般叫車");
            callCar.setVisibility(View.VISIBLE);
        });
        drunk.setOnClickListener(view12 -> {
            singleOrder.setOrderType(Constant.DRUNK);
            callCar.setText("代駕");
            callCar.setVisibility(View.VISIBLE);
        });

        callCar.setOnClickListener(v -> {
            Geocoder geocoder = new Geocoder(activity);
            try {
                getNewWebSocket();
                List<Address> addresses = geocoder.getFromLocation(callCarLocation.getLatitude(), callCarLocation.getLongitude(), 1);
                if (!addresses.isEmpty())
                    singleOrder.setStartLoc(addresses.get(0).getAddressLine(0));

                singleOrder.setMemID(member.getMemID());
                singleOrder.setStartLat(startLatLng.latitude);
                singleOrder.setStartLng(startLatLng.longitude);
                singleOrder.setEndLoc((String) takeOffLoc.getAddress());
                singleOrder.setEndLat(endLatLng.latitude);
                singleOrder.setEndLng(endLatLng.longitude);
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("action", "insert");
                jsonObject.add("singleOrder", new Gson().toJsonTree(singleOrder));
                jsonObject.addProperty("distance", distance);
                bottomSheet.setVisibility(View.GONE);
                String jsonIn;
                try {
                    jsonIn = new CommonTask().execute("/singleOrderApi", jsonObject.toString()).get();
                    Gson gson = new Gson();
                    jsonObject = gson.fromJson(jsonIn, JsonObject.class);
                    String state = jsonObject.get("state").getAsString();
                    BottomSheetDialogFragment bottomSheetDialogFragment = new BottomSheetFragment();
                    Bundle bundle = new Bundle();
                    if ("Success".equals(state)) {
                        bundle.putString("state", "Success");
                        bundle.putString("driverName", jsonObject.get("driverName").getAsString());
                        bundle.putString("plateNum", jsonObject.get("plateNum").getAsString());
                        bundle.putString("carType", jsonObject.get("carType").getAsString());
                    } else {
                        linearLayout.setVisibility(View.VISIBLE);
                        bundle.putString("state", "Failed");
                    }

                    bottomSheetDialogFragment.setArguments(bundle);
                    bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());
                    map.clear();
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
            MapFragment.this.callCarLocation = location;
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraPosition cameraPosition;
            if (takeOffLoc == null) {
                cameraPosition = new CameraPosition.Builder()
                                                   .target(latLng)
                                                   .zoom(15)
                                                   .build();
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            } else
                focusOnRoute();
        });
    }

    public void onPlaceInputCallBack(Place place) {
        this.takeOffLoc = place;
    }

    private void showDownloadDialog() {
        new AlertDialog.Builder(activity)
                       .setTitle("找不到掃描器")
                       .setMessage("請至Google play商店下載")
                       .setPositiveButton("前往", (d, i)->{
                           Uri uri = Uri.parse("market://search?q=name" + SCANNER_PACKAGE);
                           Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                           try {
                               startActivity(intent);
                           } catch (ActivityNotFoundException e) {
                               Log.e(e.toString(), "play store not found");
                           }
                       }).setNegativeButton("no", (d, i)-> d.cancel())
                       .show();
    }

    private static class DirectionTask extends AsyncTask<String, Void, String> {
        private final static String TAG = "DirectionTask";
        private MapFragment mapFragment;

        DirectionTask(MapFragment mapFragment) {
            this.mapFragment = mapFragment;
        }

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

        @Override
        protected void onPostExecute(String jsonIn) {
            JsonObject jsonObject = new Gson().fromJson(jsonIn, JsonObject.class);
            String encodeLine = jsonObject.get("routes")
                                          .getAsJsonArray()
                                          .get(0)
                                          .getAsJsonObject()
                                          .get("overview_polyline")
                                          .getAsJsonObject()
                                          .get("points")
                                          .getAsString();
            mapFragment.distance = jsonObject.get("routes")
                                             .getAsJsonArray()
                                             .get(0)
                                             .getAsJsonObject()
                                             .get("legs")
                                             .getAsJsonArray()
                                             .get(0)
                                             .getAsJsonObject()
                                             .get("distance")
                                             .getAsJsonObject()
                                             .get("value")
                                             .getAsInt();
            List<LatLng> latLngs = PolyUtil.decode(encodeLine);
            mapFragment.map.addPolyline(new PolylineOptions().color(Color.DKGRAY).width(10).addAll(latLngs));
            mapFragment.startLatLng = latLngs.get(0);
            mapFragment.endLatLng = latLngs.get(latLngs.size() - 1);
            Log.d(TAG, latLngs.toString());
        }
    }

    private void getNewWebSocket() {
        if (orderBroadcastWebSocket == null || !orderBroadcastWebSocket.isOpen()) {
            try {
                URI uri = new URI(Constant.WEB_SOCKET_URL + "/orderBroadcastWebSocket/" + member.getMemID());
                Log.d(TAG, uri.toString());
                orderBroadcastWebSocket = new OrderBroadcastWebSocket(handler, uri);
                orderBroadcastWebSocket.connect();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setInputVisible() {
        init = false;
        map.clear();
        linearLayout.setVisibility(View.VISIBLE);
        Toast.makeText(activity, "訂單已完成", Toast.LENGTH_SHORT).show();
        closeWebSocket();
    }

    public void getInCar(String content) {
        if (content != null) {
            Log.e(TAG, content);
            linearLayout.setVisibility(View.INVISIBLE);
            String url;
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(content, JsonObject.class);
            JsonObject parameter = new JsonObject();
            parameter.addProperty("action", "getInPiCar");
            parameter.addProperty(Constant.DRIVER_ID, jsonObject.get(Constant.DRIVER_ID).getAsString());
            if (jsonObject.has(Constant.ORDER_ID)) {
                String orderID = jsonObject.get(Constant.ORDER_ID).getAsString();
                if (orderID.matches("^SODR\\d+$")) {
                    parameter.addProperty(Constant.ORDER_ID, orderID);
                    url = "/singleOrderApi";
                } else
                    return;
            } else if (jsonObject.has(Constant.GROUP_ID)) {
                String groupID = jsonObject.get(Constant.GROUP_ID).getAsString();
                if (groupID.matches("^GODR\\d+$")) {
                    parameter.addProperty(Constant.GROUP_ID, groupID);
                    url = "/groupOrderApi";
                } else
                    return;
            } else
                return;

            getNewWebSocket();
            Log.e(TAG, parameter.toString());
            new CommonTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url, parameter.toString());
        }
    }

    @Override
    public void drawDirection(List<LatLng> list, boolean isGetIn) {
        map.clear();
        map.addPolyline(new PolylineOptions().color(Color.DKGRAY).width(10).addAll(list));
        if (isGetIn) {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                                                              .target(list.get(0))
                                                              .zoom(17)
                                                              .build();
            map.addMarker(new MarkerOptions().position(list.get(0)).icon(BitmapDescriptorFactory.fromBitmap(((BitmapDrawable) activity.getPhoto()).getBitmap())));
            map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        } else {
            if (!init) {
                LatLng lastLatLng = list.get(list.size() - 1);
                callCarLocation.setLatitude(lastLatLng.latitude);
                callCarLocation.setLongitude(lastLatLng.longitude);
                LatLngBounds latLngBounds = LatLngBounds.builder()
                                                        .include(list.get(0))
                                                        .include(lastLatLng)
                                                        .build();
                LatLng center = latLngBounds.getCenter();
                center = new LatLng(center.latitude - Math.abs(lastLatLng.latitude - list.get(0).latitude), center.longitude);
                latLngBounds = latLngBounds.including(center);

                map.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 100));
                if (map.getCameraPosition().zoom > 19) {
                    map.moveCamera(CameraUpdateFactory.zoomBy(19));
                }
                init = true;
            }
        }
    }

    private void closeWebSocket() {
        if (orderBroadcastWebSocket != null && orderBroadcastWebSocket.isOpen())
            orderBroadcastWebSocket.close();
    }

    private void focusOnRoute() {
        LatLngBounds latLngBounds = LatLngBounds.builder()
                .include(new LatLng(callCarLocation.getLatitude(), callCarLocation.getLongitude()))
                .include(takeOffLoc.getLatLng())
                .build();
        LatLng center = latLngBounds.getCenter();
        center = new LatLng(center.latitude - Math.abs(takeOffLoc.getLatLng().latitude - callCarLocation.getLatitude()), center.longitude);
        latLngBounds = latLngBounds.including(center);
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 100));
    }
}

package com.example.piCarCustomer;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.example.piCarCustomer.task.CommonTask;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.concurrent.ExecutionException;

public class CallCarBottomSheetFragment extends BottomSheetDialogFragment {
    private final static String TAG = "com.example.piCarCustomer.CallCarBottomSheetFragment";
    private MainActivity activity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_call_car, container, false);
        Bundle bundle = getArguments();
        assert bundle != null;
        LatLng startLatLng = bundle.getParcelable("startLatLng");
        LatLng endLatLng = bundle.getParcelable("endLatLng");
        String startLoc = bundle.getString("startLoc");
        String endLoc = bundle.getString("endLoc");
        assert startLatLng != null;
        assert endLatLng != null;
        Member member = activity.memberCallBack();
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

        callCar.setOnClickListener(view13 -> {
                singleOrder.setStartLoc(startLoc);
                singleOrder.setMemID(member.getMemID());
                singleOrder.setStartLat(startLatLng.latitude);
                singleOrder.setStartLng(startLatLng.longitude);
                singleOrder.setEndLoc(endLoc);
                singleOrder.setEndLat(endLatLng.latitude);
                singleOrder.setEndLng(endLatLng.longitude);
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("action", "insert");
                jsonObject.addProperty("singleOrder", new Gson().toJson(singleOrder));
                String jsonIn;
                try {
                    jsonIn = new CommonTask().execute("/singleOrderApi", jsonObject.toString()).get();
                    Gson gson = new Gson();
                    jsonObject = gson.fromJson(jsonIn, JsonObject.class);
                    String driverName = jsonObject.get("driverName").getAsString();
                    String plateNum = jsonObject.get("plateNum").getAsString();
                    String carType = jsonObject.get("carType").getAsString();
                    BottomSheetDialogFragment bottomSheetDialogFragment = new BottomSheetFragment();
                    Bundle bundle1 = new Bundle();
                    bundle1.putString("driverName", driverName);
                    bundle1.putString("plateNum", plateNum);
                    bundle1.putString("carType", carType);
                    bottomSheetDialogFragment.setArguments(bundle1);
                    dismiss();
                    bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());

                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.i(TAG, jsonObject.toString());
        });
        return view;
    }
}
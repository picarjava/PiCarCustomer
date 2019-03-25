package com.example.piCarCustomer;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class WebSocketHandler extends Handler {
    private final static String TAG = "WebSocketHandler";
    final static int SINGLE_ORDER_RECEIVED = 0;
    final static int GET_OFF_SUCCEED = 1;
    final static int GET_DIRECTION = 2;

    public interface WebSocketCallBack {
        void setInputVisible();
        void drawDirection(List<LatLng> list, boolean isGetIn);
    }

    private WebSocketCallBack webSocketCallBack;

    public WebSocketHandler(WebSocketCallBack webSocketCallBack) {
        this.webSocketCallBack = webSocketCallBack;
    }

    @Override
    public void handleMessage(Message message) {
        Log.d(TAG, "handleMessage");
        switch (message.what) {
            case GET_OFF_SUCCEED:
                webSocketCallBack.setInputVisible();
                break;
            case GET_DIRECTION:
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson((String) message.obj, JsonObject.class);
                webSocketCallBack.drawDirection(gson.fromJson(jsonObject.get("latLngs"), new TypeToken<List<LatLng>>(){}.getType()), jsonObject.get("getIn").getAsBoolean());
                break;
        }
    }
}

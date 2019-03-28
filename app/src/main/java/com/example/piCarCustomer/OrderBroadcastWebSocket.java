package com.example.piCarCustomer;

import android.os.Handler;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class OrderBroadcastWebSocket extends WebSocketClient {
    private final static String TAG = "OrderBroadcastWebSocket";
    private android.os.Handler handler;

    public OrderBroadcastWebSocket(Handler handler, URI serverUri) {
        super(serverUri, new Draft_6455());
        this.handler = handler;
    }

    @Override
    public void onOpen(ServerHandshake handshakeData) {
        Log.d(TAG, handshakeData.getHttpStatus() + " " + handshakeData.getHttpStatusMessage());
    }

    @Override
    public void onMessage(String message) {
        Log.d(TAG, message);
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(message, JsonObject.class);
        if (jsonObject.has("state")) {
            String state = jsonObject.get("state").getAsString();
            if ("success".equals(state))
                handler.obtainMessage(WebSocketHandler.GET_OFF_SUCCEED, state).sendToTarget();
            else
                handler.obtainMessage(WebSocketHandler.GET_DIRECTION, message).sendToTarget();
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {

    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }
}
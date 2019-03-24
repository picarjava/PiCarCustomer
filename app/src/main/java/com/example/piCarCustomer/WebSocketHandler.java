package com.example.piCarCustomer;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class WebSocketHandler extends Handler {
    private final static String TAG = "WebSocketHandler";
    final static int SINGLE_ORDER_RECEIVED = 0;
    final static int GET_OFF_SUCCEED = 1;

    public interface WebSocketCallBack {
        void setInputVisible();
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
        }
    }
}

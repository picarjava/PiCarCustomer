package com.example.piCarCustomer;

public interface Constant {
    String URL = "http://10.0.2.2:8081/PiCar";
//    String URL = "http://192.168.137.1:8081/PiCar";
    String GOOGLE_DIRECTION_URL = "https://maps.googleapis.com/maps/api/directions/json?";
    String WEB_SOCKET_URL = "ws://10.0.2.2:8081/PiCar";
    String preference = "preference";
    String DRIVER_ID = "driverID";
    String ORDER_ID = "orderID";
    String GROUP_ID = "groupID";
    int NORMAL = 0;
    int DRUNK = 1;
}

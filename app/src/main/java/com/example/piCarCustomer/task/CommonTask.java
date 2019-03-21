package com.example.piCarCustomer.task;

import android.os.AsyncTask;

import com.example.piCarCustomer.Constant;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class CommonTask extends AsyncTask<String, Void, String> {
    private final static String TAG = "CommonTask";

    @Override
    protected String doInBackground(String... strings) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(Constant.URL + strings[0]).openConnection();
            connection.setDoInput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("content-type", "charset=utf-8;");
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
            bufferedWriter.write(strings[1]);
            bufferedWriter.close();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder jsonIn = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null)
                jsonIn.append(line);

            connection.disconnect();
            return jsonIn.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}

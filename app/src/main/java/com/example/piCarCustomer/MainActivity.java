package com.example.piCarCustomer;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private final static String TAG = "MainActivity";
    private final static int SEQ_LOGIN = 0;
    private final static int PERMISSION_REQUSET = 0;
    private GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView =  findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        SharedPreferences preferences = getSharedPreferences(Util.preference, MODE_PRIVATE);
        String account = preferences.getString("account", "");
        String password = preferences.getString("password", "");
        // ask Permission
        askPermissions();
        if (preferences.getBoolean("login", false))
            if (!isValidLogin(Util.URL, account, password))
                startActivityForResult(new Intent(this, LoginActivity.class), SEQ_LOGIN);
            else {
                getSupportFragmentManager().beginTransaction()
                                           .replace(R.id.frameLayout, new MapFragment(), "Map")
                                           .commit();
            }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0)
                getSupportFragmentManager().popBackStack();
            else
                super.onBackPressed();
        }
    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_credit_card) {

        } else if (id == R.id.nav_favor_setting) {
            FragmentManager manager = getSupportFragmentManager();
            Fragment preferenceFrag = manager.findFragmentByTag("Preference");
            if (preferenceFrag == null || preferenceFrag.isDetached())
                getSupportFragmentManager().beginTransaction()
                                           .replace(R.id.frameLayout, new PreferenceFragment(), "Preference")
                                           .addToBackStack("PreferenceFragment")
                                           .commit();
        } else if (id == R.id.nav_logout) {
            SharedPreferences preferences = getSharedPreferences(Util.preference, MODE_PRIVATE);
            preferences.edit()
                       .putBoolean("login", false)
                       .putString("account", "")
                       .putString("password", "")
                       .apply();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, SEQ_LOGIN);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private static class LoginTask extends AsyncTask<String, Void, String> {
        private WeakReference<MainActivity> context;
        private ProgressDialogFragment fragment;
        LoginTask(MainActivity context) {
            this.context = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            MainActivity mainActivity = context.get();
            if (mainActivity != null) {
                fragment = new ProgressDialogFragment();
                fragment.show(mainActivity.getSupportFragmentManager(), "TTT");
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            String url = strings[0];
            String account = strings[1];
            String password = strings[2];
            String jsonIn = null;
            try {
                jsonIn = getRemoteData(url, account, password);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return jsonIn;
        }

        @Override
        protected void onPostExecute(String jsonIn) {
            fragment.dismiss();
            super.onPostExecute(jsonIn);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SEQ_LOGIN) {
                String account = data.getStringExtra("account");
                String password = data.getStringExtra("password");
                if(!isValidLogin(Util.URL, account, password))
                    startActivityForResult(new Intent(this, LoginActivity.class), SEQ_LOGIN);
            }
        }
    }

    private static String getRemoteData(String url, String account, String password) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setUseCaches(false);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("content-type", "charset=utf-8;");
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("account", account);
        jsonObject.addProperty("password", password);
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
        bufferedWriter.write(jsonObject.toString());
        bufferedWriter.close();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder jsonIn = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            jsonIn.append(line);
        }

        bufferedReader.close();
        connection.disconnect();
        return jsonIn.toString();
    }

    private boolean isValidLogin(String url, String account, String password) {
        SharedPreferences preferences = getSharedPreferences(Util.preference, MODE_PRIVATE);
        if (isNetworkConnected()) {
            String jsonIn = null;
            try {
                jsonIn = new LoginTask(this).execute(url, account, password).get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (jsonIn != null) {
                String login = new Gson().fromJson(jsonIn, String.class);
                if ("OK".equals(login)) {
                    preferences.edit()
                            .putBoolean("login", true)
                            .putString("account", account)
                            .putString("password", password)
                            .apply();
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        return  info != null && info.isConnected();
    }

    private void askPermissions() {
        String[] permissions = { Manifest.permission.ACCESS_COARSE_LOCATION,
                                 Manifest.permission.ACCESS_FINE_LOCATION
                               };
        Set<String> permissionRequest = new HashSet<>();
        for (String permission: permissions) {
            int result = checkSelfPermission(permission);
            if (result != PackageManager.PERMISSION_GRANTED)
                permissionRequest.add(permission);
        }

        if (!permissionRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionRequest.toArray(new String[permissionRequest.size()]), PERMISSION_REQUSET);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUSET:
                for (int result: grantResults)
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Permission needed", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                    break;
        }
    }
}

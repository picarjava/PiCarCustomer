package com.example.piCarCustomer;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.piCarCustomer.task.CommonTask;
import com.example.piCarCustomer.task.ImageTask;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, MemberCallBack {
    private final static String TAG = "MainActivity";
    private final static int REQ_LOGIN = 0;
    private final static int PERMISSION_REQUEST = 0;
    private Member member;
    private SharedPreferences preferences;
    private NavigationView navigationView;
    private AsyncTask imageTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ImageView hamburger = findViewById(R.id.hamburger);
        hamburger.setOnClickListener(v -> drawer.openDrawer(Gravity.START));
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        preferences = getSharedPreferences(Constant.preference, MODE_PRIVATE);
        // ask Permission
        askPermissions();
        Log.d(TAG, "create");
        if (preferences.getBoolean("login", false)) {
            String account = preferences.getString("account", "");
            String password = preferences.getString("password", "");
            if (isInvalidLogin(account, password))
                startActivityForResult(new Intent(this, LoginActivity.class), REQ_LOGIN);
            else
                setLoginInfo();
            Log.d(TAG, "valid");
        } else
            startActivityForResult(new Intent(this, LoginActivity.class), REQ_LOGIN);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "start");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "resume");
        getSupportFragmentManager().beginTransaction()
                                   .replace(R.id.frameLayout, new MapFragment(), "Map")
                                   .commit();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (imageTask != null)
            imageTask.cancel(true);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle bundle = new Bundle();
        bundle.putParcelable("member", member);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        member = savedInstanceState.getParcelable("member");
        Log.d(TAG, "restore");
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
        FragmentManager manager = getSupportFragmentManager();
        if (id == R.id.nav_credit_card) {
            String creditCard = "CreditCard";
            manager.popBackStack(creditCard, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            manager.beginTransaction()
                   .replace(R.id.frameLayout, new CreditCardFragment(), creditCard)
                   .addToBackStack(creditCard)
                   .commit();
        } else if (id == R.id.nav_favor_setting) {
            String preference = "Preference";
            manager.popBackStack(preference, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            manager.beginTransaction()
                   .replace(R.id.frameLayout, new PreferenceFragment(), preference)
                   .addToBackStack(preference)
                   .commit();
        } else if (id == R.id.nav_logout) {
            member = null;
            preferences.edit()
                       .putBoolean("login", false)
                       .putString("account", "")
                       .putString("password", "")
                       .apply();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, REQ_LOGIN);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQ_LOGIN) {
                String account = data.getStringExtra("account");
                String password = data.getStringExtra("password");
                if (isInvalidLogin(account, password))
                    startActivityForResult(new Intent(this, LoginActivity.class), REQ_LOGIN);
                else
                    setLoginInfo();
            }
        }
    }

    private boolean isInvalidLogin(String account, String password) {
        if (isNetworkConnected()) {
            String jsonIn = null;
            try {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("action", "login");
                jsonObject.addProperty("account", account);
                jsonObject.addProperty("password", password);
                jsonIn = new CommonTask().execute("/memberApi", jsonObject.toString()).get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (jsonIn != null) {
                Log.d(TAG, jsonIn);
                JsonObject jsonObject = new Gson().fromJson(jsonIn, JsonObject.class);
                if (jsonObject.has("auth") && "OK".equals(jsonObject.get("auth").getAsString())) {
                    preferences.edit()
                               .putBoolean("login", true)
                               .putString("account", account)
                               .putString("password", password)
                               .apply();
                    member = new GsonBuilder().setDateFormat("yyyy-MM-dd")
                                              .create()
                                              .fromJson(jsonObject.get("member"), Member.class);
                    jsonObject = new JsonObject();
                    jsonObject.addProperty("action", "getPicture");
                    jsonObject.addProperty("memID", member.getMemID());
                    imageTask = new ImageTask(this).execute("/memberApi", jsonObject.toString());
                    return false;
                }
            }
        }

        return true;
    }

    private void setLoginInfo() {
        View headerView = navigationView.getHeaderView(0);
        TextView name = headerView.findViewById(R.id.name);
        name.setText(member.getName());
        preferences.edit()
                   .putBoolean("pet", member.getPet() == 1)
                   .putBoolean("smoke", member.getSmoke() == 1)
                   .putBoolean("babySeat", member.getBabySeat() == 1)
                   .apply();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    private void askPermissions() {
        String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        };
        Set<String> permissionRequest = new HashSet<>();
        for (String permission : permissions) {
            int result = checkSelfPermission(permission);
            if (result != PackageManager.PERMISSION_GRANTED)
                permissionRequest.add(permission);
        }

        if (!permissionRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionRequest.toArray(new String[0]), PERMISSION_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST:
                for (int result : grantResults)
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Permission needed", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }

                break;
        }
    }

    @Override
    public Member memberCallBack() {
        return member;
    }

    @Override
    public Drawable getPhoto() {
        ImageView headShot = navigationView.getHeaderView(0).findViewById(R.id.headShot);
        return headShot.getDrawable();
    }
}

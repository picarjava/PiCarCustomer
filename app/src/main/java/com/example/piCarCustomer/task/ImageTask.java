package com.example.piCarCustomer.task;

import android.graphics.BitmapFactory;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import com.example.piCarCustomer.R;

import java.lang.ref.WeakReference;

public class ImageTask extends CommonTask {
    private final static String TAG = "ImageTask";
    private WeakReference<AppCompatActivity> mainActivity;

    public ImageTask(AppCompatActivity activityCompat) {
        mainActivity = new WeakReference<>(activityCompat);
    }

    @Override
    protected void onPostExecute(String encodeImage) {
        super.onPostExecute(encodeImage);
        AppCompatActivity activity = mainActivity.get();
        if (activity != null) {
            if (encodeImage != null && !encodeImage.isEmpty()) {
                Log.d(TAG, "get image");
                byte[] decodeImage = Base64.decode(encodeImage, Base64.DEFAULT);
                View view = ((NavigationView) activity.findViewById(R.id.nav_view)).getHeaderView(0);
                ImageView headShot = view.findViewById(R.id.headShot);
                headShot.setImageBitmap(BitmapFactory.decodeByteArray(decodeImage, 0, decodeImage.length));
            }
        }
    }
}

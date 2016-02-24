package com.crumbsauce.acro;

import android.content.Context;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.crumbsauce.acro.backend.ApiCallStatusReceiver;
import com.crumbsauce.acro.backend.Backend;
import com.crumbsauce.acro.backend.TrackingInfo;

public class TrackingInfoScreen extends AppCompatActivity {
    private static final String LOG_TAG = "TRACKING_INFO";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_info_screen);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        Backend backend = new Backend(getApplicationContext(), Util.getSessionUserEmail(), Util.getSessionUserToken());
        final TextView code = (TextView) findViewById(R.id.trackingCodeLabel);
        final TextView pin = (TextView) findViewById(R.id.trackingPinLabel);
        final Context activityContext = this;

        backend.getTrackingInfoAsync(new ApiCallStatusReceiver<TrackingInfo>() {
            @Override
            public void onSuccess(TrackingInfo result) {
                code.setText(String.format("Tracking Code: %s", result.trackingId));
                pin.setText(String.format("Tracking PIN: %s", result.trackingPin));
            }

            @Override
            public void onError(String error) {
                Log.e(LOG_TAG, error);
                Util.makeToast(activityContext, "Error getting tracking info");
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}

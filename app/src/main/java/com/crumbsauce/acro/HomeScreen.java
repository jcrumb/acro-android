package com.crumbsauce.acro;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.crumbsauce.acro.backend.ApiCallStatusReceiver;
import com.crumbsauce.acro.backend.Backend;
import com.crumbsauce.acro.backend.TrackingInfo;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.orhanobut.wasp.Wasp;

public class HomeScreen extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private static final String LOG_TAG = "HOME_SCREEN";
    private GoogleApiClient mGoogleApiClient;
    private Backend backend;
    private MonitoringServiceConnection serviceConnection;

    public void bindService() {
        Log.d(LOG_TAG, "Binding service");
        serviceConnection = new MonitoringServiceConnection(this);
        bindService(new Intent(this, AccidentMonitor.class), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void copyLinkToClipboard() {

        backend.getTrackingInfoAsync(new ApiCallStatusReceiver<TrackingInfo>() {
            @Override
            public void onError(String error) {
                Util.makeToast(getApplicationContext(), "Error getting tracking info");
            }

            @Override
            public void onSuccess(TrackingInfo result) {
                ClipboardManager manager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData data = ClipData.newPlainText("Acro Tracking Info",
                        String.format("%s PIN: %s", result.trackingUrl, result.trackingPin));
                manager.setPrimaryClip(data);
                Util.makeToast(getApplicationContext(), "Copied to clipboard");
            }
        });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            startActivity(Util.navigateHome());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.trackingToggle:
                if (backend.isTracking()) {
                    stopTracking();
                } else {
                    startTracking();
                }
                break;
            case R.id.nav_contact_manage:
                Intent i = new Intent(this, TrackingInfoScreen.class);
                startActivity(i);
                break;
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(LOG_TAG, "Error connecting: " + connectionResult);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home_screen);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        mGoogleApiClient = Util.getGoogleApiClient(this, this, this);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);
        TextView name = (TextView) header.findViewById(R.id.name_display);
        TextView email = (TextView) header.findViewById(R.id.email_display);

        ImageView profilePicture = (ImageView) header.findViewById(R.id.profile_picture_display);
        String imagePath = Util.getCurrentUserAccount().getPhotoUrl().toString();

        @SuppressWarnings("unused")
        Wasp w = new Wasp.Builder(getApplicationContext()).build();
        Wasp.Image.from(imagePath)
                .setError(android.R.drawable.sym_def_app_icon)
                .setDefault(android.R.drawable.sym_def_app_icon)
                .to(profilePicture)
                .load();


        email.setText(Util.getCurrentUserAccount().getEmail());
        name.setText(Util.getCurrentUserAccount().getDisplayName());

        findViewById(R.id.trackingToggle).setOnClickListener(this);
        backend = new Backend(getApplicationContext(), Util.getSessionUserEmail(), Util.getSessionUserToken());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_screen, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopTracking();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.nav_sign_out:
                signOut();
                break;

            case R.id.nav_tracking_manage:
                Intent moveToTrackingInfo = new Intent(this, TrackingInfoScreen.class);
                startActivity(moveToTrackingInfo);
                break;

            case R.id.nav_contact_manage:
                Intent moveToManageContacts = new Intent(this, ManageContactsScreen.class);
                startActivity(moveToManageContacts);
                break;

            case R.id.nav_share:
                copyLinkToClipboard();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void promptForAlert() {
        final AlertDialog.Builder prompt = new AlertDialog.Builder(this);

        prompt.setTitle("Are you alright?");
        prompt.setIcon(R.drawable.bike_icon);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final TextView timeoutText = new TextView(this);
        timeoutText.setText("10");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            timeoutText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        }
        layout.addView(timeoutText);

        prompt.setView(layout);
        prompt.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Util.makeToast(getApplicationContext(), "Alert cleared");
            }
        });
        prompt.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendAlert();
            }
        });

        final AlertDialog promptDialog = prompt.create();
        promptDialog.show();

        new CountDownTimer(10000, 1000) {
            int secondsRemaining = 10;

            @Override
            public void onFinish() {
                if (promptDialog.isShowing()) {
                    promptDialog.dismiss();
                    sendAlert();
                }
            }

            @Override
            public void onTick(long millisUntilFinished) {
                if (promptDialog.isShowing()) {
                    timeoutText.setText(String.valueOf(secondsRemaining));
                    secondsRemaining--;
                }
            }
        }.start();
    }

    public void sendAlert() {
        backend.generateAlertAsync(new ApiCallStatusReceiver<Void>() {
            @Override
            public void onError(String error) {
                Util.makeToast(getApplicationContext(), "Error sending alert");
            }

            @Override
            public void onSuccess(Void result) {
                Util.makeToast(getApplicationContext(), "Alert sent");
            }
        });
    }

    private void signOut() {
        if (mGoogleApiClient.isConnected()) {
            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            Log.d(LOG_TAG, "Signed out");
                            Intent moveToSignIn = new Intent(getApplicationContext(), AcroLogin.class);
                            startActivity(moveToSignIn);
                        }
                    }
            );
        } else {
            Log.e(LOG_TAG, "Failed to sign out - not connected");
        }
    }

    private void startTracking() {
        final HomeScreen c = this;

        backend.startTrackingAsync(new ApiCallStatusReceiver<Void>() {
            @Override
            public void onError(String error) {
                Util.makeToast(c, error);
            }

            @Override
            public void onSuccess(Void result) {
                c.bindService();
                Util.makeToast(c, "Tracking started");
            }
        });
    }

    private void stopTracking() {
        final HomeScreen c = this;

        backend.stopTrackingAsync(new ApiCallStatusReceiver<Void>() {
            @Override
            public void onError(String error) {
                Util.makeToast(c, error);
            }

            @Override
            public void onSuccess(Void result) {
                c.unbindService();
                Util.makeToast(c, "Tracking stopped");
            }
        });
    }

    public void unbindService() {
        Log.d(LOG_TAG, "Unbinding service");
        if (serviceConnection != null) {
            unbindService(serviceConnection);
            serviceConnection = null;
        }
    }
}

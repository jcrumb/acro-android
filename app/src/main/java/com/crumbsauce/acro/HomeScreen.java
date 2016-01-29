package com.crumbsauce.acro;

import android.content.Intent;
import android.os.Bundle;
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
import android.widget.TextView;


import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.orhanobut.wasp.Wasp;

public class HomeScreen extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener, ApiCallStatusReceiver {

    private GoogleApiClient mGoogleApiClient;
    private static final String LOG_TAG = "HOME_SCREEN";
    private Backend backend;

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
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            startActivity(Util.navigateHome());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_screen, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_sign_out) {
            signOut();
        } else if (id == R.id.nav_share) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(LOG_TAG, "Error connecting: " + connectionResult);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.trackingToggle:
                if (backend.isTracking()) {
                    backend.stopTrackingAsync(this).execute();
                } else {
                    backend.startTrackingAsync(this).execute();
                }
                break;
        }
    }

    @Override
    public void receiveCallStatus(boolean ok, String method) {
        switch (method) {
            case "/tracking/begin":
                if (ok) {
//                    backend.startHeartbeats();
                    Util.makeToast(this, "Tracking started");
                } else {
                    Util.makeToast(this, "An error occurred starting tracking, please try again");
                }
                break;
            case "/tracking/end":
                if (ok) {
                    Util.makeToast(this, "Tracking stopped");
                } else {
                    Util.makeToast(this, "An error occurred stopping tracking. please try again");
                }
        }
    }
}

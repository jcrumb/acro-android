package com.crumbsauce.acro.backend;


import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.orhanobut.wasp.Callback;
import com.orhanobut.wasp.Wasp;
import com.orhanobut.wasp.WaspError;
import com.orhanobut.wasp.utils.AuthToken;
import com.orhanobut.wasp.utils.RequestInterceptor;
import com.orhanobut.wasp.utils.WaspRetryPolicy;

import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class Backend implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private AcroApiService acroService;
    private GoogleApiClient mGoogleApiClient;

    private static final int SECONDS = 1000;
    private static final int MINUTES = 60000;

    private static boolean tracking;
    private static final String backendBaseURL = "http://192.168.0.34:5000";
    private static final String LOG_TAG = "BACKEND";
    public static final String ERROR_STRING = "ERR_BACKEND"; // Any of the methods that return String will return this value on error
    private final String userEmail;

    public Backend() { this.userEmail = ""; }

    public Backend(final Context applicationContext, final String userEmail, final String sessionToken) {
        this.userEmail = userEmail;
        acroService = new Wasp.Builder(applicationContext)
                .setEndpoint(backendBaseURL)
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void onHeadersAdded(Map<String, String> headers) {
                        headers.put("Authorization", String.format("Bearer %s", sessionToken));
                        headers.put("X-User", userEmail);
                    }

                    @Override
                    public void onQueryParamsAdded(Map<String, Object> params) {

                    }

                    @Override
                    public WaspRetryPolicy getRetryPolicy() {
                        return null;
                    }

                    @Override
                    public AuthToken getAuthToken() {
                        return null;
                    }
                })
                .build()
                .create(AcroApiService.class);

        mGoogleApiClient = new GoogleApiClient.Builder(applicationContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();
    }

    private void nullCheck() {
        if (acroService == null) { throw new NullPointerException("You used the wrong constructor"); }
    }

    private LocationString getLastLocation() {
        Location l = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        return LocationString.fromLocation(l);
    }

    private void startHeartbeats() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient,
                new LocationRequest()
                .setInterval(30 * SECONDS)
                .setFastestInterval(10 * SECONDS)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY),
                this
        );
    }

    private void sendHeartbeat(final LocationString locationString) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    acroService.sendHeartbeat(locationString);
                } catch (Exception e) {
                    Log.e(LOG_TAG, e.toString());
                }
                return null;
            }
        }.execute();
    }

    private String logInWithToken(String googleIdToken) {
        String tokenJSON = String.format("{\"idToken\":\"%s\"}", googleIdToken);
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                tokenJSON
        );


        Request req = new Request.Builder()
                .url(backendBaseURL + "/login/google/verifytoken")
                .post(body)
                .build();

        Response resp;
        String response = ERROR_STRING;
        try {
            resp = client.newCall(req).execute();
            switch (resp.code()) {
                case 400:
                    Log.d(LOG_TAG, "Received 400 from server");
                    break;
                case 200:
                    response = resp.body().string().trim().replace("\"", "");
                    Log.d(LOG_TAG, response);
                    break;
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.toString());
        }

        return response;
    }

    public boolean isTracking() {
        return tracking;
    }

    public AsyncTask<Void, Void, String> logInWithTokenAsync(final LogInResultHandler callback, final String googleIdToken) {
        return new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                return logInWithToken(googleIdToken);
            }

            @Override
            protected void onPostExecute(String result) {
                callback.receiveLoginResult(result);
            }
        };
    }

    public void startTrackingAsync(final ApiCallStatusReceiver<Void> callback) {
        nullCheck();

        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                boolean success = false;
                try {
                    acroService.beginTracking(getLastLocation());
                    tracking = true;
                    success = true;
                } catch (Exception e) {
                    Log.e(LOG_TAG, e.toString());
                }
                return success;
            }

            @Override
            protected void onPostExecute(Boolean ok) {
                if (ok) {
                    tracking = true;
                    startHeartbeats();
                    callback.onSuccess(null);
                } else {
                    callback.onError("Error starting tracking");
                }
            }
        }.execute();
    }

    public void stopTrackingAsync(final ApiCallStatusReceiver<Void> callback) {
        nullCheck();
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                boolean success = false;
                try {
                    acroService.endTracking();
                    success = true;
                } catch (Exception e) {
                    Log.e(LOG_TAG, e.toString());
                }
                return success;
            }

            @Override
            protected void onPostExecute(Boolean ok) {
                if (ok) {
                    tracking = false;
                    callback.onSuccess(null);
                } else {
                    callback.onError("Error stopping tracking");
                }
            }
        }.execute();
    }

    public void getTrackingInfoAsync(final ApiCallStatusReceiver<TrackingInfo> callback) {
        acroService.getTrackingInfo(userEmail, new Callback<TrackingInfo>() {
            @Override
            public void onSuccess(com.orhanobut.wasp.Response response, TrackingInfo trackingInfo) {
                callback.onSuccess(trackingInfo);
            }

            @Override
            public void onError(WaspError error) {
                callback.onError(error.getErrorMessage());
            }
        });
    }

    public void getContactsAsync(final ApiCallStatusReceiver<Contact[]> callback) {
        acroService.getContacts(userEmail, new Callback<Contact[]>() {
            @Override
            public void onSuccess(com.orhanobut.wasp.Response response, Contact[] contacts) {
                callback.onSuccess(contacts);
            }

            @Override
            public void onError(WaspError error) {
                callback.onError(error.getErrorMessage());
            }
        });
    }

    public void createContactAsync(final Contact newContact, final ApiCallStatusReceiver<Void> callback) {
        acroService.createContact(userEmail, newContact, new Callback<Contact>() {
            @Override
            public void onSuccess(com.orhanobut.wasp.Response response, Contact contact) {
                callback.onSuccess(null);
            }

            @Override
            public void onError(WaspError error) {
                callback.onError(error.getErrorMessage());
            }
        });
    }

    public void deleteContactAsync(final String phoneNumber, final ApiCallStatusReceiver<Void> callback) {

        acroService.deleteContact(userEmail, phoneNumber, new Callback<OperationStatusMessage>() {
            @Override
            public void onSuccess(com.orhanobut.wasp.Response response, OperationStatusMessage operationStatusMessage) {
                callback.onSuccess(null);
            }

            @Override
            public void onError(WaspError error) {
                callback.onError(error.getErrorMessage());
            }
        });
    }

    // Interface implementations for google api client
    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (isTracking()) {
            sendHeartbeat(LocationString.fromLocation(location));
        } else {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient,
                    this
            );
        }
    }
}

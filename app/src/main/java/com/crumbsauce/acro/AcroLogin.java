package com.crumbsauce.acro;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;


public class AcroLogin extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener, LogInResultHandler {

    private GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 9001;
    private static final String LOG_TAG = "LOG_IN_OUT";
    private SignInButton signInButton;
    private Backend backend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acro_login);

        findViewById(R.id.sign_in_button).setOnClickListener(this);

        signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        mGoogleApiClient = Util.getGoogleApiClient(this, this , this);
        backend = new Backend();

        signInButton.setScopes(Util.getGoogleSignInOptions().getScopeArray());

        Log.d(LOG_TAG, "onCreateFinished");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                Log.v(LOG_TAG, "Sign in button triggered");
                signIn();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Sign in button intent processed
        if (requestCode == RC_SIGN_IN) {
            processSignInResult(data);
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(Util.navigateHome());
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(LOG_TAG, "Connection failed: " + connectionResult);
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void processSignInResult(Intent data) {
        Log.v(LOG_TAG, "Sign in intent finished");
        GoogleSignInResult r = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

        if (r.isSuccess()) {
            Log.v(LOG_TAG, "Successful login");
            GoogleSignInAccount acct = r.getSignInAccount();
            Util.setCurrentUserAccount(acct);

            Log.d(LOG_TAG, "Name:          " + acct.getDisplayName());
            Log.d(LOG_TAG, "Email:         " + acct.getEmail());
            Log.d(LOG_TAG, "Backend Token: " + acct.getIdToken());
            Log.d(LOG_TAG, "User ID:       " + acct.getId());

            backend.logInWithTokenAsync(this, acct.getIdToken()).execute();

            signInButton.setEnabled(false);
        } else {
            Log.e(LOG_TAG, "Error signing in: " + r.getStatus().getStatusCode());
        }
    }

    @Override
    public void receiveLoginResult(String result) {
        if (result.equals(Backend.ERROR_STRING)) {
            Toast.makeText(
                    this,
                    "An error occurred logging in, please try again",
                    Toast.LENGTH_LONG
            ).show();

            signInButton.setEnabled(true);
        } else {
            Util.setSessionUserToken(result);
            Intent moveToMainScreen = new Intent(getApplicationContext(), HomeScreen.class);
            startActivity(moveToMainScreen);
        }
    }
}

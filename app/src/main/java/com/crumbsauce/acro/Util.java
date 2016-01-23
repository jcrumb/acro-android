package com.crumbsauce.acro;


import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;

class Util {
    private static GoogleSignInOptions mGoogleSignInOptions;

    public static Intent navigateHome() {
        Intent moveToHome = new Intent(Intent.ACTION_MAIN);
        moveToHome.addCategory(Intent.CATEGORY_HOME);
        moveToHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        return moveToHome;
    }

    public static GoogleApiClient getGoogleApiClient(
            Context context, FragmentActivity fragmentActivity, GoogleApiClient.OnConnectionFailedListener listener) {
        if (mGoogleSignInOptions == null) {
            initSettings();
        }

        GoogleApiClient c = new GoogleApiClient.Builder(context)
                .enableAutoManage(fragmentActivity, listener)
                .addApi(Auth.GOOGLE_SIGN_IN_API, mGoogleSignInOptions)
                .build();

        return c;
    }

    public static GoogleSignInOptions getGoogleSignInOptions() {
        if (mGoogleSignInOptions == null) {
            initSettings();
        }
        return mGoogleSignInOptions;
    }

    private static void initSettings() {
        mGoogleSignInOptions = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN
        )
                .requestEmail()
                .requestIdToken("346961808175-ji8aoge8npivkifclbutiu57aip6voph.apps.googleusercontent.com")
                .build();

    }

}

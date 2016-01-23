package com.crumbsauce.acro;


import android.content.Intent;

class Util {
    public static Intent navigateHome() {
        Intent moveToHome = new Intent(Intent.ACTION_MAIN);
        moveToHome.addCategory(Intent.CATEGORY_HOME);
        moveToHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        return moveToHome;
    }
}

package com.myeltapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.webkit.JavascriptInterface;

public class JavaScriptInterface {
    private Activity activity;

    public JavaScriptInterface(Activity activiy) {
        this.activity = activiy;
    }
    @JavascriptInterface
    public void loadNativeHomePage(){
    	Intent intent = new Intent(this.activity, MainActivity.class);
        activity.startActivity(intent);
    }
}
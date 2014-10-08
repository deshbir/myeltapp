package com.myeltapp;

import android.app.Activity;
import android.webkit.JavascriptInterface;

/**
 * This class acts as an interface to call Java functions from Javascript. 
 */

public class JavaScriptInterface {
    private Activity activity;

    public JavaScriptInterface(Activity activiy) {
        this.activity = activiy;
    }
    
    @JavascriptInterface
    public void loadNativeHomePage(){
    	((MyeltApp)this.activity).showNativeLoginScreen();
    }
    
    @JavascriptInterface
    public void showMyELT(){
    	((MyeltApp)this.activity).showMyELTWebView();
    }
    
    
}
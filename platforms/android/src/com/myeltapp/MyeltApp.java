/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */

package com.myeltapp;

import org.apache.cordova.CordovaActivity;
import org.apache.cordova.plugin.AndroidProgressHUD;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class MyeltApp extends CordovaActivity implements LoginAsyncResponse 
{
	public final static String SERVER_URL = "http://myelt3.comprotechnologies.com";
	
	String usernameStr;
	String passwordStr;
	EditText password;
	EditText username;
	
	AndroidProgressHUD activityIndicator = null;
	
	boolean firstLaunch = true;
	SharedPreferences pref;
	Editor editor;
	
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
    	super.onCreate(savedInstanceState);
    	pref = getApplicationContext().getSharedPreferences("MyPref", 0);
    	editor = pref.edit();
    	if(pref.getBoolean("firstLogin",true)){
    		setContentView(R.layout.activity_main);
    		password = (EditText) findViewById(R.id.password);
    		//login into MyELT application when user clicks on done button on keyboard 
    		password.setOnEditorActionListener(new OnEditorActionListener(){
    			@Override
    			public boolean onEditorAction(TextView v, int actionId,
    					KeyEvent event) {
    				if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
    					login(null);
    				}    
    	            return false;
    			}
    		});
    	}else{
    		super.init();
			JavaScriptInterface jsInterface = new JavaScriptInterface(this);
			appView.addJavascriptInterface(jsInterface, "JSInterface");
			super.loadUrl("file:///android_asset/www/index.html");
			showMyELTWebView();
    	}
    }
	
	/** Called when user clicks the Login button */
	public void login(View view) {
		activityIndicator = AndroidProgressHUD.show(this, "Loading...", true,true,null);
		username = (EditText) findViewById(R.id.username);
		password = (EditText) findViewById(R.id.password);
		usernameStr = username.getText().toString();
		passwordStr = password.getText().toString();
		String url = SERVER_URL + "/ilrn/api/logincheck?u=" + usernameStr + "&p=" + passwordStr;
		new LoginAsyncTask(this).execute(url);		
	}
    
    @Override
    public Object onMessage(String id, Object data) {   
    	
        if("onPageFinished".equals(id)) {
        	this.usernameStr=pref.getString("username","none");
        	this.passwordStr=pref.getString("password","none");
        	String js = String.format("startMyELT('%s');",SERVER_URL + "/ilrn/global/extlogin.do?u=" + usernameStr + "&p=" + passwordStr + "&isNative=true");
        	this.sendJavascript(js);
        }
        
        return super.onMessage(id, data);       
    }
    
    //function to return to HomePage
    public void returnToHome(View view){
    	String js = String.format("startMyELT('%s');",SERVER_URL + "/ilrn/course/course.do?isNative=true");
    	this.sendJavascript(js);
    }
    public void showMyELTWebView() {    
    	
    	LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	final View mainView = inflater.inflate(R.layout.myelt_content_layout, null);
    	final LinearLayout rootLayout = this.root;
    	RelativeLayout contentLayout = (RelativeLayout)mainView.findViewById(R.id.content_layout);
    	contentLayout.addView(rootLayout);
        if(pref.getBoolean("firstLogin", true)){
    	activityIndicator.dismiss();
		editor.putBoolean("firstLogin", false);
		editor.commit();
        }
		
    	this.runOnUiThread(new Runnable() {
			public void run() {
				setContentView(mainView);
			}
		});
    }
    
    public void showNativeLoginScreen() {
    	this.runOnUiThread(new Runnable() {
			public void run() {
				setContentView(R.layout.activity_main);
				password = (EditText) findViewById(R.id.password);
				//login into MyELT application when user clicks on done button on keyboard 
				password.setOnEditorActionListener(new OnEditorActionListener(){
					@Override
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
							login(null);
						}    
			            return false;
					}
				});
			}
		});
    }
    
    
    @Override
   	public void processFinish(String result) {
    	Context context = getApplicationContext();
    	CharSequence text = null;
    	int duration = Toast.LENGTH_LONG;
    	Toast toast;
   		try {
   			if(result != null) {
   				JSONObject statusJson = new JSONObject(result);
   				if(((JSONObject)statusJson.get("response")).get("status").equals("success")){
   					editor.putString("username",usernameStr);
   					editor.putString("password",passwordStr);
   					editor.commit();
   					if (firstLaunch) {
   						super.init();
   						firstLaunch = false;
   					}
   					JavaScriptInterface jsInterface = new JavaScriptInterface(this);
   			    	appView.addJavascriptInterface(jsInterface, "JSInterface");
   					super.loadUrl("file:///android_asset/www/index.html");
   				}
   				else {
   					text = "Invalid Username or Password.";
   					toast = Toast.makeText(context, text, duration);
   					toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
   					activityIndicator.dismiss();
   					toast.show();
   				}
   			} else {
   				text = "Something went wrong. Please try again later.";
   				toast= Toast.makeText(context, text, duration);
   				toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
   				activityIndicator.dismiss();
   				toast.show();
   			}
   		} catch (JSONException e) {
   			//To-do: Show user a warning that something went wrong on server.
			text = "Something went wrong. Please try again later.";
			toast= Toast.makeText(context, text, duration);
			toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
			toast.show();
   			e.printStackTrace();
   		}
   	}
}


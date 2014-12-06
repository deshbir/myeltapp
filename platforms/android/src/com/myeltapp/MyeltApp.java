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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.cordova.CordovaActivity;
import org.apache.cordova.plugin.AndroidProgressHUD;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
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
	DrawerLayout mDrawerLayout;
	AndroidProgressHUD activityIndicator = null;
	ExpandableListAdapter listAdapter;
    private ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
	boolean firstLaunch = true;
	SharedPreferences pref;
	Editor editor;
	
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
    	super.onCreate(savedInstanceState);
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
    	final View navigationDrawer = inflater.inflate(R.layout.navigationdrawer, null);
    	mDrawerLayout = (DrawerLayout) navigationDrawer.findViewById(R.id.drawer_layout);
    	expListView = (ExpandableListView) navigationDrawer.findViewById(R.id.left_drawer);
    	prepareListData();
   	 	listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild); 
        // setting list adapter
        expListView.setAdapter(listAdapter);
 
        // Listview Group click listener
        expListView.setOnGroupClickListener(new OnGroupClickListener() {
 
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                    int groupPosition, long id) {
	           	 if(listDataHeader.get(groupPosition)== "Help"){
	           		 loadHelpPage();
	           	 }else if(listDataHeader.get(groupPosition)== "Sign Out"){
	           		 signOut();
	           	 }
	           	 return false;
            }
        });
     	final LinearLayout rootLayout = this.root;
    	RelativeLayout contentLayout = (RelativeLayout)mainView.findViewById(R.id.content_layout);
    	contentLayout.addView(rootLayout);
    	final LinearLayout navigationLayout = (LinearLayout)navigationDrawer.findViewById(R.id.content_frame); 
    	navigationLayout.addView(mainView);
    	activityIndicator.dismiss();
		
    	this.runOnUiThread(new Runnable() {
			public void run() {
				setContentView(navigationDrawer);
			}
		});
    }
    private void prepareListData() {
    	listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();
 
        // Adding child data
        listDataHeader.add("Languages");
        listDataHeader.add("Help");
        listDataHeader.add("Sign Out");
 
        // Adding child data
        List<String> languages = new ArrayList<String>();
        languages.add("English");
        languages.add("Portugese");
        languages.add("Spanish");
        languages.add("Japanese");
        languages.add("Korean");
        languages.add("Chinese");
        languages.add("Chinese Traditional");
        languages.add("Arabic");
        languages.add("Vietnamese");
        
        List<String> help = new ArrayList<String>();
        List<String> signout = new ArrayList<String>();
        
        listDataChild.put(listDataHeader.get(0), languages); // Header, Child data
        listDataChild.put(listDataHeader.get(1), help);
        listDataChild.put(listDataHeader.get(2), signout);
    	
    }
    public void loadHelpPage(){
    	String js = String.format("startMyELT('%s');",SERVER_URL+"/ilrn/global/myeltHelp.do?isNative=true");
    	this.sendJavascript(js);
    }
    public void toggleSideMenu(View view){
    	mDrawerLayout.openDrawer(Gravity.LEFT);
    }
    public void signOut(){
    	String js = String.format("startMyELT('%s');",SERVER_URL+"/ilrn/accounts/logout.do?isNative=true");
    	this.sendJavascript(js);
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


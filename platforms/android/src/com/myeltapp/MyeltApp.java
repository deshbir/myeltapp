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
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class MyeltApp extends CordovaActivity implements LoginAsyncResponse 
{
	public final static String SERVER_URL = "http://myelt3.comprotechnologies.com";
	
	private String usernameStr;
	private String passwordStr;
	private EditText password;
	private EditText username;
	private DrawerLayout mDrawerLayout;
	private AndroidProgressHUD activityIndicator = null;
	private ExpandableListAdapter expandableListAdapter;
	private SimpleListAdapter simpleListAdapter;
	private ListView simpleListView;
    private ExpandableListView expListView;
    private List<String> links;
    private List<String> settings;
    private HashMap<String, List<String>> settingsDataChild;
    private boolean firstLaunch = true;
    private boolean isDrawerOpen = false;
    private static final String ENGLISH = "0";
    private static final String SPANISH = "2";
    private static final String PORTUGESE = "5";
    private static final String JAPANESE = "3";
    private static final String KOREAN = "4";
    private static final String CHINESE = "6";
    private static final String CHINESE_TRADITIONAL = "7";
    private static final String VIETNAMESE = "9";
    private static final String ARABIC = "8";
    public static final String KEY_FIRST_LOGIN = "firstLogin";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_SELECTED_LOCALE = "selectedLocale";
    
    SharedPreferences userPreferences;
	Editor editor;
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
    	super.onCreate(savedInstanceState);
    	userPreferences = getApplicationContext().getSharedPreferences("UserPreferences", 0);
    	editor = userPreferences.edit();
    	if(userPreferences.getBoolean(KEY_FIRST_LOGIN,true)){
    		setContentView(R.layout.login_view);
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
			setContentView(R.layout.splash);
			super.loadUrl("file:///android_asset/www/index.html");
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
        	this.usernameStr=userPreferences.getString(KEY_USERNAME,null);
        	this.passwordStr=userPreferences.getString(KEY_PASSWORD,null);
        	String js = String.format("startMyELT('%s');",SERVER_URL + "/ilrn/global/extlogin.do?u=" + usernameStr + "&p=" + passwordStr + "&isNative=true&locale="+userPreferences.getString(KEY_SELECTED_LOCALE, ENGLISH));
        	this.sendJavascript(js);
        }
        
        return super.onMessage(id, data);       
    }
    
    //function to return to HomePage
    public void loadHomePage(View view){
    	String js = String.format("startMyELT('%s');",SERVER_URL + "/ilrn/course/course.do?isNative=true");
    	this.sendJavascript(js);
    }
    public void showMyELTWebView() {    
    	LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	final View myeltView = inflater.inflate(R.layout.myelt_view, null);
    	mDrawerLayout = (DrawerLayout) myeltView.findViewById(R.id.drawer_layout);
    	mDrawerLayout.setScrimColor(getResources().getColor(android.R.color.transparent));
    	mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,GravityCompat.START);
    	simpleListView = (ListView) myeltView.findViewById(R.id.simple_list_view);
    	expListView = (ExpandableListView) myeltView.findViewById(R.id.expandable_list_view);
    	prepareListData();
    	simpleListAdapter = new SimpleListAdapter(this,links);
    	
        // setting list adapter
    	simpleListView.setAdapter(simpleListAdapter);
    	simpleListView.setItemChecked(0,true);
    	//simpleListView.setSelection(1);
    	expandableListAdapter = new ExpandableListAdapter(this, settings, settingsDataChild); 
        expListView.setAdapter(expandableListAdapter);
        
        //Simple ListView Item click listener
        simpleListView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	if(position == 0){
            		loadHomePage(view);
            	}else if(position == 1){
            		loadProfilePage();
            	}else if(position == 2){
            		loadMessagesPage();
            	}else if(position == 3){
            		loadHelpPage();
            	}
            }
        });
        
        // ListView Group click listener
        expListView.setOnGroupClickListener(new OnGroupClickListener() {
 
			@Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                    int groupPosition, long id) {
	           	 if(settings.get(groupPosition)== "Sign Out"){
	           		v.setBackgroundColor(getResources().getColor(R.color.gray));
	           		 signOut();
	           	 }
	           	 return false;
            }
        });
        // ListView on child click listener
        expListView.setOnChildClickListener(new OnChildClickListener() {
        	@Override
            public boolean onChildClick(ExpandableListView parent, View v,
                    int groupPosition, int childPosition, long id) {
        		  int index = parent.getFlatListPosition(ExpandableListView.getPackedPositionForChild(groupPosition, childPosition));
        		    parent.setItemChecked(index, true);
        	    if(childPosition == 0){
        	    	editor.putString(KEY_SELECTED_LOCALE, ENGLISH);
        	    	editor.commit();
                	changeLocaleNative(userPreferences.getString(KEY_SELECTED_LOCALE, ENGLISH));
                }else if(childPosition == 1){
                	editor.putString(KEY_SELECTED_LOCALE, SPANISH);
                	editor.commit();
                	changeLocaleNative(userPreferences.getString(KEY_SELECTED_LOCALE, ENGLISH));
                }else if(childPosition == 2){
                	editor.putString(KEY_SELECTED_LOCALE, PORTUGESE);
                	editor.commit();
                	changeLocaleNative(userPreferences.getString(KEY_SELECTED_LOCALE, ENGLISH));
                }else if(childPosition == 3){
                	editor.putString(KEY_SELECTED_LOCALE, JAPANESE);
                	editor.commit();
                	changeLocaleNative(userPreferences.getString(KEY_SELECTED_LOCALE, ENGLISH));
                }else if(childPosition == 4){
                	editor.putString(KEY_SELECTED_LOCALE, KOREAN);
                	editor.commit();
                	changeLocaleNative(userPreferences.getString(KEY_SELECTED_LOCALE, ENGLISH));
                }else if(childPosition == 5){
                	editor.putString(KEY_SELECTED_LOCALE, CHINESE);
                	editor.commit();
                	changeLocaleNative(userPreferences.getString(KEY_SELECTED_LOCALE, ENGLISH));
                }else if(childPosition == 6){
                	editor.putString(KEY_SELECTED_LOCALE, CHINESE_TRADITIONAL);
                	editor.commit();
                	changeLocaleNative(userPreferences.getString(KEY_SELECTED_LOCALE, ENGLISH));
                }else if(childPosition == 7){
                	editor.putString(KEY_SELECTED_LOCALE, VIETNAMESE);
                	editor.commit();
                	changeLocaleNative(userPreferences.getString(KEY_SELECTED_LOCALE, ENGLISH));
                }else if(childPosition == 8){
                	editor.putString(KEY_SELECTED_LOCALE, ARABIC);
                	editor.commit();
                	changeLocaleNative(userPreferences.getString(KEY_SELECTED_LOCALE, ENGLISH));
                }
        	    
                return false;
            }
        });
       // set up the drawer's list view with items and click listener
       
     	final LinearLayout rootLayout = this.root;
    	LinearLayout body = (LinearLayout)myeltView.findViewById(R.id.body);
    	body.addView(rootLayout);
    	if(userPreferences.getBoolean(KEY_FIRST_LOGIN, true)){
        	activityIndicator.dismiss();
    		editor.putBoolean(KEY_FIRST_LOGIN, false);
    		editor.commit();
    	}
    	this.runOnUiThread(new Runnable() {
			public void run() {
				setContentView(myeltView);
			}
		});
    }
    private void prepareListData() {
    	links = new ArrayList<String>();
    	links.add("Home");
    	links.add("Profile");
    	links.add("Messages");
    	links.add("Help");
    	settings = new ArrayList<String>();
    	settings.add("Languages");
        settings.add("Sign Out");
        
        // Adding child data
        List<String> languages = new ArrayList<String>();
        languages.add("English");
        languages.add("Portugese");
        languages.add("Spanish");
        languages.add("Japanese");
        languages.add("Korean");
        languages.add("Chinese");
        languages.add("Chinese Traditional");
        languages.add("Vietnamese");
        languages.add("Arabic");
        
        List<String> signout = new ArrayList<String>();
        settingsDataChild = new HashMap<String, List<String>>();
        settingsDataChild.put(settings.get(0), languages); // Header, Child data
        settingsDataChild.put(settings.get(1), signout);
    }
    public void changeLocaleNative(String locale){
    	String js = String.format("changeLocaleNative('%s');",locale);
    	this.sendJavascript(js);
    }
    public void loadProfilePage(){
    	String js = String.format("startMyELT('%s');",SERVER_URL+"/ilrn/global/changeAccount.do?isNative=true");
    	this.sendJavascript(js);
    }
    public void loadMessagesPage(){
    	String js = String.format("startMyELT('%s');",SERVER_URL+"/ilrn/global/announcements.do?isNative=true");
    	this.sendJavascript(js);
    }
    public void loadHelpPage(){
    	String js = String.format("startMyELT('%s');",SERVER_URL+"/ilrn/global/myeltHelp.do?isNative=true");
    	this.sendJavascript(js);
    }
    public void toggleSideMenu(View view){
    	if(isDrawerOpen == true){
    		mDrawerLayout.closeDrawer(Gravity.LEFT);
    		isDrawerOpen = false;
    	}else{
    		mDrawerLayout.openDrawer(Gravity.LEFT);	
    		isDrawerOpen = true;
    	}
    }
    public void signOut(){
    	String js = String.format("startMyELT('%s');",SERVER_URL+"/ilrn/accounts/logout.do?isNative=true");
    	this.sendJavascript(js);
    	editor.clear();
    	editor.commit();
          
    }
    public void showNativeLoginScreen() {
    	this.runOnUiThread(new Runnable() {
			public void run() {
				setContentView(R.layout.login_view);
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
   					editor.putString(KEY_USERNAME,usernameStr);
   					editor.putString(KEY_PASSWORD,passwordStr);
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


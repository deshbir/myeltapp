package com.myeltapp;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class MainActivity extends Activity implements LoginAsyncResponse  {
	
    public final static String LOGINURL = "com.myeltapp.LOGINURL";
    public final static String SERVER_URL = "http://myelt3.comprotechnologies.com";
    JSONObject statusJson = null;
    EditText username;
    EditText password;
    String usernameStr;
    String passwordStr;
    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		password = (EditText) findViewById(R.id.password);
		/*login into MyELT application when user clicks on done button on keyboard */
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

	/** Called when the user clicks the Send button */
	public void login(View view) {
		username = (EditText) findViewById(R.id.username);
		password = (EditText) findViewById(R.id.password);
		usernameStr = username.getText().toString();
		passwordStr = password.getText().toString();
		String url = SERVER_URL+"/ilrn/api/logincheck?u="+usernameStr+"&p="+passwordStr;
		new LoginAsyncTask(this).execute(url);
		
	}

	@Override
	public void processFinish(String result) {
		Intent intent = new Intent(this, MyeltApp.class);
		
		try {
			if(result != null){
				statusJson = new JSONObject(result);
				if(((JSONObject)statusJson.get("response")).get("status").equals("success")){
					intent.putExtra(LOGINURL, SERVER_URL + "/ilrn/global/extlogin.do?u="+usernameStr+"&p="+passwordStr);
					startActivity(intent);
				}
				else {
					Context context = getApplicationContext();
					CharSequence text = "Invalid Username or Password";
					int duration = Toast.LENGTH_LONG;
		
					Toast toast = Toast.makeText(context, text, duration);
					toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
					toast.show();
				}
			}
	}catch (JSONException e) {
		e.printStackTrace();
	}
	}
}


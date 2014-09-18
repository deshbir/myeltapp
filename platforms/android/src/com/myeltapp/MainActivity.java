package com.myeltapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class MainActivity extends Activity   {
	
    public final static String USERNAME = "com.myeltapp.USERNAME";
    public final static String PASSWORD = "com.myeltapp.PASSWORD";
    EditText username;
    EditText password;
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
		Intent intent = new Intent(this, MyeltApp.class);
		username = (EditText) findViewById(R.id.username);
		password = (EditText) findViewById(R.id.password);
		String usernameStr = username.getText().toString();
		String passwordStr = password.getText().toString();
		intent.putExtra(USERNAME, usernameStr);
		intent.putExtra(PASSWORD, passwordStr);
		startActivity(intent);
	}

	
}

package com.myeltapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends Activity {
	
    public final static String USERNAME = "com.myeltapp.USERNAME";
    public final static String PASSWORD = "com.myeltapp.PASSWORD";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	
	/** Called when the user clicks the Send button */
	public void login(View view) {
		Intent intent = new Intent(this, MyeltApp.class);
		EditText username = (EditText) findViewById(R.id.username);
		EditText password = (EditText) findViewById(R.id.password);
		String usernameStr = username.getText().toString();
		String passwordStr = password.getText().toString();
		intent.putExtra(USERNAME, usernameStr);
		intent.putExtra(PASSWORD, passwordStr);
		startActivity(intent);
	}
}

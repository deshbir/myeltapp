package com.myeltapp;

import java.io.BufferedInputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.AsyncTask;

public  class LoginAsyncTask extends AsyncTask<String, Void, String>{
	public LoginAsyncResponse delegate;
		public LoginAsyncTask(LoginAsyncResponse delegate){
	        this.delegate=delegate;
	    }
		/**This method makes a call to the api which authenticates the username and password.*/
		@Override
		protected String doInBackground(String... params) {
			URL url;
			HttpURLConnection urlConnection = null;
			String strFileContents = null;
			try{
				url = new URL(params[0]);
				urlConnection = (HttpURLConnection) url.openConnection();
				 BufferedInputStream in = new BufferedInputStream(urlConnection.getInputStream() );
				 byte[] contents = new byte[1024];

				 int bytesRead=0;
				 while( (bytesRead = in.read(contents)) != -1){ 
				    strFileContents = new String(contents, 0, bytesRead);               
				 }
			}catch(Exception e){
				e.printStackTrace();
			}
			finally {
				urlConnection.disconnect();
			}
			return strFileContents;
		}
		
	protected void onPostExecute(String result) {
		delegate.processFinish(result);
     }
}

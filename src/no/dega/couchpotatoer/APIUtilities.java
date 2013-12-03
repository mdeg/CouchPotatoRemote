package no.dega.couchpotatoer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.client.ClientProtocolException;

import android.graphics.drawable.Drawable;
import android.util.Log;

public class APIUtilities {
	public static String makeRequest(String request) {
		if((request == null) || (request.length() <= 0)) {
			Log.e("APIUtilities.makeRequest", "Invalid string passed to makeRequest.");
			return null;
		}
		StringBuilder builder = new StringBuilder();
		try {
			//TODO: make this generic
			URL url = new URL("http://192.168.1.10:5050/api/79fc9813360d4f288305346b54baa7da/" + request);
	
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			InputStream content = urlConnection.getInputStream();
					
			int statusCode = urlConnection.getResponseCode();
			if(statusCode == 200) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(content));
				String line;
				while((line = reader.readLine()) != null) {
					builder.append(line);
				}
			} else {
				Log.e("APICommon.makeRequest", "Status code not 200, is: " + statusCode);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			Log.e("makeRequest", "HTTP protocol error.");
		} catch (IOException e) {
			e.printStackTrace();
			//TODO: pop up some kind of connection-to-internet notice
			Log.e("makeRequest", "Could not connect to resource: API key may be missing or network not connected.");
		} 
		return builder.toString();
	}
	//Load image from network into a Drawable
	public static Drawable loadImageFromNetwork(String url) {
		if((url == null) || (url.length() <= 0)) {
			Log.e("APIUtilities.loadImageFromNetwork", "Invalid string passed to loadImageFromNetwork.");
			return null;
		}
		try {
			InputStream is = (InputStream) new URL(url).getContent();
			return Drawable.createFromStream(is, "src name");
		} catch (Exception e) {
			return null;
		}
	}

}

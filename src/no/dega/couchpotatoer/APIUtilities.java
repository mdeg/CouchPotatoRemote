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

public final class APIUtilities {
	public static String makeRequest(String request) {
		StringBuilder builder = new StringBuilder();
		try {
			//TODO: make this generic
			URL url = new URL("http://192.168.1.10:5050/api/79fc9813360d4f288305346b54baa7da/movie.list");
	
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
			Log.e("makeRequest", "Could not connect to resource: API key may be missing");
		}
		return builder.toString();
	}
	//Load image from network into a Drawable
	public static Drawable loadImageFromNetwork(String url) {
		try {
			InputStream is = (InputStream) new URL(url).getContent();
			return Drawable.createFromStream(is, "src name");
		} catch (Exception e) {
			return null;
		}
	}

}

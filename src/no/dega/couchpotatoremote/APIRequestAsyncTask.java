package no.dega.couchpotatoremote;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.client.ClientProtocolException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by root on 12/6/13.
 */
public class APIRequestAsyncTask<Parameters, Progress, Result> extends AsyncTask<String, Void, String> {

    protected String doInBackground(String... urls) {
        return sendRequest(urls[0]);
    }

    private String sendRequest(String uri) {
        StringBuilder builder = new StringBuilder();
        try {
            URL url = new URL(uri);

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
            Log.e("makeRequest", "Could not connect to resource: API key may be missing or network not connected.");
        }
        return builder.toString();
    }

}

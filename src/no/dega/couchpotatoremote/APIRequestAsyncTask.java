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

/*
*   Wrapper for AsyncTask
    Clients should construct a full URI using APIUtilities.formatQuery() and pass it to this
    Override onPreExecute/onPostExecute to do whatever is necessary
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
                Log.e("APIRequestAsyncTask", "Status code not 200, is: " + statusCode);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            Log.e("APIRequestAsyncTask", "HTTP protocol error.");
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("APIRequestAsyncTask", "Could not connect to resource: API key may be missing or network not connected.");
            return null;
        }
        return builder.toString();
    }
}

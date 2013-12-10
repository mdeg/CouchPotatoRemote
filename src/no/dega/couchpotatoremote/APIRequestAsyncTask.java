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
class APIRequestAsyncTask<Parameters, Progress, Result> extends AsyncTask<String, Void, String> {

    protected String doInBackground(String... urls) {
        return sendRequest(urls[0]);
    }

    private String sendRequest(String uri) {
        InputStream content = null;
        StringBuilder builder = new StringBuilder();

        try {
            URL url = new URL(uri);

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(30000);
            urlConnection.setConnectTimeout(30000);
            content = urlConnection.getInputStream();

            int statusCode = urlConnection.getResponseCode();
            if (statusCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            } else {
                Log.e("APIRequestAsyncTask", "Status code not 200, is: " + statusCode);
                return null;
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            Log.e("APIRequestAsyncTask", "HTTP protocol error.");
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("APIRequestAsyncTask", "Could not connect to resource: API key may be missing or network not connected.");
            return null;
        } finally {
            if (content != null) {
                try {
                    content.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("APIRequestAsyncTask", "Error trying to close the connection");
                }
            }
        }
        return builder.toString();
    }
}

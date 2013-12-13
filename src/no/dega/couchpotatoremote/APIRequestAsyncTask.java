package no.dega.couchpotatoremote;

import android.content.Context;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.conn.ssl.SSLSocketFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/*
*   Custom implementation of AsyncTask for a generic API request to the CouchPotato server
    Clients should construct a full URI using APIUtilities.formatQuery() and pass it to this
 */
class APIRequestAsyncTask<Parameters, Progress, Result> extends AsyncTask<String, Void, String> {
    boolean isHttps = false;

    //Pass in the context to find out if it's https or not
    //Context should NEVER be stored - this causes memory leaks.
    public APIRequestAsyncTask(Context context) {
        super();
        isHttps = PreferenceManager.getDefaultSharedPreferences(context).
                getBoolean("pref_key_https", false);
    }
    @Override
    protected String doInBackground(String... urls) {
        if(urls[0] == null) {
            return null;
        }
        return sendRequest(urls[0]);
    }

    private String sendRequest(String uri) {
        StringBuilder builder = new StringBuilder();
        InputStream content = null;
        try {
            URL url = new URL(uri);

            int statusCode;
            if(!isHttps) {
                //Regular HTTP
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(30000);
                urlConnection.setConnectTimeout(30000);
                content = urlConnection.getInputStream();

                statusCode = urlConnection.getResponseCode();
            } else {
                //HTTPS
                //TODO: fix me up, this is a hack. Need to get the cert from the user somehow.
                SSLContext sslContext = null;
                try {
                    sslContext = SSLContext.getInstance("TLS");
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                try {
                    //WRONG WRONG WRONG
                    sslContext.init(null, new TrustManager[]{
                            new X509TrustManager() {
                                @Override
                                public void checkClientTrusted(X509Certificate[] x509Certificates, String s)
                                        throws CertificateException { }
                                @Override
                                public void checkServerTrusted(X509Certificate[] x509Certificates, String s)
                                        throws CertificateException {}
                                @Override
                                public X509Certificate[] getAcceptedIssuers() {
                                    return new X509Certificate[0];
                                }
                            }
                    }, null);
                } catch (KeyManagementException e) {
                    e.printStackTrace();
                }

                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                //Need to keep this hostname verifier to all - hosts may be on any IP
                urlConnection.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                urlConnection.setSSLSocketFactory(sslContext.getSocketFactory());
                urlConnection.setReadTimeout(30000);
                urlConnection.setConnectTimeout(30000);
                content = urlConnection.getInputStream();

                statusCode = urlConnection.getResponseCode();
            }

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

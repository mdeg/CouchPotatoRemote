package no.dega.couchpotatoremote;

import android.content.Context;
import android.content.SharedPreferences;
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
    private static final String TAG = APIRequestAsyncTask.class.getName();

    boolean isHttps = false;
    boolean trustAll = false;
    //Pass in the context to find out if it's https or not
    //Context should NEVER be stored - this causes memory leaks.
    public APIRequestAsyncTask(Context context) {
        super();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        isHttps = pref.getBoolean("pref_key_https", false);
        trustAll = pref.getBoolean("pref_key_trustall", false);
    }
    @Override
    protected String doInBackground(String... urls) {
        if(urls[0] == null) {
            throw new IllegalArgumentException("No URL passed to doInBackground.");
        }
        return sendRequest(urls[0]);
    }

    /*
    Send a HTTP or HTTPS request to the CouchPotato server
    This will always be an API request, and the return from the server
    will always be a JSON string.
    Return the content returned by the CouchPotato server as a string
    */
    private String sendRequest(String uri) {
        StringBuilder builder = new StringBuilder();
        InputStream content = null;
        int statusCode;

        try {
            URL url = new URL(uri);

            if(!isHttps) {
                //Regular HTTP
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(30000);
                urlConnection.setConnectTimeout(30000);
                content = urlConnection.getInputStream();

                statusCode = urlConnection.getResponseCode();
            } else {
                //HTTPS
                //This will automatically use a user-installed certificate if there's a valid one installed & trustAll is false
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();

                //If the user has specified to trust all certificates. REALLY INSECURE!
                if(trustAll) {
                    try {
                        SSLContext sslContext = SSLContext.getInstance("TLS");
                        //Build our own trust manager that just accepts any certificate
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
                        urlConnection.setSSLSocketFactory(sslContext.getSocketFactory());
                    } catch (KeyManagementException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                }
                //Need to accept any hostnames - servers may be on any IP/domain, including ones not specified on the cert
                urlConnection.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

                urlConnection.setReadTimeout(30000);
                urlConnection.setConnectTimeout(30000);
                content = urlConnection.getInputStream();

                statusCode = urlConnection.getResponseCode();
            }
            //If HTTP returns a success
            if (statusCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                return builder.toString();
            } else {
                Log.e(TAG, "Status code not 200, is: " + statusCode);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            Log.e(TAG, "Protocol error.");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Could not connect to resource: API key may be missing or network not connected.");
        } finally {
            //Clean up and close the connections
            if (content != null) {
                try {
                    content.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Error trying to close the connection");
                }
            }
        }
        return null;
    }
}
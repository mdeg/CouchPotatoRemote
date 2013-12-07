package no.dega.couchpotatoremote;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class APIUtilities {
	/*
	Build a URI for the query based on the users' preferences
    Example output: String="http://192.168.1.1:5050/api/apikey/"
	 */
    public static String constructUriFromPreferences(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String ipPref = sharedPref.getString("pref_key_ip", "IP_FIND_FAIL");
        String apiPref = sharedPref.getString("pref_key_api", "API_FIND_FAIL");
        String portPref = sharedPref.getString("pref_key_port", "PORT_FIND_FAIL");
        StringBuilder uri = new StringBuilder();
        uri = uri.append("http://").append(ipPref).append(":").append(portPref).append("/api/")
                .append(apiPref).append("/");
        return uri.toString();
    }

    /*
    Construct a full and valid URI for the clients' request based on the users' settings.
    Example input: String="movie.list"
	Example output: String="http://192.168.1.1:5050/api/somekey/movie.list"
     */
    public static String formatRequest(String request, Context context) {
        if((request == null) || (request.length() <= 0)) {
            Log.e("APIUtilities.formatRequest", "Invalid string passed to formatRequest.");
            return null;
        }
        //Make sure they're connected to a network
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if(ni == null) {
            Toast.makeText(context, "Could not connect: no network is enabled.", Toast.LENGTH_LONG).show();
            Log.d("APIUtilities.formatRequest", "No connection to any network.");
            return null;
        }
   //     boolean connectedToInternet = ni.isConnected();
        //Replace any spaces that might be in the string with %20's
        for(int i = 0; i < request.length(); i++) {
            if(request.charAt(i) == ' ') {
                request = request.substring(0, i) + "%20" + request.substring(i + 1, request.length());
            }
        }

        //Construct URI based on user settings and append the request
        return constructUriFromPreferences(context) + request;
    }
}

package no.dega.couchpotatoremote;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

//Small utilities class for constructing valid URIs
class APIUtilities {
    private static final String TAG = APIUtilities.class.getName();

    /*
    Construct a full and valid URI for the clients' request based on the users' settings.
    Also do a little bit of checking to make sure the request will be okay.
    Example input: String="movie.list"
	Example output: String="http://192.168.1.1:5050/api/somekey/movie.list"
     */
    protected static String formatRequest(String request, Context context) throws IllegalArgumentException {
        if ((request == null) || (request.length() <= 0)) {
            //Log.e(TAG, "Invalid string passed to formatRequest.");
            throw new IllegalArgumentException("No request passed to formatRequest.");
        }
        //Make sure they're connected to a network
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            //No networks are enabled
            Toast.makeText(context, "Could not connect: no network is enabled.",
                    Toast.LENGTH_LONG).show();
            //Log.e(TAG, "No connection to any network.");
            return "";
        }
        //Construct URI based on user settings and append the request
        return constructUriFromPreferences(context) + request;
    }

    /*
    Build a URI for the query based on the users' preferences
    Example output: String="http://192.168.1.1:5050/api/apikey/"
     */
    private static String constructUriFromPreferences(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String ipPref = sharedPref.getString("pref_key_ip", "IP_FIND_FAIL");
        String apiPref = sharedPref.getString("pref_key_api", "API_FIND_FAIL");
        String portPref = sharedPref.getString("pref_key_port", "PORT_FIND_FAIL");
        //HTTP or HTTPS
        String protocol = sharedPref.getBoolean("pref_key_https", false) ? "https":"http" ;
        StringBuilder uri = new StringBuilder();

        uri = uri.append(protocol).append("://").append(ipPref).append(":").append(portPref).
                append("/api/").append(apiPref).append("/");
        return uri.toString();
    }
}

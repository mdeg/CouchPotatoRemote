package no.dega.couchpotatoremote;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class APIUtilities {
	
	/*
	 *	Add movie by request "movie.add?title=X&identifier=Y"
	 *	success=false means an invalid title was given.
	 *	Unfortunately, the API gives no feedback if the title is valid but identifier is invalid.
	 *	These will just fail silently.
	 */
    /*
    //TODO: add movie when movie is already there
	public static boolean addMovie(Movie movie, Context context) {		
		StringBuilder uri = new StringBuilder("movie.add");
		uri = uri.append("?title=").append(movie.getTitle()).append("&identifier=").
				append(movie.getImdbId());
		try {
			JSONObject response = new JSONObject(APIUtilities.makeRequest(uri.toString(), context));
			if(!response.getBoolean("success")) {
				return false;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return true;
	}
	*/
	/*	Search for a movie using CouchPotato's search - format "movie.search?q=NAME"
	 *	Returns a list of movies that match or loosely match the given name.
	 *	This is used when adding a new movie.
	 * 
	 * 
	 */
	/*
	//TODO: wait for result
	public static ArrayList<Movie> searchForMovie(String name, Context context) {
		String query = "movie.search?q=" + name;
		ArrayList<Movie> searchResults = new ArrayList<Movie>();
        String resp = APIUtilities.makeRequest(query, context);
        if((resp == null) || (resp.length() <= 0)) {
            Log.e("APIUtilities.searchForMovie", "searchForMovie received invalid response from makeRequest");
            return null;
        }
        try {



			JSONObject response = new JSONObject(resp);

			//If it fails or is empty we will return an empty list.
			if(!response.getBoolean("success")) {
				Log.e("searchForMovie", "Search for " + name + " failed.");
			}
			JSONArray movies = response.getJSONArray("movies");
			
			for(int i = 0; i < movies.length(); i++) {
				JSONObject movie = movies.getJSONObject(i);
				
				String title = movie.getJSONArray("titles").getString(0);
				//String poster = movie.getJSONObject("images").getJSONArray("poster").getString(0);
                //Year might be null if CouchPotato has no year for it in its database.
                String year;
                if(!movie.isNull("year")) {
                    year = movie.getString("year");
                } else {
                    year = "";
                }
				String imdbId = movie.getString("imdb");

				Movie result = new Movie(title, year, imdbId);
				searchResults.add(result);
			}
		} catch(JSONException e) {
			e.printStackTrace();
		}
		return searchResults;

	}*/

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

    public static String formatRequest(String request, Context context) {
        if((request == null) || (request.length() <= 0)) {
            Log.e("APIUtilities.makeRequest", "Invalid string passed to makeRequest.");
            return null;
        }
        //TODO: grab from local network or internet depending on connection
        //Make sure they're connected to a network
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if(ni == null) {
            Toast.makeText(context, "Could not connect: no network is enabled.", Toast.LENGTH_LONG).show();
            Log.d("APIUtilities.makeRequest", "No connection to any network.");
            return null;
        }
        boolean connectedToInternet = ni.isConnected();

        //Usually this means the user has not entered in their settings properly, or they're not connected to the network
        //Could also mean the server itself isn't responding to connections.
       // Toast.makeText(context, "Could not connect to your CouchPotato server. Check if your Settings are correct.", Toast.LENGTH_LONG).show();

        //Construct URI based on user settings

        return constructUriFromPreferences(context) + request;
    }

    public static String sendRequest(String uri) {
        StringBuilder builder = new StringBuilder();
        try {
            URL url = new URL(uri.toString());

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
/*

	/*
	*   Make a request to the API
	*   Returns String of result (which will probably be a JSON string)
    *
	public static String makeRequest(String request, Context context) {
		if((request == null) || (request.length() <= 0)) {
			Log.e("APIUtilities.makeRequest", "Invalid string passed to makeRequest.");
			return null;
		}
        //TODO: grab from local network or internet depending on connection
        //Make sure they're connected to a network
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if(ni == null) {
            Toast.makeText(context, "Could not connect: no network is enabled.", Toast.LENGTH_LONG).show();
            Log.d("APIUtilities.makeRequest", "No connection to any network.");
            return null;
        }
        boolean connectedToInternet = ni.isConnected();

		StringBuilder builder = new StringBuilder();
		try {
			//Construct URI based on user settings
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
			String ipPref = sharedPref.getString("pref_key_ip", "IP_FIND_FAIL");
			String apiPref = sharedPref.getString("pref_key_api", "API_FIND_FAIL");
			String portPref = sharedPref.getString("pref_key_port", "PORT_FIND_FAIL");
			StringBuilder uri = new StringBuilder();
			uri = uri.append("http://").append(ipPref).append(":").append(portPref).append("/api/")
					.append(apiPref).append("/").append(request);
			
			URL url = new URL(uri.toString());
	
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
		    //Usually this means the user has not entered in their settings properly, or they're not connected to the network
            //Could also mean the server itself isn't responding to connections.
            Toast.makeText(context, "Could not connect to your CouchPotato server. Check if your Settings are correct.", Toast.LENGTH_LONG).show();
			e.printStackTrace();
			Log.e("makeRequest", "Could not connect to resource: API key may be missing or network not connected.");
		}
		return builder.toString();
	}
*
    /*
	*   Status: active = true = on Wanted list
	*   					false = on Manage list
	*   Returns a SparseArray of Movie's built from the list (which can be empty if there are no movies)
    *
    //TODO: what if movie list is empty
	public static SparseArray<Movie> parseMovieList(String resp) {
		//Construct the right query for the type of movie we're looking for

		
		if((resp == null) || (resp.length() <= 0)) {
			return null;
		}
		//TODO: init this based on number of objects in movie list?		
		SparseArray<Movie> movies = new SparseArray<Movie>();

		try {
			JSONObject response = new JSONObject(resp);

		//	Log.d(this.toString(), "String Contents: " + response.toString());
			
			//Make sure our request is okay and there's movies to process
			if(!response.getBoolean("success")) {
				return null;
			}
			if(response.getBoolean("empty")) {
				return movies;
			}
			
			JSONArray jsonMoviesList = response.getJSONArray("movies");
			
			//Create a movie object from every movie in the list
			for(int i = 0; i < jsonMoviesList.length(); i++) {
				
				int libraryId = jsonMoviesList.getJSONObject(i).getInt("library_id");
				JSONObject info = jsonMoviesList.getJSONObject(i).getJSONObject("library").
                        getJSONObject("info");
			//	Log.d(this.toString(), "Movie JSON String: " + info.toString());					
				
				String title = info.getJSONArray("titles").getString(0);
				String tagline = info.getString("tagline");
				String plot = info.getString("plot");

                String year;
                if(!info.isNull("year")) {
                    year = info.getString("year");
                } else {
                    year = "";
                }

				String fullPath = jsonMoviesList.getJSONObject(i).getJSONObject("library").
                        getJSONArray("files").getJSONObject(0).getString("path");
				String posterFileName = "";

				//Grab the file name from the string
				for(int k = fullPath.length() - 1; k > 0; k--) {
                    // Need to handle both / and \ as separators (Windows/Unix)
					if((fullPath.charAt(k) == '\\') || (fullPath.charAt(k) == '/') ) {
						posterFileName = fullPath.substring(k + 1, fullPath.length());
						break;
					}
				}
				Log.d("posterFileName", posterFileName);

				JSONArray jsonActors = info.getJSONArray("actors");
				String[] actors = new String[jsonActors.length()];
				if(jsonActors != null) {
					for(int j = 0; j < jsonActors.length(); j++) {
						actors[j] = jsonActors.get(j).toString();
					}
				}
				
				JSONArray jsonDirectors = info.getJSONArray("directors");
				String[] directors = new String[jsonDirectors.length()];				
				if(jsonDirectors != null) {
					for(int j = 0; j < jsonDirectors.length(); j++) {
						directors[j] = jsonDirectors.get(j).toString();
					}
				}
	
	//			Movie newMovie = new Movie(libraryId, title, tagline, posterUri.toString(),
	//					plot, year, actors, directors);
                Movie newMovie = new Movie(libraryId, title, tagline, "",
                        plot, year, actors, directors);
				movies.put(libraryId, newMovie);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return movies;
	}
*/

}

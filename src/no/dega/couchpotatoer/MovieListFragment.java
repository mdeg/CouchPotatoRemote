package no.dega.couchpotatoer;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

//Displays a list of Movies
public class MovieListFragment extends ListFragment {
	//Indexed by CouchPotato's own libraryId
	SparseArray<Movie> movies;
	//TODO: change this to not re-request on recreation
	public MovieListFragment() {

	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		//Copy the movies into an array
		//TODO: put them in alphabetical order
		ArrayList<Movie> movieList = new ArrayList<Movie>();
		for(int i = 0; i < this.movies.size(); i++) {
			//int key = this.movies.keyAt(i);
			movieList.add(this.movies.get(this.movies.keyAt(i)));
		}
		//Use custom movielist adapter to create the list
		final MovieListAdapter<Movie> adapter = new MovieListAdapter<Movie>(
				getActivity(), R.layout.adapter_movielist, movieList);
		setListAdapter(adapter);
	}
	
	//If a user clicks on a movie, take them to the appropriate movie display
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.d("List Position", ""+position);
		Movie movie = (Movie) getListAdapter().getItem(position);
		Log.d("toString:", movie.toString());
		
		Intent intent = new Intent(getActivity(), MovieViewActivity.class);
		//TODO: change me when project name changes
		intent.putExtra("no.dega.couchpotatoer.Movie", movie);
		startActivity(intent);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_movie_list, container, false);
		this.movies = new SparseArray<Movie>();
		//TODO: move this to oncreateview
		parseWantedList(APIUtilities.makeRequest("DUD"));

		return rootView;
	}

	//TODO: put me in the right activity
	//TODO: work out what this should return (null?)
	public String parseWantedList(String resp) {
		if((resp == null) || (resp.length() <= 0)) {
			return null;
		}
		try {
			JSONObject response = new JSONObject(resp);

		//	Log.d(this.toString(), "String Contents: " + response.toString());
			
			//Make sure our request is okay and there's movies to process
			if(response.getBoolean("success") == false) {
				return getResources().getString(R.string.movies_list_fail);
			}
			if(response.getBoolean("empty") == true) {
				return getResources().getString(R.string.movies_list_empty);
			}
			
			JSONArray jsonMoviesList = response.getJSONArray("movies");
			
			//Go through every movie and collect the useful info for them
			for(int i = 0; i < jsonMoviesList.length(); i++) {
				
				int libraryId = jsonMoviesList.getJSONObject(i).getInt("library_id");
				JSONObject info = jsonMoviesList.getJSONObject(i).getJSONObject("library").getJSONObject("info");
			//	Log.d(this.toString(), "Movie JSON String: " + info.toString());					
				
				String title = info.getJSONArray("titles").getString(0);
				String tagline = info.getString("tagline");
				String plot = info.getString("plot");
				int year = info.getInt("year");
				String fullPath = jsonMoviesList.getJSONObject(i).getJSONObject("library").getJSONArray("files").getJSONObject(0).getString("path");
				String posterFileName = "";
				
				//TODO: cache, check if already cached
				//TODO: backup grab from internet
				
				//Grab the file name from the string
				for(int k = fullPath.length() - 1; k > 0; k--) {
					if(fullPath.charAt(k) == '\\' ) {
						posterFileName = fullPath.substring(k + 1, fullPath.length());
						break;
					}
				}
				Log.d("posterFileName", posterFileName);
				
				/*TODO: fix this to actually work for the settings
				SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
				final String ipPref = sharedPref.getString("pref_key_ip", "IP_FIND_FAIL");
				final String apiPref = sharedPref.getString("pref_key_api", "API_FIND_FAIL");
				final String portPref = sharedPref.getString("pref_key_port", "PORT_FIND_FAIL");
				*/
				String ipPref = "192.168.1.10";
				String portPref = "5050";
				String apiPref = "79fc9813360d4f288305346b54baa7da";
						
				StringBuilder temp = new StringBuilder();
				temp = temp.append("http://").append(ipPref).append(":").append(portPref).append("/api/")
						.append(apiPref).append("/file.cache/").append(posterFileName);
				
				Log.d("loadImageFromNetwork", temp.toString());
				Drawable poster = APIUtilities.loadImageFromNetwork(temp.toString());
//				Drawable poster = APIUtilities.loadImageFromNetwork("http://192.168.1.10:5050/api/79fc9813360d4f288305346b54baa7da/file.cache/5c2304c7aacf59d2df05c62e5a07d4ea.jpg");
				
				//TODO: handle multiple titles
		
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
				
				Movie newMovie = new Movie(libraryId, title, tagline, year,
						plot, actors, directors, poster);
				this.movies.put(libraryId, newMovie);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

}
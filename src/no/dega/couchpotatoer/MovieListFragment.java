package no.dega.couchpotatoer;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
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
		parseWantedList(APIUtilities.makeRequest("movie.list"));

		return rootView;
	}

	//TODO: work out what this should return (void?)
	private String parseWantedList(String resp) {
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
				String ipPref = sharedPref.getString("pref_key_ip", "IP_FIND_FAIL");
				String apiPref = sharedPref.getString("pref_key_api", "API_FIND_FAIL");
				String portPref = sharedPref.getString("pref_key_port", "PORT_FIND_FAIL");
				*/
				String ipPref = "192.168.1.10";
				String portPref = "5050";
				String apiPref = "79fc9813360d4f288305346b54baa7da";
						
				//Construct the full URI for the poster
				//TODO: this would rely on home connections' upload speed - grab from internet instead?
				StringBuilder posterUri = new StringBuilder();
				posterUri = posterUri.append("http://").append(ipPref).append(":").append(portPref).append("/api/")
						.append(apiPref).append("/file.cache/").append(posterFileName);
				
				//Log.d("loadImageFromNetwork", posterUri.toString());
				//Bitmap poster = imageLoader.loadImage(posterUri.toString());
				//				Drawable poster = APIUtilities.loadImageFromNetwork(temp.toString());
				
				//TODO: handle multiple titles?
		
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
	
				Movie newMovie = new Movie(libraryId, title, tagline, posterUri.toString(),
						plot, year, actors, directors);
				this.movies.put(libraryId, newMovie);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

}
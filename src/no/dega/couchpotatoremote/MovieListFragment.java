package no.dega.couchpotatoremote;

import java.util.ArrayList;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

//Displays a list of Movies
public class MovieListFragment extends ListFragment {
	//Indexed by CouchPotato's own libraryId
	SparseArray<Movie> movies;
	//True if this is the Wanted list; false if it's the Manage list
	boolean isWanted;

	public MovieListFragment() {

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_movie_list, container, false);

        String query = "movie.list?status=";
        if(getArguments().getBoolean("isWanted")) {
            query = query + "active";
        } else {
            query = query + "done";
        }
        String request = APIUtilities.formatRequest(query, getActivity());
        new DownloadMovieListTask().execute(request);

		return rootView;
	}
	//If a user clicks on a movie, take them to the appropriate movie display
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.d("List Position", ""+position);
		Movie movie = (Movie) getListAdapter().getItem(position);
		Log.d("toString:", movie.toString());
		
		Intent intent = new Intent(getActivity(), MovieViewActivity.class);
		intent.putExtra("no.dega.couchpotatoremote.Movie", movie);
		startActivity(intent);
	}
    /*
     *   Status: active = true = on Wanted list
     *   					false = on Manage list
     *   Returns a SparseArray of Movie's built from the list (which can be empty if there are no movies)
     */
    //TODO: what if movie list is empty
    private SparseArray<Movie> parseMovieList(String resp) {
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
                String posterUri = info.getJSONObject("images").getJSONArray("poster").getString(0);

                String year;
                if(!info.isNull("year")) {
                    year = info.getString("year");
                } else {
                    year = "";
                }

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

                Movie newMovie = new Movie(libraryId, title, tagline, posterUri, plot, year, actors,
                        directors);
                movies.put(libraryId, newMovie);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return movies;
    }


    private class DownloadMovieListTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            return APIUtilities.sendRequest(urls[0]);
        }

        @Override
        protected void onPostExecute(String result) {

            //TODO: put them in alphabetical order
            if((movies = parseMovieList(result)) == null) {
                movies = new SparseArray<Movie>();
            }
            ArrayList<Movie> movieList = new ArrayList<Movie>();
            for(int i = 0; i < movies.size(); i++) {
                //int key = this.movies.keyAt(i);
                movieList.add(movies.get(movies.keyAt(i)));
            }

            //Use custom movielist adapter to create the list
            final MovieListAdapter<Movie> adapter = new MovieListAdapter<Movie>(
                    getActivity(), R.layout.adapter_movielist, movieList);
            setListAdapter(adapter);
        }
    }
}
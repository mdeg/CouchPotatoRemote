package no.dega.couchpotatoremote;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SearchResultsFragment extends ListFragment {
	ArrayList<Movie> searchResults;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_search_results, container, false);

        String query = "movie.search?q=" + getArguments().getString("NameToSearch");

		String request = APIUtilities.formatRequest(query, getActivity());
        new SearchForMovieTask().execute(request);

		return rootView;
	}
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {

		Movie movie = (Movie) getListAdapter().getItem(position);

        StringBuilder uri = new StringBuilder("movie.add?title=");
        uri = uri.append(movie.getTitle()).append("&identifier=").append(movie.getImdbId());

        String request = APIUtilities.formatRequest(uri.toString(), getActivity());
        new AddMovieTask().execute(request);

        Toast.makeText(getActivity(), movie.getTitle(), Toast.LENGTH_LONG).show();
	}

    private ArrayList<Movie> parseMovieSearch(String resp) {

        ArrayList<Movie> searchResults = new ArrayList<Movie>();
        if((resp == null) || (resp.length() <= 0)) {
            Log.e("APIUtilities.searchForMovie", "searchForMovie received invalid response from makeRequest");
            return null;
        }
        try {
            JSONObject response = new JSONObject(resp);

            //If it fails or is empty we will return an empty list.
            if(!response.getBoolean("success")) {
                Log.e("searchForMovie", "Search failed: API returns success=false");
            }
            JSONArray movies = response.getJSONArray("movies");

            for(int i = 0; i < movies.length(); i++) {
                JSONObject movie = movies.getJSONObject(i);

                String title;
                if(!movie.isNull("titles")) {
                    title = movie.getJSONArray("titles").getString(0);
                } else {
                    title = "";
                }

                String year;
                if(!movie.isNull("year")) {
                    year = movie.getString("year");
                } else {
                    year = "";
                }

                String dbId;
                if(movie.has("imdb") && !movie.isNull("imdb")) {
                    dbId = movie.getString("imdb");
                } else if (movie.has("tmdb_id") && !movie.isNull("tmdb_id")) {
                    dbId = movie.getString("tmdb_id");
                } else {
                    Log.e("parseMovieSearch", "Movie returned from the search with no database ID.");
                    dbId = "";
                }

                Movie result = new Movie(title, year, dbId);
                searchResults.add(result);
            }
        } catch(JSONException e) {
            e.printStackTrace();
        }
        return searchResults;
    }

    private boolean parseAddMovieResponse(String resp) {
        if((resp == null) || (resp.length() <= 0)) {
            Log.e("SearchResultsFragment:parseAddMovieResponse", "Invalid response from addMovie");
            return false;
        }
        try {
            JSONObject response = new JSONObject(resp);
            if(!response.getBoolean("success")) {
                return false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return true;
    }

    private class SearchForMovieTask extends APIRequestAsyncTask<String, Void, String> {

        @Override
        protected void onPostExecute(String result) {
            ArrayList<Movie> searchResults = parseMovieSearch(result);
            if(searchResults != null) {
                final SearchResultsAdapter<Movie> adapter = new SearchResultsAdapter<Movie>(
                        getActivity(), R.layout.adapter_movielist, searchResults);
                setListAdapter(adapter);
            } else {
                Log.e("SearchResultsFragment", "Could not create list: searchResults is null.");
            }
        }

    }

    //TODO: some kinda slide out thing for adding movies
    private class AddMovieTask extends APIRequestAsyncTask<String, Void, String> {

        @Override
        protected void onPostExecute(String result) {
            //TODO: fill this out with UI responsiveness
            parseAddMovieResponse(result);
        }
    }
}
package no.dega.couchpotatoremote;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SearchResultsFragment extends ListFragment {

	private ProgressDialog progressDialog;
    private TextView noSearchResults;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //"Searching..." spinner wheel
        this.progressDialog = new ProgressDialog(getActivity());
        this.progressDialog.setIndeterminate(true);
        this.progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        this.progressDialog.setMessage("Searching...");
        this.progressDialog.setCancelable(true);
        //There's a listener for cancelling this dialog (and the search task with it) in SearchForMovieTask

        //A search is being made, so get rid of 'Search for a movie above'
        TextView empty = (TextView) getActivity().findViewById(R.id.searchlist_empty);
        if(empty != null) {
           empty.setVisibility(View.GONE);
        }
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_search_results, container, false);

		return rootView;
	}

    @Override
    public void onStart() {
        super.onStart();
        this.noSearchResults = (TextView) getListView().getEmptyView();
        //Construct and submit our query
        String query = "movie.search?q=" + getArguments().getString("NameToSearch");
        String request = APIUtilities.formatRequest(query, getActivity());
        new SearchForMovieTask().execute(request);
    }
    @Override
    public void onPause() {
        super.onPause();
        this.progressDialog.dismiss();
    }

    //Clicking on a list item should add the movie to CouchPotato
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Movie movie = (Movie) getListAdapter().getItem(position);

        StringBuilder uri = new StringBuilder("movie.add?title=");
        uri = uri.append(movie.getTitle()).append("&identifier=").append(movie.getImdbId());

        String request = APIUtilities.formatRequest(uri.toString(), getActivity());
        new AddMovieTask().execute(request);

        Toast.makeText(getActivity(), movie.getTitle() + " added to your Wanted list.",
                Toast.LENGTH_LONG).show();
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

                String title = !movie.isNull("titles")? movie.getJSONArray("titles").getString(0) : "";
                String year = !movie.isNull("year")? movie.getString("year") : "";

                //All movies should have an imdb or, failing that, tmdb ID. We need this to add it to CP.
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
        protected void onPreExecute() {
            //Set up a listener to cancel this task if the user presses back while the dialog is up
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    SearchForMovieTask.this.cancel(true);
                }
            });
            //Show the dialog and temporarily hide the no search results TV until this is done
            noSearchResults.setVisibility(View.GONE);
            progressDialog.show();
        }
        //Build and display the list of search results and get rid of the spinner wheel
        @Override
        protected void onPostExecute(String result) {
            ArrayList<Movie> searchResults = parseMovieSearch(result);

            if(searchResults != null) {
                if(searchResults.size() <= 0) {
                    noSearchResults.setVisibility(View.VISIBLE);
                } else {
                    final SearchResultsAdapter<Movie> adapter = new SearchResultsAdapter<Movie>(
                            getActivity(), R.layout.adapter_movielist, searchResults);
                    setListAdapter(adapter);
                }
                progressDialog.dismiss();
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
package no.dega.couchpotatoremote;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SearchResultsFragment extends ListFragment {
    private static final String TAG = SearchResultsFragment.class.getName();

    private ProgressDialog progressDialog = null;
    private TextView noSearchResults = null;
    private SearchForMovieTask task = null;
    private String request = null;
    private boolean isRunning = false;
    private boolean isCompleted = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Construct and submit our query
        String query = "movie.search?q=" + Uri.encode(getArguments().getString("nameToSearch"));
        request = APIUtilities.formatRequest(query, getActivity().getApplicationContext());
        task = new SearchForMovieTask(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search_results, container, false);
        setRetainInstance(true);
        setUpUI();

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        //Show the dialog and temporarily hide the no search results TV until this is done
        noSearchResults = (TextView) getListView().getEmptyView();
        noSearchResults.setVisibility(View.GONE);
        if (!isCompleted) {
            progressDialog.show();
        }
        //Prevent re-running the task on config change (ie screen rotation)
        if (!isRunning) {
            task.execute(request);
            isRunning = true;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //Clear the progress dialog (otherwise it'll leak)
        progressDialog.dismiss();
    }

    //Clicking on a list item should add the movie to CouchPotato
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Movie movie = (Movie) getListAdapter().getItem(position);

        StringBuilder uri = new StringBuilder("movie.add?title=");
        //Make sure we encode the title in a URL-recognisable format
        uri = uri.append(Uri.encode(movie.getTitle())).append("&identifier=").append(movie.getImdbId());

        String request = APIUtilities.formatRequest(uri.toString(), getActivity().getApplicationContext());

        new AddMovieTask(getActivity(), movie.getTitle()).execute(request);

        //Collapsing list animation
        Animation collapseList = AnimationUtils.loadAnimation(getActivity(), R.anim.collapse_search_results);
        getListView().startAnimation(collapseList);

    }

    //Set up the progress dialog and hide the 'Search for a movie above' text
    //Called when created or when it's resumed
    private void setUpUI() {
        //"Searching..." progress dialog
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setIndeterminate(true);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Searching...");
        progressDialog.setCancelable(true);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                task.cancel(true);
            }
        });

        //A search is being made, so get rid of 'Search for a movie above'
        TextView empty = (TextView) getActivity().findViewById(R.id.searchlist_empty);
        if (empty != null) {
            empty.setVisibility(View.GONE);
        }
    }

    //Asynchronously query the API for results for the users' search
    //We will then populate the list based on those results
    private class SearchForMovieTask extends APIRequestAsyncTask<String, Void, String> {
        public SearchForMovieTask(Context context) {
            super(context);
        }

        //Build and display the list of search results and get rid of the spinny wheel
        @Override
        protected void onPostExecute(String result) {
            isCompleted = true;

            ArrayList<Movie> searchResults = parseMovieSearch(result);
            if (searchResults != null) {
                if (searchResults.size() <= 0) {
                    //No results for the users' search
                    noSearchResults.setVisibility(View.VISIBLE);
                } else {
                    final SearchResultsAdapter<Movie> adapter = new SearchResultsAdapter<Movie>(
                            getActivity(), R.layout.adapter_movielist, searchResults);
                    setListAdapter(adapter);
                }
                progressDialog.dismiss();
            } else {
                Log.e(TAG, "Could not create list: searchResults is null.");
            }
        }

        //Parse the response from the search
        private ArrayList<Movie> parseMovieSearch(String resp) {
            ArrayList<Movie> searchResults = new ArrayList<Movie>();
            if ((resp == null) || (resp.length() <= 0)) {
                Log.e(TAG, "searchForMovie received invalid response from makeRequest");
                return null;
            }
            try {
                JSONObject response = new JSONObject(resp);

                //If it fails or is empty we will return an empty list.
                if (!response.getBoolean("success")) {
                    Log.e(TAG, "Search failed: API returns success=false");
                }

                JSONArray movies = response.getJSONArray("movies");
                for (int i = 0; i < movies.length(); i++) {
                    JSONObject movie = movies.getJSONObject(i);

                    String title = !movie.isNull("titles") ? movie.getJSONArray("titles").getString(0) : "";
                    String year = !movie.isNull("year") ? movie.getString("year") : "";

                    //All movies should have an imdb or, failing that, tmdb ID. We need this to add it to CP.
                    String dbId;
                    if (movie.has("imdb") && !movie.isNull("imdb")) {
                        dbId = movie.getString("imdb");
                    } else if (movie.has("tmdb_id") && !movie.isNull("tmdb_id")) {
                        dbId = movie.getString("tmdb_id");
                    } else {
                        Log.e(TAG, "Movie returned from the search with no database ID.");
                        dbId = "";
                    }

                    Movie result = new Movie(title, year, dbId);
                    searchResults.add(result);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return searchResults;
        }
    }

    //Asynchronously ask the CP server to add the specified movie
    //If this breaks, it's probably because the movie metadata is invalid (especially imdbId)
    private class AddMovieTask extends APIRequestAsyncTask<String, Void, String> {
        private final String title;
        public AddMovieTask(Context context, String title) {
            super(context);
            this.title = title;
        }

        @Override
        protected void onPostExecute(String result) {
            if(parseAddMovieResponse(result)) {
                Toast.makeText(getActivity(), title + " added to your Wanted list.",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), "Failed to add " + title + ".",
                        Toast.LENGTH_LONG).show();
            }
            //Destroy the fragment
            getActivity().getSupportFragmentManager().beginTransaction()
                    .remove(SearchResultsFragment.this).commit();
        }
        //The CP server will respond with a string {success:true} if successful
        private boolean parseAddMovieResponse(String result) {
            if ((result == null) || (result.length() <= 0)) {
                Log.e(TAG, "Invalid response from doInBackground");
                return false;
            }
            try {
                JSONObject response = new JSONObject(result);
                if (!response.getBoolean("success")) {
                    return false;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "JSONException in parseAddMovieResponse");
            }
            return true;
        }
    }
}
package no.dega.couchpotatoremote;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SearchResultsFragment extends ListFragment {

    private ProgressDialog progressDialog;
    private TextView noSearchResults;
    private SearchForMovieTask task;
    private String request;
    private boolean isRunning = false;
    private boolean isCompleted = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Construct and submit our query
        String query = "movie.search?q=" + Uri.encode(getArguments().getString("NameToSearch"));
        request = APIUtilities.formatRequest(query, getActivity().getApplicationContext());
        task = new SearchForMovieTask();
    }

    //Set up the progress dialog and hide the 'Search for a movie above' text
    //Called when created or when it's resumed
    private void setUpUI() {
        //"Searching..." spinner wheel
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
        progressDialog.dismiss();
    }

    //Clicking on a list item should add the movie to CouchPotato
    //TODO: make selections a different colour on long presses
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        @SuppressWarnings("ConstantConditions") Movie movie = (Movie) getListAdapter().getItem(position);

        StringBuilder uri = new StringBuilder("movie.add?title=");
        //Make sure we encode the title in a URL-recognisable format
        uri = uri.append(Uri.encode(movie.getTitle())).append("&identifier=").append(movie.getImdbId());

        @SuppressWarnings("ConstantConditions") String request = APIUtilities.formatRequest(uri.toString(), getActivity().getApplicationContext());

        new AddMovieTask().execute(request);
        //Have to use the application context instead of the activity context so styles aren't applied to the toast
        Toast.makeText(getActivity().getApplicationContext(), movie.getTitle() + " added to your Wanted list.",
                Toast.LENGTH_LONG).show();
        //Collapsing list animation - destroys this fragment when it's finished
        Animation collapseList = AnimationUtils.loadAnimation(getActivity(), R.anim.collapse_search_results);
        collapseList.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                killThisFragment();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        getListView().startAnimation(collapseList);

    }

    //Kill this fragment
    private void killThisFragment() {
        getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }

    private ArrayList<Movie> parseMovieSearch(String resp) {

        ArrayList<Movie> searchResults = new ArrayList<Movie>();
        if ((resp == null) || (resp.length() <= 0)) {
            Log.e("APIUtilities.searchForMovie", "searchForMovie received invalid response from makeRequest");
            return null;
        }
        try {
            JSONObject response = new JSONObject(resp);

            //If it fails or is empty we will return an empty list.
            if (!response.getBoolean("success")) {
                Log.e("searchForMovie", "Search failed: API returns success=false");
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
                    Log.e("parseMovieSearch", "Movie returned from the search with no database ID.");
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

    private boolean parseAddMovieResponse(String resp) {
        if ((resp == null) || (resp.length() <= 0)) {
            Log.e("SearchResultsFragment:parseAddMovieResponse", "Invalid response from addMovie");
            return false;
        }
        try {
            JSONObject response = new JSONObject(resp);
            if (!response.getBoolean("success")) {
                return false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return true;
    }

    private class SearchForMovieTask extends APIRequestAsyncTask<String, Void, String> {
        //Build and display the list of search results and get rid of the spinny wheel
        @Override
        protected void onPostExecute(String result) {
            isCompleted = true;

            ArrayList<Movie> searchResults = parseMovieSearch(result);

            if (searchResults != null) {
                if (searchResults.size() <= 0) {
                    noSearchResults.setVisibility(View.VISIBLE);
                } else {
                    final SearchResultsAdapter<Movie> adapter = new SearchResultsAdapter<Movie>(
                            getActivity(), R.layout.adapter_movielist, searchResults);
                    setListAdapter(adapter);
                    ListView list = (ListView) getView().findViewById(android.R.id.list);
                    list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                        @Override
                        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                            view.setBackgroundColor(Color.parseColor("#222222"));
                            return false;
                        }
                    });
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
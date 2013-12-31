package no.dega.couchpotatoremote;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

//Displays a list of Movies
public class MovieListFragment extends ListFragment {
    private static final String TAG = MovieListFragment.class.getName();

    private TextView noMovies = null;
    private DownloadMovieListTask task = null;
    private String request = null;
    private boolean hasRun = false;
    ArrayList<Movie> movies = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        task = new DownloadMovieListTask(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_list, container, false);
        //We want to keep this fragment if the user changes orientation
        setRetainInstance(true);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        //Pause image downloading/displaying while user is scrolling the list
        //Causes ugly image pop-in but it's required to make scrolling feel smooth
        PauseOnScrollListener listener = new PauseOnScrollListener(ImageLoader.getInstance(),
                true, true);
        getListView().setOnScrollListener(listener);
        String query = "movie.list?status=";
        query = getArguments().getBoolean("isWanted") ? query + "active" : query + "done";
        request = APIUtilities.formatRequest(query, getActivity().getApplicationContext());
        //Don't want to restart the download if the user changes orientation
        if (!hasRun) {
            //Movies can be either on the Wanted list or on the Manage list
            noMovies = (TextView) getListView().getEmptyView();
            noMovies.setVisibility(View.GONE);
            hasRun = true;
            task.execute(request);
        }
    }

    //If a user clicks on a movie, take them to the appropriate movie display
    @Override
    public void onListItemClick(ListView l, View v, int pos, long id) {
        Intent intent = new Intent(getActivity(), MovieViewActivity.class);
        intent.putExtra("movies", movies);
        intent.putExtra("position", pos);
        startActivity(intent);
    }

    //User has hit the refresh button and we need to redownload the list
    public void refresh() {
        //Shouldn't refresh unless the list has already been populated (or tried to be)
        //Ensure we don't make duplicate tasks if a user presses refresh multiple times
        if(task == null) {
            task = new DownloadMovieListTask(getActivity());
            task.execute(request);
            }
    }

    /*
    Download a list of movies from the CouchPotato server
    The fragment itself handles whether it's downloading the wanted or manage list
    */
    private class DownloadMovieListTask extends APIRequestAsyncTask<String, Void, String> {
        public DownloadMovieListTask(Context context) {
            super(context);
        }
        //Hide the list so the user knows it's refreshing
        @Override
        protected void onPreExecute() {
            getListView().setVisibility(View.INVISIBLE);
        }

        @Override
        protected void onPostExecute(String result) {
            //Want to display an empty list if there's been an error
            if ((movies = parseMovieList(result)) == null) {
                movies = new ArrayList<Movie>();
            }
            //If there's no movies, show the No Movies text
            if (movies.size() <= 0) {
                noMovies.setVisibility(View.VISIBLE);
            } else {
                //We want to call this even on a refresh
                final MovieListAdapter<Movie> adapter = new MovieListAdapter<Movie>(
                        getActivity(), R.layout.adapter_movielist, movies);
                setListAdapter(adapter);
                getListView().setVisibility(View.VISIBLE);
            }
            task = null;
        }

        //Parse the list of movies and generate Movie objects for each entry
        private ArrayList<Movie> parseMovieList(String resp) {
            if ((resp == null) || (resp.length() <= 0)) {
                //Log.e(TAG, "parseMovieList was passed an invalid string");
                return null;
            }
            try {
                JSONObject response = new JSONObject(resp);
                //	Log.d(TAG, "String Contents: " + response.toString());
                //Make sure our request is okay
                if (!response.getBoolean("success")) {
                    return null;
                }
                //If it's empty we just want to return an empty list
                if (response.getBoolean("empty")) {
                    return new ArrayList<Movie>();
                }
                JSONArray jsonMoviesList = response.getJSONArray("movies");

                ArrayList<Movie> movies = new ArrayList<Movie>(jsonMoviesList.length());

                //Create a Movie object from every movie listed in the json response
                for (int i = 0; i < jsonMoviesList.length(); i++) {
                    //Library ID (used for deleting movies)
                    int libraryId = jsonMoviesList.getJSONObject(i).getInt("library_id");

                    JSONObject info = jsonMoviesList.getJSONObject(i).getJSONObject("library").
                            getJSONObject("info");
                    //	Log.d(TAG, "Movie JSON String: " + info.toString());

                    //Grab the important information (have to watch for nulls)
                    String title = !info.isNull("titles") ? info.getJSONArray("titles").getString(0)
                            : "No title";
                    String tagline = !info.isNull("tagline") ? info.getString("tagline") : "No tagline";
                    String year = !info.isNull("year") ? info.getString("year") : "";
                    String plot = !info.isNull("plot") ? info.getString("plot") : "No plot";

                    //Get the poster URI (this will be a web address)
                    String posterUri = "";
                    if (!info.isNull("images") && !info.getJSONObject("images").isNull("poster")) {
                        JSONArray posters = info.getJSONObject("images").getJSONArray("poster");
                        //Some movies don't have posters
                        if(posters.length() >= 1) {
                            posterUri = posters.getString(0);
                        }
                    }

                    //Copy the actor/directors JSONArrays into a regular String array
                    String[] actors = !info.isNull("actors") ?
                            jsonStringArrayToStringArray(info.getJSONArray("actors"))
                            : new String[0];
                    String[] directors = !info.isNull("directors") ?
                            jsonStringArrayToStringArray(info.getJSONArray("directors"))
                            : new String[0];

                    Movie newMovie = new Movie(libraryId, title, tagline, posterUri, plot, year, actors,
                            directors);
                    movies.add(newMovie);
                }
                return movies;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
        //Copy a JSON array of strings to a regular String array.
        private String[] jsonStringArrayToStringArray(JSONArray array) {
            if(array == null) {
                return null;
            }
            String[] newArray = new String[array.length()];
            for(int i = 0; i < array.length(); i++) {
                try {
                    newArray[i] = array.get(i).toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            return newArray;
        }

    }

}
package no.dega.couchpotatoremote;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/*
    When a user clicks on a movie in the list, take them to this view activity
    User can see poster, title, tagline, year, plot and actors/directors
*/
public class MovieViewActivity extends ActionBarActivity {
    private static final String TAG = MovieViewActivity.class.getName();

    private ArrayList<Movie> movies = null;
    private Movie current = null;
    private GestureDetector gestureDetector = null;

    private int currentPos = 0;

    protected boolean actorsExpanded = false;
    protected boolean directorsExpanded = false;
    protected boolean plotExpanded = false;

    //Parameters for a swipe to qualify
    private static final int FLING_MIN_DISTANCE = 120;
    private static final int FLING_MAX_OFF_PATH = 250;
    private static final int FLING_THRESHOLD_VELOCITY = 200;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_movie_view);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        //Create the fling listener
        GestureDetector.SimpleOnGestureListener listener =
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                        //Too far off path?
                        if (Math.abs(e1.getY() - e2.getY()) > FLING_MAX_OFF_PATH) {
                            return false;
                        }

                        View layout = findViewById(R.id.movie_view_layout);
                        Animation slideoutMovie;
                        int pos;
                        //Check if it's a valid fling, and whether it's left or right
                        if (e1.getX() - e2.getX() > FLING_MIN_DISTANCE
                                && Math.abs(velocityX) > FLING_THRESHOLD_VELOCITY) {
                            //Left fling
                            pos = currentPos + 1;
                            slideoutMovie = AnimationUtils.loadAnimation(layout.getContext(),
                                    R.anim.movieview_slideout_left);
                        }
                        else if (e2.getX() - e1.getX() > FLING_MIN_DISTANCE
                                && Math.abs(velocityX) > FLING_THRESHOLD_VELOCITY) {
                            //Right fling
                            pos = currentPos - 1;
                            slideoutMovie = AnimationUtils.loadAnimation(layout.getContext(),
                                    R.anim.movieview_slideout_right);
                        } else {
                            //Not a valid fling in either direction
                            return false;
                        }
                        //Wraparound
                        if(pos < 0) {
                            pos = movies.size() - 1;
                        } else if(pos > movies.size() - 1) {
                            pos = 0;
                        }

                        final int finalPos = pos;
                        slideoutMovie.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {}
                            @Override
                            public void onAnimationEnd(Animation animation) {
                                displayMovie(finalPos);
                            }
                            @Override
                            public void onAnimationRepeat(Animation animation) {}
                        });
                        //Collapse the dropdowns
                        findViewById(R.id.movieview_plot_text).setVisibility(View.GONE);
                        findViewById(R.id.movieview_actors_text).setVisibility(View.GONE);
                        findViewById(R.id.movieview_directors_text).setVisibility(View.GONE);

                        layout.startAnimation(slideoutMovie);
                        return true;
                    }
                };
        gestureDetector = new GestureDetector(this, listener);

        Bundle bun = getIntent().getExtras();
        if (bun != null) {
            movies = bun.getParcelableArrayList("movies");
            currentPos = bun.getInt("position");
            //This will also initialise current
            displayMovie(currentPos);
        } else {
            //Log.e(TAG, "Null bundle passed to movieview");
        }
    }

    //Pass the fling to the gesture detector
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        super.dispatchTouchEvent(ev);
        return gestureDetector.onTouchEvent(ev);
    }

    //Display the movie at pos on the screen.
    public void displayMovie(int pos) {
        currentPos = pos;
        current = movies.get(currentPos);

        this.setTitle(current.getTitle());

        TextView movieTitle = (TextView) findViewById(R.id.movieview_title);
        TextView movieTagline = (TextView) findViewById(R.id.movieview_tagline);
        TextView movieYear = (TextView) findViewById(R.id.movieview_year);
        ImageView poster = (ImageView) findViewById(R.id.movieview_poster);

        movieTitle.setText(current.getTitle());
        //Hide the tagline if there isn't one, helps compact the view
        if(current.getTagline().length() > 0) {
            movieTagline.setText(current.getTagline());
            movieTagline.setVisibility(View.VISIBLE);
        } else {
            movieTagline.setVisibility(View.GONE);
        }
        movieYear.setText(current.getYear());
        //If the user re-enters the application in the MovieView, we need to re-init the config
        if(!ImageLoader.getInstance().isInited()) {
            DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder().
                    cacheInMemory(true).cacheOnDisc(true).build();
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                    .defaultDisplayImageOptions(defaultOptions).build();
            ImageLoader.getInstance().init(config);
        }

        ImageLoader.getInstance().displayImage(current.getPosterUri(), poster);

    }

    //Called when user presses 'Delete' button. Opens a confirmation window.
    public void onDeleteButtonPress(View view) {
        new ConfirmMovieDeleteFragment().show(getSupportFragmentManager(), null);
    }

    //Called when user presses 'Releases' button. Takes user to Releases activity.
    public void onReleasesButtonPress(View view) {
        Intent releasesActivity = new Intent(this, ReleasesActivity.class);
        releasesActivity.putExtra("movie", current);
        startActivity(releasesActivity);
    }

    //Called when user presses 'Actors' button. Expands plot.
    public void onPlotButtonPress(View view) {
        TextView plot = (TextView) findViewById(R.id.movieview_plot_text);
        if(!plotExpanded) {
            plotExpanded = true;
            plot.setText(current.getPlot());
            plot.setVisibility(View.VISIBLE);
        } else {
            plotExpanded = false;
            plot.setVisibility(View.GONE);
        }
    }

    //Called when user presses 'Actors' button. Expands list of actors.
    public void onActorButtonPress(View view) {
        TextView actors = (TextView) findViewById(R.id.movieview_actors_text);
        if(!actorsExpanded) {
            actorsExpanded = true;
            actors.setText(stringArrayToString(current.getActors(), "No actors."));
            actors.setVisibility(View.VISIBLE);
        } else {
            actorsExpanded = false;
            actors.setVisibility(View.GONE);
        }
    }

    //Called when user presses 'Directors' button. Expands list of directors.
    public void onDirectorButtonPress(View view) {
        TextView directors = (TextView) findViewById(R.id.movieview_directors_text);

        if(!directorsExpanded) {
            directorsExpanded = true;
            directors.setText(stringArrayToString(current.getDirectors(), "No directors."));
            directors.setVisibility(View.VISIBLE);
        } else {
            directorsExpanded = false;
            directors.setVisibility(View.GONE);
        }
    }

    //Convert a string array to a string. Used to convert actors/directors to a single string.
    private String stringArrayToString(String[] array, String emptyMessage) {
        StringBuilder display = new StringBuilder();
        if(array.length > 0) {
            for(int i = 0; i < array.length - 1; i++) {
                display.append(array[i]).append("\n");
            }
            //Don't want a trailing \n on the last element
            display.append(array[array.length - 1]);
        } else {
            display.append(emptyMessage);
        }
        return display.toString();
    }

    //Launch change quality dialog
    public void onQualityButtonPress(View view) {
        new ChangeQualityFragment().show(getSupportFragmentManager(), null);
    }

    //Helper functions for testing (should not need to use these normally)
    public ArrayList<Movie> getMovies() {
        return movies;
    }

    public int getCurrentPos() {
        return currentPos;
    }

    public void setMovies(ArrayList<Movie> moviesList, int pos) {
        if((pos < 0) || (pos > moviesList.size())) {
            throw new IllegalArgumentException("Position outside range of movies list");
        }
        movies = moviesList;
        currentPos = pos;
        current = moviesList.get(currentPos);
    }

    public void setCurrentPos(int pos) {
        currentPos = pos;
    }

    //Popup box for user to change qualities
    private class ChangeQualityFragment extends DialogFragment {
        //List of qualities
        private ArrayList<Quality> qualities = null;
        //Spinner to select quality
        private Spinner spinner = null;
        //Current selection
        private Quality selection = null;
        //The dialog box and the 'Loading...' text
        private AlertDialog dialog = null;
        private TextView loading = null;
        //Keep track of selected position over orientation changes
        private int selectedPosition = 0;

        //Keep the dialog up when the user changes orientation
        @Override
        public void onDestroyView() {
            if(getDialog() != null && getRetainInstance()) {
                getDialog().setDismissMessage(null);
            }
            super.onDestroyView();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            setRetainInstance(true);

            View view = getActivity().getLayoutInflater().inflate(R.layout.change_quality_dialog, null);
            spinner = (Spinner) view.findViewById(R.id.changequality_selector);
            loading = (TextView) view.findViewById(R.id.changequality_wait);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setView(view)
                    //Accept the quality change
                    .setPositiveButton(R.string.accept_change_quality, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            StringBuilder query = new StringBuilder();
                            query.append("movie.edit?").append("id=").append(current.getLibraryId())
                                    .append("&profile_id=").append(selection.getId());
                            //Put through the actual request to change quality
                            new APIRequestAsyncTask<String, Void, String>(MovieViewActivity.this)
                                    .execute(APIUtilities.formatRequest(query.toString(), MovieViewActivity.this));

                            Toast.makeText(MovieViewActivity.this,
                                    current.getTitle() + "'s quality changed to " + selection.getLabel() + ".",
                                    Toast.LENGTH_LONG).show();
                        }
                    })
                    //Reject the quality change
                    .setNegativeButton(R.string.reject_change_quality, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {}
                    });
            dialog = builder.create();
            dialog.show();
            if(qualities == null) {
                //No qualities - hide the spinner/accept button and get the qualities
                dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
                spinner.setVisibility(View.GONE);

                //Get a list of qualities
                new GetQualitiesTask(MovieViewActivity.this).execute(
                        APIUtilities.formatRequest("profile.list", MovieViewActivity.this));
            } else {
                displayQualities();
            }
            return dialog;
        }
        //Hide the 'Loading...' text, display+populate the spinner list
        private void displayQualities() {
            //Change the selected quality
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                    selectedPosition = pos;
                    selection = qualities.get(pos);
                }
                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {}
            });

            ArrayAdapter<Quality> adapter = new ArrayAdapter<Quality>(MovieViewActivity.this,
                    R.layout.spinner_dropdown_tight, qualities);
            spinner.setAdapter(adapter);
            spinner.setSelection(selectedPosition);

            loading.setVisibility(View.GONE);
            spinner.setVisibility(View.VISIBLE);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
        }

        //Get a list of qualities from the server
        private class GetQualitiesTask extends APIRequestAsyncTask<String, Void, String> {

            public GetQualitiesTask(Context context) {
                super(context);
            }

            @Override
            protected void onPostExecute(String result) {
                parseQualitiesList(result);
                displayQualities();
            }
            //Parse the list of qualities
            private void parseQualitiesList(String result) {
                try {
                    JSONArray list = new JSONObject(result).getJSONArray("list");
                    qualities = new ArrayList<Quality>(list.length());
                    for(int i = 0; i < list.length(); i++) {
                        JSONObject quality = list.getJSONObject(i);
                        Quality qual = new Quality(quality.getString("_id"), quality.getString("label"));
                        qualities.add(qual);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        //Small object that holds quality name and its profile id
        private class Quality {
            private final String id;
            private final String label;

            public Quality(String id, String label) {
                this.id = id;
                this.label = label;
            }
            public String getLabel() {
                return label;
            }
            public String getId() {
                return id;
            }
            public String toString() {
                return label;
            }
        }
    }

    //Alert dialog to confirm that the user really wants to delete a movie
    private class ConfirmMovieDeleteFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            setRetainInstance(true);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.confirm_delete_movie)
                //Confirm delete
                .setPositiveButton(R.string.accept_delete_movie, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String request = APIUtilities.formatRequest(
                                "media.delete?id=" + String.valueOf(current.getLibraryId()), getActivity());
                        //Send the request. Don't need to subclass this
                        new APIRequestAsyncTask<String, Void, String>(getActivity()).execute(request);
                        movies.remove(currentPos);
                        finish();
                    }
                })
                //Reject delete
                .setNegativeButton(R.string.reject_delete_movie, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
        return builder.create();
        }
        //Keep the dialog up when the user changes orientation
        @Override
        public void onDestroyView() {
            if(getDialog() != null && getRetainInstance()) {
                getDialog().setDismissMessage(null);
            }
            super.onDestroyView();
        }
    }
}

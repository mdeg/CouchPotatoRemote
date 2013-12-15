package no.dega.couchpotatoremote;

import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

/*
    When a user clicks on a movie in the list, take them to this view activity
    User can see poster, title, tagline, year, plot and actors/directors
*/
    //TODO: add sideways scrolling
public class MovieViewActivity extends ActionBarActivity {
    ArrayList<Movie> movies = null;
    Movie current = null;
    int currentPos = 0;
    boolean actorsExpanded = false;
    boolean directorsExpanded = false;
    GestureDetector gestureDetector = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_movie_view);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Create the fling listener
        GestureDetector.SimpleOnGestureListener listener =
                new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                int pos;
                if(e1.getAxisValue(MotionEvent.AXIS_X) > e2.getAxisValue(MotionEvent.AXIS_X)) {
                    //Left fling\
                    pos = currentPos + 1;
                } else { //Right fling
                    pos = currentPos - 1;
                }
                //Wraparound
                if(pos < 0) {
                    pos = movies.size() - 1;
                }
                if(pos > movies.size() - 1) {
                    pos = 0;
                }

                displayMovie(pos);
                //TODO: take velocity and use it to set the time of the animation
                return true;
            }
        };
        gestureDetector = new GestureDetector(this, listener);

        Bundle bun = getIntent().getExtras();
        if (bun != null) {
            //movie = bun.getParcelable("no.dega.couchpotatoremote.Movie");
            movies = bun.getParcelableArrayList("movies");
            currentPos = bun.getInt("position");
            //This will also initialise current
            displayMovie(currentPos);
        } else {
            Log.e("MovieViewActivity", "Null bundle passed to movieview");
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    public void displayMovie(int pos) {
        currentPos = pos;
        current = movies.get(currentPos);

        this.setTitle(current.getTitle());

        TextView movieTitle = (TextView) findViewById(R.id.movieview_title);
        TextView moviePlot = (TextView) findViewById(R.id.movieview_plot);
        TextView movieTagline = (TextView) findViewById(R.id.movieview_tagline);
        TextView movieYear = (TextView) findViewById(R.id.movieview_year);
        ImageView poster = (ImageView) findViewById(R.id.movieview_poster);

        movieTitle.setText(current.getTitle());
        moviePlot.setText("Plot: " + current.getPlot());
        //We want to hide the tagline if there isn't one, helps compact the view
        if(current.getTagline().length() > 0) {
            movieTagline.setText(current.getTagline());
        } else {
            movieTagline.setVisibility(View.GONE);
        }
        movieYear.setText(current.getYear());

        //Grab from cache, or network if not cached
        ImageLoader.getInstance().displayImage(current.getPosterUri(), poster);
    }


    //Called when user presses 'Actors' button. Expands list of actors.
    public void onActorButtonPress(View view) {
        //TODO: add some animation for expanding/unexpanding
        TextView actors = (TextView) findViewById(R.id.movieview_actors_text);

        if(!actorsExpanded) {
            actorsExpanded = true;

            StringBuilder display = new StringBuilder();
            if(current.getActors().length > 0) {
                for(String str: current.getActors()) {
                    display.append(str).append("\n");
                }
            } else {
                display.append("No actors.");
            }

            actors.setText(display.toString());
            actors.setVisibility(View.VISIBLE);
        } else {
            //Already expanded, and we need to close
            actorsExpanded = false;
            //TODO: this might need to be view.invisible? to preserve place in layout
            actors.setVisibility(View.GONE);
        }
    }

    //Called when user presses 'Directors' button. Expands list of directors.
    public void onDirectorButtonPress(View view) {
        //TODO: add some animation for expanding/unexpanding
        TextView directors = (TextView) findViewById(R.id.movieview_directors_text);

        if(!directorsExpanded) {
            directorsExpanded = true;

            StringBuilder display = new StringBuilder();
            if(current.getDirectors().length > 0) {
                for(String str: current.getDirectors()) {
                    display.append(str).append("\n");
                }
            } else {
                display.append("No directors.");
            }

            directors.setText(display.toString());
            directors.setVisibility(View.VISIBLE);
        } else {
            //Already expanded, and we need to close
            directorsExpanded = false;
            //TODO: this might need to be view.invisible? to preserve place in layout
            directors.setVisibility(View.GONE);
        }
    }
}

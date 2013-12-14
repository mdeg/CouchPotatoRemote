package no.dega.couchpotatoremote;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

/*
    When a user clicks on a movie in the list, take them to this view activity
    User can see poster, title, tagline, year, plot and actors/directors
*/
    //TODO: add sideways scrolling
public class MovieViewActivity extends ActionBarActivity {
    Movie movie = null;
    boolean actorsExpanded = false;
    boolean directorsExpanded = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_movie_view);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Bundle bun = getIntent().getExtras();

        TextView movieTitle = (TextView) findViewById(R.id.movieview_title);
        TextView moviePlot = (TextView) findViewById(R.id.movieview_plot);
        TextView movieTagline = (TextView) findViewById(R.id.movieview_tagline);
        TextView movieYear = (TextView) findViewById(R.id.movieview_year);
        ImageView poster = (ImageView) findViewById(R.id.movieview_poster);

        if (bun != null) {
            movie = bun.getParcelable("no.dega.couchpotatoremote.Movie");
            if (movie != null) {
                this.setTitle(movie.getTitle());

                movieTitle.setText(movie.getTitle());
                moviePlot.setText("Plot: " + movie.getPlot());
                movieTagline.setText(movie.getTagline());
                movieYear.setText(movie.getYear());

                //Grab from cache, or network if not cached
                ImageLoader.getInstance().displayImage(movie.getPosterUri(), poster);
            } else {
                Log.e("MovieViewActivity", "Movie passed to MovieView is null (parcelling may have failed)");
            }
        } else {
            Log.e("MovieViewActivity", "Null bundle passed to movieview");
        }
    }  
    //Called when user presses 'Actors' button. Expands list of actors.
    public void onActorButtonPress(View view) {
        //TODO: add some animation for expanding/unexpanding
        TextView actors = (TextView) findViewById(R.id.movieview_actors_text);

        if(!actorsExpanded) {
            actorsExpanded = true;

            StringBuilder display = new StringBuilder();
            if(movie.getActors().length > 0) {
                for(String str: movie.getActors()) {
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
    public void onActorButtonPress(View view) {
        //TODO: add some animation for expanding/unexpanding
        TextView directors = (TextView) findViewById(R.id.movieview_directors_text);

        if(!directorsExpanded) {
            directorsExpanded = true;

            StringBuilder display = new StringBuilder();
            if(movie.getDirectors().length > 0) {
                for(String str: movie.getDirectors()) {
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
            actors.setVisibility(View.GONE);
        }
    }
}

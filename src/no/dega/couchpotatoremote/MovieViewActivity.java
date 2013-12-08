package no.dega.couchpotatoremote;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

public class MovieViewActivity extends Activity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_movie_view);

	    getActionBar().setDisplayHomeAsUpEnabled(true);

		
		Bundle bun = getIntent().getExtras();
		TextView movieTitle = (TextView) findViewById(R.id.movieview_title);
		TextView moviePlot = (TextView) findViewById(R.id.movieview_plot);
		TextView movieTagline = (TextView) findViewById(R.id.movieview_tagline);
		TextView movieYear = (TextView) findViewById(R.id.movieview_year);
		ImageView poster = (ImageView) findViewById(R.id.movieview_poster);
		
		if(bun != null) {
			Movie movie = bun.getParcelable("no.dega.couchpotatoremote.Movie");

            if(movie == null) {
                Log.e("MovieViewActivity", "Movie passed to MovieView is null (parcelling may have failed)");
            } else {

                Log.d("AfterParcel", movie.toString());

                this.setTitle(movie.getTitle());

                movieTitle.setText(movie.getTitle());
                moviePlot.setText(movie.getPlot());
                movieTagline.setText(movie.getTagline());
                movieYear.setText(String.valueOf(movie.getYear()));
                //Grab from cache, or network if not cached

                ImageLoader.getInstance().displayImage(movie.getPosterUri(), poster);
            }
		} else {
			Log.e("MovieViewActivity", "Null bundle passed to movieview");
		}
	}
}

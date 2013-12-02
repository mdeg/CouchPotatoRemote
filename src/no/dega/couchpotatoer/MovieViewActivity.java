package no.dega.couchpotatoer;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

public class MovieViewActivity extends Activity {
	
	public MovieViewActivity() {
		
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_movie_view);

		//Bundle bun = getIntent().getExtras();
		//TODO: change me when project name changes
		Bundle bun = getIntent().getExtras();
		TextView movieTitle = (TextView) findViewById(R.id.movieview_title);
		TextView moviePlot = (TextView) findViewById(R.id.movieview_plot);
		TextView movieTagline = (TextView) findViewById(R.id.movieview_tagline);
		TextView movieYear = (TextView) findViewById(R.id.movieview_year);
		ImageView poster = (ImageView) findViewById(R.id.movieview_poster);
		
		if(bun != null) {
			Movie movie = bun.getParcelable("no.dega.couchpotatoer.Movie");
			Log.d("AfterParcel", movie.toString());
			if(movieTitle != null) {
				movieTitle.setText(movie.getTitle());
			}
			if(moviePlot != null) {
				moviePlot.setText(movie.getPlot());
			}
			if(movieTagline != null) {
				movieTagline.setText(movie.getTagline());
			}
			if(movieYear != null) {
				movieYear.setText(String.valueOf(movie.getYear()));
			}
			if(poster != null) {
				poster.setImageResource(R.drawable.ic_launcher);
			}
		} else {
			Log.e("MovieViewActivity", "Null bundle passed to movieview");
			//TODO: do some kind of user-oriented recovery here
		}
	}
}

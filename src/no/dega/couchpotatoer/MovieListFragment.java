package no.dega.couchpotatoer;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

//Displays a list of Movies
public class MovieListFragment extends ListFragment {
	//Indexed by CouchPotato's own libraryId
	SparseArray<Movie> movies;
	//True if this is the Wanted list; false if it's the Manage list
	boolean isWanted;

	public MovieListFragment() {

	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		//Copy the movies into an array
		//TODO: put them in alphabetical order
		ArrayList<Movie> movieList = new ArrayList<Movie>();
		for(int i = 0; i < this.movies.size(); i++) {
			//int key = this.movies.keyAt(i);
			movieList.add(this.movies.get(this.movies.keyAt(i)));
		}
		//Use custom movielist adapter to create the list
		final MovieListAdapter<Movie> adapter = new MovieListAdapter<Movie>(
				getActivity(), R.layout.adapter_movielist, movieList);
		setListAdapter(adapter);
	}
	//	Bundle bun = getIntent().getExtras();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_movie_list, container, false);
		this.isWanted = getArguments().getBoolean("isWanted");
		this.movies = APIUtilities.parseMovieList(this.isWanted, getActivity());
		return rootView;
	}
	//If a user clicks on a movie, take them to the appropriate movie display
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.d("List Position", ""+position);
		Movie movie = (Movie) getListAdapter().getItem(position);
		Log.d("toString:", movie.toString());
		
		Intent intent = new Intent(getActivity(), MovieViewActivity.class);
		//TODO: change me when project name changes
		intent.putExtra("no.dega.couchpotatoer.Movie", movie);
		startActivity(intent);
	}
}
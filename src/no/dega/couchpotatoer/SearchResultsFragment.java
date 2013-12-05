package no.dega.couchpotatoer;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class SearchResultsFragment extends ListFragment {
	ArrayList<Movie> searchResults;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		final SearchResultsAdapter<Movie> adapter = new SearchResultsAdapter<Movie>(
				getActivity(), R.layout.adapter_movielist, searchResults);
		setListAdapter(adapter);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_search_results, container, false);
	
		this.searchResults = APIUtilities.searchForMovie(getArguments().getString("NameToSearch"), getActivity());

		return rootView;
	}
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {

		Movie movie = (Movie) getListAdapter().getItem(position);
		//TODO: some kinda slide out thing for adding movies
		APIUtilities.addMovie(movie, getActivity());
	}
}
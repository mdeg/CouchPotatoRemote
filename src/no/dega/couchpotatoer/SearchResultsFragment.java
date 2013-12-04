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

		this.searchResults = APIUtilities.searchForMovie("Baraka", getActivity());
		
		return rootView;
	}
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {

		Movie movie = (Movie) getListAdapter().getItem(position);
		
		//TODO: what to do when movie is added
	}
}
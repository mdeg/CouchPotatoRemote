package no.dega.couchpotatoremote;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;


public class SearchResultsAdapter<T> extends ArrayAdapter<Movie> {

    private List<Movie> movies;

    public SearchResultsAdapter(Context context, int resource, List<Movie> movies) {
        super(context, resource, movies);
        this.movies = movies;
    }

    //Custom view for each movie element on the list
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            v = inflater.inflate(R.layout.adapter_searchresults, null);
        }

        Movie movie = movies.get(position);

        if (movie != null) {
            TextView title = (TextView) v.findViewById(R.id.searchadapter_title);
            TextView year = (TextView) v.findViewById(R.id.searchadapter_year);

            title.setText(movie.getTitle());
            year.setText(movie.getYear());
        }
        return v;
    }
}

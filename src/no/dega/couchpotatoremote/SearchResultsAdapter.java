package no.dega.couchpotatoremote;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

//Adapter to display search results (name and year)
public class SearchResultsAdapter<T> extends ArrayAdapter<Movie> {

    private final List<Movie> movies;

    public SearchResultsAdapter(Context context, int resource, List<Movie> movies) {
        super(context, resource, movies);
        this.movies = movies;
    }

    //Shows [title......... year] for every list element
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.adapter_searchresults, null);
            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.searchadapter_title);
            holder.year = (TextView) convertView.findViewById(R.id.searchadapter_year);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Movie movie = movies.get(position);
        holder.title.setText(movie.getTitle());
        holder.year.setText(movie.getYear());

        return convertView;
    }
    //Use ViewHolder to recycle views
    private static class ViewHolder {
        TextView title;
        TextView year;
    }
}

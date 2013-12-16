package no.dega.couchpotatoremote;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

public class MovieListAdapter<T> extends ArrayAdapter<Movie> {

    private final List<Movie> movies;

    public MovieListAdapter(Context context, int resource, List<Movie> movies) {
        super(context, resource, movies);
        this.movies = movies;
    }

    /*
    View for movies on the movielist. Uses ViewHolder to cache views for better performance.
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.adapter_movielist, null);
            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.adapter_title);
            holder.year = (TextView) convertView.findViewById(R.id.adapter_year);
            holder.plot = (TextView) convertView.findViewById(R.id.adapter_plot);
            holder.poster = (ImageView) convertView.findViewById(R.id.adapter_poster);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Movie movie = movies.get(position);

        if (movie != null) {
            holder.title.setText(movie.getTitle());

            holder.year.setText(movie.getYear());
            holder.plot.setText(movie.getPlot());

            ImageLoader.getInstance().displayImage(movie.getPosterUri(), holder.poster);

            //Posters are almost always 231px high (and never higher). A few are a little smaller though.
            //Irritatingly, there are some that are smaller and have transparencies in the remainder
            //So, matching
            //Have to check for null and give a default height - image might not be loaded yet
            int maxHeight = 231;
            if(holder.poster.getDrawable() != null) {
                maxHeight = holder.poster.getDrawable().getIntrinsicHeight();
            }
            //Plot size should be the height of the poster minus the height of the title
            holder.plot.setMaxHeight(maxHeight - holder.title.getLineHeight());
        }
        return convertView;
    }

    private static class ViewHolder {
        TextView title;
        TextView year;
        TextView plot;
        ImageView poster;
    }
}

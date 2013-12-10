package no.dega.couchpotatoremote;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

public class MovieListAdapter<T> extends ArrayAdapter<Movie> {

	private List<Movie> movies;
	
	public MovieListAdapter(Context context, int resource, List<Movie> movies) {
		super(context, resource, movies);
		this.movies = movies;
	}
	
	//Custom view for each movie element on the list
	public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
	    if(convertView == null) {
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

	    if(movie != null) {
	        holder.title.setText(movie.getTitle());
            holder.year.setText(movie.getYear());
            holder.plot.setText(movie.getPlot());

            ImageLoader.getInstance().displayImage(movie.getPosterUri(), holder.poster);
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

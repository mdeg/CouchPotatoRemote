package no.dega.couchpotatoremote;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.List;



//Adapter for displaying movies as a list
//Looks like this:
//[poster] [title] [year]
//[      ] [    plot    ]
//[      ] [            ]
public class MovieListAdapter<T> extends ArrayAdapter<Movie> {

    private static final int MAX_HEIGHT = 170;
    private List<Movie> movies;

    public MovieListAdapter(Context context, int resource, List<Movie> movies) {
        super(context, resource, movies);
        this.movies = movies;
    }

    //View for movies on the movielist. Uses ViewHolder to cache views for better performance.
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
        }

        holder = (ViewHolder) convertView.getTag();
        Movie movie = movies.get(position);

        if (movie != null) {
            holder.title.setText(movie.getTitle());
            holder.year.setText(movie.getYear());
            holder.plot.setText(movie.getPlot());

            ImageLoader.getInstance().displayImage(movie.getPosterUri(), holder.poster);

            holder.plot.setMaxHeight(holder.poster.getHeight() - holder.title.getHeight());

            int a = holder.plot.getHeight();
            if(holder.plot.getHeight() == 0) {
                holder.plot.setMaxHeight(MAX_HEIGHT);
            }
        }

        return convertView;
    }
    //Use ViewHolder to recycle views for better performance
    private static class ViewHolder {
        public TextView title;
        public TextView year;
        public TextView plot;
        public ImageView poster;
    }
}

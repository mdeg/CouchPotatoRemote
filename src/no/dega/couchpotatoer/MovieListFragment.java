package no.dega.couchpotatoer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONObject;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

//TODEL: previously ___ extends Fragment
public class MovieListFragment extends ListFragment {
	//Indexed by CouchPotato's own libraryId
	SparseArray<Movie> movies;
	
	public MovieListFragment() {
		this.movies = new SparseArray<Movie>();
		//TODO: move this to oncreateview
		parseWantedList(makeRequest());

	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Movie[] movieArray = new Movie[this.movies.size()];
		for(int i = 0; i < this.movies.size(); i++) {
			//int key = this.movies.keyAt(i);
			movieArray[i] = this.movies.get(this.movies.keyAt(i));
		}
		//change simplelistitem to own custom layout
		final ArrayAdapter<Movie> adapter = new ArrayAdapter<Movie>(getActivity(),
				android.R.layout.simple_list_item_1, movieArray);
		
		setListAdapter(adapter);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.d("List Position", ""+position);
		Movie movie = (Movie) getListAdapter().getItem(position);
		Log.d("toString:", movie.toString());

		
		MovieViewFragment fragment = new MovieViewFragment();

		Bundle args = new Bundle();
		args.putString("title", movie.getTitle());
		args.putString("tagline", movie.getTagline());
		args.putString("plot", movie.getPlot());
		args.putInt("year", movie.getYear());
		fragment.setArguments(args);
		

	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.fragment_movie_list, container, false);
		
	//	ListView listView = (ListView) findViewById(R.id.movielist);
	//	ImageView posterView = (ImageView) rootView.findViewById(R.id.section_label);
	//	posterView.setImageDrawable(this.posters.get(1));
		
/*			TextView dummyTextView = (TextView) rootView
				.findViewById(R.id.section_label1);
		
		dummyTextView.setText(toPrint.toString());
		*/
		return rootView;
	}
	private String makeRequest() {
		StringBuilder builder = new StringBuilder();
		try {
			//TODO: make this generic
			URL url = new URL("http://192.168.1.10:5050/api/79fc9813360d4f288305346b54baa7da/movie.list");
	
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			InputStream content = urlConnection.getInputStream();
					
			int statusCode = urlConnection.getResponseCode();
			if(statusCode == 200) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(content));
				String line;
				while((line = reader.readLine()) != null) {
					builder.append(line);
				}
			} else {
				Log.e(this.toString(), "Status code not 200, is: " + statusCode);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			Log.e("makeRequest", "HTTP protocol error.");
		} catch (IOException e) {
			e.printStackTrace();
			Log.e("makeRequest", "Could not connect to resource: API key may be missing");
		}
		
		
		return builder.toString();
	}
	
	private Drawable loadImageFromInternet(String url) {
		try {
			InputStream is = (InputStream) new URL(url).getContent();
			return Drawable.createFromStream(is, "src name");
		} catch (Exception e) {
			return null;
		}
	}

	//TODO: put me in the right activity
	public String parseWantedList(String resp) {
		try {
			JSONObject response = new JSONObject(resp);

			Log.d(this.toString(), "String Contents: " + response.toString());
			
			//Make sure our request is okay and there's movies to process
			if(response.getBoolean("success") == false) {
				return getResources().getString(R.string.movies_list_fail);
			}
			if(response.getBoolean("empty") == true) {
				return getResources().getString(R.string.movies_list_empty);
			}
			
			JSONArray jsonMoviesList = response.getJSONArray("movies");
			
			//Go through every movie and collect the useful info for them
			for(int i = 0; i < jsonMoviesList.length(); i++) {
				
				int libraryId = jsonMoviesList.getJSONObject(i).getInt("library_id");
				JSONObject info = jsonMoviesList.getJSONObject(i).getJSONObject("library").getJSONObject("info");
				Log.d(this.toString(), "Movie JSON String: " + info.toString());					
				
				String title = info.getJSONArray("titles").getString(0);
				String tagline = info.getString("tagline");
				String plot = info.getString("plot");
				int year = info.getInt("year");

			//	Drawable poster = loadImageFromInternet();
				//public Movie(int libraryId, String title, String tagline, int year,
				//		String plot, String[] actors, String[] directors) {
				//TODO: handle multiple titles
		
				JSONArray jsonActors = info.getJSONArray("actors");
				String[] actors = new String[jsonActors.length()];

				if(jsonActors != null) {
					for(int j = 0; j < jsonActors.length(); j++) {
						actors[j] = jsonActors.get(j).toString();
					}
				}
				
				JSONArray jsonDirectors = info.getJSONArray("directors");
				String[] directors = new String[jsonDirectors.length()];
				
				if(jsonDirectors != null) {
					for(int j = 0; j < jsonDirectors.length(); j++) {
						directors[j] = jsonDirectors.get(j).toString();
					}
				}
		//		Log.d("parseWantedList", "Movie: " + metadata.toString());
				
				Movie newMovie = new Movie(libraryId, title, tagline, year,
						plot, actors, directors);
				this.movies.put(libraryId, newMovie);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "Placeholder";
	}
	//TODO: add poster to this
	private class Movie {
		private int libraryId;
		private String title;
		private String tagline;
		private String plot;
		private int year;
		private String[] actors;
		private String[] directors;
	//	private Drawable poster;
		
		//Minimal constructor for movie
		public Movie(int libraryId, String title) {
			this.libraryId = libraryId;
			this.title = title;
		}
		//Full constructor used when parsing from JSON
		public Movie(int libraryId, String title, String tagline, int year,
				String plot, String[] actors, String[] directors) {
			this.libraryId = libraryId;
			this.title = title;
			this.tagline = tagline;
			this.year = year;
			this.plot = plot;
			this.actors = actors;
			this.directors = directors;
//			this.poster = poster;
		}
		
		
		public String toString() {
			return this.title + " " + this.tagline + " (" + this.year + ")";
		}
		
		//Getters and setters
		public String getPlot() {
			return plot;
		}
		public void setPlot(String plot) {
			this.plot = plot;
		}
		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		public String getTagline() {
			return tagline;
		}
		public void setTagline(String tagline) {
			this.tagline = tagline;
		}
		public int getYear() {
			return year;
		}
		public void setYear(int year) {
			this.year = year;
		}
		public String[] getActors() {
			return actors;
		}
		public void setActors(String[] actors) {
			this.actors = actors;
		}
		public String[] getDirectors() {
			return directors;
		}
		public void setDirectors(String[] directors) {
			this.directors = directors;
		}

	}
}
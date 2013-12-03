package no.dega.couchpotatoer;

//import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;


public class Movie implements Parcelable {
	private int libraryId;
	//TODO: experiment with final on these
	private String title;
	private String tagline;
	private String plot;
	private String posterUri;

	private int year;
	private String[] actors;
	private String[] directors;
	//TODO: can I remove this?
	public Movie() {
	}

	//Full constructor used when parsing from JSON
	public Movie(int libraryId, String title, String tagline, String posterUri,
			String plot, int year, String[] actors, String[] directors) {
		this.libraryId = libraryId;
		this.title = title;
		this.tagline = tagline;
		this.year = year;
		this.plot = plot;
		this.actors = actors;
		this.directors = directors;
		this.posterUri = posterUri;
	}
	//Reconstitute from parcel
	public Movie(Parcel par) {
		readFromParcel(par);
	}
		
	public static final Parcelable.Creator<Movie> CREATOR = 
			new Parcelable.Creator<Movie>() {
		public Movie createFromParcel(Parcel par) {
			return new Movie(par);
		}
		
		public Movie[] newArray(int size) {
			return new Movie[size];
		}
	};
	
	public String toString() {
		return this.title + " " + this.tagline + " (" + this.year + ")";
	}
	
	//Need to read these back in the same order we write them
	private void readFromParcel(Parcel par) {
		this.libraryId = par.readInt();
		this.title = par.readString();
		this.tagline = par.readString();
		this.plot = par.readString();
		this.posterUri = par.readString();
		this.year = par.readInt();
		this.actors = par.createStringArray();
		this.directors = par.createStringArray();
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(this.libraryId);
		dest.writeString(this.title);
		dest.writeString(this.tagline);
		dest.writeString(this.plot);
		dest.writeString(this.posterUri);
		dest.writeInt(this.year);
		dest.writeStringArray(this.actors);
		dest.writeStringArray(this.directors);
	}
	
	public int describeContents() {
		return 0;
	}
	
	//Getters and setters


	public String getPlot() {
		return plot;
	}
	public String getPosterUri() {
		return posterUri;
	}

	public void setPosterUri(String posterUri) {
		this.posterUri = posterUri;
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
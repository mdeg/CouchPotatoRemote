package no.dega.couchpotatoer;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;


public class Movie implements Parcelable {
	//TODO: add poster to this

	private int libraryId;
	private String title;
	private String tagline;
	private String plot;
	private int year;
	private String[] actors;
	private String[] directors;
	private Drawable poster;
	
	public Movie() {
	}
	
	//Minimal constructor for movie
	public Movie(int libraryId, String title) {
		this.libraryId = libraryId;
		this.title = title;
	}
	//Full constructor used when parsing from JSON
	public Movie(int libraryId, String title, String tagline, int year,
			String plot, String[] actors, String[] directors, Drawable poster) {
		this.libraryId = libraryId;
		this.title = title;
		this.tagline = tagline;
		this.year = year;
		this.plot = plot;
		this.actors = actors;
		this.directors = directors;
		this.poster = poster;
		}

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
		this.year = par.readInt();
		this.plot = par.readString();
		this.actors = par.createStringArray();
		this.directors = par.createStringArray();
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(this.libraryId);
		dest.writeString(this.title);
		dest.writeString(this.tagline);
		dest.writeInt(this.year);
		dest.writeString(this.plot);
		dest.writeStringArray(this.actors);
		dest.writeStringArray(this.directors);
	//	this.actors = dest.createStringArray();
	//	this.directors = dest.createStringArray();
	}
	public int describeContents() {
		return 0;
	}
	
	//Getters and setters
	public Drawable getPoster() {
		return poster;
	}

	public void setPoster(Drawable poster) {
		this.poster = poster;
	}

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
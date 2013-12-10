package no.dega.couchpotatoremote;

//import android.graphics.drawable.Drawable;

import android.os.Parcel;
import android.os.Parcelable;

class Movie implements Parcelable {
    private int libraryId;
    private String imdbId;

    private String title;

    private String tagline;
    private String plot;
    private String posterUri;

    private String year;
    private String[] actors;
    private String[] directors;

    //Full constructor used when parsing from JSON
    public Movie(int libraryId, String title, String tagline, String posterUri,
                 String plot, String year, String[] actors, String[] directors) {
        this.libraryId = libraryId;
        this.title = title;
        this.tagline = tagline;
        this.year = year;
        this.plot = plot;
        this.actors = actors;
        this.directors = directors;
        this.posterUri = posterUri;
    }

    //Constructor for search results
    public Movie(String title, String year, String imdbId) {
        this.imdbId = imdbId;
        this.title = title;
        this.year = year;
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
        this.year = par.readString();
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
        dest.writeString(this.year);
        dest.writeStringArray(this.actors);
        dest.writeStringArray(this.directors);
    }

    public int describeContents() {
        return 0;
    }

    //Getters and setters
    public String getImdbId() {
        return imdbId;
    }

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

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
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
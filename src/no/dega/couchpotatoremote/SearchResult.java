package no.dega.couchpotatoer;

public class SearchResult {
	
	final String title;
	final String year;

	public SearchResult(String title, String year) {
		this.title = title;
		this.year = year;
	}

	@Override
	public String toString() {
		return "SearchResult [title=" + title + ", year=" + year + "]";
	}

	public String getTitle() {
		return title;
	}

	public String getYear() {
		return year;
	}
	
}

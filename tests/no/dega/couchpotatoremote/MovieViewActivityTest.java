package no.dega.couchpotatoremote;

import android.test.ActivityInstrumentationTestCase2;
import android.test.ActivityUnitTestCase;
import android.test.TouchUtils;
import android.test.suitebuilder.annotation.SmallTest;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

public class MovieViewActivityTest extends ActivityInstrumentationTestCase2<MovieViewActivity> {

    private final int TEST_LIBRARYID = 1;
    private final String TEST_TITLE = "Test Movie";
    private final String TEST_TAGLINE = "Test Tagline";
    private final String TEST_YEAR = "2000";
    private final String TEST_PLOT = "This is a generic plot for our test movie";
    private final String[] TEST_ACTORS = {"Jane Doe", "Joe Bloggs"};
    private final String[] TEST_DIRECTORS = {"John Citizen"};
    private final String TEST_POSTERURI = "http://placehold.it/150x230";


    private MovieViewActivity activity;

    public MovieViewActivityTest() {
        super(MovieViewActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        activity = getActivity();

        Movie movie = new Movie(TEST_LIBRARYID, TEST_TITLE, TEST_TAGLINE, TEST_POSTERURI,
                TEST_PLOT, TEST_YEAR, TEST_ACTORS, TEST_DIRECTORS);
        ArrayList<Movie> moviesList = new ArrayList<Movie>();
        moviesList.add(movie);
        activity.setMovies(moviesList, 0);
        activity.displayMovie(0);
    }

    public void testEditQuality() {

    }
    //Tap the plot/actors/directors buttons and make sure they expand their text correctly
    @SmallTest
    public void testExpandButtons() {
        TextView plotText = (TextView) activity.findViewById(R.id.movieview_plot_text);
        TextView actorsText = (TextView) activity.findViewById(R.id.movieview_actors_text);
        TextView directorsText = (TextView) activity.findViewById(R.id.movieview_directors_text);

        //Expand/contract plot button
        TouchUtils.tapView(this, activity.findViewById(R.id.movieview_plot_button));
        assertTrue(plotText.getVisibility() == View.VISIBLE);
        TouchUtils.tapView(this, activity.findViewById(R.id.movieview_plot_button));
        assertTrue(plotText.getVisibility() == View.GONE);

        //Actors button
        TouchUtils.tapView(this, activity.findViewById(R.id.movieview_actors_button));
        assertTrue(actorsText.getVisibility() == View.VISIBLE);
        TouchUtils.tapView(this, activity.findViewById(R.id.movieview_actors_button));
        assertTrue(actorsText.getVisibility() == View.GONE);

        //Directors button
        TouchUtils.tapView(this, activity.findViewById(R.id.movieview_directors_button));
        assertTrue(directorsText.getVisibility() == View.VISIBLE);
        TouchUtils.tapView(this, activity.findViewById(R.id.movieview_directors_button));
        assertTrue(directorsText.getVisibility() == View.GONE);
    }

    public void testDelete() {

    }

    public void testLeftAndRightFling() {
        //Left-to-right fling
        TouchUtils.drag(this, 0, 100, 0, 0, 0);
    }
}

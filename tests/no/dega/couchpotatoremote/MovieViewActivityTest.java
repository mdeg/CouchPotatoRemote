package no.dega.couchpotatoremote;

import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.test.suitebuilder.annotation.SmallTest;
import android.view.View;
import android.widget.TextView;

/**
 * Created by root on 12/26/13.
 */
public class MovieViewActivityTest extends ActivityInstrumentationTestCase2<MovieViewActivity> {

    private MovieViewActivity activity;

    public MovieViewActivityTest() {
        super(MovieViewActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setActivityInitialTouchMode(false);
        activity = getActivity();
    }

    public void testEditQuality() {

    }
    //Tap the plot/actors/directors buttons and make sure they expand their text correctly
    @SmallTest
    public void testExpandButtons() {
        TextView plotText = (TextView) activity.findViewById(R.id.movieview_plot_text);
        TextView actorsText = (TextView) activity.findViewById(R.id.movieview_actors_text);
        TextView directorsText = (TextView) activity.findViewById(R.id.movieview_directors_text);

        TouchUtils.tapView(this, activity.findViewById(R.id.movieview_plot_button));
        assertTrue(plotText.getVisibility() == View.VISIBLE);
        TouchUtils.tapView(this, activity.findViewById(R.id.movieview_plot_button));
        assertTrue(plotText.getVisibility() == View.GONE);

        TouchUtils.tapView(this, activity.findViewById(R.id.movieview_actors_button));
        assertTrue(actorsText.getVisibility() == View.VISIBLE);
        TouchUtils.tapView(this, activity.findViewById(R.id.movieview_actors_button));
        assertTrue(actorsText.getVisibility() == View.GONE);

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

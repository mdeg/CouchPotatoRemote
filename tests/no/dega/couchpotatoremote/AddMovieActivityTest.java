package no.dega.couchpotatoremote;

import android.test.ActivityInstrumentationTestCase2;

/**
 * Created by root on 12/26/13.
 */
public class AddMovieActivityTest extends ActivityInstrumentationTestCase2<AddMovieActivity> {

    private AddMovieActivity activity;

    public AddMovieActivityTest() {
        super(AddMovieActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setActivityInitialTouchMode(false);
        activity = getActivity();
    }

    public void testSearchWithButton() {

    }
    public void testSearchWithKeyboardSearchButton() {

    }
    public void testAddMovie() {
        
    }
}

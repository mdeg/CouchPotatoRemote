package no.dega.couchpotatoremote;

import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.view.KeyEvent;
import android.widget.EditText;

//WORK IN PROGRESS
//Tests for the adding movie activity
public class AddMovieActivityTest extends ActivityInstrumentationTestCase2<AddMovieActivity> {

    private AddMovieActivity activity;
    private EditText movieToAdd;

    public AddMovieActivityTest() {
        super(AddMovieActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setActivityInitialTouchMode(false);
        activity = getActivity();
        EditText movieToAdd = (EditText) activity.findViewById(R.id.movie_to_add);
    }

    //Make a search for 'Alien'
    @UiThreadTest
    public void testSearchWithButton() {
        movieToAdd.setText("Alien");
        activity.searchButtonPressed(null);
    }

    @UiThreadTest
    public void testSearchWithKeyboardSearchButton() {
        movieToAdd.setText("Alien");
        sendKeys(KeyEvent.KEYCODE_SEARCH);
    }
    public void testAddMovie() {
        
    }
}

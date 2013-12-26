package no.dega.couchpotatoremote;

import android.test.ActivityInstrumentationTestCase2;

/**
 * Created by root on 12/26/13.
 */
public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {
    private MainActivity activity;

    public MainActivityTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setActivityInitialTouchMode(false);
        activity = getActivity();
    }

    public void testList() {

    }

    public void testRefresh() {

    }

    public void testMenuButtons() {

    }

    //Non-component tests that require a context to retrieve preferences from
    public void testAPIRequestAsyncTask() {

    }

    public void testFormatRequest() {

    }
}

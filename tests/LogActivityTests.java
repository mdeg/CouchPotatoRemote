package no.dega.couchpotatoremote.test;

import android.test.ActivityUnitTestCase;
import android.test.AndroidTestCase;

import java.lang.Override;

import no.dega.couchpotatoremote.LogActivity;

public class LogActivityTests extends ActivityUnitTestCase<LogActivity> {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        intent = new Intent(getInstrumentation().getTargetContext(), LogActivity.class);
        startActivity(intent, null, null);
    }

    public void testSpinner() {
        String[] types = getResources().getStringArray(R.string.log_types);
        activity = getActivity();
        spinner = (Spinner) activity.findViewById(R.id.log_select_type);
        for(int i = 0; i < types.length; i++) {
            activity.setSpinnerPosition();
            activity.finish(); //prob need to change this
            activity = getActivity();
            int currPos = activity.getSpinnerPosition();
        }
    }

}
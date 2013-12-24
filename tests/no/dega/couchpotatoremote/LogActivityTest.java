package no.dega.couchpotatoremote;

import android.content.Intent;
import android.test.ActivityUnitTestCase;
import android.test.suitebuilder.annotation.SmallTest;
import android.widget.Spinner;

import java.lang.Override;

public class LogActivityTest extends ActivityUnitTestCase<LogActivity> {

    private LogActivity activity;

    public LogActivityTest() {
        super(LogActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Intent intent = new Intent(getInstrumentation().getTargetContext(), LogActivity.class);
        startActivity(intent, null, null);
    }

    public void testHello() {
        assertEquals(true, true);
    }

    @SmallTest
    public void testSpinner() {
        activity = getActivity();
        String[] types = activity.getResources().getStringArray(R.array.log_types);

        Spinner spinner = (Spinner) activity.findViewById(R.id.log_select_type);
        for(int i = 0; i < types.length; i++) {
            spinner.setSelection(i);

            int currPos = spinner.getSelectedItemPosition();
            assertEquals(currPos, i);
        }
    }

}
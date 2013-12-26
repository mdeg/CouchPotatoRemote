package no.dega.couchpotatoremote;

import android.content.pm.ActivityInfo;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.widget.Spinner;

import java.util.concurrent.CountDownLatch;

public class LogActivityTest extends ActivityInstrumentationTestCase2<LogActivity> {

    private LogActivity activity;
    private Spinner logtypes;

    public LogActivityTest() {
        super(LogActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setActivityInitialTouchMode(false);
        activity = getActivity();
        logtypes = (Spinner) activity.findViewById(R.id.log_select_type);
    }

    @SmallTest
    public void testRetainLogInfo() {

    }

    @MediumTest
    public void testRetrieveLogInformation() {

    }

    //Ensure the spinner maintains its state over a rotation
    @SmallTest
    public void testSpinnerStatePersistenceOnRotation() throws InterruptedException {
        String[] types = getInstrumentation().getTargetContext().getResources()
                .getStringArray(R.array.log_types);

        for(int i = 0; i < types.length; i++) {
            //final CountDownLatch waitForOriChange = new CountDownLatch(1);
            final CountDownLatch waitForSelection = new CountDownLatch(1);
            //Set the selection on the spinner
            final int finalI = i;
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    logtypes.requestFocus();
                    logtypes.setSelection(finalI);
                    waitForSelection.countDown();
                }
            });
            //Wait for the selection to go through, then toggle the orientation
            /*
            OrientationEventListener oriChange = new OrientationEventListener(getActivity()) {
                @Override
                public void onOrientationChanged(int i) {
                    waitForOriChange.countDown();
                }
            };
            oriChange.enable();

            if(!oriChange.canDetectOrientation()) {
                fail("Can't detect orientation change - test will hang forever!");
            }*/
            waitForSelection.await();
            if(activity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
            //waitForOriChange.await();
            //Wait for the orientation change
            Thread.sleep(500);
            //Check if the state has been retained
            assertEquals(types[i], logtypes.getItemAtPosition(logtypes.getSelectedItemPosition()));
        }
    }

}
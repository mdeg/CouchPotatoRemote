package no.dega.couchpotatoremote;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.TextView;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class LogActivity extends ActionBarActivity {
    private boolean hasRun = false;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_log);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(!hasRun) {
            hasRun = true;
            GetLogTask task = new GetLogTask(this);
            String request = APIUtilities.formatRequest("logging.partial?type=info", this);
            task.execute(request);
        }
    }

    private class GetLogTask extends APIRequestAsyncTask<String, Void, String> {
        public GetLogTask(Context context) {
            super(context);
        }

        @Override
        protected void onPostExecute(String result) {
            TextView log = (TextView) findViewById(R.id.log_text);
            log.setText(parseLog(result));
        }

        private String parseLog(String result) {
            if(result == null) {
                 Log.e("GetLogTask.parseLog()", "Null result passed to GetLogTask.");
                 return null;
            }
            if(result.length() <= 0) {
                return "";
            }
            try {
                JSONObject response = new JSONObject(result);
                String log = !response.isNull("log")
                        ? response.getString("log") : "";
                //TODO: fix why this appears mangled, make it a proper list, etc
                return StringEscapeUtils.unescapeHtml(log);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        return null;
        }
    }
}


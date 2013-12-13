package no.dega.couchpotatoremote;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogActivity extends ActionBarActivity {
    private boolean hasRun;
    private String log = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_log);
        hasRun = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!hasRun) {
            //Create and execute the log retrieval task
            hasRun = true;
            GetLogTask task = new GetLogTask(this);
            String request = APIUtilities.formatRequest("logging.partial?type=info", this);
            task.execute(request);
        } else {
            //This is a recreation and we can just use the recovered log text from the bundle
            ((TextView) findViewById(R.id.log_text)).setText(log);
        }
    }

    //Save hasRun to prevent a re-running of the getLog activity on orientation change
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean("hasRun", hasRun);
        savedInstanceState.putString("log", log);
    }
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        hasRun = savedInstanceState.getBoolean("hasRun");
        log = savedInstanceState.getString("log");
    }

    private class GetLogTask extends APIRequestAsyncTask<String, Void, String> {
        public GetLogTask(Context context) {
            super(context);
        }

        @Override
        protected void onPostExecute(String result) {
            log = parseLog(result);
            ((TextView) findViewById(R.id.log_text)).setText(log);
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
                StringBuilder builder = new StringBuilder();
                //Get rid of the unicode bolding the log prints out with a handy spot of regex
                Pattern pattern = Pattern.compile(Pattern.quote("\u001b[0m"));
                Matcher matcher = pattern.matcher(log);
                int prevStart = 0;
                while(matcher.find()) {
                    //The string from the previous start to the start of the regex match
                    builder.append(log.substring(prevStart, matcher.start()));
                    prevStart = matcher.end();
                }
                //The remainder after the last matching
                builder.append(log.substring(prevStart, log.length() - 1));
                return builder.toString();
            } catch (JSONException e) {
                Log.e("LogActivity.parseLog()", "Exception parsing log JSON");
                e.printStackTrace();
            }
        return null;
        }
    }
}


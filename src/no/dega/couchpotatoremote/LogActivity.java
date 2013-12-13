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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_log);
        hasRun = false;
    }

    @Override
    public void onStart() {
        super.onStart();
        if(!hasRun) {
            //TODO: persist
            hasRun = true;
            GetLogTask task = new GetLogTask(this);
            String request = APIUtilities.formatRequest("logging.partial?type=info", this);
            task.execute(request);
        }
    }

    //Save hasRun to prevent a re-running of the getLog activity on orientation change
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean("hasRun", hasRun);
    }
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        hasRun = savedInstanceState.getBoolean("hasRun");
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


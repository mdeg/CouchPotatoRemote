package no.dega.couchpotatoremote;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;


import java.util.regex.Pattern;

/*
    Get the log from CouchPotato, format it a little then display it.
*/
public class LogActivity extends ActionBarActivity {
    private static final String TAG = LogActivity.class.getName();

    private String log = null;
    private GetLogTask task = null;
    private int selectedPosition = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_log);

        //Create the dialog to allow users to select what type of log they want
        Spinner spinner = (Spinner) findViewById(R.id.log_select_type);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.log_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                //Prevent re-requesting the log on orientation change
                if(pos == selectedPosition) {
                    return;
                }
                log = null;
                if(task != null) {
                    task.cancel(true);
                }
                task = new GetLogTask(adapterView.getContext());
                String type;
                switch(pos) {
                    case 0:
                        type = "all";
                        break;
                    case 1:
                        type = "error";
                        break;
                    case 2:
                        type = "info";
                        break;
                    case 3:
                        type = "debug";
                        break;
                    default:
                        type = "all";
                }
                String request = APIUtilities.formatRequest("logging.partial?type=" + type,
                        adapterView.getContext());
                task.execute(request);
                selectedPosition = pos;
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        spinner.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();

        //If this is a recreation
        if(log != null) {
            ((TextView) findViewById(R.id.log_text)).setText(log);
        }
    }

    //Save our variables so we don't re-request on activity recreation
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("log", log);
        outState.putInt("selectedPosition", selectedPosition);
    }
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        log = savedInstanceState.getString("log");
        selectedPosition = savedInstanceState.getInt("selectedPosition");
        //Set the current selection to the users' old one (for orientation changes)
        ((Spinner) findViewById(R.id.log_select_type)).setSelection(selectedPosition);
    }

    
    //Asynchronously request the log from the CouchPotato server
    private class GetLogTask extends APIRequestAsyncTask<String, Void, String> {
        public GetLogTask(Context context) {
            super(context);
        }

        @Override
        protected void onPostExecute(String result) {
            log = parseLog(result);
            ((TextView) findViewById(R.id.log_text)).setText(log);
            task = null;
        }
        //Parse the log returned, strip out dud characters and format it a little bit
        private String parseLog(String result) {
            if(result == null) {
                 //Log.e(TAG, "Null result passed to GetLogTask.");
                 return null;
            }
            if(result.length() <= 0) {
                return "";
            }
            try {
                JSONObject response = new JSONObject(result);
                String log = !response.isNull("log")
                        ? response.getString("log") : "";
                //Remove the unicode formatting junk the log prints out
                log = log.replaceAll(Pattern.quote("\u001b[0m"), "\n");
                log = log.replaceAll(Pattern.quote("\u001b[31m"), "\n");
                log = log.replaceAll(Pattern.quote("\u001B[36m"), "\n");
                return log;

            } catch (JSONException e) {
                //Log.e(TAG, "Exception parsing log.");
                e.printStackTrace();
            }
        return null;
        }
    }
}


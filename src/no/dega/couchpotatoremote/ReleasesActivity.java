package no.dega.couchpotatoremote;

import android.content.Context;
import android.support.v7.app.ActionBarActivity;

import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
//TODO: what about manage? LAST

//TODO: manage rotation
//TODO: test with torrents

//Shows a list of releases to the user.
//User can ignore, delete or force download releases listed.
public class ReleasesActivity extends ActionBarActivity {

    Movie movie = null;
    ArrayList<Release> releases = new ArrayList<Release>();
    ListView list = null;
    Release selected = null;
    SparseArray<String> qualities = new SparseArray<String>();
    SparseArray<String> statuses = new SparseArray<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_releases);

        //Get the list of statuses
        String request2 = APIUtilities.formatRequest("status.list", this);
        new GetStatusesTask(this).execute(request2);
        //Get the list of qualities
        String request3 = APIUtilities.formatRequest("quality.list", this);
        new GetQualitiesTask(this).execute(request3);

        Bundle bun = getIntent().getExtras();
        if (bun != null) {
            movie = bun.getParcelable("movie");

            setTitle("Releases for \"" + movie.getTitle() + "\"");

            //Get the list of releases
            String request1 = APIUtilities.formatRequest(
                    "release.for_movie?id=" + movie.getLibraryId(), this);
            new GetReleasesTask(this).execute(request1);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        list = (ListView) findViewById(android.R.id.list);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override //Set the selected release, populate the textviews and show them
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selected = releases.get(i);
               // ((TextView) findViewById(R.id.releases_name)).setText(selected.getName());
                ((TextView) findViewById(R.id.releases_quality)).setText(
                        "Quality: " + selected.getQuality());
                ((TextView) findViewById(R.id.releases_status)).setText(
                        "Status: " + selected.getStatus());
                ((TextView) findViewById(R.id.releases_provider)).setText(
                        "Provider: " + selected.getProvider());
                ((TextView) findViewById(R.id.releases_age)).setText(
                        "Age: " + selected.getAge());
                ((TextView) findViewById(R.id.releases_size)).setText(
                        "Size: " + selected.getSize());

                findViewById(R.id.releases_subview).setVisibility(View.VISIBLE);
            }
        });
    }

    //Request CouchPotato to download a specific release
    public void onDownloadButtonPress(View view) {
        String request = APIUtilities.formatRequest(
                "release.manual_download?id=" + selected.getReleaseId(), this);
        new APIRequestAsyncTask<String, Void, String>(this).execute(request);
        Toast.makeText(this, selected.getName() + " sent to downloader.",
                Toast.LENGTH_SHORT).show();
    }

    //Change a release's status to Ignored
    public void onIgnoreButtonPress(View view) {
        String request = APIUtilities.formatRequest(
                "release.ignore?id=" + selected.getReleaseId(), this);
        new APIRequestAsyncTask<String, Void, String>(this).execute(request);
        Toast.makeText(this, "Ignored \"" + selected.getName() + "\"",
                Toast.LENGTH_SHORT).show();
    }

    //Delete a release from the list
    public void onDeleteButtonPress(View view) {
        String request = APIUtilities.formatRequest(
                "release.delete?id=" + selected.getReleaseId(), this);
        new APIRequestAsyncTask<String, Void, String>(this).execute(request);
        Toast.makeText(this, "Deleted \"" + selected.getName() + "\"",
                Toast.LENGTH_SHORT).show();

        //Remove the release from the list and update it
        releases.remove(selected);
        selected = null;
        ArrayAdapter<Release> adapter = new ArrayAdapter<Release>(ReleasesActivity.this,
                R.layout.list_tight, releases);
        list.setAdapter(adapter);
        //Hide the text bits until the user selects a new release
        findViewById(R.id.releases_subview).setVisibility(View.INVISIBLE);
    }

    private class GetQualitiesTask extends APIRequestAsyncTask<String, Void, String> {
        public GetQualitiesTask(Context context) {
            super(context);
        }
        @Override
        protected void onPostExecute(String result) {
            parseQualities(result);
        }

        //Extract the qualities from the JSON response
        private void parseQualities(String result) {
            try {
                JSONArray qualitiesArray = new JSONObject(result).getJSONArray("list");
                for(int i = 0; i < qualitiesArray.length(); i++) {
                    JSONObject quality = qualitiesArray.getJSONObject(i);
                    qualities.put(quality.getInt("id"), quality.getString("label"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    private class GetStatusesTask extends APIRequestAsyncTask<String, Void, String> {
        public GetStatusesTask(Context context) {
            super(context);
        }
        @Override
        protected void onPostExecute(String result) {
            parseStatuses(result);
        }

        private void parseStatuses(String result) {
            try {
                JSONArray statusArray = new JSONObject(result).getJSONArray("list");
                for(int i = 0; i < statusArray.length(); i++) {
                    JSONObject status = statusArray.getJSONObject(i);
                    statuses.put(status.getInt("id"), status.getString("label"));
                }
            } catch(JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class GetReleasesTask extends APIRequestAsyncTask<String, Void, String> {
        public GetReleasesTask(Context context) {
            super(context);
        }

        @Override
        protected void onPostExecute(String result) {
            parseReleases(result);
            if(releases.size() <= 0) {
                findViewById(R.id.empty).setVisibility(View.VISIBLE);
            } else {
                ArrayAdapter<Release> adapter = new ArrayAdapter<Release>(ReleasesActivity.this,
                        R.layout.list_tight, releases);
                list.setAdapter(adapter);
            }
        }
        //Parse the JSON response and extract the releases out of it
        private void parseReleases(String result) {
            try {
                JSONArray rels = new JSONObject(result).getJSONArray("releases");
                for(int i = 0; i < rels.length(); i++) {
                    JSONObject relSet = rels.getJSONObject(i);
                    JSONObject info = relSet.getJSONObject("info");
                    //ID, name, qualityID, statusID, provider name, age in days, size in MB
                    Release release = new Release(
                            relSet.getInt("id"), info.getString("name"),
                            relSet.getInt("quality_id"), relSet.getInt("status_id"),
                            info.getString("provider"), info.getInt("age"),
                            info.getInt("size"));

                    releases.add(release);
                }
            } catch(JSONException e) {
                e.printStackTrace();
            }
        }
    }

    //Storage class that holds all relevant information about releases
    private class Release {
        private int releaseId;
        private String name;
        private int qualityId;
        private int statusId;
        private String provider;
        private String age;
        private String size;


        public Release(int releaseId, String name, int qualityId, int statusId,
                       String provider, int age, int size) {
            this.releaseId = releaseId;
            this.name = name;
            this.qualityId = qualityId;
            this.statusId = statusId;
            this.provider = provider;
            this.age = String.valueOf(age) + " days";
            this.size = String.valueOf(size) + "MB";
        }
        public int getReleaseId() {
            return releaseId;
        }
        public String getName() {
            return name;
        }
        public String getQuality() {
            String label;
            if((label = qualities.get(qualityId)) != null) {
                return label;
            } else {
                return "";
            }
        }
        public String getStatus() {
            String label;
            if((label = statuses.get(statusId)) != null) {
                return label;
            } else {
                return "";
            }
        }
        public String toString() {
            return name;
        }
        public String getProvider() {
            return provider;
        }
        public String getAge() {
            return age;
        }
        public String getSize() {
            return size;
        }
    }

}

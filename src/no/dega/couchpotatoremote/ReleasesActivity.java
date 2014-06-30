package no.dega.couchpotatoremote;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
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

//Shows a list of releases to the user.
//User can ignore, delete or force download releases listed.
public class ReleasesActivity extends ActionBarActivity {

    Movie movie = null;
    ListView list = null;
    Release selected = null;

    ArrayList<Release> releases = new ArrayList<Release>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_releases);

        //Set up the list
        list = (ListView) findViewById(android.R.id.list);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override //Set the selected release, populate the textviews and show them
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selected = releases.get(i);
            //    ((TextView) findViewById(R.id.releases_name)).setText(selected.getName());
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

        if(savedInstanceState != null) {
            restoreFromParcel(savedInstanceState);
        } else {
            Bundle bun = getIntent().getExtras();
            if (bun != null) {
                movie = bun.getParcelable("movie");

                setTitle("Releases for " + movie.getTitle());

                //Get the list of releases
                String request = APIUtilities.formatRequest(
                        "media.get?id=" + movie.getLibraryId(), this);
                new GetReleasesTask(this).execute(request);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("movie", movie);
        outState.putParcelableArrayList("releases", releases);
    }

    //Reconstitute the saved state
    private void restoreFromParcel(Bundle savedInstanceState) {
        movie = savedInstanceState.getParcelable("movie");
        releases = savedInstanceState.getParcelableArrayList("releases");

        setTitle("Releases for " + movie.getTitle());
        populateList();
    }
    //Populate the list with the data in releases, or set the empty view to visible
    private void populateList() {
        if(list == null) {
            throw new IllegalStateException("List has not been correctly created/assigned.");
        }
        if(releases.size() <= 0) {
            findViewById(R.id.empty).setVisibility(View.VISIBLE);
        } else {
            ArrayAdapter<Release> adapter = new ArrayAdapter<Release>(ReleasesActivity.this,
                    R.layout.list_tight, releases);
            list.setAdapter(adapter);
        }
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

        //Update the status to ignored
        selected.setStatus("ignored");
        ((TextView) findViewById(R.id.releases_status)).setText("Status: ignored");
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

    //Get the list of releases CouchPotato has for that movie
    private class GetReleasesTask extends APIRequestAsyncTask<String, Void, String> {
        public GetReleasesTask(Context context) {
            super(context);
        }

        @Override
        protected void onPostExecute(String result) {
            parseReleases(result);
            populateList();
        }

        //Parse the JSON response and extract the releases out of it
        private void parseReleases(String result) {
            try {
                JSONArray rels = new JSONObject(result).getJSONObject("media").getJSONArray("releases");
                for(int i = 0; i < rels.length(); i++) {
                    JSONObject relSet = rels.getJSONObject(i);
                    JSONObject info = relSet.getJSONObject("info");
                    //ID, name, qualityID, statusID, provider name, age in days, size in MB
                    Release release = new Release(
                            relSet.getString("_id"), info.getString("name"),
                            relSet.getString("quality"), relSet.getString("status"),
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
    private class Release implements Parcelable {
        private String releaseId;
        private String name;
        private String quality;
        private String status;
        private String provider;
        private String age;
        private String size;

        public final Parcelable.Creator<Release> CREATOR =
                new Parcelable.Creator<Release>() {
                    public Release createFromParcel(Parcel par) {
                        return new Release(par);
                    }

                    public Release[] newArray(int size) {
                        return new Release[size];
                    }
                };

        public Release(String releaseId, String name, String quality, String status,
                       String provider, int age, int size) {
            this.releaseId = releaseId;
            this.name = name;
            this.quality = quality;
            this.status = status;
            this.provider = provider;
            this.age = String.valueOf(age) + " days";
            this.size = String.valueOf(size) + "MB";
        }

        public Release(Parcel par) {
            releaseId = par.readString();
            name = par.readString();
            quality = par.readString();
            status = par.readString();
            provider = par.readString();
            age = par.readString();
            size = par.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(releaseId);
            dest.writeString(name);
            dest.writeString(quality);
            dest.writeString(status);
            dest.writeString(provider);
            dest.writeString(age);
            dest.writeString(size);
        }

        public int describeContents() {
            return 0;
        }
        //Getters and setters
        public String getReleaseId() {
            return releaseId;
        }
        public String getName() {
            return name;
        }
        public String getQuality() {
            return quality;
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
        public String getStatus() {
            return status;
        }
        public void setStatus(String status) {
            this.status = status;
        }
    }

}

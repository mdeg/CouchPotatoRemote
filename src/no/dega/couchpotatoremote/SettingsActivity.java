package no.dega.couchpotatoremote;

import android.os.Bundle;
import android.preference.PreferenceActivity;

//TODO: add back button
//TODO: pop up number-only keypad for IP/port
//TODO: wifi/internet settings
public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}

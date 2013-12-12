package no.dega.couchpotatoremote;

import android.os.Bundle;
import android.preference.PreferenceActivity;

//TODO: wifi/internet settings

/*
Unfortunately there's no AppCompat version of PreferenceActivity/PreferenceFragment
This means that I can't add a back button here, or use a SettingsFragment. There's no solution for this.
Ignore the deprecation, nothing can be done without breaking backwards compatibility.
TODO: fix this up when/if AppCompat versions of PreferenceActivity come out
 */
public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

   /*   getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();*/
    }
}

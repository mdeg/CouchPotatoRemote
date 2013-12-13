package no.dega.couchpotatoremote;

import android.support.v4.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class AddMovieActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_movie);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Listener for when user hits search
        EditText editText = (EditText) findViewById(R.id.movie_to_add);
        editText.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    //Start fragment
                    doSearch(v.getText().toString());
                    handled = true;
                }
                return handled;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add_movie, menu);
        return true;
    }

    //Called when the search button next to the text field is pressed
    public void searchButtonPressed(View view) {
        EditText editText = (EditText) findViewById(R.id.movie_to_add);
        doSearch(editText.getText().toString());
    }

    //Hide the onscreen keyboard
    private void suppressOnscreenKeyboard() {
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void doSearch(String nameToSearch) {
        suppressOnscreenKeyboard();

        //Construct and submit the fragment
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        SearchResultsFragment fragment = new SearchResultsFragment();

        Bundle args = new Bundle();
        args.putString("NameToSearch", nameToSearch);
        fragment.setArguments(args);

        fragmentTransaction.replace(R.id.searchlist_placeholder, fragment).commit();
    }

}

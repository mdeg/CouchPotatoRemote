package no.dega.couchpotatoer;


import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.app.Fragment;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class AddMovieActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_movie);
		
		//Listener for when user hits search
		//TODO: add a button to the right of the search bar
		EditText editText = (EditText) findViewById(R.id.movie_to_add);
		editText.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				boolean handled = false;
				if(actionId == EditorInfo.IME_ACTION_SEARCH) {
					//Start fragment
					FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
					SearchResultsFragment fragment = new SearchResultsFragment();
					//TODO: fix this
					//fragmentTransaction.add(R.id.movie_to_add, fragment);
					fragmentTransaction.replace(R.id.searchlist_placeholder, fragment);
					fragmentTransaction.commit();
//					getFragmentManager().executePendingTransactions();

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

}

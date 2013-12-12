package no.dega.couchpotatoremote;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class ActorDirectorFragment extends ListFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_actor_director, container, false);
    }
    @Override
    public void onStart() {
        super.onStart();

        String[] names = getArguments().getStringArray("Names");

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                R.layout.adapter_actordirector, names);
        setListAdapter(adapter);
    }
}
package no.dega.couchpotatoremote;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ActorDirectorFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_actor_director, container, false);
    }
    @Override
    public void onStart() {
        super.onStart();
        setRetainInstance(true);

        String[] names = getArguments().getStringArray("Names");
        StringBuilder display = new StringBuilder();
        for(String str: names) {
            display.append(str).append("\n");
        }
        TextView tv = (TextView) getActivity().findViewById(R.id.actordirector_name);
        tv.setText(display.toString());
    }
}
package com.hp.myidea.guidedroid.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.hp.myidea.guidedroid.R;

public final class NavigationSectionFragment extends Fragment {

    public NavigationSectionFragment() {
        super();
        // TODO: class initialization
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_guide_droid_dummy, container, false);
        Spinner spinner = (Spinner) rootView.findViewById(R.id.rooms_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.room_names, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        return rootView;
    }
}


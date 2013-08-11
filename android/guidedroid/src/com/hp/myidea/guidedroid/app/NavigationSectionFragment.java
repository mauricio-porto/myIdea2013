package com.hp.myidea.guidedroid.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.hp.myidea.guidedroid.R;
import com.hp.myidea.guidedroid.base.DirectionListener;
import com.hp.myidea.guidedroid.base.IndoorNavigation;

public final class NavigationSectionFragment extends Fragment implements DirectionListener {

    private TextView txtAngle;
    private IndoorNavigation navigation;
    private float lastDirection;

    public NavigationSectionFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.display_orientation, container, false);
        txtAngle = (TextView)rootView.findViewById(R.id.txt_orientation);

        return rootView;
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onPause()
     */
    @Override
    public void onPause() {
        this.navigation.stopListen();
        this.navigation = null; // GC ???
        super.onPause();
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onResume()
     */
    @Override
    public void onResume() {
        super.onResume();
        this.navigation = new IndoorNavigation(getActivity());
        this.navigation.startListen(this);
    }

    @Override
    public void onDirectionChanged(float direction) {
        if (Math.abs(direction - lastDirection) > 10) {
            txtAngle.setText("" + direction);
        }
        lastDirection = direction;
    }
    
/*
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_guide_droid_dummy, container, false);
        Spinner spinner = (Spinner) rootView.findViewById(R.id.rooms_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.room_names, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        return rootView;
    }
*/}


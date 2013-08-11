package com.hp.myidea.guidedroid.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hp.myidea.guidedroid.R;
import com.hp.myidea.guidedroid.base.DirectionListener;
import com.hp.myidea.guidedroid.base.IndoorNavigation;

public final class NavigationSectionFragment extends Fragment implements DirectionListener {

    private TextView txtAngle;
    private CompassView myCompass;
    private IndoorNavigation navigation;

    public NavigationSectionFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.display_orientation, container, false);
        txtAngle = (TextView)rootView.findViewById(R.id.azimuth);
        myCompass = (CompassView)rootView.findViewById(R.id.mycompass);

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
        txtAngle.setText("Azimuth: " + direction);
        myCompass.update((float) Math.toRadians(direction));
    }
}


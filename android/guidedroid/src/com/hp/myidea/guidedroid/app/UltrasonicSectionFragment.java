package com.hp.myidea.guidedroid.app;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.os.Messenger;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hp.myidea.guidedroid.R;
import com.hp.myidea.guidedroid.service.BluetoothReceiver;

public final class UltrasonicSectionFragment extends Fragment {

    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;

    private boolean isConfigured = false;

    private BluetoothReceiver btReceiver;

    private boolean receiverSvcConnected = false;
    private boolean isBound = false;
    private boolean serviceRunning = false;
    private Messenger messageReceiver = null;

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_START_SERVICE = 3;

    public UltrasonicSectionFragment() {
        super();
        // TODO: class initialization
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_ultrasonic, container, false);
        return rootView;
    }
}

/**
 * 
 */
package com.hp.myidea.guidedroid.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.hp.myidea.guidedroid.base.BluetoothConnector;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

/**
 * @author mapo
 *
 */
public class BluetoothReceiver extends Service {

    private static final String TAG = BluetoothReceiver.class.getSimpleName();

    private static final int HRM_NOTIFICATIONS = 1;

    public static final String ACTION_START = "startService";
    public static final String ACTION_STOP = "stopService";

    // Message types sent from the BluetoothConnector Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Message types received from the activity messenger
    // MUST start by zero due the enum mapping
    public static final int CONNECT_TO = 0;
    public static final int GET_HRM_STATUS = 1;
    public static final int REGISTER_LISTENER = 2;
    public static final int UNREGISTER_LISTENER = 3;
    public static final int REGISTER_HANDLER = 4;
    public static final int UNREGISTER_HANDLER = 5;

    public static enum ACTION {
    	CONNECT_TO,
    	GET_HRM_STATUS,
    	REGISTER_LISTENER,
    	UNREGISTER_LISTENER,
    	REGISTER_HANDLER,
    	UNREGISTER_HANDLER
    }

    // Bluetooth and HRM statuses
    public static final int NONE = -1;
    public static final int HRM_NOT_CONFIGURED = 0;
    public static final int BT_DISABLED = 1;
    public static final int HRM_CONNECTED = 2;
    public static final int CONNECTING = 3;
    public static final int HRM_DATA = 4;
    public static final int LOCATION_DATA = 5;
    public static final int NOT_RUNNING = 6;

    public static enum BT_STATUS {
    	HRM_NOT_CONFIGURED,
    	BT_DISABLED,
    	HRM_CONNECTED,
    	CONNECTING,
    	HRM_DATA,
    	NOT_RUNNING
    }

    // Key names received
    public static final String DEVICE_NAME = "device_name";
    public static final String DEVICE_ADRESS = "device_address";
    public static final String TOAST = "toast";
    public static final String TEXT_MSG = "text";

    // Key names sent
    public static final String KEY_HRM_DATA = "hrm_data";
    public static final String KEY_LOCATION_DATA = "location_data";

    private static NotificationManager notifMgr;

    private Toast toast;
    private Vibrator vibrator;
    private boolean mustVibrate = true;

    private boolean running = false;

    private int mBTHRMStatus = NONE;

    private Messenger activityHandler = null;

    // MAC address of the HRM device
    private String mHRMdeviceAddress = null;
    // Name of the connected device
    private String mConnectedDeviceName = null;
    private boolean hrmConnected = false;

    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;

    private BluetoothConnector connector;
    private Notification notifier;

    // Stuff to deal with the different signatures for startForeground
    /*
     * @see http://developer.android.com/reference/android/app/Service.html#startForeground(int, android.app.Notification)
     * 
     */
    private static final Class[] mStartForegroundSignature = new Class[] {int.class, Notification.class};
    private static final Class[] mStopForegroundSignature = new Class[] {boolean.class};

    private Method mStartForeground;
    private Method mStopForeground;
    private Object[] mStartForegroundArgs = new Object[2];
    private Object[] mStopForegroundArgs = new Object[1];
    
    /**
     * This is a wrapper around the new startForeground method, using the older
     * APIs if it is not available.
     */
    void startForegroundCompat(int id, Notification notification) {
        // If we have the new startForeground API, then use it.
        if (mStartForeground != null) {
            mStartForegroundArgs[0] = Integer.valueOf(id);
            mStartForegroundArgs[1] = notification;
            try {
                mStartForeground.invoke(this, mStartForegroundArgs);
                Log.d(TAG, "Invoked startForeground(...)");
            } catch (InvocationTargetException e) {
                // Should not happen.
                Log.e(TAG, "Unable to invoke startForeground", e);
            } catch (IllegalAccessException e) {
                // Should not happen.
                Log.e(TAG, "Unable to invoke startForeground", e);
            }
            return;
        }

        // Fall back on the old API.
        stopForeground(true);
        notifMgr.notify(id, notification);
    }

    /**
     * This is a wrapper around the new stopForeground method, using the older
     * APIs if it is not available.
     */
    void stopForegroundCompat() {
        // If we have the new stopForeground API, then use it.
        if (mStopForeground != null) {
            mStopForegroundArgs[0] = Boolean.TRUE;
            try {
                mStopForeground.invoke(this, mStopForegroundArgs);
                Log.d(TAG, "Invoked stopForeground(...)");
            } catch (InvocationTargetException e) {
                // Should not happen.
                Log.w(TAG, "Unable to invoke stopForeground", e);
            } catch (IllegalAccessException e) {
                // Should not happen.
                Log.w(TAG, "Unable to invoke stopForeground", e);
            }
            return;
        }

        // Fall back on the old API.  Note to cancel BEFORE changing the
        // foreground state, since we could be killed at that point.
        stopForeground(false);
    }

    /* (non-Javadoc)
     * @see android.app.Service#onBind(android.content.Intent)
     */
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind()");
        String action = intent.getAction();
        if(action.equals("com.cardiotalkair.HRM_RECEIVER") || action.equals("com.cardiotalkair.service.IHRMReceiver")) {
            return activityMsgListener.getBinder();
        }
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate()");
        if ((mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()) == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();   // TODO: localize!!!
            return;
        }
        notifMgr = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
        this.vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        this.toast = Toast.makeText(this, TAG, Toast.LENGTH_LONG);
        this.toast.setGravity(Gravity.CENTER, 0, 0);

        try {
            mStartForeground = getClass().getMethod("startForeground", mStartForegroundSignature);
            mStopForeground = getClass().getMethod("stopForeground", mStopForegroundSignature);
        } catch (NoSuchMethodException e) {
            // Running on an older platform.
            mStartForeground = mStopForeground = null;
        }
        
        this.notifier = new Notification(R.drawable.ic_stat_notify_cardiotalk, "CardioTalk is running...", System.currentTimeMillis());

        this.notifier.setLatestEventInfo(this, "CardioTalk", "Monitoring your heart", this.buildIntent());	// TODO: Localize!!!!
        this.notifier.flags |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;

    }

    // This is the old onStart method that will be called on the pre-2.0
    // platform.  On 2.0 or later we override onStartCommand() so this
    // method will not be called.
    @Override
    public void onStart(Intent intent, int startId) {
        handleCommand(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleCommand(intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    private void handleCommand(Intent intent) {
        if (ACTION_START.equals(intent.getAction())) {
            Log.d(TAG, "\n\nhandleCommand() - START ACTION");
            startForegroundCompat(HRM_NOTIFICATIONS, this.notifier);
            this.init();
        } else if (ACTION_STOP.equals(intent.getAction())) {
            Log.d(TAG, "\n\nhandleCommand() - STOP ACTION");
            this.stopAll(); 
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        this.running = false;
		// Make sure our notification is gone.
        stopForegroundCompat();
	    super.onDestroy();
        //Process.killProcess(Process.myPid());
    }

    private void init() {
    	Log.d(TAG, "init()\n\n\n\n");

    	this.mustVibrate = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(ApplicationPreference.VIBRATE_KEY, true);

    	// Connect to the HRM device
        if (!mBluetoothAdapter.isEnabled()) {
            this.mBTHRMStatus = BT_DISABLED;
            this.notifyUser("Select to enable bluetooth.", "Must enable bluetooth.");
            return;
        }
        if (!this.connectKnownDevice()) {
    		this.mBTHRMStatus = HRM_NOT_CONFIGURED;
    		this.notifyUser("Select to configure HRM device.", "HRM device not configured.");
        	return;
        }
        this.notifyUser("CardioTalk is running. Select to see your heart data.", "CardioTalk is running...");
        this.running = true;
    }

	private void stopAll() {
    	Log.d(TAG, "\n\n\n\nstopAll()\n\n\n\n");
        hrmConnected = false;
        if (this.connector != null) {
        	this.connector.stop();
        }
       
        this.notifyUser("Stopped. Select to start again.", "Stopping CardioTalk.");
		this.running = false;
    }

    private boolean connectKnownDevice() {
    	if (hrmConnected) {
    		Log.d(TAG, "\n\n\n\n\n\nconnectDevice():: hrmConnected says it is already connected!!!! Wrong?!?!?!");
    		return true;
    	}
        this.restoreState();
        if (this.mHRMdeviceAddress != null && this.mHRMdeviceAddress.length() > 0) {
            this.connectDevice(this.mHRMdeviceAddress);
            return true;
        }
		return false;    	
    }

    private void connectDevice(String deviceAddress) {
    	this.mBTHRMStatus = CONNECTING;
        if (this.connector == null) {
            this.connector = new BluetoothConnector(this, mHandler);
        }
        this.connector.connect(mBluetoothAdapter.getRemoteDevice(deviceAddress));
    }

    private void restoreState() {
        // Restore state
        SharedPreferences state = this.getSharedPreferences(ApplicationPreference.SHARED_PREFS_FILE, 0);
        this.mHRMdeviceAddress = state.getString("HRMdeviceAddress", null);
    }

    private void storeState() {
        // Persist state
        SharedPreferences state = this.getSharedPreferences(ApplicationPreference.SHARED_PREFS_FILE, 0);
        SharedPreferences.Editor editor = state.edit();
        editor.putString("HRMdeviceAddress", this.mHRMdeviceAddress);
        editor.commit();
    }

    private PendingIntent buildIntent() {
        Intent intent = new Intent(this, CardioTalk.class);

        //intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        
        return PendingIntent.getActivity(this, 0, intent, 0);
    }

    /**
     * Show a notification
     */
    private void notifyUser(String action, String alert) {
        CharSequence serviceName = "CardioTalk";  //super.getText(R.string.service_name);
        CharSequence actionText = action;	//"Monitoring your heart...";  //super.getText(R.string.something);
        CharSequence notificationText = alert;	//"CardioTalk is running.";  //super.getText(R.string.something);
        this.notifier = new Notification(R.drawable.ic_stat_notify_cardiotalk, notificationText, System.currentTimeMillis());
        this.notifier.setLatestEventInfo(this, serviceName, actionText, this.buildIntent());	// TODO: Localize!!!!
        notifMgr.notify(HRM_NOTIFICATIONS, this.notifier);
        this.thumpthump();
    }

    /**
     * Show a toast with the given text.
     *
     * @param message string to show (if null, nothing will be shown)
     */
    private void showToast(String toastText) {
        if (toastText != null) {

            this.thumpthump();

            Message msg = mHandler.obtainMessage(MESSAGE_TOAST);
            Bundle bundle = new Bundle();
            bundle.putString(TOAST, toastText);
            msg.setData(bundle);
            mHandler.sendMessage(msg);

        }
    }

    private void thumpthump() {
    	if (this.mustVibrate) {
    		this.vibrator.vibrate(new long[]{50, 200, 50, 50, 500, 200, 50, 50, 500, 200, 50, 50, 500}, -1);
    	}
    }

    // The Handler that gets information back from the BluetoothConnector
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case BluetoothConnector.STATE_CONNECTED:
                    BluetoothReceiver.this.mBTHRMStatus = HRM_CONNECTED;
                    hrmConnected = true;
                    notifyBTState();
                    break;
                case BluetoothConnector.STATE_CONNECTING:
                    BluetoothReceiver.this.mBTHRMStatus = CONNECTING;
                    notifyBTState();
                    break;
                case BluetoothConnector.STATE_FAILED:
                	BluetoothReceiver.this.mBTHRMStatus = HRM_NOT_CONFIGURED;
                    notifyBTState();
                	break;
                case BluetoothConnector.STATE_LISTEN:
                case BluetoothConnector.STATE_NONE:
                    break;
                }
                break;
            case MESSAGE_WRITE:
                break;
            case MESSAGE_READ:
                Log.d(TAG, "Data received.");
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                mHRMdeviceAddress = msg.getData().getString(DEVICE_ADRESS);
                storeState();
                showToast("Connected to " + mConnectedDeviceName);
                break;
            case MESSAGE_TOAST:
            	BluetoothReceiver.this.toast.setText(msg.getData().getString(TOAST));
            	BluetoothReceiver.this.toast.show();
                break;
            }
        }
    };

    private void notifyNotRunning() {
        if (activityHandler != null) {
        	try {
				activityHandler.send(Message.obtain(null, NOT_RUNNING, null));
			} catch (RemoteException e) {
				// Nothing to do
			}
        }
    }

    private void notifyBTState() {
        if (activityHandler != null) {
        	if (this.mBTHRMStatus > NONE) {
        		Log.d(TAG, "notifyBTState() - " + BT_STATUS.values()[this.mBTHRMStatus]);
        	}
        	try {
				activityHandler.send(Message.obtain(null, this.mBTHRMStatus, null));
			} catch (RemoteException e) {
				// Nothing to do
			}
        } else {
        	Log.d(TAG, "notifyBTState() - NO Activity handler to receive!");
        }
    }

    private void sendData(byte[] data, Messenger messenger) {
    	if (messenger != null && data != null) {
            Message msg = Message.obtain(null, HRM_DATA);
            Bundle bundle = new Bundle();
            bundle.putByteArray(KEY_HRM_DATA, data);
            msg.setData(bundle);
        	try {
				messenger.send(msg);
			} catch (RemoteException e) {
				// Nothing to do
			}    		
    	}
    }

    /**
     * Handler of incoming messages from clients, i.e., CardioTalk activity.
     */
    final Handler activityMessages = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "Received message: " + ACTION.values()[msg.what]);
            switch (msg.what) {
            case GET_HRM_STATUS:
            	break;
            case CONNECT_TO:
            	String rcvdAddress = msg.getData().getString(TEXT_MSG);
            	Log.i(TAG, "Received address: " + rcvdAddress);
            	if (rcvdAddress == null || rcvdAddress.length() == 0 ) {
            		connectKnownDevice();
            	} else {
            		connectDevice(rcvdAddress);
            	}
            	break;
            case REGISTER_LISTENER:
            	break;
            case UNREGISTER_LISTENER:
            	activityHandler = null;
            	break;
            case REGISTER_HANDLER:
            	activityHandler = msg.replyTo;
            	// TODO: MUST notify if we are running already
/*            	if (!running) {
            		notifyNotRunning();
            		break;
            	}
*/            	notifyBTState();
            	break;
            case UNREGISTER_HANDLER:
            	activityHandler = null;
            	break;
            default:
            	break;
            }
        }
    };
    final Messenger activityMsgListener = new Messenger(activityMessages);

}

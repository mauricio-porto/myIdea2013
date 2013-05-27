package com.hp.myidea.guidedroid;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.hp.myidea.guidedroid.base.Communicator;
import com.hp.myidea.guidedroid.service.BluetoothReceiver;

public class GuideDroid extends Activity {
	private static final String TAG = GuideDroid.class.getSimpleName();

	// Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;

    // MAC address of the HRM device
    private String arduinoBluetoothAddress = null;
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
    
    private Communicator communicator;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();   // TODO: localize!!!
            finish();
            return;
        }
        this.startBTReceiver();
        
        this.communicator = new Communicator(this);

        ImageButton btn = (ImageButton) this.findViewById(R.id.btn_tone);
        btn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				toggleTone();
			}
		});

        btn = (ImageButton) this.findViewById(R.id.btn_speech);
        btn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				toggleSpeech();
			}
		});

        btn = (ImageButton) this.findViewById(R.id.btn_minus);
        btn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				decreaseDist();
			}
		});

        btn = (ImageButton) this.findViewById(R.id.btn_plus);
        btn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				increaseDist();
			}
		});

        btn = (ImageButton) this.findViewById(R.id.btn_power);
        btn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				toggelPower();
			}
		});

        btn = (ImageButton) this.findViewById(R.id.btn_help);
        btn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				speakHelp();
			}
		});
	}

	private void startBTReceiver() {
		Log.d(TAG, "\t\t\t\t\tWILL START!!!!");
		Intent intent = new Intent(BluetoothReceiver.ACTION_START);
		intent.setClass(this, BluetoothReceiver.class);
		startService(intent);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!this.isBound) {
        	this.isBound = this.bindService(new Intent("com.hp.myidea.guidedroid.service.BluetoothReceiver"), this.btReceiverConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unbindBTReceiver();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	String[] results = {"OK","CANCELED","FIRST_USER"};
        Log.d(TAG, "onActivityResult with code: " + ((resultCode < 2)?results[1+resultCode]:"User defined"));
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE:
            // When BluetoothDeviceList returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                // Get the device MAC address
                String address = data.getExtras().getString(BluetoothDeviceList.EXTRA_DEVICE_ADDRESS);
                // Attempt to connect to the device
                Log.d(TAG, "\n\n\n\nonActivityResult() - O ENDERECO DO DEVICE EH: " + address + " e receciverSvcConnected diz: " + this.receiverSvcConnected + "\n\n\n\n");
            	if (address != null) {
            		this.sendTextToService(BluetoothReceiver.CONNECT_TO, address);
            	}
            	break;
            }
            // User did not enable Bluetooth or an error occurred
            Log.d(TAG, "\t\t\tHRM selection failed. Giving up...");
            Toast.makeText(this, R.string.none_paired, Toast.LENGTH_SHORT).show();
            finish();
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so attempt to connect a device
            	this.sendTextToService(BluetoothReceiver.CONNECT_TO, null);
            } else {
                // User did not enable Bluetooth or an error occurred
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
            break;
        }
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

    private ServiceConnection btReceiverConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "BluetoothReceiver connected");
            if (service == null) {
            	Log.e(TAG, "Connection to the BluetoothReceiver service failed. Giving up...");
            	return;
            }
        	receiverSvcConnected = true;

        	messageReceiver = new Messenger(service);
            try {
                Message msg = Message.obtain(null, BluetoothReceiver.REGISTER_HANDLER);
                msg.replyTo = serviceMsgReceiver;
                messageReceiver.send(msg);
            } catch (RemoteException e) {
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "BluetoothReceiver disconnected");
            receiverSvcConnected = false;
        }

    };

    private void unbindBTReceiver() {
    	Log.d(TAG, "unbindBluetoothReceiver() - supposing it is bound");
    	if (this.isBound) {
            if (messageReceiver  != null) {
                try {
                    Message msg = Message.obtain(null, BluetoothReceiver.UNREGISTER_HANDLER);
                    msg.replyTo = serviceMsgReceiver;
                    messageReceiver.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service has crashed.
                }
            }
            this.unbindService(btReceiverConnection);
    	} else {
    		Log.d(TAG, "unbindHRMReceiver() - \tBut it was not!!!");
    	}
    	this.receiverSvcConnected = false;
    	this.isBound = false;
    }

    /**
     * Handler of incoming messages from BluetoothReceiver.
     */
   final Handler serviceMessages = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	if (msg.what < 0) {
        		return;
        	}
            Log.i(TAG, "Received message: " + BluetoothReceiver.BT_STATUS.values()[msg.what]);
            switch (msg.what) {
            case BluetoothReceiver.ARDUINO_DATA:
            	break;
            case BluetoothReceiver.BT_DISABLED:
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                break;
            case BluetoothReceiver.ARDUINO_NOT_CONFIGURED:
                // Launch the BluetoothDeviceList to see devices and do scan
                Intent serverIntent = new Intent(GuideDroid.this, BluetoothDeviceList.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            	break;
            case BluetoothReceiver.ARDUINO_CONNECTED:
            	break;
            case BluetoothReceiver.CONNECTING:
                Toast.makeText(GuideDroid.this, R.string.title_connecting, Toast.LENGTH_SHORT).show();
            	break;
            case BluetoothReceiver.NOT_RUNNING:
            	serviceRunning = false;
            	//startActivityForResult(new Intent().setClass(CardioTalk.this, Controller.class), REQUEST_START_SERVICE);
            	break;
            default:
            	break;
            }
        }
    };
    final Messenger serviceMsgReceiver = new Messenger(serviceMessages);

    private void sendTextToService(int what, String text) {
        if (messageReceiver != null) {
            Message msg = Message.obtain(null, what);
            Bundle bundle = new Bundle();
            bundle.putString(BluetoothReceiver.TEXT_MSG, text);
            msg.setData(bundle);
        	try {
				messageReceiver.send(msg);
			} catch (RemoteException e) {
				// Nothing to do
			}
        } else {
        	Log.d(TAG, "sendTextToService() - NO Service handler to receive!");
        }    	
    }

    private void toggleTone() {
    	this.communicator.sayIt("Toggle tone pressed");
    }
    
    private void toggleSpeech() {
    	this.communicator.sayIt("Toggle speech pressed");
    	
    }
    
    private void decreaseDist() {
    	this.communicator.sayIt("Decrease minimal distance pressed");
    	
    }
    
    private void increaseDist() {
    	this.communicator.sayIt("Increase minimal distance pressed");
    	
    }

    private void toggelPower() {
    	this.communicator.sayIt("Toggle on/off pressed");
    	
    }

    private void speakHelp() {
    	this.communicator.sayIt("Speak help pressed");
    	
    }

}

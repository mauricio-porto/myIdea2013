/**
 * 
 */
package com.hp.myidea.guidedroid.app;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

import com.hp.myidea.guidedroid.R;

/**
 * @author mauricio
 *
 */
public class DisplayOrientation extends Activity {
	
	private SensorManager mSensorService;
	private Sensor mAccelerometer;
	private Sensor mMagneticField;
	private float[] mGravity;
	private float[] mMagnetic;
	
	private TextView txtAngle;

	/**
	 * 
	 */
	public DisplayOrientation() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.display_orientation);

		txtAngle = (TextView)findViewById(R.id.azimuth);

		mSensorService = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);

		mAccelerometer = mSensorService.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mMagneticField = mSensorService.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		mSensorService.unregisterListener(mySensorEventListener, mAccelerometer);
		mSensorService.unregisterListener(mySensorEventListener, mMagneticField);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		mSensorService.registerListener(mySensorEventListener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		mSensorService.registerListener(mySensorEventListener, mMagneticField, SensorManager.SENSOR_DELAY_NORMAL);
	}

	private SensorEventListener mySensorEventListener = new SensorEventListener() {

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			switch (event.sensor.getType()) {
				case Sensor.TYPE_ACCELEROMETER:
					mGravity = event.values.clone();
					break;
				case Sensor.TYPE_MAGNETIC_FIELD:
					mMagnetic = event.values.clone();
					break;
				default:
					return;
			}
			if (mGravity != null && mMagnetic != null) {
				txtAngle.setText("" + getDirection());
			}
		}
	};

	/**
	 * http://capycoding.blogspot.com.br/2012/10/get-angle-from-sensor-in-android.html
	 */
	private float getDirection() {

		float[] temp = new float[9];
		float[] R = new float[9];
		// Load rotation matrix into R
		SensorManager.getRotationMatrix(temp, null, mGravity, mMagnetic);

		// Remap to camera's point-of-view
		SensorManager.remapCoordinateSystem(temp, SensorManager.AXIS_X, SensorManager.AXIS_Z, R);

		// Return the orientation values
		float[] values = new float[3];
		SensorManager.getOrientation(R, values);

		// Convert to degrees
		for (int i = 0; i < values.length; i++) {
			Double degrees = (values[i] * 180) / Math.PI;
			values[i] = degrees.floatValue();
		}
		return values[0];
	}

}

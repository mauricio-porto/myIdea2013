/**
 * 
 */
package com.hp.myidea.guidedroid.base;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.util.Log;

/**
 * @author mauricio
 * 
 */
public class IndoorNavigation {

	private static final String TAG = IndoorNavigation.class.getSimpleName();

	private Context owner;
	private SensorManager mSensorService;
	private Sensor mAccelerometer;
	private Sensor mMageneticField;
	private float[] mGravity;
	private float[] mMagnetic;

	/**
	 * 
	 */
	public IndoorNavigation(Context context) {
		super();
		this.owner = context;
		this.init();
	}

	private void init() {
		mSensorService = (SensorManager) this.owner.getSystemService(Context.SENSOR_SERVICE);

		mAccelerometer = mSensorService.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mMageneticField = mSensorService.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

		mSensorService.registerListener(mySensorEventListener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		mSensorService.registerListener(mySensorEventListener, mMageneticField, SensorManager.SENSOR_DELAY_NORMAL);
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
				getDirection();
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
		// angle between the magnetic north directio
	    // 0=North, 90=East, 180=South, 270=West
		return values[0];
	}

	public static int calculateDistance(Location src, Location dst) {
		if (src != null && dst != null) {
			return Math.round(src.distanceTo(dst));
		}
		return 0;
	}

	/**
	 * 
	 * Um milionésimo de grau corresponde aproximadamente a 1 metro. Seis casas
	 * decimais nos darão essa precisão.
	 * 
	 */
	public static void testCalculate() {
		Location src = new Location("AdHoc");
		src.setLongitude(-51.21662651);
		src.setLatitude(-30.14779148);
		src.setAltitude(34);

		Location dst270 = new Location("AdHoc");
		dst270.setLongitude(-51.21663651);
		dst270.setLatitude(-30.14779148);
		dst270.setAltitude(43);

		Log.d(TAG, "\n\n\n\n\nDistancia: " + IndoorNavigation.calculateDistance(src, dst270));
		Log.d(TAG, "\n\n\n\n\nDistance: " + src.distanceTo(dst270));

		Location dst90 = new Location("AdHoc");
		dst90.setLongitude(-51.21661651);
		dst90.setLatitude(-30.14779148);

		Location dst180 = new Location("AdHoc");
		dst180.setLongitude(-51.21662651);
		dst180.setLatitude(-30.14789148);

		Log.d(TAG, "\n\n\n\n\nAngulo (deve ser 90): " + src.bearingTo(dst90));
		Log.d(TAG, "\n\n\n\n\nAngulo (deve ser 180): " + src.bearingTo(dst180));
		Log.d(TAG, "\n\n\n\n\nAngulo (deve ser 270): " + src.bearingTo(dst270));
	}
}

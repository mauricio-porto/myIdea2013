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
	private SensorManager sensorService;
	private Sensor sensor;

	/**
	 * 
	 */
	public IndoorNavigation(Context context) {
		super();
		this.owner = context;
		this.init();
	}

	private void init() {
		sensorService = (SensorManager) this.owner.getSystemService(Context.SENSOR_SERVICE);

		Sensor mAccelerometer = sensorService.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		Sensor mMageneticField = sensorService.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

		sensorService.registerListener(mySensorEventListener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		sensorService.registerListener(mySensorEventListener, mMageneticField, SensorManager.SENSOR_DELAY_NORMAL);
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
	private float[] mGravity;
	private float[] mMagnetic;

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

		Location dst = new Location("AdHoc");
		dst.setLongitude(-51.21663651);
		dst.setLatitude(-30.14779148);
		src.setAltitude(43);

		Log.d(TAG, "\n\n\n\n\nDistancia: " + IndoorNavigation.calculateDistance(src, dst));
		Log.d(TAG, "\n\n\n\n\nDistance: " + src.distanceTo(dst));
	}
}

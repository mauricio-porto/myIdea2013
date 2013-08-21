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
	private DirectionListener listener;
	private SensorManager mSensorService;
	private Sensor mAccelerometer;
	private Sensor mMagneticField;

	private float[] valuesAccelerometer;
    private float[] valuesMagneticField;
    private float smoothed[] = new float[3];

    private float[] orientation;
    private float[] rotation;
    private float[] matrixValues;


	/**
	 * 
	 */
	public IndoorNavigation(Context context) {
		super();
		this.owner = context;
		this.init();
	}

	public void startListen(DirectionListener listener) {
	    this.listener = listener;
        mSensorService.registerListener(mySensorEventListener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorService.registerListener(mySensorEventListener, mMagneticField, SensorManager.SENSOR_DELAY_NORMAL);
	}

	public void stopListen() {
        mSensorService.unregisterListener(mySensorEventListener, mAccelerometer);
        mSensorService.unregisterListener(mySensorEventListener, mMagneticField);
        this.listener = null;
	}

	private void init() {
		mSensorService = (SensorManager) this.owner.getSystemService(Context.SENSOR_SERVICE);

		mAccelerometer = mSensorService.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mMagneticField = mSensorService.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

		valuesAccelerometer = new float[3];
        valuesMagneticField = new float[3];

        orientation = new float[9];
        rotation = new float[9];
        matrixValues = new float[3];
	}

	private SensorEventListener mySensorEventListener = new SensorEventListener() {

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}

		@Override
		public void onSensorChanged(SensorEvent event) {

	        switch (event.sensor.getType()) {
	        case Sensor.TYPE_ACCELEROMETER:
	            smoothed = LowPassFilter.filter(event.values, valuesAccelerometer);
	            for (int i = 0; i < 3; i++) {
	                valuesAccelerometer[i] = smoothed[i];
	            }
	            break;
	        case Sensor.TYPE_MAGNETIC_FIELD:
	            smoothed = LowPassFilter.filter(event.values, valuesMagneticField);
	            for (int i = 0; i < 3; i++) {
	                valuesMagneticField[i] = smoothed[i];
	            }
	            break;
	        }

	        boolean success = SensorManager.getRotationMatrix(orientation, rotation, valuesAccelerometer, valuesMagneticField);

	        if (success) {
	            //float[] temp = new float[9];

	            // Remap to camera's point-of-view
	            //SensorManager.remapCoordinateSystem(orientation, SensorManager.AXIS_X, SensorManager.AXIS_Z, temp);

	            SensorManager.getOrientation(orientation, matrixValues);

	            //double azimuth = Math.toDegrees(matrixValues[0]);
	            //double pitch = Math.toDegrees(matrixValues[1]);
	            //double roll = Math.toDegrees(matrixValues[2]);

	            double floatBearing = matrixValues[0];

	            // Convert from radians to degrees
	            floatBearing = Math.toDegrees(floatBearing); // degrees east of true north (180 to -180)

	            // Compensate for the difference between true north and magnetic north
	            // if (gmf != null) floatBearing += gmf.getDeclination();

	            // adjust to 0-360
	            if (floatBearing < 0) floatBearing += 360;

	            if (listener != null) {
                    listener.onDirectionChanged((float) floatBearing);
                }
	        }
		}
	};

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
		dst90.setLongitude(-51.21652651);
		dst90.setLatitude(-30.14779148);

		Location dst180 = new Location("AdHoc");
		dst180.setLongitude(-51.21662651);
		dst180.setLatitude(-30.14789148);

		Log.d(TAG, "\n\n\n\n\nAngulo (deve ser 90): " + src.bearingTo(dst90));
        Log.d(TAG, "\n\n\n\n\nDistance: " + src.distanceTo(dst90));
		Log.d(TAG, "\n\n\n\n\nAngulo (deve ser 180): " + src.bearingTo(dst180));
		Log.d(TAG, "\n\n\n\n\nAngulo (deve ser 270): " + src.bearingTo(dst270));
	}
	
	public static void checkData() {
	    
	    // Prendedores
        Location src = new Location("AdHoc");
        src.setLongitude(-51.216933);
        src.setLatitude(-30.147798);
	    
        Location lixo = new Location("AdHoc");
        lixo.setLongitude(-51.21698243946563);
        lixo.setLatitude(-30.147758133159815);
        Log.d(TAG, "\n\n\n\n\nDistancia ate lixo: " + src.distanceTo(lixo));
        Log.d(TAG, "Angulo (deve ser complemento de 313): " + src.bearingTo(lixo));
        
        Location janela = new Location("AdHoc");
        janela.setLongitude(-51.21699019997152);
        janela.setLatitude(-30.147797999987613);
        Log.d(TAG, "\n\n\n\n\nDistancia ate janela: " + src.distanceTo(janela));
        Log.d(TAG, "Angulo (deve ser complemento de 270): " + src.bearingTo(janela));
        
	}
}

package com.sensormanager;

import android.os.Bundle;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.support.annotation.Nullable;

import java.io.*;
import java.util.Date;
import java.util.Timer;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.ReactApplicationContext;

public class UserAccelerationRecord implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mUserAcceleration;
    private long lastUpdate = 0;
    private int i = 0, n = 0;
	private int delay;
	private int isRegistered = 0;

	private ReactContext mReactContext;
	private Arguments mArguments;


    public UserAccelerationRecord(ReactApplicationContext reactContext) {
        mSensorManager = (SensorManager)reactContext.getSystemService(reactContext.SENSOR_SERVICE);
        mUserAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		mReactContext = reactContext;
    }

	public int start(int delay) {
		this.delay = delay;
		if (mUserAcceleration != null && isRegistered == 0) {
			mSensorManager.registerListener(this, mUserAcceleration, SensorManager.SENSOR_DELAY_NORMAL);
			isRegistered = 1;
			return (1);
		}
		return (0);
	}

    public void stop() {
		if (isRegistered == 1) {
			mSensorManager.unregisterListener(this);
			isRegistered = 0;
		}
    }

	private void sendEvent(String eventName, @Nullable WritableMap params)
	{
		try {
			mReactContext 
				.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class) 
				.emit(eventName, params);
		} catch (RuntimeException e) {
			Log.e("ERROR", "java.lang.RuntimeException: Trying to invoke JS before CatalystInstance has been set!");
		}
	}

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;
		WritableMap map = mArguments.createMap();

        if (mySensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            long curTime = System.currentTimeMillis();
            i++;
            if ((curTime - lastUpdate) > delay) {
				i = 0;
				float uaX = sensorEvent.values[0] / 9.8;
				float uaY = sensorEvent.values[1] / 9.8;
				float uaZ = sensorEvent.values[2] / 9.8;
				map.putDouble("x", uaX);
				map.putDouble("y", uaY);
				map.putDouble("z", uaZ);
				sendEvent("UserAcceleration", map);
                lastUpdate = curTime;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}

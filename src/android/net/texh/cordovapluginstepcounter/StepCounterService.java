package net.texh.cordovapluginstepcounter;

/*
    Copyright 2015 Jarrod Linahan <jarrod@texh.net>

    Permission is hereby granted, free of charge, to any person obtaining
    a copy of this software and associated documentation files (the
    "Software"), to deal in the Software without restriction, including
    without limitation the rights to use, copy, modify, merge, publish,
    distribute, sublicense, and/or sell copies of the Software, and to
    permit persons to whom the Software is furnished to do so, subject to
    the following conditions:

    The above copyright notice and this permission notice shall be
    included in all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
    EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
    MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
    NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
    LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
    OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
    WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 */

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class StepCounterService extends Service implements SensorEventListener {

    private final  String TAG        = "StepCounterService";
    private final  IBinder mBinder   = new StepCounterServiceBinder();
    private static boolean isRunning = false;

    private SensorManager mSensorManager;
    private Sensor        mStepSensor;
    private Integer       offset        = 0;
    private Integer       stepsCounted  = 0;
    private Boolean       haveSetOffset = false;

    public Integer getStepsCounted() {
        return stepsCounted;
    }

    public void stopTracking() {
        Log.i(TAG, "Setting isRunning flag to false");
        isRunning = false;
        mSensorManager.unregisterListener(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind" + intent);
        return mBinder;
    }

    public class StepCounterServiceBinder extends Binder {
        StepCounterService getService() {
            // Return this instance of StepCounterService so clients can call public methods
            return StepCounterService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");

        // Do some setup stuff
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onCreate");

        if (isRunning /* || has no step sensors */) {
            Log.i(TAG, "Not initialising sensors");
            return Service.START_REDELIVER_INTENT;
        }

        Log.i(TAG, "Initialising sensors");
        doInit();

        isRunning = true;
        return START_REDELIVER_INTENT;
    }


    public void doInit() {
        Log.i(TAG, "Registering STEP_DETECTOR sensor");
        stepsCounted  = 0;
        offset        = 0;
        haveSetOffset = false;

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mStepSensor    = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mSensorManager.registerListener(this, mStepSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public boolean stopService(Intent intent) {
        Log.i(TAG, "- Received stop: " + intent);
        isRunning = false;
        return super.stopService(intent);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Log.i(TAG, "onSensorChanged event!");
        Integer steps = Math.round(sensorEvent.values[0]);

        if (!haveSetOffset) {
            offset        = steps - 1;
            stepsCounted  = 1;
            haveSetOffset = true;

            Log.i(TAG, "  * Updated offset: " + offset);
        } else {
            Log.i(TAG, "  * Setting stepsCounted:");
            stepsCounted = steps - offset;
            Log.i(TAG, "    - " + stepsCounted);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        Log.i(TAG, "onAccuracyChanged: " + sensor);
        Log.i(TAG, "  Accuracy: " + i);
    }
}

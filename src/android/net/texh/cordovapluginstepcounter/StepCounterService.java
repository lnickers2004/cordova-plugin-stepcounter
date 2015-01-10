package net.texh.cordovapluginstepcounter;

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

    private final  String TAG        = "StepServiceCounter";
    private final IBinder mBinder    = new StepCounterServiceBinder();
    private static boolean isRunning = false;

    private SensorManager mSensorManager;
    private Sensor        mStepSensor;
    private Integer       offset        = 0;
    private Integer       stepsCounted  = 0;
    private Boolean       haveSetOffset = false;

    public Integer getStepsCounted() {
        return stepsCounted;
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
        //super.onStartCommand(intent, flags, startId);
        Log.i(TAG, "onCreate");

        if (isRunning /* || has no step sensors */) {
            return Service.START_REDELIVER_INTENT;
        }

        initSensors();

        isRunning = true;
        return START_REDELIVER_INTENT;
    }


    public void initSensors() {
        Log.i(TAG, "Registering STEP_DETECTOR sensor");
        stepsCounted  = 0;
        offset        = 0;
        haveSetOffset = false;

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mStepSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mSensorManager.registerListener(this, mStepSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public boolean stopService(Intent intent) {
        Log.i(TAG, "- Received stop: " + intent);
        Toast.makeText(this, "Background step counter stopped", Toast.LENGTH_SHORT).show();
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

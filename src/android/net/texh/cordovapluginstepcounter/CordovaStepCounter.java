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

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

public class CordovaStepCounter extends CordovaPlugin {

    private final String TAG = "CordovaStepCounter";

    //private final String ACTION_CONFIGURE        = "configure";
    private final String ACTION_START            = "start";
    private final String ACTION_STOP             = "stop";
    private final String ACTION_GET_STEPS        = "get_step_count";
    private final String ACTION_CAN_COUNT_STEPS  = "can_count_steps";

    private Intent  stepCounterIntent;
    private Boolean isEnabled    = false;

    private StepCounterService stepCounterService;
    boolean bound = false;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            StepCounterService.StepCounterServiceBinder binder = (StepCounterService.StepCounterServiceBinder) service;
            stepCounterService = binder.getService();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
        LOG.i(TAG, "execute()");
        Boolean result = true;

        Activity activity = this.cordova.getActivity();
        stepCounterIntent = new Intent(activity, StepCounterService.class);
        activity.bindService(stepCounterIntent, mConnection, Context.BIND_AUTO_CREATE);

        if (ACTION_CAN_COUNT_STEPS.equals(action)) {
            Boolean can = deviceHasStepCounter(activity.getPackageManager());
            Log.i(TAG, "Checking if device has step counter APIS: "+ can);
            callbackContext.success( can ? 1 : 0 );
        }
        else if (ACTION_START.equals(action)) {
            Log.i(TAG, "Starting StepCounterService");
            isEnabled = true;
            activity.startService(stepCounterIntent);
        }
        else if (ACTION_STOP.equals(action)) {
            Log.i(TAG, "Stopping StepCounterService");
            isEnabled = false;
            activity.stopService(stepCounterIntent);
        }
        else if (ACTION_GET_STEPS.equals(action)) {
            if (isEnabled && bound) {
                Integer steps = stepCounterService.getStepsCounted();
                Log.i(TAG, "Geting steps counted from stepCounterService: " + steps);
                callbackContext.success(steps);
            } else {
                Log.i(TAG, "Can't get steps from stepCounterService as we're not enabled / bound - returning 0");
                callbackContext.success(0);
            }
        } else {
            Log.e(TAG, "Invalid action called on class " + TAG + ", " + action);
            callbackContext.error("Invalid action called on class " + TAG + ", " + action);
        }

        return result;
    }

    public static boolean deviceHasStepCounter(PackageManager pm) {
        // Require at least Android KitKat
        int currentApiVersion = Build.VERSION.SDK_INT;

        // Check that the device supports the step counter and detector sensors
        return currentApiVersion >= 19
                && pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER)
                && pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_DETECTOR);
    }

}
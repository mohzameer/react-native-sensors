package com.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

public class MagLessRNSensor extends ReactContextBaseJavaModule implements SensorEventListener {

  private final ReactApplicationContext reactContext;
  private final SensorManager sensorManager;
  private final Sensor sensor;
  private double lastReading = (double) System.currentTimeMillis();
  private int interval;
  private Arguments arguments;
  private int logLevel = 0;
  private String sensorName;
  private final float[] mMagnet = new float[3];               // magnetic field vector
  private final float[] mAcceleration = new float[3];         // accelerometer vector
  private final float[] mAccMagOrientation = new float[3];    // orientation angles from mAcceleration and mMagnet
  private float[] mRotationMatrix = new float[9];

  public MagLessRNSensor(ReactApplicationContext reactContext, String sensorName) {
    super(reactContext);
    this.reactContext = reactContext;
    this.sensorName = sensorName;
    this.sensorManager = (SensorManager)reactContext.getSystemService(reactContext.SENSOR_SERVICE);
    this.sensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
  }

  // RN Methods
  @ReactMethod
  public void isAvailable(Promise promise) {
    if (this.sensor == null) {
      // No sensor found, throw error
      promise.reject(new RuntimeException("No " + this.sensorName + " found"));
      return;
    }
    promise.resolve(null);
  }

  @ReactMethod
  public void setUpdateInterval(int newInterval) {
    this.interval = newInterval;
  }

  @ReactMethod
  public void setLogLevel(int newLevel) {
    this.logLevel = newLevel;
  }

  @ReactMethod
  public void startUpdates() {
    // Milliseconds to Microseconds conversion
    sensorManager.registerListener(this, sensor, this.interval * 1000);
  }

  @ReactMethod
  public void stopUpdates() {
    sensorManager.unregisterListener(this);
  }

  @Override
  public String getName() {
    return this.sensorName;
  }

  private static double sensorTimestampToEpochMilliseconds(long elapsedTime) {
    // elapsedTime = The time in nanoseconds at which the event happened.
    return System.currentTimeMillis() + ((elapsedTime- SystemClock.elapsedRealtimeNanos())/1000000L);
  }

  // SensorEventListener Interface
  private void sendEvent(String eventName, @Nullable WritableMap params) {
    try {
      this.reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
        .emit(eventName, params);
    } catch (RuntimeException e) {
      Log.e("ERROR", "java.lang.RuntimeException: Trying to invoke Javascript before CatalystInstance has been set!");
    }
  }

  @Override
  public void onSensorChanged(SensorEvent sensorEvent) {
    WritableMap map = this.arguments.createMap();
    map.putDouble("yaw", 12);
    map.putDouble("pitch", 12);
    map.putDouble("roll", 12);

    Log.e("ERROR", "Sensor type - "+sensorEvent.sensor.getType());

    double tempMs = (double) System.currentTimeMillis();
    if (tempMs - lastReading >= interval) {
      lastReading = tempMs;


      switch (sensorEvent.sensor.getType()) {
        case Sensor.TYPE_ACCELEROMETER:
          System.arraycopy(sensorEvent.values, 0, mAcceleration, 0, 3);   // save datas
          calculateAccMagOrientation();                       // then calculate new orientation
          map.putDouble("yaw", mAccMagOrientation[0]);
          map.putDouble("pitch", mAccMagOrientation[1]);
          map.putDouble("roll", mAccMagOrientation[2]);
          break;

        default: break;
      }

      // timestamp is added to all events
      map.putDouble("timestamp", this.sensorTimestampToEpochMilliseconds(sensorEvent.timestamp));
      this.sendEvent(this.sensorName, map);
    }
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {
  }

  public void calculateAccMagOrientation() {
    // Most chances are that there are no magnet datas
      double gx, gy, gz;
      gx = mAcceleration[0] / 9.81f;
      gy = mAcceleration[1] / 9.81f;
      gz = mAcceleration[2] / 9.81f;
      // http://theccontinuum.com/2012/09/24/arduino-imu-pitch-roll-from-accelerometer/
      float pitch = (float) -Math.atan(gy / Math.sqrt(gx * gx + gz * gz));
      float roll = (float) -Math.atan(gx / Math.sqrt(gy * gy + gz * gz));
      float azimuth = 0; // Impossible to guess

      mAccMagOrientation[0] = azimuth;
      mAccMagOrientation[1] = pitch;
      mAccMagOrientation[2] = roll;
      mRotationMatrix = getRotationMatrixFromOrientation(mAccMagOrientation);

  }
  public static float[] getRotationMatrixFromOrientation(float[] o) {
    float[] xM = new float[9];
    float[] yM = new float[9];
    float[] zM = new float[9];

    float sinX = (float) Math.sin(o[1]);
    float cosX = (float) Math.cos(o[1]);
    float sinY = (float) Math.sin(o[2]);
    float cosY = (float) Math.cos(o[2]);
    float sinZ = (float) Math.sin(o[0]);
    float cosZ = (float) Math.cos(o[0]);

    // rotation about x-axis (pitch)
    xM[0] = 1.0f;xM[1] = 0.0f;xM[2] = 0.0f;
    xM[3] = 0.0f;xM[4] = cosX;xM[5] = sinX;
    xM[6] = 0.0f;xM[7] =-sinX;xM[8] = cosX;

    // rotation about y-axis (roll)
    yM[0] = cosY;yM[1] = 0.0f;yM[2] = sinY;
    yM[3] = 0.0f;yM[4] = 1.0f;yM[5] = 0.0f;
    yM[6] =-sinY;yM[7] = 0.0f;yM[8] = cosY;

    // rotation about z-axis (azimuth)
    zM[0] = cosZ;zM[1] = sinZ;zM[2] = 0.0f;
    zM[3] =-sinZ;zM[4] = cosZ;zM[5] = 0.0f;
    zM[6] = 0.0f;zM[7] = 0.0f;zM[8] = 1.0f;

    // rotation order is y, x, z (roll, pitch, azimuth)
    float[] resultMatrix = matrixMultiplication(xM, yM);
    resultMatrix = matrixMultiplication(zM, resultMatrix);
    return resultMatrix;
  }
  public static float[] matrixMultiplication(float[] A, float[] B) {
    float[] result = new float[9];

    result[0] = A[0] * B[0] + A[1] * B[3] + A[2] * B[6];
    result[1] = A[0] * B[1] + A[1] * B[4] + A[2] * B[7];
    result[2] = A[0] * B[2] + A[1] * B[5] + A[2] * B[8];

    result[3] = A[3] * B[0] + A[4] * B[3] + A[5] * B[6];
    result[4] = A[3] * B[1] + A[4] * B[4] + A[5] * B[7];
    result[5] = A[3] * B[2] + A[4] * B[5] + A[5] * B[8];

    result[6] = A[6] * B[0] + A[7] * B[3] + A[8] * B[6];
    result[7] = A[6] * B[1] + A[7] * B[4] + A[8] * B[7];
    result[8] = A[6] * B[2] + A[7] * B[5] + A[8] * B[8];

    return result;
  }
}


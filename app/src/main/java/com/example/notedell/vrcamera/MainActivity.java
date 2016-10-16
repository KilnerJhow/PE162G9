package com.example.notedell.vrcamera;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private Timer timerAtual = new Timer();
    private TimerTask task;

    private static final String TAG = "My Activity";

    Handler writeHandler;
    private final Handler handler = new Handler();

    private TextView txtAzimuth;
    private TextView txtPitch;
    private TextView txtRoll;

    float[] mGravity;
    float[] mGeomagnetic;
    float[] mGyroscope;
    double azimuth;
    double pitch;
    double roll;

    private static boolean start = false;
    private static boolean connected = false;

    private SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private Sensor gyroscope;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);    // Register the sensor listeners

        checkSensors();

        txtAzimuth = (TextView) findViewById(R.id.txtAzimuth);
        txtPitch = (TextView) findViewById(R.id.txtPitch);
        txtRoll = (TextView) findViewById(R.id.txtRoll);


        turnOnTimer();

    }

    private void turnOnTimer(){
        task = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        sendPosition();
                        Log.d(TAG,"Ativa timer");
                    }
                });
            }};

        timerAtual.schedule(task, 300, 500);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {  }



    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values.clone();
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values.clone();
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE)
            mGyroscope = event.values.clone();


        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                //orientation[0] = Azimuth - Z - 180° a -180°
                //orientation[1] = Pitch - X - 180° a -180°
                //orientation[2] = roll - Y - 90° a -90°
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimuth = Math.toDegrees(orientation[0]); // orientation contains: azimut, pitch and roll
                //azimuth = orientation[0];
                pitch = Math.toDegrees(orientation[1]);
                //pitch = orientation[1];
                roll = Math.toDegrees(orientation[2]);
                //roll = orientation[2];
            }

            txtAzimuth.setText("Azimuth: " + (int)azimuth);
            txtPitch.setText("Pitch: " + (int)pitch);
            txtRoll.setText("Roll: " + (int)roll);
            //Delay();
        }
    }


    public void sendPosition() {

            int az = (int) azimuth;
            int pt = (int) pitch;
            int ro = (int) roll;
            String data = Double.toString(az) + "\n" + Double.toString(pt) + "\n" + Double.toString(ro) + "\n";
            //String data = Integer.toString(az) + "&" + Integer.toString(pt) + "&" + Integer.toString(ro) + "&";

            Message msg = Message.obtain();
            msg.obj = data;
            writeHandler.sendMessage(msg);


    }


    public void send(View view) {

            int az = (int) azimuth;
            int pt = (int) pitch;
            int ro = (int) roll;
            //String data = Integer.toString(az) + "&" +Integer.toString(pt) + "&" + Integer.toString(ro) + "&" ;
            String data = Integer.toString(az) + "\n" + Integer.toString(pt) + "\n" + Integer.toString(ro) + "\n";


            Message msg = Message.obtain();
            msg.obj = data;
            writeHandler.sendMessage(msg);

            start = true;

    }

    private void checkSensors() {
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        writeHandler = ConnectBluetooth.btt.getWriteHandler();

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null){
            magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }
        else {
            Toast.makeText(MainActivity.this, "No Magnetometer", Toast.LENGTH_SHORT).show();
        }

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null){
            gyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        }
        else {
            Toast.makeText(MainActivity.this, "No Gyroscope", Toast.LENGTH_SHORT).show();
        }

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
            accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        else {
            Toast.makeText(MainActivity.this, "No Accel", Toast.LENGTH_SHORT).show();
        }
    }

}

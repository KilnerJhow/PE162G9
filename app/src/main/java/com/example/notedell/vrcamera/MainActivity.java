/***
 *
 * Low-Pass filter by: https://www.built.io/blog/applying-low-pass-filter-to-android-sensor-s-readings
 *
 */
package com.example.notedell.vrcamera;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private Timer timerAtual = new Timer();
    TimerTask task;

    private static final String TAG = "Main Activity";

    private static Handler writeHandler;

    private final Handler handler = new Handler();

    private TextView txtAzimuth;
    private TextView txtPitch;
    private TextView txtRoll;
    private TextView tvRead;

    private float[] mGravity;
    private float[] mGeomagnetic;
    private float[] mGyroscope;
    private float azimuth;
    private float pitch;
    private float roll;

    static final float ALPHA = 0.25f;

    private static boolean start = false;

    private SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private Sensor gyroscope;

    private Camera mCamera;
    private CameraPreview mPreview;
    private int orientation;
    private int deviceHeight;
    private Button ibRetake;
    private Button ibUse;
    private Button ibCapture;
    private FrameLayout flBtnContainer;
    private FrameLayout preview;

    private CameraView camView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG,"On create Called");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        //writeHandler = ConnectBluetooth.btt.getWriteHandler();

        Log.d(TAG, "WriteHandler called");

        //ConnectBluetooth.btt.setReadHandler(readHandler);

        checkSensors();

        txtAzimuth = (TextView) findViewById(R.id.txtAzimuth);
        txtPitch = (TextView) findViewById(R.id.txtPitch);
        txtRoll = (TextView) findViewById(R.id.txtRoll);
        flBtnContainer = (FrameLayout) findViewById(R.id.flBtnContainer);
        preview = (FrameLayout) findViewById(R.id.camera_preview);

        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        deviceHeight = display.getHeight();
        camView = new CameraView(this, preview, deviceHeight);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(ConnectBluetooth.btt == null) {
            //startConnectBluetooth();
        }
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        camView.createCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        if(ConnectBluetooth.btt != null) {
            ConnectBluetooth.btt.interrupt();
            ConnectBluetooth.btt = null;
            start = false;
            timerAtual.cancel();
        }
        camView.releaseCamera();

        // removing the inserted view - so when we come back to the app we
        // won't have the views on top of each other.
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.removeViewAt(0);


    }

    /***
     * Timer which controls the data that is sent
     */
    private void turnOnTimer(){
        task = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        //sendPosition();
                    }
                });
            }
        };

        timerAtual.schedule(task, 300, 50);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {  }



    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = lowPass(event.values.clone(),mGravity);
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = lowPass(event.values.clone(),mGeomagnetic);
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
                /*azimuth = (float)(((orientation[0]*180)/Math.PI)+180);
                pitch = (float)(((orientation[1]*180/Math.PI))+90);
                roll = (float)(((orientation[2]*180/Math.PI)));*/

                azimuth = (float) Math.toDegrees(orientation[0]);
                pitch = (float) Math.toDegrees(orientation[1]);
                roll = (float) Math.toDegrees(orientation[2]);
            }

            txtAzimuth.setText("Azimuth: " + (int)azimuth);
            txtPitch.setText("Pitch: " + (int)pitch);
            txtRoll.setText("Roll: " + (int)roll);
        }
    }


    /**
     * Filtered data comes from the function onSensorChanged
     * @param input values which comes from the sensors
     * @param output variable which store the filtered value
     * @return return the filtered value
     */
    private float[] lowPass( float[] input, float[] output ) {
        if ( output == null ) return input;
        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }

    /**
     * Sends data continuously through the "turnOnTimer"
     */
    private void sendPosition() {

        if(start) {
            int az = (int) azimuth;
            int pt = (int) pitch;
            int ro = (int) roll;
            //String data = Double.toString(az) + "\n" + Double.toString(pt) + "\n" + Double.toString(ro) + "\n";
            String data = Integer.toString(az) + "&" + Integer.toString(pt) + "&" + Integer.toString(ro) + "&";

            Message msg = Message.obtain();
            msg.obj = data;
            writeHandler.sendMessage(msg);
        }

    }


    /**
     * Sends data when the button is pressed
     * @param view
     */
    public void send(View view) {

        int az = (int) azimuth;
        int pt = (int) pitch;
        int ro = (int) roll;
        //String data = Integer.toString(az) + "&" +Integer.toString(pt) + "&" + Integer.toString(ro) + "&" ;
        String data = Integer.toString(az) + "\n" + Integer.toString(pt) + "\n" + Integer.toString(ro) + "\n";

        Log.d(TAG, "Button pressed");

        Message msg = Message.obtain();
        msg.obj = data;
        writeHandler.sendMessage(msg);

        start = true;

        turnOnTimer();

    }

    /**
     * Sends a special value which is treated as initial value, when the button
     * "Valor Inicial" is pressioned.
     */
    public void initialValue(View view){

        int az = (int) azimuth;
        int pt = (int) pitch;
        int ro = (int) roll;
        String data = "$" + Integer.toString(az) + "&" +Integer.toString(pt) + "&" +
                Integer.toString(ro) + "&" ;
        Message msg = Message.obtain();
        msg.obj = data;
        writeHandler.sendMessage(msg);


    }



    private void checkSensors() {

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


    /***
     * Function that receive messages from thread, for disconnect and ends the
     * MainActivity
     */
    Handler readHandler = new Handler () {
        @Override
        public void handleMessage(Message msg) {

            String s = (String) msg.obj;
            Log.d(TAG, s);

            tvRead.setText(s);

            if (s.equals("DISCONNECT")) {
                    Toast.makeText(getApplicationContext(),"Desconectado", Toast.LENGTH_SHORT).show();

                    startConnectBluetooth();
            }
        }

    };

    /***
     * Used for disconnect the bluetooth socket
     * @param v
     */
    public void disconnectButtonPressed(View v) {
        Log.v(TAG, "Disconnect button pressed.");

        if(ConnectBluetooth.btt != null) {
            ConnectBluetooth.btt.interrupt();
            ConnectBluetooth.btt = null;
            startConnectBluetooth();
        }
    }

    /***
     * Ends the activity
     */
    private void close() {
        this.finish();
    }


    /***
     * Used for start the initial activity, when the bluetooth connection ends
     */
    private void startConnectBluetooth(){

        start = false;
        Toast.makeText(getApplicationContext(),"Desconectado", Toast.LENGTH_SHORT).show();
        writeHandler = null;
        Log.d(TAG, "WriteHandler ended");
        Intent intent = new Intent(getApplicationContext(),ConnectBluetooth.class);
        startActivity(intent);
        close();
    }


}


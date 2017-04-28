package com.example.example2;

import android.app.Activity;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Time;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Smart Phone Sensing Example 2 - 2017. Working with sensors.
 */
public class MainActivity extends Activity implements SensorEventListener {

    /**
     * The sensor manager object.
     */
    private SensorManager sensorManager;
    /**
     * The accelerometer.
     */
    private Sensor accelerometer;
    /**
     * The wifi manager.
     */
    private WifiManager wifiManager;
    /**
     * The wifi info.
     */
    private WifiInfo wifiInfo;
    /**
     * Accelerometer x value
     */
    private float aX = 0;
    /**
     * Accelerometer y value
     */
    private float aY = 0;
    /**
     * Accelerometer z value
     */
    private float aZ = 0;

    /**
     * Text fields to show the sensor values.
     */
    private TextView currentX, currentY, currentZ, titleAcc, textRssi;

    private boolean record;

    private File LogFile;
    private BufferedWriter buf;

    Button buttonRssi;
    Button buttonStartWrite;
    Button buttonStopWrite;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create the text views.
        currentX = (TextView) findViewById(R.id.currentX);
        currentY = (TextView) findViewById(R.id.currentY);
        currentZ = (TextView) findViewById(R.id.currentZ);
        titleAcc = (TextView) findViewById(R.id.titleAcc);
        textRssi = (TextView) findViewById(R.id.textRSSI);

        record=false;

        // Create the button
        buttonRssi = (Button) findViewById(R.id.buttonRSSI);
        buttonStartWrite=(Button) findViewById(R.id.buttonStartToWrite);
        buttonStopWrite=(Button) findViewById(R.id.buttonStopWriting);

        // Set the sensor manager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // if the default accelerometer exists
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // set accelerometer
            accelerometer = sensorManager
                    .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            // register 'this' as a listener that updates values. Each time a sensor value changes,
            // the method 'onSensorChanged()' is called.
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            // No accelerometer!
        }

        // Set the wifi manager
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        LogFile = new File(downloads, "sensorLogs.txt");
        if(!LogFile.exists()){
            try {
                LogFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Create a click listener for our button.
        buttonRssi.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // get the wifi info.
                wifiInfo = wifiManager.getConnectionInfo();
                // update the text.
                textRssi.setText("\n\tSSID = " + wifiInfo.getSSID()
                        + "\n\tRSSI = " + wifiInfo.getRssi()
                        + "\n\tLocal Time = " + System.currentTimeMillis());
            }
        });

        buttonStartWrite.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isExternalStorageWritable()) {  //check if the external storage is available
                    record = true;
                    try {
                        buf= new BufferedWriter(new FileWriter(LogFile,true));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        buttonStopWrite.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                record=false;
                String localTime;

                //get the local time
                Calendar calendar=Calendar.getInstance();
                int year  =calendar.get(Calendar.YEAR);
                int month =calendar.get(Calendar.MONTH);
                int day   =calendar.get(Calendar.DATE);
                int hour  =calendar.get(Calendar.HOUR);
                int minute=calendar.get(Calendar.MINUTE);
                int second=calendar.get(Calendar.SECOND);
                localTime="====="+Integer.toString(year)+"-"+Integer.toString(month)+"-"+Integer.toString(day)+"-"+
                        Integer.toString(hour)+":"+Integer.toString(minute)+":"+Integer.toString(second)+"=====";

                try {
                    buf.newLine();
                    buf.append(localTime);
                    buf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // onResume() registers the accelerometer for listening the events
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    // onPause() unregisters the accelerometer for stop listening the events
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do nothing.
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        currentX.setText("0.0");
        currentY.setText("0.0");
        currentZ.setText("0.0");

        // get the the x,y,z values of the accelerometer
        aX = event.values[0];
        aY = event.values[1];
        aZ = event.values[2];

        // display the current x,y,z accelerometer values
        currentX.setText(Float.toString(aX));
        currentY.setText(Float.toString(aY));
        currentZ.setText(Float.toString(aZ));

        if ((Math.abs(aX) > Math.abs(aY)) && (Math.abs(aX) > Math.abs(aZ))) {
            titleAcc.setTextColor(Color.RED);
        }
        if ((Math.abs(aY) > Math.abs(aX)) && (Math.abs(aY) > Math.abs(aZ))) {
            titleAcc.setTextColor(Color.BLUE);
        }
        if ((Math.abs(aZ) > Math.abs(aY)) && (Math.abs(aZ) > Math.abs(aX))) {
            titleAcc.setTextColor(Color.GREEN);
        }

        //write to file
        if(record){
            String accWriteData="!"+Float.toString(aX)+","+Float.toString(aY)+","+Float.toString(aZ)+"!";
            try {
                buf.newLine();
                buf.append(accWriteData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
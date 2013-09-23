package com.mobileproto.lab3;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by mingram on 9/22/13.
 */
public class LightSensorActivity extends Activity {

    ProgressBar lightMeter;
    float max;
    SharedPreferences prefs;
    TextView textMax, textReading;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_sensor);

        prefs = this.getSharedPreferences(
                "com.mobileproto.lab3", Context.MODE_PRIVATE);
        Float light_max = prefs.getFloat("light_sensor_max", 10);
        max = light_max;

        Button resetMax = (Button) findViewById(R.id.reset_light_max);
        resetMax.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                max = 100;
                lightMeter.setMax((int)max);
                textMax.setText("Max Reading: " + String.valueOf(max));
            }
        });

        lightMeter = (ProgressBar)findViewById(R.id.lightmeter);
        textMax = (TextView)findViewById(R.id.max);
        textReading = (TextView)findViewById(R.id.reading);

        SensorManager sensorManager
                = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        Sensor lightSensor
                = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        if (lightSensor == null){
            Toast.makeText(LightSensorActivity.this,
                    "No Light Sensor! quit-",
                    Toast.LENGTH_LONG).show();
        }else{
            lightMeter.setMax((int)max);
            textMax.setText("Max Reading: " + String.valueOf(max));

            sensorManager.registerListener(lightSensorEventListener,
                    lightSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);

        }
    }

    SensorEventListener lightSensorEventListener
            = new SensorEventListener(){

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            // TODO Auto-generated method stub
            if(event.sensor.getType()==Sensor.TYPE_LIGHT){
                float currentReading = event.values[0];
                if (currentReading>max){
                    max = currentReading;
                    lightMeter.setMax((int)max);
                    textMax.setText("Max Reading: " + String.valueOf(max));
                }
                lightMeter.setProgress((int)currentReading);
                textReading.setText("Current Reading: " + String.valueOf(currentReading));
            }
        }
    };
    protected void onStop(){
        super.onStop();
        SharedPreferences.Editor edit = prefs.edit();
        edit.putFloat("light_sensor_max", max);
        edit.apply();
    }

}

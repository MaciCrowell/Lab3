package com.mobileproto.lab3;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button toFlashlight = (Button) findViewById(R.id.to_flashlight);
        Button toCamera = (Button) findViewById(R.id.to_camera);
        Button toGPS = (Button) findViewById(R.id.to_gps);
        Button toNFC = (Button) findViewById(R.id.to_nfc);
        Button toLightSensor = (Button) findViewById(R.id.to_light_sensor);

        toFlashlight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToFlashlight(view);
            }
        });

        toCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToCamera(view);
            }
        });

        toGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToGPS(view);
            }
        });

        toNFC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToNFC(view);
            }
        });

        toLightSensor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToLightSensor(view);
            }
        });



    }


    public void goToFlashlight(View view){
        Intent i = new Intent(this, FlashLightActivity.class);
        startActivity(i);
    }

    public void goToCamera(View view){
        Intent i = new Intent(this, CameraActivity.class);
        startActivity(i);
    }

    public void goToGPS(View view){
        Intent i = new Intent(this, GPSActivity.class);
        startActivity(i);
    }

    public void goToNFC(View view){
        Intent i = new Intent(this, NFCActivity.class);
        startActivity(i);
    }

    public void goToLightSensor(View view){
        Intent i = new Intent(this, LightSensorActivity.class);
        startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}

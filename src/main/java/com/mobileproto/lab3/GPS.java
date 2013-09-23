package com.mobileproto.lab3;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

/**
 * Created by evan on 9/19/13.
 */
public class GPS extends Service implements LocationListener{

    private final Context mContext;

    boolean isGPSEnabled = false;

    boolean isNetworkEnabled = false;

    boolean canGetLocation = false;

    Location location;

    double latitude;
    double longitude;

    private static final long MIN_DIST_FOR_UPDATE = 10; //meters

    private static final long MIN_TIME_FOR_UPDATE = 1000 * 10; //milliseconds

    protected LocationManager lm;

    public GPS(Context context){
        this.mContext = context;
        getLocation();
    }

    public Location getLocation(){
        try {
            lm = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

            isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

            isNetworkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled){

            }else{
                this.canGetLocation = true;

                if (isNetworkEnabled) {
                    lm.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_FOR_UPDATE,
                            MIN_DIST_FOR_UPDATE,
                            this);

                    Log.d("Network", "Network");
                    if (lm != null) {
                        location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if(location != null){
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }

                if (isGPSEnabled) {
                    if (location == null) {
                        lm.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_FOR_UPDATE,
                                MIN_DIST_FOR_UPDATE,
                                this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if(lm != null){
                            location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null){
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
                else {
                    showSettingsAlert();
                }

            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return location;
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    public double getLatitude(){
        if(location != null){
            latitude = location.getLatitude();
        }

        return latitude;
    }

    public double getLongitude(){
        if(location != null){
            longitude = location.getLongitude();
        }

        return longitude;
    }

    public boolean canGetLocation(){
        return this.canGetLocation;
    }

    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        alertDialog.setTitle("GPS settings");

        alertDialog.setMessage("Enable GPS?");

        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }


    public void stopUsingGPS(){
        if(lm != null){
            lm.removeUpdates(GPS.this);
        }
    }
}

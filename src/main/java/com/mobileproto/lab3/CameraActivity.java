package com.mobileproto.lab3;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

/**
 * Created by evan on 9/15/13.
 */
public class CameraActivity extends Activity {


    private static final int TAKE_PICTURE = 0;
    private Uri mUri;
    private Bitmap mPhoto;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        Button take_picture = (Button) findViewById(R.id.takePicture);

        take_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent("android.media.action.IMAGE_CAPTURE");
                File f = new File(Environment.getExternalStorageDirectory(),  "logcat.jpg");
                i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                mUri = Uri.fromFile(f);
                startActivityForResult(i, TAKE_PICTURE);
            }
        });

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PICTURE:
                if (data != null){
                    if (resultCode != RESULT_CANCELED){
                        if (resultCode == Activity.RESULT_OK) {
                            getContentResolver().notifyChange(mUri, null);
                            ContentResolver cr = getContentResolver();
                            try {
                                mPhoto = android.provider.MediaStore.Images.Media.getBitmap(cr, mUri);
                                ((ImageView)findViewById(R.id.photoHolder)).setImageBitmap(mPhoto);
                            } catch (Exception e) {
                                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        }
    }
}

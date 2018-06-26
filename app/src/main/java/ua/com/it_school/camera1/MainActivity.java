package ua.com.it_school.camera1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;

import static android.content.ContentValues.TAG;
import static android.net.Uri.fromFile;

public class MainActivity extends Activity
{
        final int CAMERA_ID = 0;
        final int TYPE_PHOTO = 1;
        final int TYPE_VIDEO = 2;

        final int REQUEST_CODE_PHOTO = 1;
        final int REQUEST_CODE_VIDEO = 2;

        File directory;

        Button button;
        Boolean light = true;
        CameraDevice cameraDevice;
        CameraManager cameraManager;
        CameraCharacteristics cameraCharacteristics;
        String cameraId;
        CameraNew cameraNew;

        @Override
        protected void onCreate (Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            button = (Button) findViewById(R.id.btnLight);
            createDirectory();

            cameraNew = new CameraNew(this);

            cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            try {
                cameraId = cameraManager.getCameraIdList()[0];
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (light)
                {
                    try {
                        cameraManager.setTorchMode(cameraId, true);
                    }
                    catch (CameraAccessException e)
                    {
                        e.printStackTrace();
                    }
                    light = false;
                } else {

                    try {

                        cameraManager.setTorchMode(cameraId, false);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }


                    light = true;
                }
            }
        });
    }

    private void createDirectory() {
        directory = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "MyFolder");
        if (!directory.exists())
            directory.mkdirs();
    }


    private Uri generateFileUri(int type) {
        File file = null;
        String path = directory.getPath();
        switch (type) {
            case TYPE_PHOTO:
                file = new File(path + "/" + "photo_" + System.currentTimeMillis() + ".jpg");
                break;
            case TYPE_VIDEO:
                file = new File(path + "/" + "video_"
                        + System.currentTimeMillis() + ".mp4");
                break;
        }
        Log.d(TAG, "fileName = " + file);
        return fromFile(file);
    }

    public void onClickPhoto(View view)
    {
        cameraNew.open(CAMERA_ID);
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, generateFileUri(TYPE_PHOTO));
//        startActivityForResult(intent, REQUEST_CODE_PHOTO);
    }
}

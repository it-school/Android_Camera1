package ua.com.it_school.camera1;
//@SuppressWarnings("ALL")

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileOutputStream;

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

        SurfaceView surfaceView;
        Camera camera;
        MediaRecorder mediaRecorder;
        File photoFile;
        File videoFile;

        @Override
        protected void onCreate (Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            button = findViewById(R.id.btnLight);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", this.getPackageName(), null);
                intent.setData(uri);
                this.startActivity(intent);
            } else {
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
                        if (light) {
                            try {
                                cameraManager.setTorchMode(cameraId, true);
                            } catch (CameraAccessException e) {
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


                File pictures = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                photoFile = new File(pictures, "myphoto.jpg");
                videoFile = new File(pictures, "myvideo.3gp");

                surfaceView = findViewById(R.id.surfaceView);

                SurfaceHolder holder = surfaceView.getHolder();
                holder.addCallback(new SurfaceHolder.Callback() {
                    @Override
                    public void surfaceCreated(SurfaceHolder holder) {
                        try {
                            camera.setPreviewDisplay(holder);
                            camera.startPreview();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                    }

                    @Override
                    public void surfaceDestroyed(SurfaceHolder holder) {
                    }
                });
            }
    }

    private void createDirectory() {
        directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyFolder");
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
                file = new File(path + "/" + "video_" + System.currentTimeMillis() + ".mp4");
                break;
        }
        Log.d(TAG, "fileName = " + file);
        return fromFile(file);
    }

    @Override
    protected void onResume() {
        super.onResume();
        camera = Camera.open();
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();
        if (camera != null)
            camera.release();
        camera = null;
    }

    public void onClickPicture(View view) {

        Intent intent = new Intent();

        camera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                try {
                    FileOutputStream fos = new FileOutputStream(photoFile);
                    fos.write(data);
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void onClickStartRecord(View view) {
        if (prepareVideoRecorder()) {
            mediaRecorder.start();
        } else {
            releaseMediaRecorder();
        }
    }

    public void onClickStopRecord(View view) {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            releaseMediaRecorder();
        }
    }

    private boolean prepareVideoRecorder() {

        camera.unlock();

        mediaRecorder = new MediaRecorder();

        mediaRecorder.setCamera(camera);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        mediaRecorder.setOutputFile(videoFile.getAbsolutePath());
        mediaRecorder.setPreviewDisplay(surfaceView.getHolder().getSurface());

        try {
            mediaRecorder.prepare();
        } catch (Exception e) {
            e.printStackTrace();
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
            camera.lock();
        }
    }

    public void ShowSettings(View view) {
        startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS));
    }
}

package ua.com.it_school.camera1;

public interface CameraSupport {
    CameraSupport open(int cameraId);
    int getOrientation(int cameraId);
}
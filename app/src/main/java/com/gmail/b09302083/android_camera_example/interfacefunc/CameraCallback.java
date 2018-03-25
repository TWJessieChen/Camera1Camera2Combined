package com.gmail.b09302083.android_camera_example.interfacefunc;

import android.hardware.Camera;
import android.media.ImageReader;

/**
 * Created by b09302083 on 2018/3/24.
 */
public interface CameraCallback {

    void cameraCallback(byte[] data, Camera camera);

    void camera2Callback(ImageReader data);

}

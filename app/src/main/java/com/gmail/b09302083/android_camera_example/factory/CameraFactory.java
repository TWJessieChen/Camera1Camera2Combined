package com.gmail.b09302083.android_camera_example.factory;

import com.gmail.b09302083.android_camera_example.camera.CameraBuilder;
import com.gmail.b09302083.android_camera_example.camera2.Camera2Builder;
import com.gmail.b09302083.android_camera_example.constant.CameraSettingData;

import android.util.Log;

/**
 * Created by po-chunchen on 2018/3/27.
 */
public class CameraFactory {
    private static final String TAG = CameraFactory.class.getSimpleName();

    private static ICamera sICamera = null;

    public enum CameraType{
        CAMERA_1,
        CAMERA_2,
    }

    public static void releaseICamera() {
        sICamera = null;
    }

    public static ICamera getCamera(CameraType aCameraType, CameraSettingData settingData) {
        if(sICamera == null){
            synchronized (CameraFactory.class){
                if(sICamera == null){
                    switch (aCameraType){
                        case CAMERA_1:
                            Log.d(TAG,"Camera1");
                            sICamera = new CameraBuilder(settingData.getContext())
                                    .setPreviewSurfaceTexture(settingData.getPreviewSurfaceTexture())
                                    .setCameraSourceCallback(settingData.isUseCameraCallback());
                            break;
                        case CAMERA_2:
                            Log.d(TAG,"Camera2");
                            sICamera = new Camera2Builder(settingData.getContext())
                            .setDisplayOrientation(settingData.getDisplayOrientation())
                            .setOffScreenSurfaceTexture(settingData.getOffScreenSurfaceTexture())
                            .setPreviewSurfaceTexture(settingData.getPreviewSurfaceTexture())
                            .setCameraPreviewTextureView(settingData.getCameraPreviewTextureView())
                            .setCameraSourceCallback(settingData.isUseCameraCallback());
                            break;
                    }
                }
            }
        }
        return sICamera;
    }


}

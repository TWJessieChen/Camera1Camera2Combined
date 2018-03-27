package com.gmail.b09302083.android_camera_example.camera2;

import com.gmail.b09302083.android_camera_example.chillingvantextureView.CameraPreviewTextureView;
import com.gmail.b09302083.android_camera_example.constant.CameraInitObject;
import com.gmail.b09302083.android_camera_example.constant.CameraPreViewObject;
import com.gmail.b09302083.android_camera_example.constant.Constants;
import com.gmail.b09302083.android_camera_example.factory.ICamera;

import android.content.Context;
import android.graphics.SurfaceTexture;

import java.io.IOException;

/**
 * Created by b09302083 on 2018/3/24.
 */
public class Camera2Builder implements ICamera {

    private CameraSource2 mCameraSource;

    public Camera2Builder(Context context, CameraInitObject initObject) {
        if (context == null) {
            throw new IllegalArgumentException("No context supplied.");
        } else {
            mCameraSource = new CameraSource2(context);
        }
    }

//    public CameraSource2 build() {
//        return mCameraSource;
//    }

    public Camera2Builder setFacing(int facing) {
        if ((facing != Constants.CAMERA_FACING_BACK) && (facing != Constants.CAMERA_FACING_FRONT)) {
            throw new IllegalArgumentException("Invalid camera: " + facing);
        }
        mCameraSource.setFacing(facing);
        return this;
    }

    @Override
    public void onStart(CameraPreViewObject object) {
        if (mCameraSource.isCamera2Native()) {
            try {
                mCameraSource.onStart(object.getCameraPreviewTextureView(),
                        object.getPreviewSurfaceTexture(),
                        object.getOffScreenSurfaceTexture(),
                        object.getDisplayOrientation());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onStop() {

    }
}

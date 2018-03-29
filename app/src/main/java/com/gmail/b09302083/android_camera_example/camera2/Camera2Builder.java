package com.gmail.b09302083.android_camera_example.camera2;

import com.gmail.b09302083.android_camera_example.chillingvantextureView.CameraPreviewTextureView;
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

    public Camera2Builder(Context context) {
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

    public Camera2Builder setDisplayOrientation(int displayOrientation) {
        if (displayOrientation < 0) {
            throw new IllegalArgumentException("Invalid displayOrientation is null!!");
        }
        mCameraSource.setDisplayOrientation(displayOrientation);
        return this;
    }

    public Camera2Builder setOffScreenSurfaceTexture(SurfaceTexture offScreenSurfaceTexture) {
        if (offScreenSurfaceTexture == null) {
            throw new IllegalArgumentException("Invalid camera offScreen SurfaceTexture is null!!");
        }
        mCameraSource.setOffScreenSurfaceTexture(offScreenSurfaceTexture);
        return this;
    }

    public Camera2Builder setPreviewSurfaceTexture(SurfaceTexture previewSurfaceTexture) {
        if (previewSurfaceTexture == null) {
            throw new IllegalArgumentException("Invalid camera preview SurfaceTexture is null!!");
        }
        mCameraSource.setPreviewSurfaceTexture(previewSurfaceTexture);
        return this;
    }

    public Camera2Builder setCameraPreviewTextureView(CameraPreviewTextureView textureView) {
        if (textureView == null) {
            throw new IllegalArgumentException("Invalid camera CameraPreviewTextureView is null!!");
        }
        mCameraSource.setCameraPreviewTextureView(textureView);
        return this;
    }

    public Camera2Builder setCameraSourceCallback(boolean isUseCallback) {
        mCameraSource.setCameraSourceCallBack(isUseCallback);
        return this;
    }

    @Override
    public void onStart() {
        if (mCameraSource.isCamera2Native()) {
            try {
                mCameraSource.onStart();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onStop() {

    }
}

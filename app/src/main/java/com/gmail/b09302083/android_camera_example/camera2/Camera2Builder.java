package com.gmail.b09302083.android_camera_example.camera2;

import com.gmail.b09302083.android_camera_example.camera.CameraSourcePreview;
import com.gmail.b09302083.android_camera_example.chillingvantextureView.CameraPreviewTextureView;
import com.gmail.b09302083.android_camera_example.constant.Constants;

import android.content.Context;
import android.graphics.SurfaceTexture;

/**
 * Created by b09302083 on 2018/3/24.
 */
public class Camera2Builder {

    private CameraSource2 mCameraSource;

    public Camera2Builder(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("No context supplied.");
        } else {
            mCameraSource = new CameraSource2(context);
        }
    }

    public CameraSource2 build() {
        return mCameraSource;
    }

    public Camera2Builder setDisplayOrientation(int displayOrientation) {
        mCameraSource.setDisplayOrientation(displayOrientation);
        return this;
    }

    public Camera2Builder setOffScreenSurfaceTexture(SurfaceTexture offScreenSurfaceTexture) {
        mCameraSource.setOffScreenSurfaceTexture(offScreenSurfaceTexture);
        return this;
    }

    public Camera2Builder setPreviewSurfaceTexture(SurfaceTexture previewSurfaceTexture) {
        mCameraSource.setPreviewSurfaceTexture(previewSurfaceTexture);
        return this;
    }

    public Camera2Builder setCameraPreviewTextureView(CameraPreviewTextureView textureView) {
        mCameraSource.setTextureView(textureView);
        return this;
    }

    public Camera2Builder setFacing(int facing) {
        if ((facing != Constants.CAMERA_FACING_BACK) && (facing != Constants.CAMERA_FACING_FRONT)) {
            throw new IllegalArgumentException("Invalid camera: " + facing);
        }
        mCameraSource.setFacing(facing);
        return this;
    }

}

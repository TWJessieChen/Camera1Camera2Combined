package com.gmail.b09302083.android_camera_example.camera;

import com.gmail.b09302083.android_camera_example.constant.Constants;
import com.gmail.b09302083.android_camera_example.factory.ICamera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.support.annotation.IntDef;
import android.support.annotation.StringDef;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by b09302083 on 2018/3/24.
 */
public class CameraBuilder implements ICamera{

    @StringDef({
            Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE,
            Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO,
            Camera.Parameters.FOCUS_MODE_AUTO,
            Camera.Parameters.FOCUS_MODE_EDOF,
            Camera.Parameters.FOCUS_MODE_FIXED,
            Camera.Parameters.FOCUS_MODE_INFINITY,
            Camera.Parameters.FOCUS_MODE_MACRO
    })
    @Retention(RetentionPolicy.SOURCE)
    private @interface FocusMode {}

    @StringDef({
            Camera.Parameters.FLASH_MODE_ON,
            Camera.Parameters.FLASH_MODE_OFF,
            Camera.Parameters.FLASH_MODE_AUTO,
            Camera.Parameters.FLASH_MODE_RED_EYE,
            Camera.Parameters.FLASH_MODE_TORCH
    })
    @Retention(RetentionPolicy.SOURCE)
    private @interface FlashMode {}

    @IntDef({
            CamcorderProfile.QUALITY_LOW,
            CamcorderProfile.QUALITY_HIGH,
            CamcorderProfile.QUALITY_480P,
            CamcorderProfile.QUALITY_720P,
            CamcorderProfile.QUALITY_1080P,
            CamcorderProfile.QUALITY_2160P,
            CamcorderProfile.QUALITY_CIF,
            CamcorderProfile.QUALITY_QCIF,
            CamcorderProfile.QUALITY_QVGA
    })
    @Retention(RetentionPolicy.SOURCE)
    private @interface VideoMode {}

    private CameraSource mCameraSource = null;

    public CameraBuilder(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("No context supplied.");
        } else {
            mCameraSource = new CameraSource(context);
        }
    }

//    public CameraSource build() {
//        return mCameraSource;
//    }

    public CameraBuilder setRequestedFps(float fps) {
        if (fps <= 0) {
            throw new IllegalArgumentException("Invalid fps: " + fps);
        }

        mCameraSource.setRequestedFps(fps);
        return this;
    }

    public CameraBuilder setFocusMode(@FocusMode String mode) {
        if (mode == null) {
            throw new IllegalArgumentException("Invalid FocusMode is null!!");
        }
        mCameraSource.setFocusMode(mode);
        return this;
    }

    public CameraBuilder setFlashMode(@FlashMode String mode) {
        if (mode == null) {
            throw new IllegalArgumentException("Invalid FlashMode is null!!");
        }
        mCameraSource.setFlashMode(mode);
        return this;
    }

    public CameraBuilder setFacing(int facing) {
        if ((facing != Constants.CAMERA_FACING_BACK) && (facing != Constants.CAMERA_FACING_FRONT)) {
            throw new IllegalArgumentException("Invalid camera: " + facing);
        }
        mCameraSource.setFacing(facing);
        return this;
    }

    public CameraBuilder setPreviewSurfaceTexture(SurfaceTexture _surfaceTexture) {
        if (_surfaceTexture == null) {
            throw new IllegalArgumentException("Invalid camera SurfaceTexture is null!!");
        }
        mCameraSource.setPreviewSurfaceTexture(_surfaceTexture);

        return this;
    }

    public CameraBuilder setCameraSourceCallback(boolean isUseCallback) {
        mCameraSource.setCameraSourceCallBack(isUseCallback);
        return this;
    }

    public CameraBuilder setPreviewSize(int width, int height) {
        // Restrict the requested range to something within the realm of possibility.  The
        // choice of 1000000 is a bit arbitrary -- intended to be well beyond resolutions that
        // devices can support.  We bound this to avoid int overflow in the code later.
        final int MAX = 1000000;
        if ((width <= 0) || (width > MAX) || (height <= 0) || (height > MAX)) {
            throw new IllegalArgumentException("Invalid preview size: " + width + "x" + height);
        }

        mCameraSource.setPreviewWidth(width);
        mCameraSource.setPreviewHeight(height);
        return this;
    }

    @Override
    public void onStart() {
        try {
            mCameraSource.onStart();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {

    }

}

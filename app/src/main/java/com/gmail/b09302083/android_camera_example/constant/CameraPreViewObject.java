package com.gmail.b09302083.android_camera_example.constant;

import com.gmail.b09302083.android_camera_example.camera.CameraSourcePreview;
import com.gmail.b09302083.android_camera_example.camera2.CameraSource2Preview;
import com.gmail.b09302083.android_camera_example.chillingvantextureView.CameraPreviewTextureView;

import android.graphics.SurfaceTexture;

import java.io.Serializable;

/**
 * Created by po-chunchen on 2018/3/27.
 */
public class CameraPreViewObject implements Serializable {

    private SurfaceTexture previewSurfaceTexture = null;

    private SurfaceTexture surfaceTexture = null;

    private SurfaceTexture offScreenSurfaceTexture = null;

    private CameraPreviewTextureView cameraPreviewTextureView = null;

    private int displayOrientation;

    public SurfaceTexture getPreviewSurfaceTexture() {
        return previewSurfaceTexture;
    }

    public void setPreviewSurfaceTexture(SurfaceTexture previewSurfaceTexture) {
        this.previewSurfaceTexture = previewSurfaceTexture;
    }

    public SurfaceTexture getSurfaceTexture() {
        return surfaceTexture;
    }

    public void setSurfaceTexture(SurfaceTexture surfaceTexture) {
        this.surfaceTexture = surfaceTexture;
    }

    public SurfaceTexture getOffScreenSurfaceTexture() {
        return offScreenSurfaceTexture;
    }

    public void setOffScreenSurfaceTexture(SurfaceTexture offScreenSurfaceTexture) {
        this.offScreenSurfaceTexture = offScreenSurfaceTexture;
    }

    public int getDisplayOrientation() {
        return displayOrientation;
    }

    public void setDisplayOrientation(int displayOrientation) {
        this.displayOrientation = displayOrientation;
    }

    public CameraPreviewTextureView getCameraPreviewTextureView() {
        return cameraPreviewTextureView;
    }

    public void setCameraPreviewTextureView(
            CameraPreviewTextureView cameraPreviewTextureView) {
        this.cameraPreviewTextureView = cameraPreviewTextureView;
    }
}

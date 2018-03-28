package com.gmail.b09302083.android_camera_example.constant;

import com.gmail.b09302083.android_camera_example.chillingvantextureView.CameraPreviewTextureView;

import android.content.Context;
import android.graphics.SurfaceTexture;

import java.io.Serializable;

/**
 * Created by po-chunchen on 2018/3/27.
 */
public class CameraSettingData implements Serializable {

    private Context context;

    private int focusMode = -1;

    private int flashMode = -1;

    private int facing = Constants.CAMERA_FACING_BACK;

    private float fps = 30.0f;

    private int width = 1920;

    private int height = 1080;

    private SurfaceTexture previewSurfaceTexture = null;

    private SurfaceTexture surfaceTexture = null;

    private SurfaceTexture offScreenSurfaceTexture = null;

    private CameraPreviewTextureView cameraPreviewTextureView = null;

    private int displayOrientation;

    public CameraSettingData(Context _context){
        this.context = _context;
    }

    public Context getContext() {
        return context;
    }

    public int getFocusMode() {
        return focusMode;
    }

    public void setFocusMode(int focusMode) {
        this.focusMode = focusMode;
    }

    public int getFlashMode() {
        return flashMode;
    }

    public void setFlashMode(int flashMode) {
        this.flashMode = flashMode;
    }

    public int getFacing() {
        return facing;
    }

    public void setFacing(int facing) {
        this.facing = facing;
    }

    public float getFps() {
        return fps;
    }

    public void setFps(float fps) {
        this.fps = fps;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

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

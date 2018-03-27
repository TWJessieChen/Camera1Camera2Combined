package com.gmail.b09302083.android_camera_example.constant;

import android.content.Context;

import java.io.Serializable;

/**
 * Created by po-chunchen on 2018/3/27.
 */
public class CameraInitObject implements Serializable {

    private Context context;

    private int focusMode = -1;

    private int flashMode = -1;

    private int facing = Constants.CAMERA_FACING_BACK;

    private float fps = 30.0f;

    private int width = 1920;

    private int height = 1080;

    public CameraInitObject(Context _context){
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
}

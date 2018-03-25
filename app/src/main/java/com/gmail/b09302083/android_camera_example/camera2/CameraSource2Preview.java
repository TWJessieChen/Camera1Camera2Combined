package com.gmail.b09302083.android_camera_example.camera2;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.ViewGroup;

import com.gmail.b09302083.android_camera_example.camera.GraphicOverlay;
import com.gmail.b09302083.android_camera_example.utils.ScreenUtil;

import com.google.android.gms.common.images.Size;

import java.io.IOException;

public class CameraSource2Preview extends ViewGroup {
    private static final String TAG = "CameraSource2Preview";

    private SurfaceView mSurfaceView;
    private AutoFitTextureView mAutoFitTextureView;

    private boolean mStartRequested;
    private boolean mSurfaceAvailable;
    private boolean viewAdded = false;

    private CameraSource2 mCamera2Source;

    private GraphicOverlay mOverlay;
    private int screenWidth;
    private int screenHeight;
    private int screenRotation;

    public CameraSource2Preview(Context context) {
        super(context);
        screenHeight = ScreenUtil.getScreenHeight(context);
        screenWidth = ScreenUtil.getScreenWidth(context);
        screenRotation = ScreenUtil.getScreenRotation(context);
        mStartRequested = false;
        mSurfaceAvailable = false;
        mSurfaceView = new SurfaceView(context);
        mSurfaceView.getHolder().addCallback(mSurfaceViewListener);
        mAutoFitTextureView = new AutoFitTextureView(context);
        mAutoFitTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
    }
    
    public CameraSource2Preview(Context context, AttributeSet attrs) {
        super(context, attrs);
        screenHeight = ScreenUtil.getScreenHeight(context);
        screenWidth = ScreenUtil.getScreenWidth(context);
        screenRotation = ScreenUtil.getScreenRotation(context);
        mStartRequested = false;
        mSurfaceAvailable = false;
        mSurfaceView = new SurfaceView(context);
        mSurfaceView.getHolder().addCallback(mSurfaceViewListener);
        mAutoFitTextureView = new AutoFitTextureView(context);
        mAutoFitTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
    }


    public void onStart(CameraSource2 camera2Source) throws IOException {
        if (camera2Source == null) {
            onStop();
        }
        mCamera2Source = camera2Source;
        if(mCamera2Source != null) {
            mStartRequested = true;
            if(!viewAdded) {
                addView(mAutoFitTextureView);
                viewAdded = true;
            }
            try {
                startIfReady();
            } catch (IOException e) {
                Log.e(TAG, "Could not start camera source.", e);
            }
        }
    }

    public void onStop() {
        mStartRequested = false;
        if(mCamera2Source != null) {
            mCamera2Source.onStop();
        }
    }

    private void startIfReady() throws IOException {
        if (mStartRequested && mSurfaceAvailable) {
            try {
                mCamera2Source.onStart(mAutoFitTextureView, screenRotation);
                if (mOverlay != null) {
                    Size size = mCamera2Source.getPreviewSize();
                    if(size != null) {
                        int min = Math.min(size.getWidth(), size.getHeight());
                        int max = Math.max(size.getWidth(), size.getHeight());
                        // FOR GRAPHIC OVERLAY, THE PREVIEW SIZE WAS REDUCED TO QUARTER
                        // IN ORDER TO PREVENT CPU OVERLOAD
                        mOverlay.setCameraInfo(min/4, max/4, mCamera2Source.getCameraFacing());
                        mOverlay.clear();
                    } else {
                        onStop();
                    }
                }
                mStartRequested = false;
            } catch (SecurityException e) {
                Log.d(TAG, "SECURITY EXCEPTION: "+e);
            }
        }
    }

    private final SurfaceHolder.Callback mSurfaceViewListener = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder surface) {
            Log.d(TAG, "surfaceCreated");
            mSurfaceAvailable = true;
            mOverlay.bringToFront();
            try {startIfReady();} catch (IOException e) {Log.e(TAG, "Could not start camera source.", e);}
        }
        @Override
        public void surfaceDestroyed(SurfaceHolder surface) {
            mSurfaceAvailable = false;
            Log.d(TAG, "surfaceDestroyed");
        }
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Log.d(TAG, "surfaceChanged");
        }
    };

    private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            Log.d(TAG, "onSurfaceTextureAvailable");
            mSurfaceAvailable = true;
//            mOverlay.bringToFront();
            try {startIfReady();} catch (IOException e) {Log.e(TAG, "Could not start camera source.", e);}
        }
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            Log.d(TAG, "onSurfaceTextureSizeChanged");
        }
        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            Log.d(TAG, "onSurfaceTextureDestroyed");
            mSurfaceAvailable = false;
            return true;
        }
        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {}
    };

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int width = 1920;
        int height = 1080;
        if (mCamera2Source != null) {
            Size size = mCamera2Source.getPreviewSize();
            if (size != null) {
                // Swap width and height sizes when in portrait, since it will be rotated 90 degrees
                height = size.getWidth();
                width = size.getHeight();
            }
        }

        //RESIZE PREVIEW IGNORING ASPECT RATIO. THIS IS ESSENTIAL.
        int newWidth = (height * screenWidth) / screenHeight;

        final int layoutWidth = right - left;
        final int layoutHeight = bottom - top;
        // Computes height and width for potentially doing fit width.
        int childWidth = layoutWidth;
        int childHeight = (int)(((float) layoutWidth / (float) newWidth) * height);
        // If height is too tall using fit width, does fit height instead.
        if (childHeight > layoutHeight) {
            childHeight = layoutHeight;
            childWidth = (int)(((float) layoutHeight / (float) height) * newWidth);
        }
        for (int i = 0; i < getChildCount(); ++i) {getChildAt(i).layout(0, 0, childWidth, childHeight);}
        try {startIfReady();} catch (IOException e) {Log.e(TAG, "Could not start camera source.", e);}
    }
}

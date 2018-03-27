package com.gmail.b09302083.android_camera_example.camera2;

import com.google.android.gms.common.images.Size;

import com.gmail.b09302083.android_camera_example.chillingvantextureView.CameraPreviewTextureView;
import com.gmail.b09302083.android_camera_example.constant.Constants;
import com.gmail.b09302083.android_camera_example.factory.ICameraDataCallback;
import com.gmail.b09302083.android_camera_example.utils.ScreenUtil;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created by b09302083 on 2018/3/24.
 */
public class CameraSource2 {
    private static final String TAG = "CameraSource2";

    private int mFacing = Constants.CAMERA_FACING_BACK;

    private static final int MAX_PREVIEW_WIDTH = 1920;

    private static final int MAX_PREVIEW_HEIGHT = 1080;

    private static final double ratioTolerance = 0.1;

    private static final double maxRatioTolerance = 0.18;

    private ICameraDataCallback mICameraDataCallback;

    private boolean isMeteringAreaAFSupported = false;

    private boolean swappedDimensions = false;

    private Rect sensorArraySize;

    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    private String mCameraId;

    private Context mContext;

    private CameraManager manager = null;

    private CameraDevice mCameraDevice;

    private boolean cameraStarted = false;

    private HandlerThread mBackgroundThread;

    private Handler mBackgroundHandler;

    private CaptureRequest mPreviewRequest;

    private int mState = Constants.STATE_PREVIEW;

    private int mFlashMode = Constants.CAMERA_FLASH_AUTO;

    private int mFocusMode = Constants.CAMERA_AF_AUTO;

    private int mDisplayOrientation;

    private int mSensorOrientation;

    private boolean mFlashSupported;

    private CaptureRequest.Builder mPreviewRequestBuilder;

    private CameraCaptureSession mCaptureSession;

    private AutoFitTextureView mAutoFitTextureView;

    private Size mPreviewSize;

    private Size mVideoSize;

    //GL Render
    private CameraPreviewTextureView mTextureView;

    private SurfaceTexture previewSurfaceTexture;

    private SurfaceTexture offScreenSurfaceTexture;

    private ImageReader mImageReaderPreview;

    private ImageReader mImageReaderStill;

    private ShutterCallback mShutterCallback;

    private AutoFocusCallback mAutoFocusCallback;

    public CameraSource2(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("No context supplied.");
        } else {
            this.mContext = context;
            this.mICameraDataCallback = (ICameraDataCallback) mContext;
        }
    }

    public CameraSource2 onStart(@NonNull CameraPreviewTextureView textureView, @NonNull SurfaceTexture surfaceTexture ,SurfaceTexture _offScreenSurfaceTexture,int displayOrientation) throws IOException {
        mDisplayOrientation = displayOrientation;
        if(ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            if (cameraStarted) {
                return this;
            }
            cameraStarted = true;
            startBackgroundThread();

            mTextureView = textureView;
            previewSurfaceTexture = surfaceTexture;
            offScreenSurfaceTexture = _offScreenSurfaceTexture;
            if (mTextureView.isAvailable()) {
                setUpCameraOutputs(mTextureView.getWidth(), mTextureView.getHeight());
            }
        }
        return this;
    }

    public CameraSource2 onStart(@NonNull AutoFitTextureView textureView, int displayOrientation) throws IOException {
        mDisplayOrientation = displayOrientation;
        if(ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            if (cameraStarted) {
                return this;
            }
            cameraStarted = true;
            startBackgroundThread();

            mAutoFitTextureView = textureView;
            if (mAutoFitTextureView.isAvailable()) {
                setUpCameraOutputs(mAutoFitTextureView.getWidth(), mAutoFitTextureView.getHeight());
            }
        }
        return this;
    }

    private CameraSource2 onStart() throws
            IOException {
        if(ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            if (cameraStarted) {
                return this;
            }
            cameraStarted = true;
            startBackgroundThread();

            if (mTextureView.isAvailable()) {
                setUpCameraOutputs(mTextureView.getWidth(), mTextureView.getHeight());
            }
        }
        return this;
    }

    public void onStop() {
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCaptureSession) {
                mCaptureSession.close();
                mCaptureSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mImageReaderPreview) {
                mImageReaderPreview.close();
                mImageReaderPreview = null;
            }
            if (null != mImageReaderStill) {
                mImageReaderStill.close();
                mImageReaderStill = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
            stopBackgroundThread();
        }
    }

    public boolean isCamera2Native() {
        try {
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {return false;}
            manager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
            mCameraId = manager.getCameraIdList()[mFacing];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(mCameraId);
            //CHECK CAMERA HARDWARE LEVEL. IF CAMERA2 IS NOT NATIVELY SUPPORTED, GO BACK TO CAMERA1
            Integer deviceLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
            return deviceLevel != null && (deviceLevel != CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY);
        }
        catch (CameraAccessException ex) {return false;}
        catch (NullPointerException e) {return false;}
        catch (ArrayIndexOutOfBoundsException ez) {
            return false;
        }
    }

    private void setUpCameraOutputs(int width, int height) {
        try {
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {return;}
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            if(manager == null) manager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
            mCameraId = manager.getCameraIdList()[mFacing];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(mCameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (map == null) {return;}

            // For still image captures, we use the largest available size.
            Size largest = getBestAspectPictureSize(map.getOutputSizes(ImageFormat.JPEG));
            mImageReaderStill = ImageReader.newInstance(largest.getWidth(), largest.getHeight(), ImageFormat.JPEG, /*maxImages*/2);
            mImageReaderStill.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);

            sensorArraySize = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
            Integer maxAFRegions = characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF);
            if(maxAFRegions != null) {
                isMeteringAreaAFSupported = maxAFRegions >= 1;
            }
            // Find out if we need to swap dimension to get the preview size relative to sensor
            // coordinate.
            Integer sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            if(sensorOrientation != null) {
                mSensorOrientation = sensorOrientation;
                switch (mDisplayOrientation) {
                    case Surface.ROTATION_0:
                    case Surface.ROTATION_180:
                        if (mSensorOrientation == 90 || mSensorOrientation == 270) {
                            swappedDimensions = true;
                        }
                        break;
                    case Surface.ROTATION_90:
                    case Surface.ROTATION_270:
                        if (mSensorOrientation == 0 || mSensorOrientation == 180) {
                            swappedDimensions = true;
                        }
                        break;
                    default:
                        Log.e(TAG, "Display rotation is invalid: " + mDisplayOrientation);
                }
            }

            Point displaySize = new Point(ScreenUtil.getScreenWidth(mContext), ScreenUtil.getScreenHeight(mContext));
            int rotatedPreviewWidth = width;
            int rotatedPreviewHeight = height;
            int maxPreviewWidth = displaySize.x;
            int maxPreviewHeight = displaySize.y;

            if (swappedDimensions) {
                rotatedPreviewWidth = height;
                rotatedPreviewHeight = width;
                maxPreviewWidth = displaySize.y;
                maxPreviewHeight = displaySize.x;
            }

            if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                maxPreviewWidth = MAX_PREVIEW_WIDTH;
            }

            if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                maxPreviewHeight = MAX_PREVIEW_HEIGHT;
            }

            // Danger, W.R.! Attempting to use too large a preview size could  exceed the camera
            // bus' bandwidth limitation, resulting in gorgeous previews but the storage of
            // garbage capture data.
            Size[] outputSizes = ScreenUtil.sizeToSize(map.getOutputSizes(SurfaceTexture.class));
            Size[] outputSizesMediaRecorder = ScreenUtil.sizeToSize(map.getOutputSizes(MediaRecorder.class));
            mPreviewSize = chooseOptimalSize(outputSizes, rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth, maxPreviewHeight, largest);
            mVideoSize = chooseVideoSize(outputSizesMediaRecorder);

            // We fit the aspect ratio of TextureView to the size of preview we picked.
            int orientation = mDisplayOrientation;

//            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
//                mAutoFitTextureView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
//            } else {
//                mAutoFitTextureView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
//            }

            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mTextureView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            } else {
                mTextureView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
            }

            // Check if the flash is supported.
            Boolean available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
            mFlashSupported = available == null ? false : available;

            configureTransform(width, height);

            manager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        } catch (NullPointerException e) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
            Log.d(TAG, "Camera Error: "+e.getMessage());
        }
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        try {
            if(mBackgroundThread != null) {
                mBackgroundThread.quitSafely();
                mBackgroundThread.join();
                mBackgroundThread = null;
                mBackgroundHandler = null;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {

        private void process(CaptureResult result) {
            switch (mState) {
                case Constants.STATE_PREVIEW: {
                    // We have nothing to do when the camera preview is working normally.
                    break;
                }
                case Constants.STATE_WAITING_LOCK: {
                    Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                    if (afState == null) {
                        captureStillPicture();
                    } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState
                            || CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState
                            || CaptureResult.CONTROL_AF_STATE_PASSIVE_UNFOCUSED == afState
                            || CaptureRequest.CONTROL_AF_STATE_PASSIVE_FOCUSED == afState
                            || CaptureRequest.CONTROL_AF_STATE_INACTIVE == afState) {
                        // CONTROL_AE_STATE can be null on some devices
                        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                        if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                            mState = Constants.STATE_PICTURE_TAKEN;
                            captureStillPicture();
                        } else {
                            runPrecaptureSequence();
                        }
                    }
                    break;
                }
                case Constants.STATE_WAITING_PRECAPTURE: {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null ||
                            aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                            aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                        mState = Constants.STATE_WAITING_NON_PRECAPTURE;
                    }
                    break;
                }
                case Constants.STATE_WAITING_NON_PRECAPTURE: {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        mState = Constants.STATE_PICTURE_TAKEN;
                        captureStillPicture();
                    }
                    break;
                }
            }
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            if(request.getTag() == ("FOCUS_TAG")) {
                //The focus trigger is complete!
                //Resume repeating request, clear AF trigger.
                mAutoFocusCallback.onAutoFocus(true);
                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, null);
                mPreviewRequestBuilder.setTag("");
                mPreviewRequest = mPreviewRequestBuilder.build();
                try {
                    mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mBackgroundHandler);
                } catch(CameraAccessException ex) {
                    Log.d(TAG, "AUTO FOCUS FAILURE: "+ex);
                }
            } else {
                process(result);
            }
        }

        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
            if(request.getTag() == "FOCUS_TAG") {
                Log.d(TAG, "Manual AF failure: "+failure);
                mAutoFocusCallback.onAutoFocus(false);
            }
        }

    };

    private final ImageReader.OnImageAvailableListener mOnPreviewAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image mImage = reader.acquireNextImage();
            if(mImage == null) {
                return;
            }else {
                mICameraDataCallback.camera2Callback(reader);
                mImage.close();
            }
        }
    };

    private PictureDoneCallback mOnImageAvailableListener = new PictureDoneCallback();

    private class PictureDoneCallback implements ImageReader.OnImageAvailableListener {
        private PictureCallback mDelegate;

        @Override
        public void onImageAvailable(ImageReader reader) {
            if(mDelegate != null) {
                mDelegate.onPictureTaken(reader.acquireNextImage());
            }
        }

    };

    /**
     * {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its state.
     */
    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            createCameraPreviewSession();
        }
        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }
        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }
    };

    private void createCameraPreviewSession() {
        try {
//            SurfaceTexture texture = mTextureView.getSurfaceTexture();
//            SurfaceTexture autoFitTextureView = mAutoFitTextureView.getSurfaceTexture();
//            autoFitTextureView.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
//            Surface autoFitSurface = new Surface(autoFitTextureView);

            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            SurfaceTexture previewTexture = null;
            SurfaceTexture offscreenTexture = null;
            Surface previewSurface = null;
            if(this.previewSurfaceTexture != null) {
                previewTexture = this.previewSurfaceTexture;
                previewTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                previewSurface = new Surface(previewTexture);
                mPreviewRequestBuilder.addTarget(previewSurface);//app view
            }

            if(this.offScreenSurfaceTexture != null) {
                offscreenTexture = this.offScreenSurfaceTexture;
            }

//            assert previewTexture != null;
//            assert offscreenTexture != null;
            // We configure the size of default buffer to be the size of camera preview we want.

            offScreenSurfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            Surface offScreenSurface = new Surface(offScreenSurfaceTexture);
            mPreviewRequestBuilder.addTarget(offScreenSurface);//stream package

            mImageReaderPreview = ImageReader.newInstance(mPreviewSize.getWidth(), mPreviewSize.getHeight(), ImageFormat.YUV_420_888, 1);
            mImageReaderPreview.setOnImageAvailableListener(mOnPreviewAvailableListener, mBackgroundHandler);
            mPreviewRequestBuilder.addTarget(mImageReaderPreview.getSurface());//face detect or zbar code.
//            mPreviewRequestBuilder.addTarget(autoFitSurface);

            // Here, we create a CameraCaptureSession for camera preview.
            mCameraDevice.createCaptureSession(
                    Arrays.asList(previewSurface ,offScreenSurface , mImageReaderPreview.getSurface(), mImageReaderStill.getSurface() ),
//                    Arrays.asList(autoFitSurface , mImageReaderPreview.getSurface(), mImageReaderStill.getSurface() ),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            // The camera is already closed
                            if (null == mCameraDevice) {
                                return;
                            }

                            // When the session is ready, we start displaying the preview.
                            mCaptureSession = cameraCaptureSession;

                            try {
                                // Auto focus should be continuous for camera preview.
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, mFocusMode);
                                if(mFlashSupported) {
                                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, mFlashMode);
                                }

                                // Finally, we start displaying the camera preview.
                                mPreviewRequest = mPreviewRequestBuilder.build();
                                mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mBackgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {Log.d(TAG, "Camera Configuration failed!");}
                    }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void captureStillPicture() {
        try {
            if (null == mCameraDevice) {
                return;
            }
            if(mShutterCallback != null) {
                mShutterCallback.onShutter();
            }
            // This is the CaptureRequest.Builder that we use to take a picture.
            final CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReaderStill.getSurface());

            // Use the same AE and AF modes as the preview.
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, mFocusMode);
            if(mFlashSupported) {
                captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, mFlashMode);
            }

            // Orientation
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(mDisplayOrientation));

            CameraCaptureSession.CaptureCallback CaptureCallback = new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    unlockFocus();
                }
            };

            mCaptureSession.stopRepeating();
            mCaptureSession.capture(captureBuilder.build(), CaptureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void runPrecaptureSequence() {
        try {
            // This is how to tell the camera to trigger.
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
            // Tell #mCaptureCallback to wait for the precapture sequence to be set.
            mState = Constants.STATE_WAITING_PRECAPTURE;
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void unlockFocus() {
        try {
            // Reset the auto-focus trigger
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            if(mFlashSupported) {
                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, mFlashMode);
            }
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler);
            // After this, the camera will go back to the normal state of preview.
            mState = Constants.STATE_PREVIEW;
            mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private Size getBestAspectPictureSize(android.util.Size[] supportedPictureSizes) {
        float targetRatio = ScreenUtil.getScreenRatio(mContext);
        Size bestSize = null;
        TreeMap<Double, List<android.util.Size>> diffs = new TreeMap<>();

        //Select supported sizes which ratio is less than ratioTolerance
        for (android.util.Size size : supportedPictureSizes) {
            float ratio = (float) size.getWidth() / size.getHeight();
            double diff = Math.abs(ratio - targetRatio);
            if (diff < ratioTolerance){
                if (diffs.keySet().contains(diff)){
                    //add the value to the list
                    diffs.get(diff).add(size);
                } else {
                    List<android.util.Size> newList = new ArrayList<>();
                    newList.add(size);
                    diffs.put(diff, newList);
                }
            }
        }

        //If no sizes were supported, (strange situation) establish a higher ratioTolerance
        if(diffs.isEmpty()) {
            for (android.util.Size size : supportedPictureSizes) {
                float ratio = (float)size.getWidth() / size.getHeight();
                double diff = Math.abs(ratio - targetRatio);
                if (diff < maxRatioTolerance){
                    if (diffs.keySet().contains(diff)){
                        //add the value to the list
                        diffs.get(diff).add(size);
                    } else {
                        List<android.util.Size> newList = new ArrayList<>();
                        newList.add(size);
                        diffs.put(diff, newList);
                    }
                }
            }
        }

        //Select the highest resolution from the ratio filtered ones.
        for (Map.Entry entry: diffs.entrySet()){
            List<?> entries = (List) entry.getValue();
            for (int i=0; i<entries.size(); i++) {
                android.util.Size s = (android.util.Size) entries.get(i);
                if(bestSize == null) {
                    bestSize = new Size(s.getWidth(), s.getHeight());
                } else if(bestSize.getWidth() < s.getWidth() || bestSize.getHeight() < s.getHeight()) {
                    bestSize = new Size(s.getWidth(), s.getHeight());
                }
            }
        }
        return bestSize;
    }

    private static Size chooseOptimalSize(Size[] choices, int textureViewWidth, int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {

        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        // Collect the supported resolutions that are smaller than the preview Surface
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
                    option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth &&
                        option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }

        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    private static Size chooseVideoSize(Size[] choices) {
        for (Size size : choices) {
            if (size.getWidth() == size.getHeight() * 16 / 9)
            {
                return size;
            }
        }
        Log.e(TAG, "Couldn't find any suitable video size");
        return choices[0];
    }

    private static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }

    private void configureTransform(int viewWidth, int viewHeight) {
        if (null == mTextureView || null == mPreviewSize) {
            return;
        }
        int rotation = mDisplayOrientation;
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

    private int getOrientation(int rotation) {
        return (Constants.ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360;
    }

    public void setDisplayOrientation(int displayOrientation) {
        mDisplayOrientation = displayOrientation;
    }

    public void setOffScreenSurfaceTexture(SurfaceTexture offScreenSurfaceTexture) {
        this.offScreenSurfaceTexture = offScreenSurfaceTexture;
    }

    public void setPreviewSurfaceTexture(SurfaceTexture previewSurfaceTexture) {
        this.previewSurfaceTexture = previewSurfaceTexture;
    }

    public void setTextureView(
            CameraPreviewTextureView textureView) {
        mTextureView = textureView;
    }

    public Size getPreviewSize() {
        return mPreviewSize;
    }

    public int getCameraFacing() {
        return mFacing;
    }

    public void setFacing(int facing) {
        mFacing = facing;
    }

    public interface AutoFocusCallback {
        void onAutoFocus(boolean success);
    }

    public interface ShutterCallback {
        void onShutter();
    }

    public interface PictureCallback {
        void onPictureTaken(Image image);
    }
}

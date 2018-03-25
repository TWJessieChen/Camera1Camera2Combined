package com.gmail.b09302083.android_camera_example;

import com.chillingvan.canvasgl.Loggers;
import com.chillingvan.canvasgl.glcanvas.RawTexture;
import com.chillingvan.canvasgl.glview.texture.GLSurfaceTextureProducerView;
import com.chillingvan.canvasgl.glview.texture.gles.EglContextWrapper;
import com.chillingvan.canvasgl.glview.texture.gles.GLThread;
import com.gmail.b09302083.android_camera_example.camera.CameraBuilder;
import com.gmail.b09302083.android_camera_example.camera.CameraSource;
import com.gmail.b09302083.android_camera_example.camera.CameraSourcePreview;
import com.gmail.b09302083.android_camera_example.camera2.Camera2Builder;
import com.gmail.b09302083.android_camera_example.camera2.CameraSource2;
import com.gmail.b09302083.android_camera_example.camera2.CameraSource2Preview;
import com.gmail.b09302083.android_camera_example.chillingvantextureView.CameraPreviewTextureView;
import com.gmail.b09302083.android_camera_example.chillingvantextureView.EncoderCanvas;
import com.gmail.b09302083.android_camera_example.constant.Constants;
import com.gmail.b09302083.android_camera_example.encoder.MyBaseEncoder;
import com.gmail.b09302083.android_camera_example.encoder.MyVideoEncoder;
import com.gmail.b09302083.android_camera_example.interfacefunc.CameraCallback;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.media.ImageReader;
import android.media.MediaFormat;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.View;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements CameraCallback {
    private String TAG = this.getClass().getSimpleName();

    private CameraPreviewTextureView txv_cameraPreviewWithFilter;

    private CameraSourcePreview mPreview;

    private CameraSource2Preview mPreview2;

    private SurfaceTexture previewSurfaceTexture;

    private OrientationEventListener orientationListener;

    private RawTexture previewRawTexture;

    private EglContextWrapper previewEglCtx;

    private EncoderCanvas offScreenCanvasWithFilter;

    private SurfaceTexture offScreenSurfaceTexture;

    private RawTexture offScreenRawTexture;

    private boolean useCamera2 = true;

    private CameraSource mCameraSource;

    private MyVideoEncoder myVideoEncoderStream1;

    private CameraSource2 mCameraSource2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (useCamera2) {
            initEncode();
            createCameraSource2();
        } else {
            createCameraSource();
        }

        findView();
//        setListener();
    }

    private void initEncode() {
        long startRecordWhenNs = System.nanoTime();
        int stream1BitRate = 5 * 1000 * 1000;

        try {
            myVideoEncoderStream1 = new MyVideoEncoder(startRecordWhenNs);
            myVideoEncoderStream1
                    .prepareVideoEncoder(1920, 1080, stream1BitRate, "camera2mediacodec0.264",
                            true);
            myVideoEncoderStream1.setMyEncoderCallBackFunction(myEncoderCallBackFunction);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createCameraSource() {

        CameraBuilder builder = new CameraBuilder(MainActivity.this)
                .setFacing(Constants.CAMERA_FACING_BACK)
                .setPreviewSize(1920, 1080);
        mCameraSource = builder.build();

//        if(previewSurfaceTexture != null) {
//            try {
//                mCameraSource.onStart(previewSurfaceTexture);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }

    }

    private void createCameraSource2() {

        Camera2Builder builder = new Camera2Builder(MainActivity.this)
                .setFacing(Constants.CAMERA_FACING_BACK);
        mCameraSource2 = builder.build();
    }


    private void findView() throws SecurityException{

//        txv_cameraPreviewWithFilter = (CameraPreviewTextureView) findViewById(
//                R.id.txv_camera_preview_with_filter);
//        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mPreview2 = (CameraSource2Preview) findViewById(R.id.preview2);

        if (useCamera2) {

//            mPreview.setVisibility(View.GONE);

            if(mCameraSource2.isCamera2Native()) {
                try {
                    mPreview2.onStart(mCameraSource2);
                } catch (IOException e) {
                    Log.e(TAG, "Unable to start camera source 2.", e);
                    mCameraSource2.onStop();
                    mCameraSource2 = null;
                }
            }

//            orientationListener = new OrientationEventListener(this,
//                    SensorManager.SENSOR_DELAY_NORMAL) {
//                @Override
//                public void onOrientationChanged(int orientation) {
////                if (txv_cameraPreviewWithFilter != null && txv_cameraPreviewWithFilter.isAvailable()) {
////                    configureTransform(txv_cameraPreviewWithFilter, txv_cameraPreviewWithFilter.getWidth(), txv_cameraPreviewWithFilter.getHeight());
////                }
//
//                }
//            };
        } else {
//            txv_cameraPreviewWithFilter.setVisibility(View.GONE);

            try {
                mPreview.start(mCameraSource);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.onStop();
                mCameraSource = null;
            }

        }


    }

    private void setListener() {

        if(useCamera2) {
            txv_cameraPreviewWithFilter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "txv_cameraPreviewWithFilter onClick");
                }
            });
            txv_cameraPreviewWithFilter.setOnCreateGLContextListener(new GLThread.OnCreateGLContextListener() {
                @Override
                public void onCreate(EglContextWrapper eglContext) {
                    Log.d(TAG, "setOnCreateGLContextListener");
                    previewEglCtx = eglContext;
                }
            });
            //txv_cameraPreviewWithFilter.setTextureFilter(new PixelationFilter(15));


            txv_cameraPreviewWithFilter.setOnSurfaceTextureSet(new CameraPreviewTextureView.OnSurfaceTextureSet() {
                @Override
                public void onSet(SurfaceTexture surfaceTexture, RawTexture surfaceTextureRelatedTexture) {
                    Log.d(TAG, "setOnSurfaceTextureSet");
                    //set Preview
                    previewSurfaceTexture = surfaceTexture;
                    previewRawTexture = surfaceTextureRelatedTexture;

                    if(useCamera2) {

                    } else {
                        createCameraSource();
                    }



                    surfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
                        @Override
                        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                            //Log.d(TAG,"preview_onFrameAvailable");
                            txv_cameraPreviewWithFilter.requestRenderAndWait();
//                        if(myVideoEncoderStream1!=null)
//                            myVideoEncoderStream1.requestRenderAndWait();

                            if (offScreenCanvasWithFilter != null)
                                offScreenCanvasWithFilter.requestRenderAndWait();
                        }
                    });

                    if (useCamera2) {
//                    offScreenCanvasWithFilter = new EncoderCanvas(myVideoEncoderStream1.getMyMediaCodecWrapper().width, myVideoEncoderStream1.getMyMediaCodecWrapper().height, myVideoEncoderStream1.getMyMediaCodecWrapper().encoderSurface);
                    } else {
                        offScreenCanvasWithFilter = new EncoderCanvas(myVideoEncoderStream1.getMyMediaCodecWrapper().width, myVideoEncoderStream1.getMyMediaCodecWrapper().height, previewEglCtx, myVideoEncoderStream1.getMyMediaCodecWrapper().encoderSurface);
                        offScreenCanvasWithFilter.setSharedTexture(previewRawTexture, previewSurfaceTexture);
                    }


                    offScreenCanvasWithFilter.setOnCreateGLContextListener(new GLThread.OnCreateGLContextListener() {
                        @Override
                        public void onCreate(EglContextWrapper eglContext) {
                            Log.d(TAG, "aaa_offScreenCanvas_oncreate");
                        }
                    });
                    offScreenCanvasWithFilter.setOnSurfaceTextureSet(new GLSurfaceTextureProducerView.OnSurfaceTextureSet() {
                        @Override
                        public void onSet(SurfaceTexture surfaceTexture, RawTexture surfaceTextureRelatedTexture) {
                            Log.d(TAG, "aaa_offScreenCanvas_onSet");
                            offScreenSurfaceTexture = surfaceTexture;
                            offScreenRawTexture = surfaceTextureRelatedTexture;
                            surfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
                                @Override
                                public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                                    Log.d(TAG, "onFrameAvailable");
                                    offScreenCanvasWithFilter.requestRenderAndWait();
                                }
                            });

                        }
                    });

                    offScreenCanvasWithFilter.start();
                    offScreenCanvasWithFilter.onResume();

                }
            });
        } else{

        }



    }

    @Override
    public void cameraCallback(byte[] data, Camera camera) {
        Log.d(TAG,"cameraCallback");
    }

    @Override
    public void camera2Callback(ImageReader data) {
        Log.d(TAG,"camera2Callback");
    }

    private MyBaseEncoder.MyEncoderCallBackFunction  myEncoderCallBackFunction = new MyBaseEncoder.MyEncoderCallBackFunction() {
        @Override
        public void encodeAudioSuccess(byte[] encodeDate, int encodeSize , int channelCount,int sampleBit ,int sampleRate) {
            Log.d(TAG,"encodeAudioSuccess");

        }

        @Override
        public void encodeVideoSuccess(byte[] encodeDate, int encodeSize, boolean isVideoKeyFrame, int width, int height) {
            Log.d(TAG,"encodeVideoSuccess:isVideoKeyFrame:"+isVideoKeyFrame + ",w:"+width+",h:"+height);

        }

        @Override
        public void outputFormatChanged(MyBaseEncoder.GEO_ENCODER_TYPE encoderType, MediaFormat format) {
            Log.d(TAG,"outputFormatChanged");

        }

    };
}

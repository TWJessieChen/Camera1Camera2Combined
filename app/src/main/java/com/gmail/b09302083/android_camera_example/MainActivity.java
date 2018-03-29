package com.gmail.b09302083.android_camera_example;

import com.chillingvan.canvasgl.glcanvas.RawTexture;
import com.chillingvan.canvasgl.glview.texture.GLSurfaceTextureProducerView;
import com.chillingvan.canvasgl.glview.texture.gles.EglContextWrapper;
import com.chillingvan.canvasgl.glview.texture.gles.GLThread;
import com.gmail.b09302083.android_camera_example.chillingvantextureView.CameraPreviewTextureView;
import com.gmail.b09302083.android_camera_example.chillingvantextureView.EncoderCanvas;
import com.gmail.b09302083.android_camera_example.constant.CameraSettingData;
import com.gmail.b09302083.android_camera_example.constant.Constants;
import com.gmail.b09302083.android_camera_example.encoder.MyBaseEncoder;
import com.gmail.b09302083.android_camera_example.encoder.MyVideoEncoder;
import com.gmail.b09302083.android_camera_example.factory.CameraFactory;
import com.gmail.b09302083.android_camera_example.factory.ICamera;
import com.gmail.b09302083.android_camera_example.factory.ICameraDataCallback;
import com.gmail.b09302083.android_camera_example.utils.CrashHandler;
import com.gmail.b09302083.android_camera_example.utils.ScreenUtil;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.media.ImageReader;
import android.media.MediaFormat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.View;

public class MainActivity extends AppCompatActivity implements ICameraDataCallback {
    private String TAG = this.getClass().getSimpleName();

    private CameraPreviewTextureView txv_cameraPreviewWithFilter;

    private SurfaceTexture previewSurfaceTexture;

    private OrientationEventListener orientationListener;

    private RawTexture previewRawTexture;

    private EglContextWrapper previewEglCtx;

    private EncoderCanvas offScreenCanvasWithFilter;

    private SurfaceTexture offScreenSurfaceTexture;

    private RawTexture offScreenRawTexture;

    private CameraFactory.CameraType cameraType = CameraFactory.CameraType.CAMERA_1;

    private MyVideoEncoder myVideoEncoderStream1;

    private boolean usingFrontCamera = false;

    private CameraSettingData settingData;

    private static ICamera sICamera = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        settingData = new CameraSettingData(MainActivity.this);
        if (usingFrontCamera) {
            settingData.setFacing(Constants.CAMERA_FACING_FRONT);
        }

        //Crash Handler write output file.
        CrashHandler.init(getApplicationContext());
        initEncode();


        findView();
        setListener();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "AAA_onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "AAA_onStop");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "AAA_onResume");
        if (orientationListener != null && orientationListener.canDetectOrientation()) {
            orientationListener.enable();
        }
        if (txv_cameraPreviewWithFilter != null) {
            txv_cameraPreviewWithFilter.onResume();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "AAA_onDestroy");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "AAA_onPause");
        if (orientationListener != null) {
            orientationListener.disable();
        }
        if (txv_cameraPreviewWithFilter != null) {
            txv_cameraPreviewWithFilter.onPause();
        }

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

    private void findView() throws SecurityException{
        txv_cameraPreviewWithFilter = (CameraPreviewTextureView) findViewById(R.id.txv_camera_preview_with_filter);

    }

    private void setListener() {

        orientationListener = new OrientationEventListener(this,
                SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                Log.d(TAG,"onOrientationChanged value: " + orientation);
            }
        };



        txv_cameraPreviewWithFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        txv_cameraPreviewWithFilter.setOnCreateGLContextListener(new GLThread.OnCreateGLContextListener() {
            @Override
            public void onCreate(EglContextWrapper eglContext) {
                Log.d(TAG, "AAA_txv_cameraPreviewWithFilter_onCreate");
                previewEglCtx = eglContext;


            }
        });
        //txv_cameraPreviewWithFilter.setTextureFilter(new PixelationFilter(15));


        txv_cameraPreviewWithFilter.setOnSurfaceTextureSet(new CameraPreviewTextureView.OnSurfaceTextureSet() {
            @Override
            public void onSet(SurfaceTexture surfaceTexture, RawTexture surfaceTextureRelatedTexture) {
                Log.d(TAG, "AAA_txv_cameraPreviewWithFilter_onSet");
                //set Preview
                previewSurfaceTexture = surfaceTexture;
                previewRawTexture = surfaceTextureRelatedTexture;
                Log.d(TAG, String.format("onSet: "));
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

//                configureTransform(txv_cameraPreviewWithFilter, txv_cameraPreviewWithFilter.getWidth(), txv_cameraPreviewWithFilter.getHeight());
                if (CameraFactory.CameraType.CAMERA_2.equals(cameraType)) {
                    offScreenCanvasWithFilter = new EncoderCanvas(myVideoEncoderStream1.getMyMediaCodecWrapper().width,
                            myVideoEncoderStream1.getMyMediaCodecWrapper().height,
                            myVideoEncoderStream1.getMyMediaCodecWrapper().encoderSurface);
                } else {
                    offScreenCanvasWithFilter = new EncoderCanvas(myVideoEncoderStream1.getMyMediaCodecWrapper().width,
                            myVideoEncoderStream1.getMyMediaCodecWrapper().height, previewEglCtx,
                            myVideoEncoderStream1.getMyMediaCodecWrapper().encoderSurface);
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
                                offScreenCanvasWithFilter.requestRenderAndWait();
                            }
                        });

                        ///在這裡才可以正式開啟camera

                        settingData.setCameraPreviewTextureView(txv_cameraPreviewWithFilter);
                        settingData.setPreviewSurfaceTexture(previewSurfaceTexture);
                        settingData.setOffScreenSurfaceTexture(offScreenSurfaceTexture);
                        settingData.setDisplayOrientation(ScreenUtil.getScreenRotation(MainActivity.this));

                        sICamera = CameraFactory.getCamera(cameraType, settingData);

                        sICamera.onStart();
                        myVideoEncoderStream1.startCodec();
                    }
                });

                offScreenCanvasWithFilter.start();
                offScreenCanvasWithFilter.onResume();

            }
        });



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

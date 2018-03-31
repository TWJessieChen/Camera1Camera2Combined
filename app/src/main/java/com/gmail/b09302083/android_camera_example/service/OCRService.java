package com.gmail.b09302083.android_camera_example.service;

//import com.abbyy.mobile.rtr.Engine;
//import com.abbyy.mobile.rtr.ITextCaptureService;
//import com.abbyy.mobile.rtr.Language;

import android.app.Service;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.Serializable;

/**
 * Created by po-chunchen on 2018/3/30.
 */
public class OCRService extends Service implements Serializable {

    private final String TAG = this.getClass().getSimpleName();

    private OCRServiceBinder mBinder = new OCRServiceBinder();

//    private ITextCaptureService textCaptureService;
//    private Engine engine;

    private static final String licenseFileName = "AbbyyRtrSdk.license";

    private byte[] ocrData;

//    private Language[] languages = {
//            Language.ChineseSimplified,
//            Language.ChineseTraditional,
//            Language.English,
//            Language.French,
//            Language.German,
//            Language.Italian,
//            Language.Japanese,
//            Language.Korean,
//            Language.Polish,
//            Language.PortugueseBrazilian,
//            Language.Russian,
//            Language.Spanish,
//    };

    public class OCRServiceBinder extends Binder {
        public OCRService getService()
        {
            return OCRService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "[OCRService]_onBind() executed");
        return mBinder;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");

//        createTextCaptureService();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
        Log.d(TAG, "[CamAppService]_onDestroy() executed");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "[CamAppService]_onStartCommand() executed");
        return super.onStartCommand(intent, flags, startId);
    }

    public void onStopOCR() {
//        if( textCaptureService != null ) {
//            textCaptureService.stop();
//        }
    }

    public void onStartOCR(int width, int height, int orientation ) {

        //辨識範圍
        Rect areaOfInterest = new Rect(0, 0, width, height );

//        textCaptureService.start(width,
//                height,
//                orientation,
//                areaOfInterest );
    }

    public void setOCRData(byte[] data) {
        if(ocrData == null) {
            ocrData = new byte[data.length];
        }
        System.arraycopy(data, 0, ocrData, 0, data.length);
//        textCaptureService.submitRequestedFrame(ocrData);
    }

//    private ITextCaptureService.Callback textCaptureCallback = new ITextCaptureService.Callback() {
//
//        @Override
//        public void onRequestLatestFrame( byte[] buffer )
//        {
//            Log.d(TAG,"onRequestLatestFrame");
//
//            ocrData = buffer;
//        }
//
//        /**
//         *
//         * NotReady : No content available.
//         *
//         * Tentative : Content detected on a single frame.
//         *
//         * Verified : Content verified: matching content found in at least two frames.
//         *
//         * Available : Matching content found in three or more frames. The content is recognized and the result is available, though the result can still vary with the addition of new frames.
//         *
//         * TentativelyStable : The result has been stable in the last two frames.
//         *
//         * Stable : The result has been stable in the last three or more frames.
//         *
//         * */
//
//        @Override
//        public void onFrameProcessed( ITextCaptureService.TextLine[] lines,
//                ITextCaptureService.ResultStabilityStatus resultStatus, ITextCaptureService.Warning warning )
//        {
//            Log.d(TAG,"onFrameProcessed");
//
//            if(lines!=null) {
////                String[] resultLines = new String[lines.length];
//                String result = "";
//                for( int i = 0; i < lines.length; i++ ) {
////                    result = result + i + " : " + lines[i].Text + " , ";
//                    Log.d(TAG,"Result OCR: " + lines[i].Text);
//                }
////                Log.d(TAG,"Result OCR: " + result);
//            }
//
//
//        }
//
//        @Override
//        public void onError( Exception e )
//        {
//            Log.d(TAG,"onError : " + e);
//            // An error occurred while processing. Log it. Processing will continue
//        }
//    };
//
//    private boolean createTextCaptureService()
//    {
//        // Initialize the engine and text capture service
//        try {
//            engine = Engine.load( this, licenseFileName );
//            textCaptureService = engine.createTextCaptureService( textCaptureCallback );
//            Log.d(TAG,"createTextCaptureService is ok!!!");
//            return true;
//        } catch( java.io.IOException e ) {
//            // Troubleshooting for the developer
//            Log.e( TAG, "Error loading ABBYY RTR SDK:", e );
//            Log.e( TAG, "Could not load some required resource files. Make sure to configure " +
//                    "'assets' directory in your application and specify correct 'license file name'. See logcat for details.");
//        } catch( Engine.LicenseException e ) {
//            // Troubleshooting for the developer
//            Log.e( TAG, "Error loading ABBYY RTR SDK:", e );
//            Log.e( TAG, "License not valid. Make sure you have a valid license file in the " +
//                    "'assets' directory and specify correct 'license file name' and 'application id'. See logcat for details.");
//        } catch( Throwable e ) {
//            // Troubleshooting for the developer
//            Log.e( TAG, "Error loading ABBYY RTR SDK:", e );
//            Log.e( TAG, "Unspecified error while loading the engine. See logcat for details." );
//        }
//
//        return false;
//    }


}

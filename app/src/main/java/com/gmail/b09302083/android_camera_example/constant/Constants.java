package com.gmail.b09302083.android_camera_example.constant;

import android.hardware.Camera;
import android.hardware.camera2.CaptureRequest;
import android.media.CamcorderProfile;
import android.support.annotation.IntDef;
import android.support.annotation.StringDef;
import android.util.SparseIntArray;
import android.view.Surface;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by b09302083 on 2018/3/24.
 */
public class Constants {
    private static final String TAG = "Constants";

    //camera Front or Back
    public static final int CAMERA_FACING_BACK = 0;

    public static final int CAMERA_FACING_FRONT = 1;


    public static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    public static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();

    static {
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0);
    }


    /**
     * Camera state: Showing camera preview.
     */
    public static final int STATE_PREVIEW = 0;

    /**
     * Camera state: Waiting for the focus to be locked.
     */
    public static final int STATE_WAITING_LOCK = 1;

    /**
     * Camera state: Waiting for the exposure to be precapture state.
     */
    public static final int STATE_WAITING_PRECAPTURE = 2;

    /**
     * Camera state: Waiting for the exposure state to be something other than precapture.
     */
    public static final int STATE_WAITING_NON_PRECAPTURE = 3;

    /**
     * Camera state: Picture was taken.
     */
    public static final int STATE_PICTURE_TAKEN = 4;

    public static final int CAMERA_AF_AUTO = CaptureRequest.CONTROL_AF_MODE_AUTO;
    public static final int CAMERA_AF_EDOF = CaptureRequest.CONTROL_AF_MODE_EDOF;
    public static final int CAMERA_AF_MACRO = CaptureRequest.CONTROL_AF_MODE_MACRO;
    public static final int CAMERA_AF_OFF = CaptureRequest.CONTROL_AF_MODE_OFF;
    public static final int CAMERA_AF_CONTINUOUS_PICTURE = CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE;
    public static final int CAMERA_AF_CONTINUOUS_VIDEO = CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO;

    public static final int CAMERA_FLASH_OFF = CaptureRequest.CONTROL_AE_MODE_OFF;
    public static final int CAMERA_FLASH_ON = CaptureRequest.CONTROL_AE_MODE_ON;
    public static final int CAMERA_FLASH_AUTO = CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH;
    public static final int CAMERA_FLASH_ALWAYS = CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH;
    public static final int CAMERA_FLASH_REDEYE = CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH_REDEYE;
}

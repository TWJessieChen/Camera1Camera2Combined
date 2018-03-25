package com.gmail.b09302083.android_camera_example.chillingvantextureView;

import com.google.android.gms.vision.barcode.Barcode;

import com.chillingvan.canvasgl.textureFilter.BasicTextureFilter;
import com.chillingvan.canvasgl.textureFilter.TextureFilter;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.hardware.camera2.params.Face;
import android.util.Size;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by f9021 on 2018/1/5.
 */

public class MyGlRenderFilter {
    private static TextureFilter textureFilter = new BasicTextureFilter();
    private static Bitmap logoBitmap;
    private static  RectF[] faceRectFList;
    private static  RectF[] decodeFaceRectFList;
    private static  Face[] faceList;

    private static Map<Integer,com.google.android.gms.vision.face.Face> faceMap = new HashMap<>();
    private static Map<Integer,BarcodeBean> barcodeMap = new ConcurrentHashMap<>();

    private static Size previewSize;

    public static void setTextureFilter(TextureFilter filter) {
        textureFilter = filter;
    }

    public static TextureFilter getTextureFilter() {
        return textureFilter;
    }

    public static Bitmap getLogoBitmap() {
        return logoBitmap;
    }

    public static void setLogoBitmap(Bitmap logoBitmap) {
        MyGlRenderFilter.logoBitmap = logoBitmap;
    }
    public static RectF[] getFaceResult() {
        return faceRectFList;
    }

    public static Face[] getFaceList() {
        return faceList;
    }


    public static RectF[] getDecodeFaceList() {
        return decodeFaceRectFList;
    }


    public static void setFaceDetectResult( Face[] _faceList , RectF[] _faceRectFList , RectF[] _decodeMatrix) {

        MyGlRenderFilter.faceList = _faceList;
        MyGlRenderFilter.faceRectFList = _faceRectFList;
        MyGlRenderFilter.decodeFaceRectFList = _decodeMatrix;
    }


    public static Map<Integer, com.google.android.gms.vision.face.Face> getFaceMap() {
        return faceMap;
    }
    public static Map<Integer, BarcodeBean> getBarcodeBeanMap() {
        return barcodeMap;
    }

    public static Size getPreviewSize() {
        return previewSize;
    }

    public static void setPreviewSize(Size _previewSize) {
        previewSize = _previewSize;
    }

    static public class BarcodeBean{

        private Barcode barcode;
        private Bitmap textBitmap;

        public BarcodeBean(Barcode _barcode ,Bitmap _textBitmap ){
            barcode = _barcode;
            textBitmap = _textBitmap;
        }

        public Barcode getBarcode() {
            return barcode;
        }

        public void setBarcode(Barcode barcode) {
            this.barcode = barcode;
        }

        public Bitmap getTextBitmap() {
            return textBitmap;
        }

        public void setTextBitmap(Bitmap textBitmap) {
            this.textBitmap = textBitmap;
        }
    }


}

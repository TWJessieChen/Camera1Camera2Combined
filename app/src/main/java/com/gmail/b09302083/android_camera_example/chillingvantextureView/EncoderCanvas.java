package com.gmail.b09302083.android_camera_example.chillingvantextureView;

import com.google.android.gms.vision.barcode.Barcode;

import com.chillingvan.canvasgl.ICanvasGL;
import com.chillingvan.canvasgl.OffScreenCanvas;
import com.chillingvan.canvasgl.glcanvas.BasicTexture;
import com.chillingvan.canvasgl.glcanvas.GLPaint;
import com.chillingvan.canvasgl.glcanvas.RawTexture;
import com.chillingvan.canvasgl.glview.texture.gles.EglContextWrapper;
import com.chillingvan.canvasgl.textureFilter.TextureFilter;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.support.annotation.Nullable;
import android.util.Size;
import android.view.Surface;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by f9021 on 2018/1/4.
 */

public class EncoderCanvas extends OffScreenCanvas {

    private OnDrawListener onDrawListener;
    private static GLPaint paint ;

    public EncoderCanvas(int width, int height, EglContextWrapper eglCtx  ,Surface inputSurface ) {
        super(width, height, eglCtx, inputSurface);


    }
    public EncoderCanvas(int width, int height, Surface inputSurface ) {
        super(width, height, inputSurface);
        // !!!!!!!!!!!!!!!!!!!!!!!!!!!! important ///////////
        setProducedTextureTarget(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
    }


    @Override
    protected void onGLDraw(ICanvasGL iCanvasGL, SurfaceTexture surfaceTexture, RawTexture rawTexture, @Nullable SurfaceTexture outsideSurfaceTexture, @Nullable BasicTexture outsideTexture) {
//        if(outsideTexture== null || outsideSurfaceTexture==null)
//            return;

        rawTexture.setIsFlippedVertically(true);
        if (onDrawListener != null) {
            onDrawListener.onGLDraw(iCanvasGL, surfaceTexture, rawTexture, outsideSurfaceTexture, outsideTexture);
        }
        Size rotatedPreviewSize = MyGlRenderFilter.getPreviewSize();
        TextureFilter textureFilter = MyGlRenderFilter.getTextureFilter();
        Bitmap logoBitmap = MyGlRenderFilter.getLogoBitmap();
        int canvasWidth = rawTexture.getWidth();
        int canvasHeight = rawTexture.getHeight();
        if(outsideTexture== null || outsideSurfaceTexture==null) //maybe no set setSharedTexture() ---->for camera 2
            iCanvasGL.drawSurfaceTexture(rawTexture, surfaceTexture, 0, 0, rawTexture.getWidth(), rawTexture.getHeight(), textureFilter);
        else //use setSharedTexture()---->for camera 1
            iCanvasGL.drawSurfaceTexture(outsideTexture, outsideSurfaceTexture, 0, 0, canvasWidth , canvasHeight , textureFilter);

        if(logoBitmap!=null)
            iCanvasGL.drawBitmap(logoBitmap, 0, 0 , 200, 50);



        RectF[] deoceFaceList = MyGlRenderFilter.getDecodeFaceList();

        if(paint==null){
            paint  = new GLPaint();
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.STROKE);
            paint.setLineWidth(3);
        }


        if(deoceFaceList!=null){
            for(RectF rectF :deoceFaceList){
                iCanvasGL.drawRect(rectF,paint);
            }
        }

        //use google mobile vision SDK
        Map<Integer, MyGlRenderFilter.BarcodeBean> barcodeMap =  MyGlRenderFilter.getBarcodeBeanMap();
        Set<Integer> barcodeMapKeySet =  barcodeMap.keySet();
        Iterator<Integer> barcodeMapKeySetIterator = barcodeMapKeySet.iterator();
        while(barcodeMapKeySetIterator.hasNext()){
            Integer key = barcodeMapKeySetIterator.next();
            MyGlRenderFilter.BarcodeBean barcodeBean = barcodeMap.get(key);
            if(barcodeBean!=null && rotatedPreviewSize!=null){
                Barcode barcode = barcodeBean.getBarcode();
                Bitmap textBitmap = barcodeBean.getTextBitmap();
                float scaleFactorWidth = canvasWidth/(rotatedPreviewSize.getWidth()/4f);
                float scaleFactorHeight = canvasHeight/(rotatedPreviewSize.getHeight()/4f);
                RectF rect = new RectF(barcode.getBoundingBox());
                iCanvasGL.drawBitmap(textBitmap , (int)(rect.centerX()*scaleFactorWidth) ,  (int)(rect.centerY()*scaleFactorHeight));
                iCanvasGL.drawCircle(rect.centerX()*scaleFactorWidth,  rect.centerY()*scaleFactorHeight , 10, paint);
            }
        }

    }



    public void setOnDrawListener(OnDrawListener l) {
        this.onDrawListener = l;
    }
    public interface OnDrawListener {
        void onGLDraw(ICanvasGL canvasGL, SurfaceTexture surfaceTexture, RawTexture rawTexture,
                @Nullable SurfaceTexture outsideSurfaceTexture,
                @Nullable BasicTexture outsideTexture);
    }
}

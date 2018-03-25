/*
 *
 *  *
 *  *  * Copyright (C) 2016 ChillingVan
 *  *  *
 *  *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  * you may not use this file except in compliance with the License.
 *  *  * You may obtain a copy of the License at
 *  *  *
 *  *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *  *
 *  *  * Unless required by applicable law or agreed to in writing, software
 *  *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  * See the License for the specific language governing permissions and
 *  *  * limitations under the License.
 *  *
 *
 */

package com.gmail.b09302083.android_camera_example.chillingvantextureView;

import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.face.Face;

import com.chillingvan.canvasgl.ICanvasGL;
import com.chillingvan.canvasgl.glcanvas.BasicTexture;
import com.chillingvan.canvasgl.glcanvas.GLPaint;
import com.chillingvan.canvasgl.glcanvas.RawTexture;
import com.chillingvan.canvasgl.glview.texture.GLSurfaceTextureProducerView;
import com.chillingvan.canvasgl.textureFilter.BasicTextureFilter;
import com.chillingvan.canvasgl.textureFilter.TextureFilter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Size;
import android.view.View;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by Chilling on 2016/11/3.
 */

public class CameraPreviewTextureView extends GLSurfaceTextureProducerView implements IAutoFixView{
    private final String TAG = this.getClass().getSimpleName();
    private TextureFilter textureFilter = new BasicTextureFilter();
    private Bitmap robot;

    private int ratioWidth = 0;
    private int ratioHeight = 0;
    private static GLPaint paint;
    private SurfaceTexture _producedSurfaceTexture;
    private RawTexture _producedRawTexture;


    public CameraPreviewTextureView(Context context) {
        super(context);
    }

    public CameraPreviewTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CameraPreviewTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    @Override
    public void setAspectRatio(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        ratioWidth = width;
        ratioHeight = height;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = View.MeasureSpec.getSize(widthMeasureSpec);
        int height = View.MeasureSpec.getSize(heightMeasureSpec);
        if (0 == ratioWidth || 0 == ratioHeight) {
            setMeasuredDimension(width, height);
        } else {
            if (width < height * ratioWidth / ratioHeight) {
                setMeasuredDimension(width, width * ratioHeight / ratioWidth);
            } else {
                setMeasuredDimension(height * ratioWidth / ratioHeight, height);
            }
        }
    }

    public void setTextureFilter(TextureFilter textureFilter) {
        this.textureFilter = textureFilter;
    }

    @Override
    protected void onGLDraw(ICanvasGL canvas, SurfaceTexture producedSurfaceTexture, RawTexture producedRawTexture, @Nullable SurfaceTexture sharedSurfaceTexture, @Nullable BasicTexture sharedTexture) {
        //修正鏡像的問題
        producedRawTexture.setIsFlippedVertically(true);

        int canvasHeight = producedRawTexture.getHeight();
        int canvasWidth = producedRawTexture.getWidth();
        Size rotatedPreviewSize = MyGlRenderFilter.getPreviewSize();

        TextureFilter textureFilter = MyGlRenderFilter.getTextureFilter();
        Bitmap logoBitmap = MyGlRenderFilter.getLogoBitmap();
        canvas.drawSurfaceTexture(producedRawTexture, producedSurfaceTexture, 0, 0, producedRawTexture.getWidth(), producedRawTexture.getHeight(), textureFilter);


        _producedSurfaceTexture = producedSurfaceTexture;
        _producedRawTexture = producedRawTexture;
        if(logoBitmap!=null)
            canvas.drawBitmap(logoBitmap, 0, 0 , 200, 50);

        if(paint==null) {
            paint = new GLPaint();
            paint.setColor(Color.BLUE);
            paint.setStyle(Paint.Style.STROKE);
            paint.setLineWidth(3);
        }

        RectF[] faceList = MyGlRenderFilter.getFaceResult();
        if(faceList != null){
            for(RectF rectF :faceList){

                canvas.drawRect(rectF,paint);

//                GLPaint paint1 = new GLPaint();
//                paint1.setColor(Color.RED);
//                canvas.drawCircle(rectF.centerX(),rectF.centerY(),20,paint1);
            }

        }

/*
        20180212 y3 : S6的公式推算,
                要注意~~S6 左右相反,所以需要0變1440, 1440變0.
        Face[] faces = MyGlRenderFilter.getFaceList();
        if(faces != null){
            for(Face face :faces){
                GLPaint paint1 = new GLPaint();
                paint1.setColor(Color.YELLOW);
                paint1.setStyle(Paint.Style.STROKE);
                //canvas.drawRect(face.getBounds(),paint1);

                GLPaint paint = new GLPaint();
                paint.setColor(Color.RED);

                float s1 = producedRawTexture.getHeight() / 5328f  ;//1920
                float s2 = producedRawTexture.getWidth() / 2988f  ;//1440
                float ratio = Math.max(s1,s2);
                float offsetX = (5312f - (producedRawTexture.getHeight()/ratio) )/2;
                float offsetY = (2988f - (producedRawTexture.getWidth()/ratio) )/2;
                canvas.drawCircle(producedRawTexture.getWidth()- ((face.getBounds().centerY()-offsetY) *ratio ) , ((face.getBounds().centerX()-offsetX) *ratio) , 20,paint);
                Log.w(TAG,"faceList_ok2:" + ( producedRawTexture.getWidth()- ((face.getBounds().centerY()- offsetY) *ratio ) ) + "," + ( (face.getBounds().centerX()-offsetX) *ratio ) );
            }
        }
*/


        //use google mobile vision SDK
        Map<Integer, Face> faceMap =  MyGlRenderFilter.getFaceMap();
        Set<Integer> keySet =  faceMap.keySet();
        Iterator<Integer> keySetIterator = keySet.iterator();
        while(keySetIterator.hasNext()){
            Integer key = keySetIterator.next();
            Face face = faceMap.get(key);
            if(face!=null && rotatedPreviewSize!=null){

                float scaleFactorWidth = canvasWidth/(rotatedPreviewSize.getWidth()/4f);
                float scaleFactorHeight = canvasHeight/(rotatedPreviewSize.getHeight()/4f);

                float x = face.getPosition().x + face.getWidth() / 2  ;
                float y = face.getPosition().y + face.getHeight() / 2;
                canvas.drawCircle(x*scaleFactorWidth,  y*scaleFactorHeight , 10, paint);
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
                canvas.drawBitmap(textBitmap , (int)(rect.centerX()*scaleFactorWidth) ,  (int)(rect.centerY()*scaleFactorHeight));
                canvas.drawCircle(rect.centerX()*scaleFactorWidth,  rect.centerY()*scaleFactorHeight , 10, paint);
            }
        }

    }


   /* @Override
    public void onSurfaceChanged(int width, int height) {
        super.onSurfaceChanged(width, height);
        if (_producedRawTexture == null) {
            Log.d("AAA ","_producedRawTexture is null ");
            _producedSurfaceTexture = new SurfaceTexture(1);
            post(new Runnable() {
                @Override
                public void run() {
                    if (onSurfaceTextureSet != null) {
                        onSurfaceTextureSet.onSet(_producedSurfaceTexture, _producedRawTexture);
                    }
                }
            });
        }else {
            _producedRawTexture.setSize(width, height);
        }

    }
    private OnSurfaceTextureSet onSurfaceTextureSet;
    public interface OnSurfaceTextureSet {
        void onSet(SurfaceTexture surfaceTexture, RawTexture surfaceTextureRelatedTexture);
    }
    public void setOnSurfaceTextureSet(OnSurfaceTextureSet _onSurfaceTextureSet) {
        onSurfaceTextureSet = _onSurfaceTextureSet;
    }


    public void clearProducedRawTexture(){
        if(_producedRawTexture!=null){
            _producedRawTexture = null;
        }
        if (_producedSurfaceTexture != null) {
            _producedSurfaceTexture = null;
        }
    }*/

}

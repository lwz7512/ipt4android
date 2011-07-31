package com.pintu.tool;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.pintu.tool.LazyImageLoader.ImageLoaderCallback;
import com.pintu.PintuApp;

public class SimpleImageLoader {
    
    public static void display(final ImageView imageView, String url) {
        //��Ϊ�ж���������Ҫ�õ�ַƥ�����
    	imageView.setTag(url);
        Bitmap image = PintuApp.mImageLoader.get(url, createImageViewCallback(imageView, url));
        //����������о͵����������
        //���û�У���ʹ��Ĭ��ͼ��ʾ�����ȡ��ͼƬ��ʹ�ûص�����refresh        
        imageView.setImageBitmap(image);
    }
    
    public static ImageLoaderCallback createImageViewCallback(final ImageView imageView, String url)
    {
        return new ImageLoaderCallback() {
            @Override
            public void refresh(String url, Bitmap bitmap) {
                if (url.equals(imageView.getTag())) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        };
    }
}

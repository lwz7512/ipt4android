package com.pintu.tool;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.pintu.tool.LazyImageLoader.ImageLoaderCallback;
import com.pintu.PintuApp;

public class SimpleImageLoader {
    
    public static void display(final ImageView imageView, String url) {
        //因为有多个组件，需要用地址匹配组件
    	imageView.setTag(url);
        Bitmap image = PintuApp.mImageLoader.get(url, createImageViewCallback(imageView, url));
        //如果缓存中有就调用下面这句
        //如果没有，先使用默认图显示，随后取到图片后使用回调方法refresh        
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

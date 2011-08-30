package com.pintu.tool;

import java.io.File;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.pintu.PintuApp;
import com.pintu.R;
import com.pintu.tool.LazyImageLoader.ImageLoaderCallback;

public class SimpleImageLoader {
	
	

	//对外暴露的第一个方法，调用路径：
	//LazyImageLoader-->GetImageTask-->ImageManager-->PintuApp.mApi.getImgByUrl(url)
    public static void display(final ImageView imageView, String url) {
        //因为有多个组件，需要用地址匹配组件
    	imageView.setTag(url);
        Bitmap image = PintuApp.mImageLoader.get(url, createImageViewCallback(imageView, url));
        //如果缓存中有就调用下面这句
        //如果没有，先使用默认图显示，随后取到图片后使用回调方法refresh        
        imageView.setImageBitmap(image);
    }
    
    public static void displayForLarge(final ImageView imageView, String url){
    	imageView.setTag(url);
        Bitmap image = PintuApp.mImageLoader.get(url, createImageViewCallback(imageView, url));        
        //如果取回默认小图片，就用空白大图片先来展示  
        if(image==ImageManager.mDefaultBitmap){
        	imageView.setImageResource(R.drawable.mockpic_gray);        	
        }else{
        	imageView.setImageBitmap(image);
        }
    	
    }

	//对外暴露的第二个方法
	public static void clearAll(){
		PintuApp.mImageLoader.clearCache();
	}
	//对外暴露的第三个方法
	public static File compressRawImage(File targetFile){
		return PintuApp.mImageLoader.compreseImage(targetFile);
	}
    
    
    private static ImageLoaderCallback createImageViewCallback(final ImageView imageView, String url) {
        return new ImageLoaderCallback() {
            @Override
            public void refresh(String url, Bitmap bitmap) {
                if (url.equals(imageView.getTag())) {
                	if(bitmap!=null)
                		imageView.setImageBitmap(bitmap);
                }
            }
        };
    }
    
    
}

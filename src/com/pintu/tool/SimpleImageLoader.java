package com.pintu.tool;

import java.io.File;

import android.graphics.Bitmap;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.pintu.PintuApp;
import com.pintu.R;
import com.pintu.http.HttpException;
import com.pintu.tool.LazyImageLoader.ImageLoaderCallback;

public class SimpleImageLoader {

	// 对外暴露的第一个方法，调用路径：
	// LazyImageLoader-->GetImageTask-->ImageManager-->PintuApp.mApi.getImgByUrl(url)
	public static void display(final ImageView imageView, String url) {
		// 因为有多个组件，需要用地址匹配组件
		imageView.setTag(url);
		Bitmap image = PintuApp.mImageLoader.get(url,
				createImageViewCallback(imageView, url));
		// 如果缓存中有就调用下面这句
		// 如果没有，先使用默认图显示，随后取到图片后使用回调方法refresh
		imageView.setImageBitmap(image);
	}

	// 对外暴露的第二个方法
	public static void clearAll() {
		PintuApp.mImageLoader.clearCache();
	}

	// 对外暴露的第三个方法
	public static File compressRawImage(File targetFile) {
		return PintuApp.mImageLoader.compreseImage(targetFile);
	}

	// 对外暴露的第四个方法
	public static void displayForLarge(final ImageView imageView, String url) {
		imageView.setTag(url);
		Bitmap image = PintuApp.mImageLoader.get(url,
				createImageViewCallback(imageView, url));
		// 如果取回默认小图片，就用空白大图片先来展示
		if (image == ImageManager.mDefaultBitmap) {
			imageView.setImageResource(R.drawable.mockpic_gray);
		} else {
			imageView.setImageBitmap(image);
		}

	}

	// 对外暴露的第五个方法，查看图片详情时，用来修改图片的大小
	public static void displayForFit(final ImageView imageView, String url) {
		imageView.setTag(url);
		ImageLoaderCallback resize = new ImageLoaderCallback() {
			@Override
			public void refresh(String url, Bitmap bitmap) {
				if (url.equals(imageView.getTag())) {
					if (bitmap == null)
						return;
					// 取回后重新布局
					relayoutBitmap(bitmap, imageView);
				}
			}
		};
		Bitmap bitmap = PintuApp.mImageLoader.get(url, resize);
		// 如果没有缓存该图片，就用空白大图片先来展示
		if (bitmap == ImageManager.mDefaultBitmap) {
			imageView.setImageResource(R.drawable.mockpic_gray);
		} else {
			// 如果已经缓存了就直接显示
			relayoutBitmap(bitmap, imageView);
		}
	}
	
	//对外暴露的第6个方法
	/**
	 * 根据图片编号和类型下载文件到SD卡
	 * 注意该方法在UI线程中做
	 * 
	 * @param picId
	 * @param picType .png .jpg
	 * @return 保存在SD卡中的完整路径
	 */
	public static String downloadImage(String picId, String picType){
		String url = PintuApp.mApi.composeImgUrlById(picId);
		String completeFile = null;
		try {
			completeFile = PintuApp.mImageLoader.downloadImage(url, picType);
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return completeFile;
	}

	// 重新定义图片所在的布局容器
	// 注意，应当是在线性布局下才行
	private static void relayoutBitmap(Bitmap bitmap, ImageView imageView) {
		int picWidth = bitmap.getWidth();
		int picHeight = bitmap.getHeight();
		LinearLayout.LayoutParams layouts = new LinearLayout.LayoutParams(
				picWidth, picHeight);
		layouts.topMargin = 2;
		layouts.bottomMargin = 2;
		layouts.gravity = Gravity.CENTER;
		imageView.setLayoutParams(layouts);
		imageView.setImageBitmap(bitmap);
	}

	private static ImageLoaderCallback createImageViewCallback(
			final ImageView imageView, String url) {
		return new ImageLoaderCallback() {
			@Override
			public void refresh(String url, Bitmap bitmap) {
				if (url.equals(imageView.getTag())) {
					if (bitmap == null)
						return;
					imageView.setImageBitmap(bitmap);
				}
			}
		};
	}

}

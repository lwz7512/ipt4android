package com.pintu.adapter;

import java.util.ArrayList;
import java.util.List;

import com.pintu.tool.LazyImageLoader.ImageLoaderCallback;
import com.pintu.PintuApp;
import com.pintu.R;
import com.pintu.api.PTApi;
import com.pintu.data.TPicDesc;
import com.pintu.widget.StateImage;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class GalleryImageAdapter extends BaseAdapter {
	
	private Context mContext;
	//计算屏幕大小
	private DisplayMetrics  dm = new DisplayMetrics(); 
	//网格间距单位像素
	private int horiGap = 4;
	
	//画廊使用的列表数据
	private List<TPicDesc>  cells;
	
	private ImageLoaderCallback callback = new ImageLoaderCallback(){
		public void refresh(String url, Bitmap bitmap){
			GalleryImageAdapter.this.refresh();
		}
	};

	public GalleryImageAdapter(Context c) {
		mContext = c;
		cells = new ArrayList<TPicDesc>();
		((Activity) c).getWindowManager().getDefaultDisplay().getMetrics(dm); 
	}

	public int getCount() {
		return cells.size();
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView;
		if (convertView == null) {
			imageView = new StateImage(mContext);			
			//xml布局设置列数为4列
			int cellWidth = dm.widthPixels/4-horiGap;
			imageView.setLayoutParams(new GridView.LayoutParams(LayoutParams.FILL_PARENT, cellWidth));
			imageView.setAdjustViewBounds(false);
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			imageView.setPadding(0, 0, 0, 0);
		} else {
			imageView = (ImageView) convertView;
		}
		
		TPicDesc thumbnail = cells.get(position);
		String tbnlUrl = PintuApp.mApi.composeImgUrl(PTApi.THUMBNAIL_PIC, thumbnail.thumbnailId);
		Bitmap tbnlPic = PintuApp.mImageLoader.get(tbnlUrl, callback);
		imageView.setImageBitmap(tbnlPic);

		return imageView;
	}
	
	//为画廊指定数据
	public void refresh(List<TPicDesc> items){
		this.cells = items;
		this.notifyDataSetChanged();
	}
	
	
	public void refresh(){
		this.notifyDataSetChanged();
	}
	


} //end of class

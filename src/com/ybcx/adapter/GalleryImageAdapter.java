package com.ybcx.adapter;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.ybcx.R;
import com.ybcx.data.TPicDesc;
import com.ybcx.tool.SimpleImageLoader;

public class GalleryImageAdapter extends BaseAdapter {

	private Context mContext;
	// 网格间距单位像素
	//FIXME, 以后如果觉得距离有点大，就减小这个值就行了
	//2011/11/25
	private int horiGap = 8;

	// 画廊使用的列表数据
	private List<TPicDesc> cells;

	private LayoutInflater mInflater;
	// 计算屏幕大小
	private DisplayMetrics dm = new DisplayMetrics();

	// 画廊图片最大个数
	// private int maxThumbnailNum = 32;

	public GalleryImageAdapter(Context c) {
		mContext = c;
		// 初始化一个空的画廊数据容器
		cells = new ArrayList<TPicDesc>();
		mInflater = LayoutInflater.from(c);
		// 获得屏幕尺寸
		((Activity) c).getWindowManager().getDefaultDisplay().getMetrics(dm);
	}

	public int getCount() {
		return cells.size();
	}

	public TPicDesc getItem(int position) {
		return cells.get(position);
	}

	public long getItemId(int position) {
		return position;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView;
		if (convertView == null) {
			imageView = new ImageView(mContext);			
			//xml布局设置列数为4列，不要随便改
			int cellWidth = dm.widthPixels/4-horiGap;
			imageView.setLayoutParams(new GridView.LayoutParams(cellWidth, cellWidth));
			imageView.setAdjustViewBounds(false);
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			//FIXME, 修正了排列方式，这样间距比较合适，上下左右距离相等了
			//以后不要动这里了，算法就固定了
			//2011/11/25
			imageView.setPadding(0, 0, 0, 0);
		} else {
			imageView = (ImageView) convertView;
		}

		TPicDesc thumbnail = cells.get(position);	

		//获取图片，这个url是在RetrieveGalleryTask中取回数据后，
		//根据thumbnail编号拼接而成，详见该任务的数据处理
		SimpleImageLoader.display(imageView, thumbnail.url);

		return imageView;
	}


	// 为画廊指定数据
	public void refresh(List<TPicDesc> items) {
		this.cells = items;
		// 更新视图
		this.notifyDataSetChanged();
	}

	public void refresh() {
		this.notifyDataSetChanged();
	}

	public List<TPicDesc> getCells() {
		return cells;
	}

	private static class ViewHolder {
		public ImageView thumbnail;
	}
	


} // end of class

package com.pintu.adapter;

import com.pintu.R;
import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class GalleryImageAdapter extends BaseAdapter {

	private Context mContext;

	private DisplayMetrics  dm = new DisplayMetrics(); 
	       

	public GalleryImageAdapter(Context c) {
		mContext = c;
		((Activity) c).getWindowManager().getDefaultDisplay().getMetrics(dm); 
	}

	public int getCount() {
		return mThumbIds.length;
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
			imageView = new ImageView(mContext);
			int horiGap = 4;
			//xml布局设置列数为4列
			int cellWidth = dm.widthPixels/4-horiGap;
			imageView.setLayoutParams(new GridView.LayoutParams(LayoutParams.FILL_PARENT, cellWidth));
			imageView.setAdjustViewBounds(false);
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			imageView.setPadding(0, 0, 0, 0);
		} else {
			imageView = (ImageView) convertView;
		}

		imageView.setImageResource(mThumbIds[position]);

		return imageView;
	}

	private Integer[] mThumbIds = { R.drawable.sample_thumb_0,
			R.drawable.sample_thumb_1, R.drawable.sample_thumb_2,
			R.drawable.sample_thumb_3, R.drawable.sample_thumb_4,
			R.drawable.sample_thumb_5, R.drawable.sample_thumb_6,
			R.drawable.sample_thumb_7, R.drawable.sample_thumb_0,
			R.drawable.sample_thumb_1, R.drawable.sample_thumb_2,
			R.drawable.sample_thumb_3, R.drawable.sample_thumb_4,
			R.drawable.sample_thumb_5, R.drawable.sample_thumb_6,
			R.drawable.sample_thumb_7, R.drawable.sample_thumb_0,
			R.drawable.sample_thumb_1, R.drawable.sample_thumb_2,
			R.drawable.sample_thumb_3, R.drawable.sample_thumb_4,
			R.drawable.sample_thumb_5, R.drawable.sample_thumb_6,
			R.drawable.sample_thumb_7, R.drawable.sample_thumb_0,
			R.drawable.sample_thumb_1, R.drawable.sample_thumb_2,
			R.drawable.sample_thumb_3, R.drawable.sample_thumb_4,
			R.drawable.sample_thumb_5, R.drawable.sample_thumb_6,
			R.drawable.sample_thumb_7, };

}

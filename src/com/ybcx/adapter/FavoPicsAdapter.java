package com.ybcx.adapter;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.ybcx.R;
import com.ybcx.PintuApp;
import com.ybcx.data.TPicItem;
import com.ybcx.tool.SimpleImageLoader;
import com.ybcx.util.DateTimeHelper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FavoPicsAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private List<TPicItem> pics;
	
	public FavoPicsAdapter(Context c){
		mInflater = LayoutInflater.from(c);
		pics = new ArrayList<TPicItem>();
	}
	
	@Override
	public int getCount() {
		return pics.size();
	}

	@Override
	public Object getItem(int position) {
		return pics.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		if (convertView == null){
			view = mInflater.inflate(R.layout.simplepic, parent, false);
			ViewHolder holder = new ViewHolder();
			holder.favopic = (ImageView) view.findViewById(R.id.favpic);
			holder.pubTime = (TextView) view.findViewById(R.id.pubtime);
			view.setTag(holder);
		}else{
			view = convertView;
		}
		
		ViewHolder holder = (ViewHolder) view.getTag();
		TPicItem tpic = (TPicItem) this.getItem(position);
		
	   	String tpicUrl = PintuApp.mApi.composeImgUrlById(tpic.mobImgId);
    	//显示品图手机图片
    	SimpleImageLoader.displayForLarge(holder.favopic, tpicUrl);
		try {
			String relativeTime = DateTimeHelper.getRelativeTimeByFormatDate(tpic.publishTime);
			holder.pubTime.setText(relativeTime);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return view;
	}
	
	private static class ViewHolder{
		public ImageView favopic;
		public TextView pubTime;
	}
	
	
	public void refresh(List<TPicItem> pics){
		this.pics = pics;
		this.notifyDataSetChanged();
	}

}

package com.pintu.adapter;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.pintu.PintuApp;
import com.pintu.R;
import com.pintu.data.StoryInfo;
import com.pintu.data.TPicItem;
import com.pintu.tool.SimpleImageLoader;
import com.pintu.util.DateTimeHelper;
import com.pintu.util.IptHelper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
/**
 * @deprecated 此类已经不再使用了：2011/11/21
 * @author lwz
 *
 */
public class StoryInfoAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private List<StoryInfo> stories;
	
	public StoryInfoAdapter(Context c){
		mInflater = LayoutInflater.from(c);
		stories = new ArrayList<StoryInfo>();		
	}
	
	
	@Override
	public int getCount() {
		return stories.size();
	}

	@Override
	public Object getItem(int position) {
		return stories.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		if (convertView == null){
			view = mInflater.inflate(R.layout.storyrow, parent, false);
			ViewHolder holder = new ViewHolder();
			holder.pic_thumbnail = (ImageView) view.findViewById(R.id.pic_thumbnail);
			holder.story_pubtime = (TextView) view.findViewById(R.id.story_pubtime);
			holder.story_content = (TextView) view.findViewById(R.id.story_content);
			holder.heart_num = (TextView) view.findViewById(R.id.heart_num);
			holder.flower_num = (TextView) view.findViewById(R.id.flower_num);
			holder.egg_num = (TextView) view.findViewById(R.id.egg_num);
			holder.classic_num = (TextView) view.findViewById(R.id.classic_num);
			view.setTag(holder);
		}else{
			view = convertView;
		}
		
		ViewHolder holder = (ViewHolder) view.getTag();
		StoryInfo si = stories.get(position);
		
		// 显示发布时间
		String pubRelativeTime;
		try {
			pubRelativeTime = DateTimeHelper.getRelativeTimeByFormatDate(
					si.publishTime);
			holder.story_pubtime.setText(pubRelativeTime);
		} catch (ParseException e) {			
			e.printStackTrace();
		}
		
		//缩略图
		String thumbnailId = getThumbnailByPicId(si.follow);
    	String thumbnailUrl = PintuApp.mApi.composeImgUrlById(thumbnailId);    	
    	SimpleImageLoader.display(holder.pic_thumbnail, thumbnailUrl);	
		
		holder.story_content.setText(si.content);
		holder.heart_num.setText(String.valueOf(si.heart));
		holder.egg_num.setText(String.valueOf(si.egg));
		holder.flower_num.setText(String.valueOf(si.flower));
		holder.classic_num.setText(String.valueOf(si.star));

		return view;
	}
	
	private String getThumbnailByPicId(String picId){
		return picId + "_Thumbnail";
	}	
	
	private static class ViewHolder{
		public ImageView pic_thumbnail;
		public TextView story_pubtime;		
		public TextView story_content;
		
		public TextView heart_num;
		public TextView flower_num;
		public TextView egg_num;
		public TextView classic_num;

		
	}
	
	public void refresh(List<StoryInfo> stories){
		this.stories = stories;
		this.notifyDataSetChanged();
	}

}

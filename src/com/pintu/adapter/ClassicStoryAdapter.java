package com.pintu.adapter;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pintu.PintuApp;
import com.pintu.R;
import com.pintu.data.StoryInfo;
import com.pintu.tool.SimpleImageLoader;
import com.pintu.util.DateTimeHelper;
import com.pintu.util.IptHelper;

public class ClassicStoryAdapter extends BaseAdapter {

	private Context ctxt;
	private LayoutInflater mInflater;
	private List<StoryInfo> storyies;
	
	public ClassicStoryAdapter(Context c){
		this.ctxt = c;
		mInflater = LayoutInflater.from(c);
		storyies = new ArrayList<StoryInfo>();
	}
	
	@Override
	public int getCount() {
		return storyies.size();
	}

	@Override
	public Object getItem(int position) {
		return storyies.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		if (convertView == null){
			view = mInflater.inflate(R.layout.classicitem, parent, false);
			ViewHolder holder = new ViewHolder();
			//作者头像
			holder.classic_author_img = (ImageView) view.findViewById(R.id.classic_author_img);
			holder.classic_author_name = (TextView) view.findViewById(R.id.classic_author_name);
			holder.classic_pubtime = (TextView) view.findViewById(R.id.classic_pubtime);
			//故事正文
			holder.classic_story_txt = (TextView) view.findViewById(R.id.classic_story_txt);
			//缩略图
			holder.pic_thumbnail = (ImageView) view.findViewById(R.id.pic_thumbnail);
			view.setTag(holder);
		}else{
			view = convertView;
		}
		
		ViewHolder holder = (ViewHolder) view.getTag();
		StoryInfo story = (StoryInfo) getItem(position);
		
		String authorName = IptHelper.getShortUserName(story.author);
		holder.classic_author_name.setText(authorName);
		
    	String profileUrl = PintuApp.mApi.composeImgUrlByPath(story.avatarImgPath);
    	//显示头像
    	SimpleImageLoader.display(holder.classic_author_img, profileUrl);
    	
		try {
			String relativeTime = DateTimeHelper.getRelativeTimeByFormatDate(story.publishTime);
			holder.classic_pubtime.setText(relativeTime);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//故事正文
		holder.classic_story_txt.setText(story.content);
		
		//缩略图
		String thumbnailId = getThumbnailByPicId(story.follow);
    	String thumbnailUrl = PintuApp.mApi.composeImgUrlById(thumbnailId);    	
    	SimpleImageLoader.display(holder.pic_thumbnail, thumbnailUrl);		

		return view;
	}
	
	private String getThumbnailByPicId(String picId){
		return picId + "_Thumbnail";
	}
	
	private static class ViewHolder{
		public ImageView classic_author_img;
		public TextView classic_author_name;
		public TextView classic_pubtime;		
		public TextView classic_story_txt;
		public ImageView pic_thumbnail;		
	}

	public void refresh(List<StoryInfo> stories){
		this.storyies = stories;
		this.notifyDataSetChanged();
	}


}

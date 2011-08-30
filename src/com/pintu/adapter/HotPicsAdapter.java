package com.pintu.adapter;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.pintu.PintuApp;
import com.pintu.R;
import com.pintu.data.TPicDetails;
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

public class HotPicsAdapter extends BaseAdapter {

	private Context ctxt;
	private LayoutInflater mInflater;
	private List<TPicDetails> hotpicData;
	
	public HotPicsAdapter(Context c){
		ctxt = c;
		mInflater = LayoutInflater.from(c);
		hotpicData = new ArrayList<TPicDetails>();
	}
	
	@Override
	public int getCount() {
		return hotpicData.size();
	}

	@Override
	public Object getItem(int position) {
		return hotpicData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		if (convertView == null){
			view = mInflater.inflate(R.layout.picitem, parent, false);
			ViewHolder holder = new ViewHolder();
			//作者头像
			holder.hotpic_author_img = (ImageView) view.findViewById(R.id.hotpic_author_img);			
			holder.hotpic_author_name = (TextView) view.findViewById(R.id.hotpic_author_name);
			holder.hotpic_pubtime = (TextView) view.findViewById(R.id.hotpic_pubtime);
			//热图
			holder.hot_pic_image = (ImageView) view.findViewById(R.id.hot_pic_image);
			holder.hot_pic_commentnum = (TextView) view.findViewById(R.id.hot_pic_commentnum);
			holder.hot_pic_storynum = (TextView) view.findViewById(R.id.hot_pic_storynum);			
			view.setTag(holder);
		}else{
			view = convertView;
		}
		
		ViewHolder holder = (ViewHolder) view.getTag();
		TPicDetails tpic = (TPicDetails) getItem(position);
		String authorName = IptHelper.getShortUserName(tpic.author);
		holder.hotpic_author_name.setText(authorName);
		try {
			String relativeTime = DateTimeHelper.getRelativeTimeByFormatDate(tpic.publishTime, ctxt);
			holder.hotpic_pubtime.setText(relativeTime);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		holder.hot_pic_commentnum.setText(tpic.commentsNum);
		holder.hot_pic_storynum.setText(tpic.storiesNum);
		
    	String profileUrl = PintuApp.mApi.composeImgUrlByPath(tpic.avatarImgPath);
    	//显示头像
    	SimpleImageLoader.display(holder.hotpic_author_img, profileUrl);
    	String tpicUrl = PintuApp.mApi.composeImgUrlById(tpic.mobImgId);
    	//显示品图手机图片
    	SimpleImageLoader.displayForLarge(holder.hot_pic_image, tpicUrl);
		
		return view;
	}
	
	private static class ViewHolder{
		public ImageView hotpic_author_img;
		public TextView hotpic_author_name;
		public TextView hotpic_pubtime;
		
		public ImageView hot_pic_image;
		
		public TextView hot_pic_commentnum;
		public TextView hot_pic_storynum;		
	}
	
	public void refresh(List<TPicDetails> hotpicData){
		this.hotpicData = hotpicData;
		this.notifyDataSetChanged();
	}

}

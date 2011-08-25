package com.pintu.adapter;

import java.text.ParseException;
import java.util.ArrayList;

import com.pintu.R;
import com.pintu.data.CommentInfo;
import com.pintu.data.StoryInfo;
import com.pintu.util.DateTimeHelper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class CommentsAdapter extends BaseAdapter {

	
	private Context ctxt;
	private ArrayList<CommentInfo> comments;
	private LayoutInflater mInflater;
	
	public CommentsAdapter(Context c){
		ctxt = c;
		mInflater = LayoutInflater.from(c);
		comments = new ArrayList<CommentInfo>();
	}
	
	
	@Override
	public int getCount() {
		return comments.size();
	}

	@Override
	public Object getItem(int position) {
		return comments.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		if (convertView == null) {
			view = mInflater.inflate(R.layout.commentitem, parent, false);
			
			ViewHolder holder = new ViewHolder();
			holder.comment_author = (TextView) view.findViewById(R.id.comment_author);
			holder.comment_pubtime = (TextView) view.findViewById(R.id.comment_pubtime);
			holder.comment_txt = (TextView) view.findViewById(R.id.comment_txt);			
			// 缓存起来
			view.setTag(holder);
		}else {
			view = convertView;
		}
		
		ViewHolder holder = (ViewHolder) view.getTag();
		CommentInfo ci = comments.get(position);
		
		String author = getShortUserName(ci.author);
		//显示评论人，没必要提供点击查看此人了，就此打住
		holder.comment_author.setText(author);
		
		// 评论发布相对时间
		String pubRelativeTime;
		try {
			pubRelativeTime = DateTimeHelper.getRelativeTimeByFormatDate(
					ci.publishTime, ctxt);
			//评论时间
			holder.comment_pubtime.setText(pubRelativeTime);
		} catch (ParseException e) {			
			e.printStackTrace();
		}
		//评论内容
		holder.comment_txt.setText(ci.content);				
		
		return view;
	}
	
	private String getShortUserName(String userName) {
		String showName = userName;
		if (userName.contains("@")) {
			int atPos = userName.indexOf("@");
			showName = userName.substring(0, atPos);
		}
		return showName;
	}

	
	private static class ViewHolder {
		
		public TextView comment_author;
		public TextView comment_pubtime;
		public TextView comment_txt;
	
	}
	
	
	public void refresh(ArrayList<CommentInfo> comments){
		this.comments = comments;
		this.notifyDataSetChanged();
	}

}

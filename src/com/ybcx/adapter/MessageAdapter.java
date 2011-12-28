package com.ybcx.adapter;

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

import com.ybcx.R;
import com.ybcx.PintuApp;
import com.ybcx.data.TMsg;
import com.ybcx.tool.SimpleImageLoader;
import com.ybcx.util.DateTimeHelper;
import com.ybcx.util.IptHelper;

public class MessageAdapter extends BaseAdapter {

	private List<TMsg> msgs;
	private Context ctxt;
	private LayoutInflater mInflater;
	
	public MessageAdapter(Context c){
		this.ctxt = c;
		mInflater = LayoutInflater.from(c);
		msgs = new ArrayList<TMsg>();
	}
	
	@Override
	public int getCount() {
		return msgs.size();
	}

	@Override
	public Object getItem(int position) {
		return msgs.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		if(convertView == null){
			view = mInflater.inflate(R.layout.msgitem, parent, false);
			ViewHolder holder = new ViewHolder();

			holder.sender_img = (ImageView) view.findViewById(R.id.sender_img);
			holder.msg_sender = (TextView) view.findViewById(R.id.msg_sender);
			holder.msg_content = (TextView) view.findViewById(R.id.msg_content);
			holder.msg_pubtime = (TextView) view.findViewById(R.id.msg_pubtime);
			view.setTag(holder);
		}else{
			view = convertView;
		}
		
		ViewHolder holder = (ViewHolder) view.getTag();
		TMsg msg = (TMsg) msgs.get(position);
		
		String profileUrl = PintuApp.mApi.composeImgUrlByPath(msg.senderAvatar);
		//显示头像
		SimpleImageLoader.display(holder.sender_img, profileUrl);
		String authorName = IptHelper.getShortUserName(msg.senderName);
		holder.msg_sender.setText(authorName);
		holder.msg_content.setText(msg.content);

		try {
			String relativeTime = DateTimeHelper.getRelativeTimeByFormatDate(msg.writeTime);
			holder.msg_pubtime.setText(relativeTime);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return view;
	}
	
	private static class ViewHolder{
		public ImageView sender_img;
		public TextView msg_sender;
		public TextView msg_content;
		public TextView msg_pubtime;
	}
	
	public void refresh(List<TMsg> msgs){
		this.msgs = msgs;
		this.notifyDataSetChanged();
	}
	
	public void append(List<TMsg> queriedMsg){
		for(TMsg msg : queriedMsg){
			boolean isShowed = false;
			for(TMsg show : msgs){
				if(show.id.equals(msg.id))
					isShowed = true;
				break;
			}
			if(!isShowed)
				this.msgs.add(msg);
		}
		this.notifyDataSetChanged();
	}

}

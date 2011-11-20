package com.pintu.adapter;

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
import com.pintu.data.UserInfo;
import com.pintu.tool.SimpleImageLoader;
import com.pintu.util.IptHelper;

public class UserAdapter extends BaseAdapter {

	public static final String PIC_DAREN = "picdaren";
	public static final String CMT_DAREN = "cmtdaren";
	
	private LayoutInflater mInflater;
	private List<UserInfo> users;
	private Context ctxt;
	//图片达人还是评论达人
	private String type;
	
	public UserAdapter(Context c, String type){
		ctxt = c;
		this.type = type;
		mInflater = LayoutInflater.from(c);
		users = new ArrayList<UserInfo>();
	}
	
	@Override
	public int getCount() {
		return users.size();
	}

	@Override
	public Object getItem(int position) {
		return users.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		if (convertView == null){
			view = mInflater.inflate(R.layout.dareninfo, parent, false);
			ViewHolder holder = new ViewHolder();
			holder.profile_image = (ImageView) view.findViewById(R.id.profile_image);
			holder.user_name = (TextView) view.findViewById(R.id.user_name);
			holder.user_level = (TextView) view.findViewById(R.id.user_level);
			holder.user_score = (TextView) view.findViewById(R.id.user_score);			
			holder.user_picnum = (TextView) view.findViewById(R.id.user_picnum);
			holder.user_storynum = (TextView) view.findViewById(R.id.user_storynum);			
			view.setTag(holder);
		}else{
			view = convertView;
		}
		
		ViewHolder holder = (ViewHolder) view.getTag();
		UserInfo usr = (UserInfo) this.getItem(position);	
		
		String profileUrl = PintuApp.mApi.composeImgUrlByPath(usr.avatar);
		// 显示头像
		SimpleImageLoader.display(holder.profile_image, profileUrl);

		String userName = IptHelper.getShortUserName(usr.account);
		holder.user_name.setText(userName);
		
		String userLevel = ctxt.getText(R.string.userlevel)+"  "+usr.level;
		holder.user_level.setText(userLevel);
		
		String userScore = ctxt.getText(R.string.totalscore)+"  "+usr.score;
		holder.user_score.setText(userScore);				

		String userPicNum = ctxt.getText(R.string.picnum)+"  "+usr.tpicNum;
		holder.user_picnum.setText(userPicNum);
		String storyNum = ctxt.getText(R.string.storynum)+"  "+usr.storyNum;
		holder.user_storynum.setText(storyNum);
		
		//图片达人不看评论数目
		if(PIC_DAREN.equals(type)){
			holder.user_storynum.setVisibility(View.GONE);
		}else{
			//评论达人不看图片数目
			holder.user_picnum.setVisibility(View.GONE);
		}
		
		return view;
	}
	
	private static class ViewHolder{
		public ImageView profile_image;
		public TextView user_name;
		public TextView user_level;
		public TextView user_score;
		public TextView user_picnum;
		public TextView user_storynum;
		
	}
	
	public void refresh(List<UserInfo> users){
		this.users = users;
		this.notifyDataSetChanged();
	}

}

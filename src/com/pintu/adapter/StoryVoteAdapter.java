package com.pintu.adapter;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.pintu.R;
import com.pintu.data.StoryInfo;
import com.pintu.util.DateTimeHelper;
import com.pintu.util.IptHelper;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class StoryVoteAdapter extends BaseAdapter {

	private Context ctxt;
	private List<StoryInfo> items;
	private LayoutInflater mInflater;
	// 在StoryList中设置该回调方法
	private VoteActionListener voteListener;

	public StoryVoteAdapter(Context c) {
		ctxt = c;
		mInflater = LayoutInflater.from(c);
		items = new ArrayList<StoryInfo>();
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	private static class ViewHolder {
		//存下来为投票作参数
		public String storyId;
		//故事作者
		public String owner;
		public TextView story_author;
		public TextView story_pubtime;

		//设置点击开关，只能点一次
		boolean flowerFlag = false;
		boolean eggFlag = false;
		boolean heartFlag = false;
		boolean starFlag = false;

		public TextView story_txt;
		public TextView heart_num;
		public TextView flower_num;
		public TextView egg_num;
		public TextView classic_num;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		if (convertView == null) {
			view = mInflater.inflate(R.layout.storyitem, parent, false);

			ViewHolder holder = new ViewHolder();
			holder.story_author = (TextView) view
					.findViewById(R.id.story_author);
			holder.story_pubtime = (TextView) view
					.findViewById(R.id.story_pubtime);
			holder.story_txt = (TextView) view.findViewById(R.id.story_txt);
			
			//FIXME, 这些投票都用不着了
//			holder.heart_num = (TextView) view.findViewById(R.id.heart_num);
//			holder.flower_num = (TextView) view.findViewById(R.id.flower_num);
//			holder.egg_num = (TextView) view.findViewById(R.id.egg_num);
//			holder.classic_num = (TextView) view.findViewById(R.id.classic_num);
			// 缓存起来
			view.setTag(holder);
		} else {
			view = convertView;
		}

		// 显示内容
		ViewHolder holder = (ViewHolder) view.getTag();
		// 添加投票交互
//		if (voteListener != null) addVoteAction(holder);

		StoryInfo si = items.get(position);
		//保存故事ID，以便投票使用
		holder.storyId = si.id;
		//保存故事作者
		holder.owner = si.owner;
		
		// 显示作者
		String author = IptHelper.getShortUserName(si.author);
		holder.story_author.setText(author);
		// 显示发布时间
		String pubRelativeTime;
		try {
			pubRelativeTime = DateTimeHelper.getRelativeTimeByFormatDate(
					si.publishTime);
			holder.story_pubtime.setText(pubRelativeTime);
		} catch (ParseException e) {			
			e.printStackTrace();
		}
		holder.story_txt.setText(si.content);
		
		//FIXME, 这些投票项先不用了
//		holder.heart_num.setText(String.valueOf(si.heart));
//		holder.egg_num.setText(String.valueOf(si.egg));
//		holder.flower_num.setText(String.valueOf(si.flower));
//		holder.classic_num.setText(String.valueOf(si.star));

		return view;
	}

	private void addVoteAction(ViewHolder holder) {
		// 需要变成final的才能在内部类调用
		final ViewHolder fv = holder;

		holder.heart_num.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				//不能连续点
				if (!fv.heartFlag) {
					//通知外面投票点击了
					voteListener.send(fv.owner, fv.storyId, "heart");
					addVoteNum(fv.heart_num);
					fv.heartFlag = true;
				}
			}
		});
		holder.classic_num.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				//不能连续点
				if (!fv.starFlag) {
					//通知外面投票点击了
					voteListener.send(fv.owner, fv.storyId, "star");
					addVoteNum(fv.classic_num);
					fv.starFlag = true;
				}
			}
		});
		holder.egg_num.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				//不能连续点
				if (!fv.eggFlag) {
					//通知外面投票点击了
					voteListener.send(fv.owner, fv.storyId, "egg");
					addVoteNum(fv.egg_num);
					fv.eggFlag = true;

				}
			}
		});
		holder.flower_num.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				//不能连续点
				if (!fv.flowerFlag) {
					//通知外面投票点击了
					voteListener.send(fv.owner, fv.storyId, "flower");
					addVoteNum(fv.flower_num);
					fv.flowerFlag = true;
				}
			}
		});

	}

	//给投票文字加一
	private void addVoteNum(TextView vote) {
		Activity parent = (Activity) ctxt;
		final TextView fv = vote;
		// 在UI线程中更新文字，不能直接更新
		parent.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				int origNum = Integer.valueOf(fv.getText().toString());
				// 点一下，加一下
				fv.setText(String.valueOf(origNum + 1));
			}
		});
	}
	

	public void refresh(List<StoryInfo> stories) {
		this.items = stories;
		this.notifyDataSetChanged();
	}

	public void setVoteListener(VoteActionListener voteListener) {
		this.voteListener = voteListener;
	}

	// 只能定义接口
	public interface VoteActionListener {
		public void send(String owner, String storyId, String type);
	}

}

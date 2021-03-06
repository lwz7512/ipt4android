package com.ybcx.activity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ybcx.R;
import com.ybcx.activity.base.TempletActivity;
import com.ybcx.adapter.StoryVoteAdapter;
import com.ybcx.adapter.StoryVoteAdapter.VoteActionListener;
import com.ybcx.api.PTApi;
import com.ybcx.data.StoryInfo;
import com.ybcx.task.RetrieveStoriesTask;
import com.ybcx.task.SendTask;
import com.ybcx.task.TaskParams;
import com.ybcx.tool.SimpleImageLoader;
import com.ybcx.widget.StateListView;

/**
 * 目前称之为评论列表
 * 其实最初打算是叫故事来着，后来简化为评论了
 * 但是这个Story的称呼还留着，意思是精彩的评论
 * 2011//11/21
 * @author lwz
 *
 */
public class StoryList extends TempletActivity {

	private static String TAG = "StoryList";

	// Header
	// 返回按钮
	private ImageButton top_back;
	// 顶部标题
	private TextView tv_title;
	// 加载进度条
	private ProgressBar details_prgrsBar;

	// Pic info
	private ImageView pic_to_storied;
	private TextView pic_author;
	private TextView pub_time;

	// 故事列表
	private StateListView story_votes;
	private StoryVoteAdapter svAdptr;

	// 获取故事列表的目标图编号
	private String tpId;
	
	
	//故事作者
	private String owner;
	//发送投票的目标故事编号
	private String storyId;
	//投票种类
	private String voteType;


	@Override
	protected int getLayout() {
		return R.layout.stories;
	}

	protected void getViews() {
		top_back = (ImageButton) findViewById(R.id.top_back);

		tv_title = (TextView) findViewById(R.id.tv_title);
		// 标题文字设置
		tv_title.setText(R.string.commentlist);

		details_prgrsBar = (ProgressBar) findViewById(R.id.details_prgrsBar);

		pic_to_storied = (ImageView) findViewById(R.id.pic_to_storied);
		pic_author = (TextView) findViewById(R.id.pic_author);
		pub_time = (TextView) findViewById(R.id.pub_time);

		svAdptr = new StoryVoteAdapter(this);
		svAdptr.setVoteListener(voteListener);
		story_votes = (StateListView) findViewById(R.id.story_votes);
		story_votes.setAdapter(svAdptr);

	}
	
	@Override
	protected void justDoIt() {
		// 展示图片信息
		showPicInfo();
		// 获取故事列表
		getStories();
	}


	private VoteActionListener voteListener = new VoteActionListener() {
		@Override
		public void send(String owner, String storyId, String type) {
			//只有在列表静止状态下，才处理投票提交
			if (story_votes.isStatic())
				postVoteForStory(owner, storyId, type); 
		}
	};
	
	private void postVoteForStory(String owner, String storyId, String vote){
		this.owner = owner;
		this.storyId = storyId;
		this.voteType = vote;
		if(storyId!=null && vote!=null && owner!=null){
			doSend();
		}else{
			this.updateProgress("Can not vote, missing info! ");
		}
	}
	


	protected void addEventListeners() {
		top_back.setOnClickListener(mGoListener);
	}

	private OnClickListener mGoListener = new OnClickListener() {
		public void onClick(View v) {
			finish();
		}
	};

	private void showPicInfo() {
		// 只能在活动创建时获取参数
		Intent received = getIntent();
		Bundle extras = received.getExtras();
		// Must has extras
		if (null == extras) {
			Log.e(TAG, this.getClass().getName() + " must has extras.");
			finish();
			return;
		}
		String tpicUrl = extras.getString("tpicUrl");
		String author = extras.getString("author");
		String pubTime = extras.getString("pubTime");
		// 保存一个原图ID，作为获取故事列表的重要参数
		tpId = extras.getString("tpId");

		SimpleImageLoader.display(pic_to_storied, tpicUrl);
		pic_author.setText(author);
		pub_time.setText(pubTime);
	}

	private void getStories() {
		if (tpId != null) {
			doRetrieve();
		} else {
			updateProgress("Warning, tpId is null!");
			Log.e(TAG, "Warning, tpId is null!");
		}

	}

	protected void doRetrieve() {
		if(!checkTaskStatus()) return;

		mRetrieveTask = new RetrieveStoriesTask();
		mRetrieveTask.setListener(mRetrieveTaskListener);

		TaskParams params = new TaskParams();
		params.put("tpId", tpId);
		params.put("method", PTApi.GETSTORIESOFPIC);
		mRetrieveTask.execute(params);

		this.manageTask(mRetrieveTask);
	}
	

	@Override
	protected void doSend() {
		if(!checkTaskStatus()) return;
		
		int mode = SendTask.TYPE_VOTE;
		mSendTask = new SendTask();
		mSendTask.setListener(mSendTaskListener);

		TaskParams params = new TaskParams();
		params.put("mode", mode);
		//后台依据receiver来发送投票消息给故事作者
		params.put("receiver", owner);
		params.put("type", voteType);
		params.put("follow", storyId);
		params.put("amount","1");
		mSendTask.execute(params);
		
		this.manageTask(mSendTask);
	}

	@Override
	protected void onSendBegin() {
		details_prgrsBar.setVisibility(View.VISIBLE);
	}

	@Override
	protected void onSendSuccess() {
		details_prgrsBar.setVisibility(View.GONE);
	}

	@Override
	protected void onSendFailure() {
		updateProgress(getString(R.string.page_status_unable_to_update));
		details_prgrsBar.setVisibility(View.GONE);
	}

	@Override
	protected void onRetrieveBegin() {
		details_prgrsBar.setVisibility(View.VISIBLE);
	}

	@Override
	protected void onRetrieveSuccess() {
		details_prgrsBar.setVisibility(View.GONE);
	}

	@Override
	protected void onRetrieveFailure() {
		updateProgress(getString(R.string.page_status_unable_to_update));
		details_prgrsBar.setVisibility(View.GONE);
	}

	@Override
	protected void onParseJSONResultFailue() {
		updateProgress("Response to json data parse Error!");
	}

	@Override
	protected void refreshListView(List<Object> results) {
		ArrayList<StoryInfo> stories = new ArrayList<StoryInfo>();
		for (Object o : results) {
			stories.add((StoryInfo) o);
		}
		// 更新视图列表
		svAdptr.refresh(stories);
	}

	@Override
	protected void refreshMultView(JSONObject json) {
		// do nothing, but, do not kill me...
	}


} // end of class

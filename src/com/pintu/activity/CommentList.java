package com.pintu.activity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pintu.R;
import com.pintu.adapter.CommentsAdapter;
import com.pintu.adapter.StoryVoteAdapter;
import com.pintu.data.CommentInfo;
import com.pintu.task.RetrieveCommentsTask;
import com.pintu.task.TaskParams;
import com.pintu.tool.SimpleImageLoader;
import com.pintu.widget.StateListView;

public class CommentList extends TempletActivity {

	private static String TAG = "CommentList";

	// Header
	// 返回按钮
	private Button top_back;
	// 顶部标题
	private TextView tv_title;
	// 加载进度条
	private ProgressBar details_prgrsBar;

	// Pic info
	private ImageView pic_to_storied;
	private TextView pic_author;
	private TextView pub_time;

	// 故事列表
	private ListView comments_lv;
	private CommentsAdapter cmtAdptr;

	// 发送评论的目标图编号
	private String tpId;

	
	@Override
	protected int getLayout() {
		return R.layout.comments;
	}

	@Override
	protected void getViews() {
		top_back = (Button) findViewById(R.id.top_back);

		tv_title = (TextView) findViewById(R.id.tv_title);
		// 标题文字设置
		tv_title.setText(R.string.commentlist);

		details_prgrsBar = (ProgressBar) findViewById(R.id.details_prgrsBar);

		pic_to_storied = (ImageView) findViewById(R.id.pic_to_storied);
		pic_author = (TextView) findViewById(R.id.pic_author);
		pub_time = (TextView) findViewById(R.id.pub_time);

		cmtAdptr = new CommentsAdapter(this);
		comments_lv = (ListView) findViewById(R.id.comments_lv);
		comments_lv.setAdapter(cmtAdptr);
	}

	@Override
	protected void addEventListeners() {
		top_back.setOnClickListener(mGoListener);		
	}
	
	private OnClickListener mGoListener = new OnClickListener() {
		public void onClick(View v) {
			finish();
		}
	};


	@Override
	protected void justDoIt() {
		//显示被评论的图片和作者
		showPicInfo();
		//取评论数据
		getComments();
	}

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
	
	private void getComments() {
		if (tpId != null) {
			doRetrieve();
		} else {
			updateProgress("Warning, tpId is null!");
			Log.e(TAG, "Warning, tpId is null!");
		}
	}


	@Override
	protected void doRetrieve() {
		this.checkTaskStatus();
		
		mRetrieveTask = new RetrieveCommentsTask();
		mRetrieveTask.setListener(mRetrieveTaskListener);
		
		TaskParams params = new TaskParams();
		params.put("tpId", tpId);
		mRetrieveTask.execute(params);
		
		this.manageTask(mRetrieveTask);
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
	}

	@Override
	protected void onParseJSONResultFailue() {
		updateProgress("Response to json data parse Error!");		
	}

	//评论数据已经在RetrieveCommentsTask处理完成了
	//由TaskListener回调传到这个方法中，进行视图刷新
	@Override
	protected void refreshListView(List<Object> results) {
		ArrayList<CommentInfo> comments = new ArrayList<CommentInfo>();
		for(Object o : results){
			CommentInfo cmt = (CommentInfo) o;
			comments.add(cmt);
		}
		this.cmtAdptr.refresh(comments);
	}
	
	
//	------  下面的方法这里用不着，但是别删了，因为他们是父类抽象方法的实现 ------

	@Override
	protected void refreshMultView(JSONObject json) {
		//do nothing, but leave it stay here don't kill me...		
	}
	
	@Override
	protected void doSend() {
		//do nothing, but leave it stay here don't kill me...		
	}

	@Override
	protected void onSendBegin() {
		//do nothing, but leave it stay here don't kill me...		
	}

	@Override
	protected void onSendSuccess() {
		//do nothing, but leave it stay here don't kill me...		
	}

	@Override
	protected void onSendFailure() {
		//do nothing, but leave it stay here don't kill me...		
	}

	
} //end of class

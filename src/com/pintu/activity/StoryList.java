package com.pintu.activity;

import java.util.ArrayList;
import java.util.List;

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
import android.widget.Toast;

import com.pintu.R;
import com.pintu.adapter.StoryVoteAdapter;
import com.pintu.adapter.StoryVoteAdapter.VoteActionListener;
import com.pintu.data.StoryInfo;
import com.pintu.task.GenericTask;
import com.pintu.task.RetrieveStoriesTask;
import com.pintu.task.TaskAdapter;
import com.pintu.task.TaskListener;
import com.pintu.task.TaskManager;
import com.pintu.task.TaskParams;
import com.pintu.task.TaskResult;
import com.pintu.tool.SimpleImageLoader;
import com.pintu.widget.StateListView;

public class StoryList extends FullScreenActivity {

	private static String TAG = "StoryList";

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
	private StateListView story_votes;
	private StoryVoteAdapter svAdptr;

	// 发送故事的目标图编号
	private String tpId;

	// 获取故事列表任务
	private GenericTask mRetrieveTask;

	protected TaskManager taskManager = new TaskManager();

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stories);

		// 获得组件引用
		getViews();
		// 添加事件监听
		addEventListeners();
		// 展示图片信息
		showPicInfo();
		// 获取故事列表
		getStories();
	}

	private void getViews() {
		top_back = (Button) findViewById(R.id.top_back);

		tv_title = (TextView) findViewById(R.id.tv_title);
		// 标题文字设置
		tv_title.setText(R.string.storylist);

		details_prgrsBar = (ProgressBar) findViewById(R.id.details_prgrsBar);

		pic_to_storied = (ImageView) findViewById(R.id.pic_to_storied);
		pic_author = (TextView) findViewById(R.id.pic_author);
		pub_time = (TextView) findViewById(R.id.pub_time);

		svAdptr = new StoryVoteAdapter(this);
		svAdptr.setVoteListener(voteListener);
		story_votes = (StateListView) findViewById(R.id.story_votes);
		story_votes.setAdapter(svAdptr);

	}

	private VoteActionListener voteListener = new VoteActionListener() {
		@Override
		public void send(String storyId, String type) {
			// TODO, 只有在列表静止状态下，才处理投票提交
			if (story_votes.isStatic())
				updateProgress("This is: " + type);
		}
	};

	private void addEventListeners() {
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
			doRetrieve(tpId);
		} else {
			updateProgress("Warning, tpId is null!");
			Log.e(TAG, "Warning, tpId is null!");
		}

	}

	private void doRetrieve(String tpId) {
		Log.d(TAG, "Attempting retrieve gallery data...");

		if (mRetrieveTask != null
				&& mRetrieveTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		} else {
			mRetrieveTask = new RetrieveStoriesTask();
			mRetrieveTask.setListener(mRetrieveTaskListener);

			TaskParams params = new TaskParams();
			params.put("tpId", tpId);
			mRetrieveTask.execute(params);

			// Add Task to manager
			taskManager.addTask(mRetrieveTask);
		}
	}

	private TaskListener mRetrieveTaskListener = new TaskAdapter() {
		@Override
		public void onPreExecute(GenericTask task) {
			details_prgrsBar.setVisibility(View.VISIBLE);
		}

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.OK) {// 成功发送
				// do nothing currently...
			} else if (result == TaskResult.IO_ERROR) {
				updateProgress(getString(R.string.page_status_unable_to_update));
			} else if (result == TaskResult.JSON_PARSE_ERROR) {
				updateProgress("Response to json data parse Error!");
			}
			details_prgrsBar.setVisibility(View.GONE);
		}

		public void deliverRetrievedList(List<Object> results) {
			ArrayList<StoryInfo> stories = new ArrayList<StoryInfo>();
			for (Object o : results) {
				stories.add((StoryInfo) o);
			}
			// 更新视图列表
			svAdptr.refresh(stories);
		}

	};

	protected void onDestroy() {
		super.onDestroy();
		taskManager.cancelAll();
	}

	private void updateProgress(String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}

} // end of class

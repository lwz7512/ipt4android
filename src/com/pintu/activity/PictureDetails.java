package com.pintu.activity;

import java.text.ParseException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.pintu.PintuApp;
import com.pintu.R;
import com.pintu.activity.base.FullScreenActivity;
import com.pintu.api.PTApi;
import com.pintu.data.TPicDetails;
import com.pintu.data.TPicItem;
import com.pintu.task.GenericTask;
import com.pintu.task.RetrieveDetailTask;
import com.pintu.task.SendTask;
import com.pintu.task.SimpleTask;
import com.pintu.task.TaskAdapter;
import com.pintu.task.TaskListener;
import com.pintu.task.TaskManager;
import com.pintu.task.TaskParams;
import com.pintu.task.TaskResult;
import com.pintu.tool.SimpleImageLoader;
import com.pintu.util.DateTimeHelper;
import com.pintu.util.IptHelper;

public class PictureDetails extends FullScreenActivity {

	private static String TAG = "DetailPicture";

	// Header
	// 返回按钮
	private ImageButton top_back;
	// 顶部标题
	private TextView tv_title;
	// 加载进度条
	private ProgressBar details_prgrsBar;

	// Body
	private ViewGroup usercolumn;
	// 头像
	private ImageView profile_image;
	// 贴图作者
	private TextView user_name;
	// 用户资料
	private TextView user_info;
	// 发布时间
	private TextView created_at;

	// 品图图片
	private ImageView t_picture;
	// 标签，没有内容就隐藏
	private TextView tv_tags;
	// 描述，没有内容就隐藏
	private TextView tv_description;
	// 发布来源
	private TextView send_source;
	// 原创
	private TextView isOriginal;
	// 喜欢人数
	private TextView likeNum;
	// 浏览次数，点击查看列表
	private TextView browseCount;
	// 评论数，点击查看列表
	private Button commentnum;

	// Footer
	// 喜欢
	private TextView tv_like;
	// 评论
	private TextView tv_comment;
	// 收藏
	private TextView tv_favorite;
	// 转发到微博
	private TextView tv_forward;
	// 举报
	private TextView tv_report;

	// 获取详情任务
	private GenericTask mRetrieveTask;
	// 发送请求任务
	private GenericTask mSendTask;
	protected TaskManager taskManager = new TaskManager();

	// 保存一个详情对象吧，方便以后下载原始图
	private TPicDetails details = null;
	// 当前查看的图片
	private String tpId;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.picdetail);
		// 获得组件引用
		getViews();
		// 添加事件监听
		addEventListeners();
		// 获得画廊传递过来的贴图id
		shouldRetrieve();
		// 初始化收藏按钮，先从本地缓存中查询是否已经收藏
		// 如果已经收藏了，就置为不可用，如果没有置为可用
		initNavBtns();
	}

	private void initNavBtns() {
		boolean isExist = PintuApp.dbApi.hasAlreadyMarked(tpId);
		if (isExist) {
			tv_favorite.setEnabled(false);
			tv_favorite.setTextColor(0xFF999999);
			// 换个图标更形象
			tv_favorite.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.favorited, 0, 0);
		}
	}

	protected void onDestroy() {
		super.onDestroy();
		taskManager.cancelAll();
	}

	private void getViews() {
		top_back = (ImageButton) findViewById(R.id.top_back);
		tv_title = (TextView) findViewById(R.id.tv_title);
		// 设置标题文字
		tv_title.setText(R.string.picdetails);
		details_prgrsBar = (ProgressBar) findViewById(R.id.details_prgrsBar);

		usercolumn = (ViewGroup) findViewById(R.id.usercolumn);
		profile_image = (ImageView) findViewById(R.id.profile_image);
		user_name = (TextView) findViewById(R.id.user_name);
		user_info = (TextView) findViewById(R.id.user_info);
		created_at = (TextView) findViewById(R.id.created_at);

		t_picture = (ImageView) findViewById(R.id.t_picture);
		tv_tags = (TextView) findViewById(R.id.tv_tags);
		tv_description = (TextView) findViewById(R.id.tv_description);

		isOriginal = (TextView) findViewById(R.id.tv_isoriginal);
		likeNum = (TextView) findViewById(R.id.likenum);
		browseCount = (TextView) findViewById(R.id.browsenum);
		commentnum = (Button) findViewById(R.id.commentnum);
		send_source = (TextView) findViewById(R.id.send_source);

		tv_like = (TextView) findViewById(R.id.tv_like);
		tv_comment = (TextView) findViewById(R.id.tv_comment);
		tv_favorite = (TextView) findViewById(R.id.tv_favorite);
		tv_forward = (TextView) findViewById(R.id.tv_forward);
		tv_report = (TextView) findViewById(R.id.tv_report);
	}

	private void addEventListeners() {
		top_back.setOnClickListener(mGoListener);
		// 查看作者详情
		usercolumn.setOnClickListener(viewUserAction);

		// 查看评论列表
		commentnum.setOnClickListener(commentsAction);
		// 赞一下
		tv_like.setOnClickListener(toTasteActivity);
		// 添加评论
		tv_comment.setOnClickListener(toCommentsActivity);
		// 收藏，不跳转
		tv_favorite.setOnClickListener(toFavoriteActivity);

		// 转发，不跳转吧？
		tv_forward.setOnClickListener(toFowardActivity);
		// FIXME, 先不做转发，禁用掉吧
		tv_forward.setEnabled(false);
		tv_forward.setTextColor(0xFF999999);
		
		// 举报
		tv_report.setOnClickListener(toReportActivity);		

		// 点击图片弹出保存
		t_picture.setOnClickListener(popupSaveListener);
	}

	private OnClickListener mGoListener = new OnClickListener() {
		public void onClick(View v) {
			finish();
		}
	};

	// 投票喜欢
	private OnClickListener toTasteActivity = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (details != null) {
				likeIt();
			} else {
				updateProgress("picture is blank, waiting for it to add story...");
			}
		}
	};

	// 添加评论
	private OnClickListener toCommentsActivity = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent it = new Intent();			
			// 这里用故事当评论发了
			it.setClass(PictureDetails.this, StoryEdit.class);
			String tpicUrl = null;
			if (details != null) {
				tpicUrl = PintuApp.mApi.composeImgUrlById(details.mobImgId);
				it.putExtra("tpicUrl", tpicUrl);
				it.putExtra("author", details.author);
				it.putExtra("pubTime", details.relativeTime);
				it.putExtra("tpId", details.id);
				// 启动故事编辑
				startActivity(it);
			} else {
				updateProgress("picture is blank, waiting for it to add story...");
			}

		}
	};

	// 查看评论列表
	private OnClickListener commentsAction = new OnClickListener() {
		@Override
		public void onClick(View v) {
			String tpicUrl = null;
			Intent it = new Intent();
			// 仍然是将故事当做评论
			it.setClass(PictureDetails.this, StoryList.class);

			if (details != null && details.id != null) {
				tpicUrl = PintuApp.mApi.composeImgUrlById(details.mobImgId);
				it.putExtra("tpicUrl", tpicUrl);
				it.putExtra("author", details.author);
				it.putExtra("pubTime", details.relativeTime);
				it.putExtra("tpId", details.id);
				// 启动故事列表
				startActivity(it);
			} else {
				updateProgress("picture is blank, waiting for it to view stories...");
			}

		}
	};

	// 查看作者详情
	private OnClickListener viewUserAction = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (details != null) {
				String userId = details.owner;
				Intent it = new Intent();
				it.setClass(PictureDetails.this, TadiAssets.class);
				it.putExtra("userId", userId);
				// 打开作者详情模块
				startActivity(it);
			}
		}
	};

	// 收藏，不跳转吧？
	private OnClickListener toFavoriteActivity = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// 必须是取回详情才能收藏
			if (details != null) {
				doFavorite();
			}
		}
	};

	// 弹出保存
	private OnClickListener popupSaveListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			savePic();
		}
	};

	/**
	 * 投票：喜欢
	 */
	private void likeIt() {
		if (mSendTask != null
				&& mSendTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		}
		int mode = SendTask.TYPE_VOTE;
		mSendTask = new SendTask();
		mSendTask.setListener(voteTaskListener);

		TaskParams params = new TaskParams();
		params.put("mode", mode);
		// 后台依据receiver来发送投票消息给故事作者
		params.put("receiver", details.owner);
		params.put("follow", tpId);
		mSendTask.execute(params);

		taskManager.addTask(mSendTask);
	}

	private TaskListener voteTaskListener = new TaskAdapter() {
		@Override
		public void onPreExecute(GenericTask task) {
			details_prgrsBar.setVisibility(View.VISIBLE);
			// 禁用，并换图标
			tv_like.setEnabled(false);
			tv_like.setTextColor(0xFF999999);
			// 换个图标更形象
			tv_like.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.heart, 0, 0);
		}

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.OK) {
				details_prgrsBar.setVisibility(View.GONE);
			} else if (result == TaskResult.FAILED) {
				onSendFailure();
			} else if (result == TaskResult.IO_ERROR) {
				onSendFailure();
			}
		}

	};

	/**
	 * 本地用户收藏动作
	 */
	private void doFavorite() {

		if (mSendTask != null
				&& mSendTask.getStatus() == GenericTask.Status.RUNNING)
			return;
		// 收藏此图片
		int mode = SendTask.TYPE_MARK;
		mSendTask = new SendTask();
		mSendTask.setListener(markTaskListener);

		TaskParams params = new TaskParams();
		params.put("mode", mode);
		params.put("picId", tpId);
		// 取本地用户
		params.put("userId", PintuApp.getUser());
		if (tpId != null) {
			mSendTask.execute(params);
		} else {
			updateProgress("Target pic id is null!");
		}

		taskManager.addTask(mSendTask);
	}

	private TaskListener markTaskListener = new TaskAdapter() {
		@Override
		public void onPreExecute(GenericTask task) {
			onSendBegin();
		}

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.OK) {
				onSendSuccess();
			} else if (result == TaskResult.FAILED) {
				onSendFailure();
			} else if (result == TaskResult.IO_ERROR) {
				onSendFailure();
			}
		}

	};

	private void onSendBegin() {
		details_prgrsBar.setVisibility(View.VISIBLE);
		// 锁定收藏按钮
		tv_favorite.setEnabled(false);
		tv_favorite.setTextColor(0xFF999999);
		// 换个图标更形象
		tv_favorite.setCompoundDrawablesWithIntrinsicBounds(0,
				R.drawable.favorited, 0, 0);
		// 先把它存起来再说
		TPicItem pic = new TPicItem();
		pic.id = details.id;
		pic.mobImgId = details.mobImgId;
		pic.owner = details.owner;
		pic.publishTime = details.publishTime;
		PintuApp.dbApi.insertOneMarkedPic(pic);
	}

	private void onSendSuccess() {
		details_prgrsBar.setVisibility(View.GONE);
		updateProgress(getString(R.string.marksuccess));
	}

	private void onSendFailure() {
		details_prgrsBar.setVisibility(View.GONE);
		updateProgress(getString(R.string.page_status_unable_to_update));
	}

	// 转发，不跳转吧？
	private OnClickListener toFowardActivity = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO, Forward to user activity...
		}
	};

	// 举报
	private OnClickListener toReportActivity = new OnClickListener() {
		@Override
		public void onClick(View v) {
			reportIt();
		}
	};
	
	private void reportIt(){
		if (mSendTask != null
				&& mSendTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		}
		this.mSendTask = new SendTask();
		this.mSendTask.setListener(reportTaskListener);
		
		int mode = SendTask.TYPE_MESSAGE;
		String sender = PintuApp.getUser();
		String receiver = PintuApp.getKefu();
		String msgContent = "I think the picture: "+tpId+" is illegal, isn't it?";
		TaskParams params = new TaskParams();
		params.put("mode", mode);
		params.put("userId", sender);
		params.put("receiver", receiver);
		params.put("content", msgContent);
		
		this.mSendTask.execute(params);
		
		taskManager.addTask(mSendTask);
	}
	
	private TaskListener reportTaskListener = new TaskAdapter() {
		@Override
		public void onPreExecute(GenericTask task) {
			details_prgrsBar.setVisibility(View.VISIBLE);
			tv_report.setEnabled(false);
		}

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.OK) {
				details_prgrsBar.setVisibility(View.GONE);
				updateProgress(getText(R.string.report_thanks).toString());
			} else if (result == TaskResult.FAILED) {
				onSendFailure();
			} else if (result == TaskResult.IO_ERROR) {
				onSendFailure();
			}
		}

	};

	private void shouldRetrieve() {
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		// Must has extras
		if (null == extras) {
			Log.e(TAG, this.getClass().getName() + " must has extras.");
			finish();
			return;
		}
		// 保存下来做按钮状态处理
		tpId = extras.getString("tpId");
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
			mRetrieveTask = new RetrieveDetailTask();
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
			if (result == TaskResult.OK) {
				// 成功发送
				details_prgrsBar.setVisibility(View.GONE);
			} else if (result == TaskResult.IO_ERROR) {
				updateProgress(getString(R.string.page_status_unable_to_update));
			} else if (result == TaskResult.JSON_PARSE_ERROR) {
				updateProgress("Response to json data parse Error!");
			}
		}

		// Update UI elements with TPicDetails
		public void deliveryResponseJson(JSONObject json) {
			try {
				// 保存下来，跳转时要用来传参
				details = TPicDetails.parseJsonToObj(json);
				if (details != null) {
					updateUIwithPicDetails(details);
				} else {
					updateProgress("details is null can not update UI!");
				}
			} catch (JSONException e) {
				updateProgress("Parse json to details Error!");
				e.printStackTrace();
			}
		}
	};

	/**
	 * @deprecated not use anymore...
	 */
	private void fetchLikeNumOfPic() {

		mRetrieveTask = new SimpleTask();
		mRetrieveTask.setListener(likeTaskListener);

		TaskParams params = new TaskParams();
		params.put("method", PTApi.GETPICCOOLCOUNT);
		params.put("name", "pId");
		params.put("value", tpId);
		mRetrieveTask.execute(params);

		// Add Task to manager
		taskManager.addTask(mRetrieveTask);
	}

	/**
	 * @deprecated not use anymore...
	 */
	private TaskListener likeTaskListener = new TaskAdapter() {
		@Override
		public void deliverResponseString(String response) {
			String suffix = getText(R.string.peoplelike).toString();
			// 没人投票
			if (response.equals("0")) {
				response = getText(R.string.none).toString();
				likeNum.setText(response + suffix);
			} else {
				likeNum.setText(response + " " + suffix);
			}
		}
	};

	private void updateUIwithPicDetails(TPicDetails details) {
		// 显示头像
		String profileUrl = PintuApp.mApi
				.composeImgUrlByPath(details.avatarImgPath);
		SimpleImageLoader.display(profile_image, profileUrl);
		// 手机尺寸图片
		String tpicUrl = PintuApp.mApi.composeImgUrlById(details.mobImgId);

		// 显示品图手机图片
		SimpleImageLoader.displayForFit(t_picture, tpicUrl);

		details.author = IptHelper.getShortUserName(details.author);
		user_name.setText(details.author);
		String userInfo = getText(R.string.level_zh) + "  " + details.level;
		// 等级和积分之间空4个格
		userInfo += "    ";
		userInfo += getText(R.string.score_zh) + "  " + details.score;
		user_info.setText(userInfo);
		// 格式化化为XXX以前，而不是显示绝对时间
		try {
			details.relativeTime = DateTimeHelper
					.getRelativeTimeByFormatDate(details.publishTime);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		// 显示格式化后的相对时间
		created_at.setText(details.relativeTime);

		if (details.tags.equals("")) {
			tv_tags.setVisibility(View.GONE);
		} else {
			tv_tags.setText(details.tags);
		}

		tv_description.setText(details.description);
		String sourcePrefix = getText(R.string.picfrom).toString();
		send_source.setText(sourcePrefix + "  " + "Android");

		if (details.commentNum != null) {
			String comment = getText(R.string.comment).toString();
			commentnum.setText(details.commentNum + " " + comment);
		}
		if (details.browseCount != null) {
			int browseNum = Integer.valueOf(details.browseCount);
			// 每次浏览客户端加1
			browseNum++;
			String prefix = getText(R.string.browsenum).toString();
			browseCount.setText(browseNum + " " + prefix);
		}

		if (details.isOriginal == 1) {
			isOriginal.setTextColor(0xFF0c8918);
			isOriginal.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
			isOriginal.setText(R.string.original);
		} else {
			isOriginal.setText(R.string.notoriginal);
		}

		String suffix = getText(R.string.peoplelike).toString();
		// 没人投票
		if (details.coolCount.equals("0")) {
			String none = getText(R.string.none).toString();
			likeNum.setText(none + suffix);
		} else {
			likeNum.setText(details.coolCount + " " + suffix);
		}

	}

	private void updateProgress(String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuItem item;
		item = menu.add(0, 1, 0, R.string.save);
		item.setIcon(android.R.drawable.ic_menu_save);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			savePic();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void savePic() {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				PictureDetails.this);
		builder.setTitle(getText(R.string.save));
		builder.setMessage(getText(R.string.suretosave));
		builder.setPositiveButton(getText(R.string.yes), okListener);
		builder.setNegativeButton(getText(R.string.cancel), cancelListener);

		Dialog dialog = builder.create();
		dialog.show();
	}

	private final DialogInterface.OnClickListener okListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			saveFileToSDCard();
		}
	};

	private void saveFileToSDCard() {

		if (mRetrieveTask != null
				&& mRetrieveTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		} else {
			mRetrieveTask = new SimpleTask() {
				String downloadFile = null;

				@Override
				protected void onPreExecute() {
					details_prgrsBar.setVisibility(View.VISIBLE);
				}

				@Override
				protected TaskResult doInBackground(TaskParams... params) {
					String picId = details.rawImgId;
					String picType = (details.name == null) ? details.name
							.split(".")[1] : "jpg";
					picType = "." + picType;
					downloadFile = SimpleImageLoader.downloadImage(picId,
							picType);
					return TaskResult.OK;
				}

				@Override
				protected void onPostExecute(TaskResult result) {
					updateProgress("saved in: " + downloadFile);
					details_prgrsBar.setVisibility(View.GONE);
				}

			};
			mRetrieveTask.execute(new TaskParams());

			// Add Task to manager
			taskManager.addTask(mRetrieveTask);
		}

	}

	private final DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
		}
	};

} // end of activity

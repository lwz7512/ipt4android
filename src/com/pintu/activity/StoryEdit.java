package com.pintu.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.pintu.R;
import com.pintu.activity.base.FullScreenActivity;
import com.pintu.task.GenericTask;
import com.pintu.task.SendTask;
import com.pintu.task.TaskAdapter;
import com.pintu.task.TaskListener;
import com.pintu.task.TaskParams;
import com.pintu.task.TaskResult;
import com.pintu.tool.SimpleImageLoader;

public class StoryEdit extends FullScreenActivity {

	private static String TAG = "StoryEdit";
	private static int STORYLENGTH = 140;

	// Header
	private ImageButton top_back;
	private Button top_send_btn;
	private ProgressBar sending_prgrsBar;

	// Pic info
	private ImageView pic_to_storied;
	private TextView pic_author;
	private TextView pub_time;

	// Content
	private EditText story_edit;
	private TextView storytxt_left_hint;
	private int originTextColor;
	private LinearLayout clear_story_btn;

	// Task
	private GenericTask mSendTask;

	// 发送故事的目标图编号
	private String tpId;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.poststory);

		// 获得组件引用
		getViews();
		// 添加事件监听
		addEventListeners();
		// 初始化图片信息，作为故事参考
		showPicInfo();
	}

	private void getViews() {
		top_back = (ImageButton) findViewById(R.id.top_back);
		top_send_btn = (Button) findViewById(R.id.top_send_btn);
		sending_prgrsBar = (ProgressBar) findViewById(R.id.sending_prgrsBar);
		
		pic_to_storied = (ImageView) findViewById(R.id.pic_to_storied);
		pic_author = (TextView) findViewById(R.id.pic_author);
		pub_time = (TextView) findViewById(R.id.pub_time);

		story_edit = (EditText) findViewById(R.id.story_edit);
		storytxt_left_hint = (TextView) findViewById(R.id.storytxt_left_hint);
		// 保存初始文字颜色
		originTextColor = storytxt_left_hint.getTextColors().getDefaultColor();
		clear_story_btn = (LinearLayout) findViewById(R.id.clear_story_btn);
	}

	private void addEventListeners() {
		top_back.setOnClickListener(mGoListener);
		top_send_btn.setOnClickListener(sendListener);
		// 更新剩余文字提示
		story_edit.addTextChangedListener(mTextWatcher);
		//添加键盘完成按键动作监听
		story_edit.setOnEditorActionListener(editorActionListener);
		clear_story_btn.setOnClickListener(popupCleanListener);
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
		// 保存一个原图ID，作为发送故事的重要参数
		tpId = extras.getString("tpId");

		SimpleImageLoader.display(pic_to_storied, tpicUrl);
		pic_author.setText(author);
		pub_time.setText(pubTime);
	}

	private TextWatcher mTextWatcher = new TextWatcher() {
		@Override
		public void afterTextChanged(Editable e) {
			updateCharsRemain();
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}
	};
	
	private OnEditorActionListener editorActionListener = new OnEditorActionListener(){
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			//如果按下完成键，就执行发送动作
			if(actionId == EditorInfo.IME_ACTION_DONE){
				doSend();
			}
			return false;
		}		
	};

	public void updateCharsRemain() {
		int remaining = STORYLENGTH - story_edit.length();
		if (remaining < 0) {
			storytxt_left_hint.setTextColor(Color.RED);
		} else {
			storytxt_left_hint.setTextColor(originTextColor);
		}
		storytxt_left_hint.setText(remaining + "");
	}

	private OnClickListener mGoListener = new OnClickListener() {
		public void onClick(View v) {
			finish();
		}
	};

	private OnClickListener sendListener = new OnClickListener() {
		public void onClick(View v) {
			doSend();
		}
	};

	private OnClickListener popupCleanListener = new OnClickListener() {
		public void onClick(View v) {
			int storyLength = story_edit.getText().toString().length();
			if (storyLength > 0) {
				clearContentWarning();
			}
		}
	};

	private void clearContentWarning() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("提示");
		builder.setMessage("清除全部文字吗?");
		builder.setPositiveButton("确定", okListener);
		builder.setNegativeButton("取消", cancelListener);

		Dialog dialog = builder.create();
		dialog.show();

	}

	private final DialogInterface.OnClickListener okListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			story_edit.getText().clear();
		}
	};

	private final DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
		}
	};

	private void doSend() {
		Log.d(TAG, "dosend story... ");

		if (mSendTask != null
				&& mSendTask.getStatus() == GenericTask.Status.RUNNING)
			return;

		String storyContent = story_edit.getText().toString();
		if (storyContent.length() == 0) {
			updateProgress("Write content first!");
			return;
		} 

		int mode = SendTask.TYPE_STORY;
		mSendTask = new SendTask();
		mSendTask.setListener(mSendTaskListener);

		TaskParams params = new TaskParams();
		params.put("mode", mode);
		params.put("story", storyContent);
		params.put("follow", tpId);
		if (tpId != null) {
			mSendTask.execute(params);
		} else {
			updateProgress("Target pic id is null!");
		}

	}

	private TaskListener mSendTaskListener = new TaskAdapter() {
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
		sending_prgrsBar.setVisibility(View.VISIBLE);
		top_send_btn.setVisibility(View.GONE);
	}

	private void onSendSuccess() {
		sending_prgrsBar.setVisibility(View.GONE);
		top_send_btn.setVisibility(View.VISIBLE);
		//关闭当前窗口
		finish();
	}

	private void onSendFailure() {
		updateProgress(getString(R.string.page_status_unable_to_update));
	}

	protected void onDestroy() {
		super.onDestroy();
		if (mSendTask != null
				&& mSendTask.getStatus() == GenericTask.Status.RUNNING) {
			// Doesn't really cancel execution (we let it continue running).
			// See the SendTask code for more details.
			mSendTask.cancel(true);
		}
	}

	private void updateProgress(String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}

} // end of class

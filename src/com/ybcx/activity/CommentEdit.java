package com.ybcx.activity;

import java.util.List;

import org.json.JSONObject;

import android.content.Intent;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.ybcx.R;
import com.ybcx.activity.base.TempletActivity;
import com.ybcx.task.SendTask;
import com.ybcx.task.TaskParams;
import com.ybcx.tool.SimpleImageLoader;

public class CommentEdit extends TempletActivity {

	private static String TAG = "CommentEdit";
	
	// Header
	private ImageButton top_back;
	private Button top_send_btn;
	private ProgressBar sending_prgrsBar;

	// Pic info
	private ImageView pic_to_storied;
	private TextView pic_author;
	private TextView pub_time;

	// Content
	private EditText comment_edit;
	private static int COMMENTLENGTH = 32;

	// 发送评论的目标图编号
	private String tpId;
	
	@Override
	protected int getLayout() {		
		return R.layout.postcomment;
	}

	@Override
	protected void getViews() {
		top_back = (ImageButton) findViewById(R.id.top_back);
		top_send_btn = (Button) findViewById(R.id.top_send_btn);
		sending_prgrsBar = (ProgressBar) findViewById(R.id.sending_prgrsBar);

		pic_to_storied = (ImageView) findViewById(R.id.pic_to_storied);
		pic_author = (TextView) findViewById(R.id.pic_author);
		pub_time = (TextView) findViewById(R.id.pub_time);

		comment_edit = (EditText) findViewById(R.id.comment_edit);
		
	}

	@Override
	protected void addEventListeners() {
		top_back.setOnClickListener(mGoListener);
		top_send_btn.setOnClickListener(sendListener);
		//添加键盘完成按键动作监听
		comment_edit.setOnEditorActionListener(editorActionListener);		
		//做输入长度提示
		comment_edit.addTextChangedListener(mTextWatcher);
	}

	//视图创建完成后要调用该方法	
	@Override
	protected void justDoIt() {
		showPicInfo();		
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
		// 保存一个原图ID，作为发送评论的重要参数
		tpId = extras.getString("tpId");

		SimpleImageLoader.display(pic_to_storied, tpicUrl);
		pic_author.setText(author);
		pub_time.setText(pubTime);
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
	
	private TextWatcher mTextWatcher = new TextWatcher() {
		@Override
		public void afterTextChanged(Editable e) {
			noteUserForLength(e);
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
	
	private void noteUserForLength(Editable e){
		int currentLength = e.length();
		if(currentLength==COMMENTLENGTH){
			updateProgress(R.string.comment_length_note);
		}
	}


	@Override
	protected void doSend() {
		//先检查任务是否在执行，是则退出
		if(!checkTaskStatus()) return;
		
		String comment = comment_edit.getText().toString();
		if (comment.length() > 0) {			

			int mode = SendTask.TYPE_COMMENT;
			mSendTask = new SendTask();
			mSendTask.setListener(mSendTaskListener);

			TaskParams params = new TaskParams();
			params.put("mode", mode);
			params.put("content", comment);
			params.put("follow", tpId);
			if (tpId != null) {
				mSendTask.execute(params);
			} else {
				updateProgress("Target pic id is null!");
			}

		} else {
			updateProgress("Write content first!");
		}

		//将新建任务添加到管理器中，方便销毁
		this.manageTask(mSendTask);
		
	}

	@Override
	protected void onSendBegin() {
		sending_prgrsBar.setVisibility(View.VISIBLE);
		top_send_btn.setVisibility(View.GONE);
	}

	@Override
	protected void onSendSuccess() {
		sending_prgrsBar.setVisibility(View.GONE);
		top_send_btn.setVisibility(View.VISIBLE);
		//关闭当前窗口
		finish();
	}

	@Override
	protected void onSendFailure() {
		updateProgress(getString(R.string.page_status_unable_to_update));		
	}
	

//	------  下面的方法这里用不着，但是别删了，因为他们是父类抽象方法的实现 ------
	
	@Override
	protected void doRetrieve() {
		//do nothing, but leave it stay here don't kill me...		
	}

	@Override
	protected void onRetrieveBegin() {
		//do nothing, but leave it stay here don't kill me...		
	}

	@Override
	protected void onRetrieveSuccess() {
		//do nothing, but leave it stay here don't kill me...
	}

	@Override
	protected void onRetrieveFailure() {		
		//do nothing, but leave it stay here don't kill me...		
	}

	@Override
	protected void onParseJSONResultFailue() {
		//do nothing, but leave it stay here don't kill me...		
	}

	@Override
	protected void refreshListView(List<Object> results) {
		//do nothing, but leave it stay here don't kill me...		
	}

	@Override
	protected void refreshMultView(JSONObject json) {
		//do nothing, but leave it stay here don't kill me...		
	}


	
} //end of class

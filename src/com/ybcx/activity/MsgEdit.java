package com.ybcx.activity;

import java.util.List;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.ybcx.R;
import com.ybcx.PintuApp;
import com.ybcx.activity.base.TempletActivity;
import com.ybcx.task.SendTask;
import com.ybcx.task.TaskParams;

public class MsgEdit extends TempletActivity {
	private static String TAG = "MsgEdit";
	
	private static int STORYLENGTH = 140;
	
	// Header
	private ImageButton top_back;
	private Button top_send_btn;
	private ProgressBar sending_prgrsBar;

	//接收人
	private EditText receiver_edit;
	// Content
	private EditText msg_content_edit;
	private TextView storytxt_left_hint;
	private int originTextColor;
	private LinearLayout clear_story_btn;

	
	@Override
	protected int getLayout() {
		return R.layout.postmsg;
	}

	@Override
	protected void getViews() {
		top_back = (ImageButton) findViewById(R.id.top_back);
		top_send_btn = (Button) findViewById(R.id.top_send_btn);
		sending_prgrsBar = (ProgressBar) findViewById(R.id.sending_prgrsBar);

		msg_content_edit = (EditText) findViewById(R.id.msg_content_edit);
		//自动聚焦
		msg_content_edit.requestFocus();
		
		storytxt_left_hint = (TextView) findViewById(R.id.storytxt_left_hint);
		// 保存初始文字颜色
		originTextColor = storytxt_left_hint.getTextColors().getDefaultColor();
		clear_story_btn = (LinearLayout) findViewById(R.id.clear_story_btn);

		//如果时回复的消息，这里要显示原文和原作者
		Intent it = getIntent();
		Bundle extras = it.getExtras();
		if(extras==null) return;
		//显示回复原文
		String sender = extras.getString("sender");
		String content = extras.getString("content");
		String replyTxt = "\n\n"+"------------------------------\n";
		replyTxt = replyTxt+sender+":"+content;
		msg_content_edit.setText(replyTxt);
	}

	@Override
	protected void addEventListeners() {
		top_back.setOnClickListener(mGoListener);
		top_send_btn.setOnClickListener(sendListener);
		// 更新剩余文字提示
		msg_content_edit.addTextChangedListener(mTextWatcher);
		//添加键盘完成按键动作监听
		msg_content_edit.setOnEditorActionListener(editorActionListener);
		clear_story_btn.setOnClickListener(popupCleanListener);
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
		int remaining = STORYLENGTH - msg_content_edit.length();
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
			int storyLength = msg_content_edit.getText().toString().length();
			if (storyLength > 0) {
				clearContentWarning();
			}
		}
	};

	private void clearContentWarning() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getText(R.string.note));
		builder.setMessage(R.string.clear_all);
		builder.setPositiveButton(getText(R.string.yes), okListener);
		builder.setNegativeButton(getText(R.string.cancel), cancelListener);

		Dialog dialog = builder.create();
		dialog.show();

	}

	private final DialogInterface.OnClickListener okListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			msg_content_edit.getText().clear();
		}
	};

	private final DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
		}
	};

	
	@Override
	protected void justDoIt() {
		//do nothing...
	}

	@Override
	protected void doSend() {
		
		String msgContent = msg_content_edit.getText().toString();
		if(msgContent.length()==0){
			this.updateProgress("Write Content fisrt!");
			return;
		}
		
		if(!checkTaskStatus()) return;
		
		this.mSendTask = new SendTask();
		this.mSendTask.setListener(mSendTaskListener);
		
		int mode = SendTask.TYPE_MESSAGE;
		String sender = PintuApp.getUser();
		String receiver = PintuApp.getKefu();
		TaskParams params = new TaskParams();
		params.put("mode", mode);
		params.put("userId", sender);
		params.put("receiver", receiver);
		params.put("content", msgContent);
		
		this.mSendTask.execute(params);
		
		this.manageTask(mSendTask);
	}

	@Override
	protected void onSendBegin() {
		sending_prgrsBar.setVisibility(View.VISIBLE);
		top_send_btn.setVisibility(View.GONE);
		
		// 关闭软键盘
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(msg_content_edit.getWindowToken(),0);
	}

	@Override
	protected void onSendSuccess() {
		sending_prgrsBar.setVisibility(View.GONE);
		top_send_btn.setVisibility(View.VISIBLE);
		
		updateProgress(R.string.msg_send_success);
		
		//关闭当前窗口
		finish();
	}

	@Override
	protected void onSendFailure() {
		updateProgress(getString(R.string.page_status_unable_to_update));
		sending_prgrsBar.setVisibility(View.GONE);
		top_send_btn.setVisibility(View.VISIBLE);
	}

	@Override
	protected void doRetrieve() {
		// do nothing ...
	}

	@Override
	protected void onRetrieveBegin() {
		// do nothing here ...
	}

	@Override
	protected void onRetrieveSuccess() {
		// do nothing here ...
	}

	@Override
	protected void onRetrieveFailure() {
		// do nothing here ...
	}

	@Override
	protected void onParseJSONResultFailue() {
		// do nothing here ...
	}

	@Override
	protected void refreshListView(List<Object> results) {
		// do nothing here ...
	}

	@Override
	protected void refreshMultView(JSONObject json) {
		// do nothing here ...
	}

}

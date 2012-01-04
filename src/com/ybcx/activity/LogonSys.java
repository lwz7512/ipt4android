package com.ybcx.activity;

import java.util.List;

import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.ybcx.R;
import com.ybcx.PintuApp;
import com.ybcx.activity.base.TempletActivity;
import com.ybcx.task.SendTask;
import com.ybcx.task.TaskParams;

public class LogonSys extends TempletActivity {

	private static final String USERNOTEXIST = "-1";
	private static final String PASSWORDERROR = "0";
		
	private ProgressDialog dialog;
	
	private EditText account_edit;
	private EditText password_edit;
	private TextView hint_text;
	private Button signin_button;
	
	@Override
	protected int getLayout() {
		return R.layout.login;
	}

	@Override
	protected void getViews() {
		account_edit = (EditText) findViewById(R.id.account_edit);
		password_edit = (EditText) findViewById(R.id.password_edit);
		hint_text = (TextView) findViewById(R.id.hint_text);
		signin_button = (Button) findViewById(R.id.signin_button);
		
	}

	@Override
	protected void addEventListeners() {
		signin_button.setOnClickListener(signinListener);
		//当密码获得焦点时，校验邮箱账号
		password_edit.setOnFocusChangeListener(pwdInputListener);
		//密码输入结束时
		password_edit.setOnEditorActionListener(editorActionListener);
	}
	
	private OnClickListener signinListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
			doLogin();			
		}		
	};
	
	private OnFocusChangeListener pwdInputListener = new OnFocusChangeListener(){
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if(hasFocus){//捕获焦点时，判断是否为邮箱
				String account = account_edit.getText().toString();
				boolean actBlank =  TextUtils.isEmpty(account);
				if(actBlank){
					//移除聚焦
					password_edit.clearFocus();
					//返回重新输入
					account_edit.requestFocus();
				}else{
					checkEmail(account);
				}				
			}			
		}		
	};
	
	private void checkEmail(String account){
		//FIXME, 修改邮箱验证规则，以支持qq邮箱
		//2012/01/04
		String emailFormat = "[a-z0-9]\\w{2,15}[@][a-z0-9]{2,}[.]\\p{Lower}{2,}"; 
		//这是最初的
//		String emailFormat = "\\p{Alpha}\\w{2,15}[@][a-z0-9]{3,}[.]\\p{Lower}{2,}"; 
		//输入时可能默认给加了个空格，去除下后面的空格
		account = account.trim();
		if(!account.matches(emailFormat)){
			//移除聚焦
			password_edit.clearFocus();
			//返回重新输入
			account_edit.requestFocus();
			account_edit.selectAll();		
			//提示邮箱账号非法
			hint_text.setText(R.string.login_not_email_format);
		}else{
			hint_text.setText("");
		}
	}
	
	
	private OnEditorActionListener editorActionListener = new OnEditorActionListener(){
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			//如果按下完成键，就执行发送动作
			if(actionId == EditorInfo.IME_ACTION_DONE){
				doLogin();
			}
			return false;
		}		
	};

	private void doLogin(){
		String account = account_edit.getText().toString();
		String pswd = password_edit.getText().toString();
		
		boolean checkResult = true;
		
		boolean actBlank =  TextUtils.isEmpty(account);
		boolean pwdBlank = TextUtils.isEmpty(pswd);
		//前面密码焦点获得时已经校验过格式了
		if(actBlank || pwdBlank){			
			checkResult = false;
			hint_text.setText(R.string.login_status_null_username_or_password);
		}		
		
		if(checkResult){
			hint_text.setText("");
			doSend();
		}
	}


	@Override
	protected void doSend() {
		if(!checkTaskStatus()) return;
		
		this.mSendTask = new SendTask();
		this.mSendTask.setListener(mSendTaskListener);
		TaskParams params = new TaskParams();
		int mode = SendTask.TYPE_NORMAL;
		String account = account_edit.getText().toString();
		String pswd = password_edit.getText().toString();
		params.put("account", account);
		params.put("password", pswd);
		params.put("mode", mode);
		mSendTask.execute(params);

		this.manageTask(mSendTask);
	}

	@Override
	protected void onSendBegin() {
		//打开对话框
		dialog = ProgressDialog.show(this, "",
				getString(R.string.login_status_logging_in), true);
		if (dialog != null) {
			dialog.setCancelable(true);
		}
		
		 // 关闭软键盘
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(password_edit.getWindowToken(),0);
	}

	@Override
	protected void onSendSuccess() {		
		if (dialog != null) {
			dialog.dismiss();
		}
	}

	@Override
	protected void onSendFailure() {
		if (dialog != null) {
			dialog.dismiss();
		}
		this.updateProgress(R.string.page_status_unable_to_update);
	}
	
	@Override
	protected void responseForSend(String response){
		if(response==null){
			this.updateProgress("Service inavailable!");
			return;
		}
		if(response.equals("")){
			this.updateProgress("Logon Failed!");
			return;			
		}
		
		//除掉换行符号		
		response = response.trim();
		
		if(USERNOTEXIST.equals(response)){//用户不存在
			account_edit.setText("");
			password_edit.setText("");
			this.updateProgress(R.string.login_status_invalid_username);
		}else if(PASSWORDERROR.equals(response)){//密码错误
			password_edit.setText("");
			this.updateProgress(R.string.login_status_invalid__password);
		}else{//登录成功，拿到：角色@用户ID
			String userId;
			if(response.indexOf("@")==-1){
				this.updateProgress("Invalid user result!");
				return;
			}else{
				userId = response.split("@")[1];
			}
			//记录用户ID
			PintuApp.rememberUser(userId);
			//进入画廊
			Intent homePage = new Intent();
			homePage.setClass(this, HomeGallery.class);
			startActivity(homePage);
			//关闭当前活动
			finish();
		}
		
	}

	@Override
	protected void doRetrieve() {
		// do nothing ...
	}

	@Override
	protected void onRetrieveBegin() {
		// do nothing ...
	}

	@Override
	protected void onRetrieveSuccess() {
		// do nothing ...
	}

	@Override
	protected void onRetrieveFailure() {
		// do nothing ...
	}

	@Override
	protected void onParseJSONResultFailue() {
		// do nothing ...
	}

	@Override
	protected void refreshListView(List<Object> results) {
		// do nothing ...
	}

	@Override
	protected void refreshMultView(JSONObject json) {
		// do nothing ...
	}
	
	@Override
	protected void justDoIt() {
		// do nothing ...
	}


}

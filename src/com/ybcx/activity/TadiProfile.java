package com.ybcx.activity;

import java.util.List;

import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ybcx.R;
import com.ybcx.PintuApp;
import com.ybcx.activity.base.HeadSwitchActivity;
import com.ybcx.activity.base.SubMainCallBack;
import com.ybcx.activity.base.TempletActivity;
import com.ybcx.data.UserInfo;
import com.ybcx.task.RetrieveUserInfoTask;
import com.ybcx.task.TaskParams;
import com.ybcx.tool.SimpleImageLoader;
import com.ybcx.util.IptHelper;

/**
 * 数据处理逻辑： 临时存放数据到父活动，只允许查询一次，除非退出父活动
 * 
 * @author lwz
 * 
 */
public class TadiProfile extends TempletActivity implements SubMainCallBack {
	private static final String TAG = "TadiProfile";
	
	// 存放数据的KEY
	private static final String TADIPROFILE_DATA = "tadiprofile";

	// Common User info
	private ImageView profile_image;
	private TextView user_name;
	private TextView user_level;
	private TextView user_score;
	private TextView user_remainscore;
	private TextView user_picnum;
	private TextView user_storynum;

	private ProgressBar pb;
	
	//主活动传递过来的被查看用户
	private String currentUser;


	@Override
	protected int getLayout() {
		return R.layout.userdetails;
	}

	@Override
	protected void getViews() {
		profile_image = (ImageView) findViewById(R.id.profile_image);
		user_name = (TextView) findViewById(R.id.user_name);
		user_level = (TextView) findViewById(R.id.user_level);
		user_score = (TextView) findViewById(R.id.user_score);
		user_remainscore = (TextView) findViewById(R.id.user_remainscore);
		user_picnum = (TextView) findViewById(R.id.user_picnum);
		user_storynum = (TextView) findViewById(R.id.user_storynum);

	}

	@Override
	protected void addEventListeners() {
		// do nothing...
	}

	/**
	 * 不能在此方法中远程查询
	 * 因为进度条还没准备好
	 */
	@Override
	protected void justDoIt() {
		if (this.getObj(TADIPROFILE_DATA) != null) {
			//从父活动的缓存中获取
			UserInfo usr = (UserInfo) this.getObj(TADIPROFILE_DATA);
			updateUIWithUserInfo(usr);
		}
	}
	
	private void updateUIWithUserInfo(UserInfo usr) {
		String profileUrl = PintuApp.mApi.composeImgUrlByPath(usr.avatar);
		// 显示头像
		SimpleImageLoader.display(profile_image, profileUrl);

		String userName = IptHelper.getShortUserName(usr.account);
		user_name.setText(userName);
		
		String userLevel = getText(R.string.userlevel)+"  "+usr.level;
		user_level.setText(userLevel);
		
		String userScore = getText(R.string.totalscore)+"  "+usr.score;
		user_score.setText(userScore);
		
		String userExchange = getText(R.string.excgscore)+"  "+usr.exchangeScore;
		user_remainscore.setText(userExchange);

		String userPicNum = getText(R.string.picnum)+"  "+usr.tpicNum;
		user_picnum.setText(userPicNum);
		String storyNum = getText(R.string.storynum)+"  "+usr.storyNum;
		user_storynum.setText(storyNum);

	}


	
	private void shouldRetrieve(){
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		// Must has extras
		if (null == extras) {
			Log.e(TAG, this.getClass().getName() + " must has extras.");
			return;
		}
		//保存得到的用户
		currentUser = extras.getString("userId");
		if(currentUser!=null){
			doRetrieve();
		}else{
			updateProgress("Warning, currentUser is null!");
			Log.e(TAG, "Warning, currentUser is null!");
		}
		
	}


	@Override
	protected void doSend() {
		// do nothing here...
	}

	@Override
	protected void onSendBegin() {
		// do nothing here...
	}

	@Override
	protected void onSendSuccess() {
		// do nothing here...
	}

	@Override
	protected void onSendFailure() {
		// do nothing here...
	}

	/**
	 * 注意：
	 * 这里用主活动传进来的用户去查询
	 */
	@Override
	protected void doRetrieve() {
		if(!checkTaskStatus()) return;

		this.mRetrieveTask = new RetrieveUserInfoTask();
		this.mRetrieveTask.setListener(mRetrieveTaskListener);
		TaskParams params = new TaskParams();
		params.put("userId", this.currentUser);
		this.mRetrieveTask.execute(params);

		this.manageTask(mRetrieveTask);
	}

	@Override
	protected void onRetrieveBegin() {
		if(this.pb!=null){
			this.pb.setVisibility(View.VISIBLE);			
		}else{
			this.updateProgress("Loading My favorite pics...");
		}
		
	}

	@Override
	protected void onRetrieveSuccess() {
		if(this.pb!=null)
			this.pb.setVisibility(View.GONE);
		
		this.rememberLastVisit();
	}

	@Override
	protected void onRetrieveFailure() {
		if(this.pb!=null)
			this.pb.setVisibility(View.GONE);
		
		this.updateProgress(R.string.page_status_unable_to_update);
	}

	@Override
	protected void onParseJSONResultFailue() {
		if(this.pb!=null)
			this.pb.setVisibility(View.GONE);
		
		this.updateProgress(R.string.json_parse_error);
	}

	@Override
	protected void refreshListView(List<Object> results) {
		//do nothing here ...
	}

	@Override
	protected void refreshMultView(JSONObject json) {
		UserInfo usr = UserInfo.parseJsonToObj(json);
		//更新UI
		updateUIWithUserInfo(usr);
		//缓存起来
		this.putObj(TADIPROFILE_DATA, usr);
	}

	@Override
	public void addProgress(ProgressBar pb) {
		this.pb = pb;
		
		if (this.getObj(TADIPROFILE_DATA) == null) {
			shouldRetrieve();
		}
	}

	@Override
	public void refresh(ImageButton refreshBtn) {
		//do nothing here ... 
	}

	@Override
	public void putObj(String key, Object value) {
		if (this.getParent() != null) {
			HeadSwitchActivity frame = (HeadSwitchActivity) this.getParent();
			frame.cacheRepo(key, value);
		}
	}

	@Override
	public Object getObj(String key) {
		if (this.getParent() != null) {
			HeadSwitchActivity frame = (HeadSwitchActivity) this.getParent();
			return frame.getRepo(key);
		}
		return null;
	}

}

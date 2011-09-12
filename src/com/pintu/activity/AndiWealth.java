package com.pintu.activity;

import java.util.List;

import org.json.JSONObject;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pintu.PintuApp;
import com.pintu.R;
import com.pintu.activity.base.HeadSwitchActivity;
import com.pintu.activity.base.SubMainCallBack;
import com.pintu.activity.base.TempletActivity;
import com.pintu.data.UserDetails;
import com.pintu.task.RetrieveMyWealthTask;
import com.pintu.task.TaskParams;
import com.pintu.tool.SimpleImageLoader;
import com.pintu.util.IptHelper;

/**
 * 数据处理逻辑： 临时存放数据到父活动，只允许查询一次，除非退出父活动
 * 
 * @author lwz
 * 
 */
public class AndiWealth extends TempletActivity implements SubMainCallBack {
	// 存放数据的KEY
	private static final String ANDIWEALTH_DATA = "andiwealth";

	// Common User info
	private ImageView profile_image;
	private TextView user_name;
	private TextView user_level;
	private TextView user_score;
	private TextView user_remainscore;
	private TextView user_picnum;
	private TextView user_storynum;

	// My Wealth
	private TextView seashell_num;
	private TextView coppershell_num;
	private TextView silvershell_num;
	private TextView goldshell_num;

	// Main Activity Query UI
	private ImageButton refreshBtn;
	private ProgressBar pb;

	@Override
	protected int getLayout() {
		return R.layout.adwealth;
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

		seashell_num = (TextView) findViewById(R.id.seashell_num);
		coppershell_num = (TextView) findViewById(R.id.coppershell_num);
		silvershell_num = (TextView) findViewById(R.id.silvershell_num);
		goldshell_num = (TextView) findViewById(R.id.goldshell_num);

	}

	@Override
	protected void addEventListeners() {
		// do nothing...
	}

	@Override
	protected void justDoIt() {
		if (this.getObj(ANDIWEALTH_DATA) == null) {
			// 请求主活动来调用刷新方法
			// 为什么要请求而不自己去查呢？
			// 因为主活动暴露出了刷新按钮，所以只能由主活动来触发查询
			// 如果主活动没有暴露出刷新按钮，子活动就能自己查了
			this.AUTOREFRESH = true;
		} else {
			UserDetails usr = (UserDetails) this.getObj(ANDIWEALTH_DATA);
			updateUIWithUserDetails(usr);
		}
	}

	private void updateUIWithUserDetails(UserDetails usr) {
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

		String shellUnit = getText(R.string.shellunit).toString();
		seashell_num.setText(usr.seaShell+" "+shellUnit);
		coppershell_num.setText(usr.copperShell+" "+shellUnit);
		silvershell_num.setText(usr.silverShell+" "+shellUnit);
		goldshell_num.setText(usr.goldShell+" "+shellUnit);

	}


	@Override
	protected void doSend() {
		// do nothing...
	}

	@Override
	protected void onSendBegin() {
		// do nothing...
	}

	@Override
	protected void onSendSuccess() {
		// do nothing...
	}

	@Override
	protected void onSendFailure() {
		// do nothing...
	}

	@Override
	protected void doRetrieve() {
		this.checkTaskStatus();

		this.mRetrieveTask = new RetrieveMyWealthTask();
		this.mRetrieveTask.setListener(mRetrieveTaskListener);
		TaskParams params = new TaskParams();
		params.put("userId", PintuApp.getUser());
		this.mRetrieveTask.execute(params);

		this.manageTask(mRetrieveTask);
	}

	@Override
	protected void onRetrieveBegin() {
		if (this.pb != null) {
			this.pb.setVisibility(View.VISIBLE);
		} else {
			this.updateProgress("Loading My favorite pics...");
		}
		if (this.refreshBtn != null)
			this.refreshBtn.setVisibility(View.GONE);
	}

	@Override
	protected void onRetrieveSuccess() {
		if (this.pb != null)
			this.pb.setVisibility(View.GONE);

		if (this.refreshBtn != null)
			this.refreshBtn.setVisibility(View.VISIBLE);

		this.rememberLastVisit();
	}

	@Override
	protected void onRetrieveFailure() {
		if (this.pb != null)
			this.pb.setVisibility(View.GONE);

		if (this.refreshBtn != null)
			this.refreshBtn.setVisibility(View.VISIBLE);

		this.updateProgress(R.string.page_status_unable_to_update);
	}

	@Override
	protected void onParseJSONResultFailue() {
		if (this.pb != null)
			this.pb.setVisibility(View.GONE);

		if (this.refreshBtn != null)
			this.refreshBtn.setVisibility(View.VISIBLE);

		this.updateProgress(R.string.json_parse_error);
	}

	@Override
	protected void refreshListView(List<Object> results) {
		// do nothing...
	}

	@Override
	protected void refreshMultView(JSONObject json) {
		UserDetails details = UserDetails.parseJsonToObj(json);
		updateUIWithUserDetails(details);
		// 缓存起来
		this.putObj(ANDIWEALTH_DATA, details);
	}

	@Override
	public void addProgress(ProgressBar pb) {
		this.pb = pb;
	}

	@Override
	public void refresh(ImageButton refreshBtn) {
		this.refreshBtn = refreshBtn;

		// 数据是否有没有缓存是查询唯一依据
		// 这里不判断查询间隔了
		if (this.getObj(ANDIWEALTH_DATA) == null) {
			doRetrieve();
		}
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

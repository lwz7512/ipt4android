package com.ybcx.activity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.AdapterView.OnItemClickListener;

import com.ybcx.PintuApp;
import com.ybcx.R;
import com.ybcx.activity.base.HeadSwitchActivity;
import com.ybcx.activity.base.SubMainCallBack;
import com.ybcx.activity.base.TempletActivity;
import com.ybcx.adapter.UserAdapter;
import com.ybcx.data.UserInfo;
import com.ybcx.task.RetrieveDarenTask;
import com.ybcx.task.TaskParams;
/**
 * 
 * 可切换的活动必须实现SubMainCallBack
 * @author lwz
 *
 */
public class CommentDaren extends TempletActivity implements SubMainCallBack {

	// 存放数据的KEY
	private static final String CMNTDAREN_DATA = "cmntdaren";
	
	private ListView actusers;
	private UserAdapter usrAdptr;
	private ProgressBar pb;
	
	@Override
	protected int getLayout() {
		return R.layout.userlist;
	}

	@Override
	protected void getViews() {
		usrAdptr = new UserAdapter(this, UserAdapter.CMT_DAREN);
		actusers = (ListView) findViewById(R.id.userlist);
		actusers.setAdapter(usrAdptr);
	}

	@Override
	protected void addEventListeners() {
		actusers.setOnItemClickListener(listener);
	}
	
	private OnItemClickListener listener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			UserInfo selectedUsr = (UserInfo) usrAdptr.getItem(position);
			String userId = selectedUsr.id;
			Intent it = new Intent();
			it.setClass(CommentDaren.this, TadiAssets.class);
			it.putExtra("userId", userId);
			//打开作者详情模块
			startActivity(it);
		}		
	};
		

	@SuppressWarnings("unchecked")
	@Override
	protected void justDoIt() {
		// 先看缓存有没，如果没有去远程查
		if (this.getObj(CMNTDAREN_DATA) != null) {
			List<UserInfo> users = (List<UserInfo>) this.getObj(CMNTDAREN_DATA);
			usrAdptr.refresh(users);
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

	@Override
	protected void doRetrieve() {
		//网络检查
		if(!PintuApp.isNetworkAvailable()){
			updateProgress("Network not Available, try later!");
			return ;
		}
		
		if(!checkTaskStatus()) return;

		this.mRetrieveTask = new RetrieveDarenTask();
		this.mRetrieveTask.setListener(mRetrieveTaskListener);
		TaskParams param = new TaskParams();
		
		// 查询图片达人
		param.put("type", RetrieveDarenTask.CMNT_DAREN);
		this.mRetrieveTask.execute(param);
		
		this.manageTask(mRetrieveTask);
	}

	@Override
	protected void onRetrieveBegin() {
		if (this.pb != null) {
			this.pb.setVisibility(View.VISIBLE);
		} else {
			this.updateProgress("Loading daren...");
		}

	}

	@Override
	protected void onRetrieveSuccess() {
		if (this.pb != null)
			this.pb.setVisibility(View.GONE);

		this.rememberLastVisit();
	}

	@Override
	protected void onRetrieveFailure() {
		this.updateProgress(R.string.page_status_unable_to_update);
	}

	@Override
	protected void onParseJSONResultFailue() {
		this.updateProgress(R.string.json_parse_error);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void refreshListView(List<Object> results) {
		if (results.size() == 0) {
			return;
		}

		ArrayList<UserInfo> users = new ArrayList<UserInfo>();
		for (Object o : results) {
			users.add((UserInfo) o);
		}
		// 先缓存
		this.putObj(CMNTDAREN_DATA, users);
		// 再从缓存中取出
		List<UserInfo> cachedUsers = (List<UserInfo>) this.getObj(CMNTDAREN_DATA);
		//刷新视图
		this.usrAdptr.refresh(cachedUsers);
	}

	@Override
	protected void refreshMultView(JSONObject json) {
		// do nothing here...
	}

	@Override
	public void addProgress(ProgressBar pb) {
		this.pb = pb;
		
		if (this.getObj(CMNTDAREN_DATA) == null) {
			doRetrieve();
		}
	}

	@Override
	public void refresh(ImageButton refreshBtn) {
		// do nothing here...
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

package com.pintu.activity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.pintu.R;
import com.pintu.activity.base.HeadSwitchActivity;
import com.pintu.activity.base.SubMainCallBack;
import com.pintu.activity.base.TempletActivity;
import com.pintu.adapter.UserAdapter;
import com.pintu.data.UserInfo;
import com.pintu.task.RetrieveDarenTask;
import com.pintu.task.TaskParams;

/**
 * 
 * 可切换的活动必须实现SubMainCallBack
 * @author lwz
 *
 */
public class PicDaren extends TempletActivity  implements SubMainCallBack {

	// 存放数据的KEY
	private static final String PICDAREN_DATA = "picdaren";
	
	private ListView actusers;
	private UserAdapter usrAdptr;
	private ProgressBar pb;
	
	@Override
	protected int getLayout() {
		return R.layout.userlist;
	}

	@Override
	protected void getViews() {
		usrAdptr = new UserAdapter(this, UserAdapter.PIC_DAREN);
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
			it.setClass(PicDaren.this, TadiAssets.class);
			it.putExtra("userId", userId);
			//打开作者详情模块
			startActivity(it);
		}		
	};
		

	@SuppressWarnings("unchecked")
	@Override
	protected void justDoIt() {
		// 先看缓存有没，如果没有去远程查
		if (this.getObj(PICDAREN_DATA) != null) {
			List<UserInfo> users = (List<UserInfo>) this.getObj(PICDAREN_DATA);
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
		this.checkTaskStatus();

		this.mRetrieveTask = new RetrieveDarenTask();
		this.mRetrieveTask.setListener(mRetrieveTaskListener);
		TaskParams param = new TaskParams();
		
		// 查询图片达人
		param.put("type", RetrieveDarenTask.PIC_DAREN);
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
		this.putObj(PICDAREN_DATA, users);
		// 再从缓存中取出
		List<UserInfo> cachedUsers = (List<UserInfo>) this.getObj(PICDAREN_DATA);
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
		
		if (this.getObj(PICDAREN_DATA) == null) {
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

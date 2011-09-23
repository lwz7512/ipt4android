package com.pintu.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.AdapterView.OnItemClickListener;

import com.pintu.PintuApp;
import com.pintu.R;
import com.pintu.activity.base.HeadSwitchActivity;
import com.pintu.activity.base.SubMainCallBack;
import com.pintu.activity.base.TempletActivity;
import com.pintu.adapter.FavoPicsAdapter;
import com.pintu.api.PTApi;
import com.pintu.data.TPicItem;
import com.pintu.task.RetrieveFavoritesTask;
import com.pintu.task.TaskParams;

/**
 * 数据处理逻辑： 临时存放数据到父活动，只允许查询一次，除非退出父活动
 * 
 * @author lwz
 * 
 */
public class TadiPics extends TempletActivity implements SubMainCallBack {
	private static final String TAG = "TadiPics";

	// 存放数据的KEY
	private static final String TADIPICS_DATA = "tadipics";

	private ListView adpics_lv;
	private FavoPicsAdapter fpAdptr;
	private ProgressBar pb;

	// 主活动传递过来的被查看用户
	private String currentUser;

	@Override
	protected int getLayout() {
		// 与我的贴图共用布局，其实就是个列表
		return R.layout.adpics;
	}

	@Override
	protected void getViews() {
		fpAdptr = new FavoPicsAdapter(this);
		adpics_lv = (ListView) findViewById(R.id.adpics_lv);
		adpics_lv.setAdapter(fpAdptr);
	}

	@Override
	protected void addEventListeners() {
		adpics_lv.setOnItemClickListener(listener);

	}

	private OnItemClickListener listener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// 跳转到查看图片详情
			TPicItem selectedPic = (TPicItem) fpAdptr.getItem(position);
			String picId = selectedPic.id;
			Intent it = new Intent();
			it.setClass(TadiPics.this, PictureDetails.class);			
			it.putExtra("tpId", picId);
			//打开详情活动
			startActivity(it);
		}
	};

	@SuppressWarnings("unchecked")
	@Override
	protected void justDoIt() {
		// 先看缓存有没，如果没有去远程查
		if (this.getObj(TADIPICS_DATA) != null) {
			List<TPicItem> cachedPics = (List<TPicItem>) this
					.getObj(TADIPICS_DATA);
			this.fpAdptr.refresh(cachedPics);
		}

	}


	private void shouldRetrieve() {
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		// Must has extras
		if (null == extras) {
			Log.e(TAG, this.getClass().getName() + " must has extras.");
			return;
		}
		// 保存下来做按钮状态处理
		currentUser = extras.getString("userId");
		if (currentUser != null) {
			doRetrieve();
		} else {
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
	 * 注意： 这里用主活动传进来的用户去查询
	 */
	@Override
	protected void doRetrieve() {
		this.checkTaskStatus();

		this.mRetrieveTask = new RetrieveFavoritesTask();
		this.mRetrieveTask.setListener(mRetrieveTaskListener);
		TaskParams param = new TaskParams();
		// 用传递进来的用户查询
		param.put("userId", this.currentUser);
		// 只查一页
		param.put("pageNum", 1);
		// 指明是按用户查询图片
		param.put("method", PTApi.GETTPICSBYUSER);
		this.mRetrieveTask.execute(param);

		this.manageTask(mRetrieveTask);
	}

	@Override
	protected void onRetrieveBegin() {
		if (this.pb != null) {
			this.pb.setVisibility(View.VISIBLE);
		} else {
			this.updateProgress("Loading TADI pics...");
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

		ArrayList<TPicItem> pics = new ArrayList<TPicItem>();
		for (Object o : results) {
			pics.add((TPicItem) o);
		}
		// 先缓存
		this.putObj(TADIPICS_DATA, pics);
		// 再从缓存中取出
		List<TPicItem> cachedPics = (List<TPicItem>) this.getObj(TADIPICS_DATA);
		this.fpAdptr.refresh(cachedPics);
	}

	@Override
	protected void refreshMultView(JSONObject json) {
		// do nothing here...
	}

	@Override
	public void addProgress(ProgressBar pb) {
		this.pb = pb;
		
		if (this.getObj(TADIPICS_DATA) == null) {
			shouldRetrieve();
		}
	}

	@Override
	public void refresh(ImageButton refreshBtn) {
		// do nothing...
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

package com.pintu.activity;

import java.util.ArrayList;
import java.util.List;

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

import com.pintu.R;
import com.pintu.activity.base.HeadSwitchActivity;
import com.pintu.activity.base.TempletActivity;
import com.pintu.adapter.FavoPicsAdapter;
import com.pintu.adapter.StoryInfoAdapter;
import com.pintu.adapter.SubMainCallBack;
import com.pintu.api.PTApi;
import com.pintu.data.StoryInfo;
import com.pintu.data.TPicItem;
import com.pintu.task.RetrieveFavoritesTask;
import com.pintu.task.RetrieveStoriesTask;
import com.pintu.task.TaskParams;
/**
 * 数据处理逻辑：
 * 临时存放数据到父活动，只允许查询一次，除非退出父活动
 * @author lwz
 *
 */
public class TadiPintu extends TempletActivity implements SubMainCallBack {
	private static final String TAG = "TadiPintu";
	//存放数据的KEY
	private static final String TADIPINTU_DATA = "tadipintu";
	
	private ListView pintu_lv;
	private StoryInfoAdapter storyAdptr;
	private ProgressBar pb;

	//主活动传递过来的被查看用户
	private String currentUser;


	@Override
	protected int getLayout() {
		return R.layout.adpintu;
	}

	@Override
	protected void getViews() {
		pintu_lv = (ListView) findViewById(R.id.pintu_lv);
		storyAdptr = new StoryInfoAdapter(this);
		pintu_lv.setAdapter(storyAdptr);
	}

	@Override
	protected void addEventListeners() {
		pintu_lv.setOnItemClickListener(listener);
	}

	private OnItemClickListener listener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO 跳转到查看图片详情
			StoryInfo story = (StoryInfo) storyAdptr.getItem(position);
			String picId = story.follow;
			
		}		
	};

	@SuppressWarnings("unchecked")
	@Override
	protected void justDoIt() {
		//先看缓存有没，如果没有去远程查
		if(this.getObj(TADIPINTU_DATA)!=null){
			List<StoryInfo>cachedStories = (List<StoryInfo>) this.getObj(TADIPINTU_DATA);
			this.storyAdptr.refresh(cachedStories);
		}			
	}

	
	private void shouldRetrieve(){
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		// Must has extras
		if (null == extras) {
			Log.e(TAG, this.getClass().getName() + " must has extras.");
			return;
		}
		//保存下来做按钮状态处理
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

	@Override
	protected void doRetrieve() {
		this.checkTaskStatus();
		
		mRetrieveTask = new RetrieveStoriesTask();
		mRetrieveTask.setListener(mRetrieveTaskListener);

		TaskParams params = new TaskParams();
		//用传递进来的用户查询
		params.put("userId", this.currentUser);
		//只查1页
		params.put("pageNum", 1);
		//指明是按用户查询图片
		params.put("method", PTApi.GETSTORIESBYUSER);
		this.mRetrieveTask.execute(params);
		
		this.manageTask(mRetrieveTask);
	}

	@Override
	protected void onRetrieveBegin() {
		if(this.pb!=null){
			this.pb.setVisibility(View.VISIBLE);			
		}else{
			this.updateProgress("Loading TADI pintu...");
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
		this.updateProgress(R.string.page_status_unable_to_update);
	}

	@Override
	protected void onParseJSONResultFailue() {
		this.updateProgress(R.string.json_parse_error);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void refreshListView(List<Object> results) {
		if(results.size()==0){	
			return;
		}
		
		ArrayList<StoryInfo> stories = new ArrayList<StoryInfo>();
		for(Object o : results){
			stories.add((StoryInfo) o);
		}
		//先缓存
		this.putObj(TADIPINTU_DATA, stories);
		//再从缓存中取出
		List<StoryInfo>cachedStories = (List<StoryInfo>) this.getObj(TADIPINTU_DATA);
		this.storyAdptr.refresh(cachedStories);
	}


	@Override
	protected void refreshMultView(JSONObject json) {
		// do nothing here...
	}
	
	@Override
	public void addProgress(ProgressBar pb) {
		this.pb = pb;
		
		if(this.getObj(TADIPINTU_DATA)==null){
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

package com.pintu.activity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.pintu.PintuApp;
import com.pintu.R;
import com.pintu.activity.base.TempletActivity;
import com.pintu.adapter.ClassicStoryAdapter;
import com.pintu.adapter.SubMainCallBack;
import com.pintu.data.StoryInfo;
import com.pintu.data.TPicDetails;
import com.pintu.task.RetrieveClassicTask;
import com.pintu.task.TaskParams;
import com.pintu.util.DateTimeHelper;
import com.pintu.util.Preferences;

public class HistoryClassicStory extends TempletActivity implements SubMainCallBack {

	private static String TAG = "HistoryClassicStory";

	private ListView classic_story_lv;
	private ClassicStoryAdapter csAdptr;

	private ProgressBar pb;
	
	private List<StoryInfo> cachedStories;

	@Override
	protected int getLayout() {
		return R.layout.historyclassic;
	}

	@Override
	protected void getViews() {
		classic_story_lv = (ListView) findViewById(R.id.classic_story_lv);
		csAdptr = new ClassicStoryAdapter(this);
		classic_story_lv.setAdapter(csAdptr);
	}

	@Override
	protected void addEventListeners() {
		classic_story_lv.setOnItemClickListener(itemClickListener);

	}

	private OnItemClickListener itemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO 添加点击查看图片详情的动作
			StoryInfo story = (StoryInfo) csAdptr.getItem(position);
			String tpId = story.follow;

		}
	};

	@Override
	protected void justDoIt() {
		//首先尝试取缓存数据
		cachedStories = PintuApp.dbApi.getCachedClassicStories();
		if(cachedStories!=null && cachedStories.size()>0){
			csAdptr.refresh(cachedStories);			
		}		
	}

	@Override
	protected void doItLater() {
		long lastVisitTime = this.getPreferences().getLong(
				Preferences.LAST_VISIT_TIME, 0);
		long now = DateTimeHelper.getNowTime();
		long diff = now - lastVisitTime;
		// 经典是1小时计算一次
		// 第一次使用应用肯定要从远程取
		if (diff > oneHourMiliSeconds || lastVisitTime == 0 || cachedStories.size()==0) {
			// 取远程数据
			doRetrieve();
		}
	}


	@Override
	protected void doRetrieve() {
		this.checkTaskStatus();
		this.mRetrieveTask = new RetrieveClassicTask();
		this.mRetrieveTask.setListener(mRetrieveTaskListener);
		this.mRetrieveTask.execute(new TaskParams());
		this.manageTask(mRetrieveTask);				
	}

	@Override
	protected void onRetrieveBegin() {
		if (this.pb != null)
			this.pb.setVisibility(View.VISIBLE);
	}

	@Override
	protected void onRetrieveSuccess() {
		if (this.pb != null)
			this.pb.setVisibility(View.GONE);
		// 记下取回数据的时间，下次切换时就不从远程取了
		this.rememberLastVisit();
	}

	@Override
	protected void onRetrieveFailure() {
		if (this.pb != null)
			this.pb.setVisibility(View.GONE);
		updateProgress(R.string.page_status_unable_to_update);
	}

	@Override
	protected void onParseJSONResultFailue() {
		updateProgress(R.string.json_parse_error);
	}

	@Override
	protected void refreshListView(List<Object> results) {
		ArrayList<StoryInfo> stories = new ArrayList<StoryInfo>();
		for(Object o : results){
			stories.add((StoryInfo) o);
		}
		if(stories.size()==0){
			updateProgress("No classic story in system currently!");
			return;
		}
		//先入库缓存
		PintuApp.dbApi.insertClassicStories(stories);
		//再从库中取出
		List<StoryInfo>cachedStories = PintuApp.dbApi.getCachedClassicStories();
		this.csAdptr.refresh(cachedStories);
	}

	@Override
	protected void refreshMultView(JSONObject json) {
		// do nothing here...
	}

	@Override
	public void addProgress(ProgressBar pb) {
		this.pb = pb;
	}

	@Override
	public void refresh() {
		// 10分钟后切换进来后会自动重取
		// 该方法是预留给主活动标题栏中的刷新按钮调用的
	}

	@Override
	public void putObj(String key, Object value) {
		// 用sqlite缓存了，不用这么缓存
	}

	@Override
	public Object getObj(String key) {
		// 用sqlite缓存了，不用这么缓存
		return null;
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


}

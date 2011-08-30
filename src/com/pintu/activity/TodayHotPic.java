package com.pintu.activity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pintu.PintuApp;
import com.pintu.R;
import com.pintu.adapter.HotPicsAdapter;
import com.pintu.adapter.SubMainCallBack;
import com.pintu.data.TPicDetails;
import com.pintu.task.RetrieveHotPicsTask;
import com.pintu.task.TaskParams;
import com.pintu.util.DateTimeHelper;
import com.pintu.util.Preferences;

public class TodayHotPic extends TempletActivity implements SubMainCallBack{

	private static String TAG = "TodayHotPic";	

	//List
	private ListView hotpicList;
	private HotPicsAdapter hpAdptr;
	
	//ProgressBar
	private ProgressBar pb;
	
	@Override
	protected int getLayout() {
		return R.layout.todayhot;
	}

	@Override
	protected void getViews() {
		
		hotpicList = (ListView) findViewById(R.id.hotpics_lv);
		hpAdptr = new HotPicsAdapter(this);
		hotpicList.setAdapter(hpAdptr);
		hotpicList.setOnItemClickListener(itemClickListener);
	}
	
	private OnItemClickListener itemClickListener = new OnItemClickListener(){

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO 添加点击查看详情的动作
			TPicDetails tpic = (TPicDetails) hpAdptr.getItem(position);
			String tpId = tpic.id;
			
		}
		
	};

	@Override
	protected void addEventListeners() {
		//do nothing...
	}
		

	@Override
	protected void justDoIt() {
		//首先尝试取缓存数据
		List<TPicDetails>cachedHotPics = PintuApp.dbApi.getCachedHotPics();
		if(cachedHotPics!=null && cachedHotPics.size()>0){
			hpAdptr.refresh(cachedHotPics);			
		}
		
	}
	
	@Override
	protected void doItLater() {
		long lastVisitTime = this.getPreferences().getLong(Preferences.LAST_VISIT_TIME, 0);
		long now = DateTimeHelper.getNowTime();
		long diff = now - lastVisitTime;	
		//如果登录超过1小时就允许重新取数据了
		//或者第一次使用应用肯定要从远程取
		if(diff>oneHourMiliSeconds || lastVisitTime==0){
			//取远程数据
			doRetrieve();
		}				
	}

	
	@Override
	protected void doSend() {
		// do nothing, but keep me here ...
	}

	@Override
	protected void onSendBegin() {
		// do nothing, but keep me here ...
	}

	@Override
	protected void onSendSuccess() {
		// do nothing, but keep me here ...
	}

	@Override
	protected void onSendFailure() {
		// do nothing, but keep me here ...
	}

	@Override
	protected void doRetrieve() {
		this.checkTaskStatus();
		this.mRetrieveTask = new RetrieveHotPicsTask();
		this.mRetrieveTask.setListener(mRetrieveTaskListener);
		this.mRetrieveTask.execute(new TaskParams());
		this.manageTask(mRetrieveTask);		
	}

	@Override
	protected void onRetrieveBegin() {
		if(this.pb!=null)
				this.pb.setVisibility(View.VISIBLE);
	}

	@Override
	protected void onRetrieveSuccess() {
		if(this.pb!=null)
				this.pb.setVisibility(View.GONE);
		//记下取回数据的时间，下次切换时就不从远程取了
		this.rememberLastVisit();
	}

	@Override
	protected void onRetrieveFailure() {
		if(this.pb!=null)
			this.pb.setVisibility(View.GONE);
		updateProgress(R.string.page_status_unable_to_update);
	}

	@Override
	protected void onParseJSONResultFailue() {
		updateProgress(R.string.json_parse_error);
	}

	@Override
	protected void refreshListView(List<Object> results) {
		ArrayList<TPicDetails> hotpics = new ArrayList<TPicDetails>();
		for(Object o : results){
			hotpics.add((TPicDetails) o);
		}
		//先入库缓存
		PintuApp.dbApi.insertHotPics(hotpics);
		//再从库中取出
		List<TPicDetails>cachedHotPics = PintuApp.dbApi.getCachedHotPics();
		this.hpAdptr.refresh(cachedHotPics);
	}

	@Override
	protected void refreshMultView(JSONObject json) {
		//do nothing...
	}

	@Override
	public void addProgress(ProgressBar pb) {
		this.pb = pb;
	}

	@Override
	public void refresh() {
		// TODO Auto-generated method stub
		
	}


}

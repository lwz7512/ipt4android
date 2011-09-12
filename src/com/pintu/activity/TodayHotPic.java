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
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pintu.PintuApp;
import com.pintu.R;
import com.pintu.activity.base.SubMainCallBack;
import com.pintu.activity.base.TempletActivity;
import com.pintu.adapter.HotPicsAdapter;
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
	
	//pic in local db
	private List<TPicDetails>cachedHotPics;
	
	@Override
	protected int getLayout() {
		return R.layout.todayhot;
	}

	@Override
	protected void getViews() {		
		hotpicList = (ListView) findViewById(R.id.hotpics_lv);
		hpAdptr = new HotPicsAdapter(this);
		hotpicList.setAdapter(hpAdptr);		
	}
	

	@Override
	protected void addEventListeners() {
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
	protected void justDoIt() {
		//首先尝试取缓存数据
		cachedHotPics = PintuApp.dbApi.getCachedHotPics();
		if(cachedHotPics!=null && cachedHotPics.size()>0){
			hpAdptr.refresh(cachedHotPics);			
		}		
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
		if(results.size()==0){
			updateProgress("No hot pictures in system currently!");
			return;
		}
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
		
		//如果登录超过10分钟就允许重新取数据了
		//或者第一次使用应用肯定要从远程取
		long diff = this.elapsedFromLastVisit();
		if(diff>tenMinutesMiliSeconds || cachedHotPics.size()==0){
			//取远程数据
			doRetrieve();
		}
	}

	@Override
	public void refresh(ImageButton refreshBtn) {
		// 10分钟后切换进来后会自动重取
		//该方法是预留给主活动标题栏中的刷新按钮调用的
	}

	@Override
	public void putObj(String key, Object value) {
		//用sqlite缓存了，不用这么缓存		
	}

	@Override
	public Object getObj(String key) {
		//用sqlite缓存了，不用这么缓存
		return null;
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


}

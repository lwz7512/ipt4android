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

import com.pintu.PintuApp;
import com.pintu.R;
import com.pintu.activity.base.SubMainCallBack;
import com.pintu.activity.base.TempletActivity;
import com.pintu.adapter.HotPicsAdapter;
import com.pintu.api.PTApi;
import com.pintu.data.TPicDetails;
import com.pintu.task.RetrieveHotPicsTask;
import com.pintu.task.TaskParams;

public class ClassicWorks extends TempletActivity implements SubMainCallBack {

	private static String TAG = "ClassicWorks";


	//List
	private ListView hotpicList;
	private HotPicsAdapter hpAdptr;
	
	//ProgressBar
	private ProgressBar pb;
	private ImageButton refreshBtn;
	
	//pic in local db
	private List<TPicDetails>cachedClassicPics;
	
	@Override
	protected int getLayout() {
		//与热图公用一个列表，内容相同
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
			// 添加点击查看详情的动作
			TPicDetails tpic = (TPicDetails) hpAdptr.getItem(position);
			String tpId = tpic.id;
			Intent it = new Intent();
			it.setClass(ClassicWorks.this, PictureDetails.class);			
			it.putExtra("tpId", tpId);
			//打开详情活动
			startActivity(it);

		}		
	};

	
	@Override
	protected void justDoIt() {
		//首先尝试取缓存数据		
		cachedClassicPics = PintuApp.dbApi.getCachedClassicPics();
		if(cachedClassicPics!=null && cachedClassicPics.size()>0){
			hpAdptr.refresh(cachedClassicPics);			
		}	
		if(cachedClassicPics.size()==0){
			this.AUTOREFRESH = true;
		}
	}
	

	@Override
	protected void doRetrieve() {
		if(!checkTaskStatus()) return;
		
		//与查询热图共用一个任务，用参数区分
		this.mRetrieveTask = new RetrieveHotPicsTask();
		this.mRetrieveTask.setListener(mRetrieveTaskListener);
		TaskParams method = new TaskParams();
		method.put("method", PTApi.GETClASSICALPICS);
		this.mRetrieveTask.execute(method);
		
		this.manageTask(mRetrieveTask);		
	}

	@Override
	protected void onRetrieveBegin() {
		if(this.pb!=null)
				this.pb.setVisibility(View.VISIBLE);
		if(this.refreshBtn!=null)
			this.refreshBtn.setVisibility(View.GONE);
	}

	@Override
	protected void onRetrieveSuccess() {
		if(this.pb!=null)
				this.pb.setVisibility(View.GONE);
		
		if(this.refreshBtn!=null)
			this.refreshBtn.setVisibility(View.VISIBLE);
		
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
		ArrayList<TPicDetails> clscpics = new ArrayList<TPicDetails>();
		for(Object o : results){
			clscpics.add((TPicDetails) o);
		}
		//先入库缓存
		PintuApp.dbApi.insertClassicPics(clscpics);
		//再从库中取出
		List<TPicDetails>cachedClscPics = PintuApp.dbApi.getCachedClassicPics();
		this.hpAdptr.refresh(cachedClscPics);
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
	public void refresh(ImageButton refreshBtn) {
		this.refreshBtn = refreshBtn;
		//如果登录超过10分钟就允许重新取数据了
		//或者第一次使用应用肯定要从远程取
		long diff = this.elapsedFromLastVisit();
		if(diff>tenMinutesMiliSeconds || cachedClassicPics.size()==0){
			//取远程数据
			doRetrieve();
		}
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

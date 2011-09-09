package com.pintu.activity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.AdapterView.OnItemClickListener;

import com.pintu.PintuApp;
import com.pintu.R;
import com.pintu.activity.base.TempletActivity;
import com.pintu.adapter.FavoPicsAdapter;
import com.pintu.adapter.SubMainCallBack;
import com.pintu.api.PTApi;
import com.pintu.data.TPicItem;
import com.pintu.task.RetrieveFavoritesTask;
import com.pintu.task.TaskParams;

/**
 * 数据处理逻辑：
 * 先取缓存显示，调用刷新方法时，判断时间间隔
 * 超过1分钟了，允许重新查询
 * @author lwz
 *
 */
public class AndiPics extends TempletActivity implements SubMainCallBack {

	private ListView adpics_lv;
	private FavoPicsAdapter fpAdptr;
	private ProgressBar pb;
	private ImageButton refreshBtn;
	//当前页码数目
	private int pageNum = 0;
	
	private int lastFetchNum = 0;

	@Override
	protected int getLayout() {
		return R.layout.adpics;
	}

	@Override
	protected void getViews() {
		fpAdptr = new FavoPicsAdapter(this);
		adpics_lv = (ListView) findViewById(R.id.markpics_lv);
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
			// TODO 跳转到查看图片详情
			TPicItem selectedPic = (TPicItem) fpAdptr.getItem(position);
			String picId = selectedPic.id;
			
		}		
	};

	
	@Override
	protected void justDoIt() {
		//先看缓存中有没
		List<TPicItem> pics = PintuApp.dbApi.getCachedFavoritePics();
		if(pics.size()>0)
			fpAdptr.refresh(pics);
	}

	@Override
	protected void doItLater() {
		//不自动取了，手动刷新取
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
		
		this.mRetrieveTask = new RetrieveFavoritesTask();
		this.mRetrieveTask.setListener(mRetrieveTaskListener);
		TaskParams param = new TaskParams();
		//查询前页码增加
		pageNum ++;
		param.put("userId", PintuApp.getUser());
		param.put("pageNum", pageNum);
		//指明是查询我的图片
		param.put("method", PTApi.GETTPICSBYUSER);
		this.mRetrieveTask.execute(param);
		
		this.manageTask(mRetrieveTask);
	}

	@Override
	protected void onRetrieveBegin() {
		if(this.pb!=null){
			this.pb.setVisibility(View.VISIBLE);			
		}else{
			this.updateProgress("Loading My favorite pics...");
		}
		if(this.refreshBtn!=null)
			this.refreshBtn.setVisibility(View.GONE);
		
	}

	@Override
	protected void onRetrieveSuccess() {
		if(this.pb!=null)
			this.pb.setVisibility(View.GONE);
		
		if(this.refreshBtn!=null)
			this.refreshBtn.setVisibility(View.VISIBLE);
		
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

	@Override
	protected void refreshListView(List<Object> results) {
		if(results.size()==0){	
			updateProgress("Ended, 10 sec Later to Refresh Newest!");
			pageNum = 0;
			return;
		}
		
		if(results.size()<lastFetchNum){
			updateProgress("Ended, 10 sec Later to Refresh Newest!");
			pageNum = 0;
		}
		
		//记下上次取回记录数以便提醒
		lastFetchNum = results.size();
		
		ArrayList<TPicItem> pics = new ArrayList<TPicItem>();
		for(Object o : results){
			pics.add((TPicItem) o);
		}
		//先入库缓存
		PintuApp.dbApi.insertMyPics(pics);
		//再从库中取出
		List<TPicItem>cachedPics = PintuApp.dbApi.getCachedMyPics(1);
		this.fpAdptr.refresh(cachedPics);
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
	public void refresh(ImageButton refreshBtn) {
		this.refreshBtn = refreshBtn;
		long diff = this.elapsedFromLastVisit();
		if(diff>this.tenSecsMiliSeconds){
			this.doRetrieve();
		}else{
			this.updateProgress("10 sec Later to Refresh ...");
		}
	}

	@Override
	public void putObj(String key, Object value) {
		//do nothing here...
		//use sqlite instead;
	}

	@Override
	public Object getObj(String key) {
		//do nothing here...
		//use sqlite instead;
		return null;
	}


}

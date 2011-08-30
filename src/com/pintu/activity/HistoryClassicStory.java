package com.pintu.activity;

import java.util.List;

import org.json.JSONObject;

import android.util.Log;
import android.widget.ProgressBar;

import com.pintu.R;
import com.pintu.adapter.SubMainCallBack;
import com.pintu.util.DateTimeHelper;
import com.pintu.util.Preferences;

public class HistoryClassicStory extends TempletActivity implements SubMainCallBack{

	private static String TAG = "HistoryClassicStory";
	
	@Override
	protected int getLayout() {
		return R.layout.historyclassic;
	}

	@Override
	protected void getViews() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void addEventListeners() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void justDoIt() {
		//TODO, 首先尝试取缓存数据
		
		long lastVisitTime = this.getPreferences().getLong(Preferences.LAST_VISIT_TIME, 0);
		long now = DateTimeHelper.getNowTime();
		long diff = now - lastVisitTime;	
		//如果登录超过1小时就允许重新取数据了
		//第一次使用应用肯定要从远程取
		if(diff>oneHourMiliSeconds || lastVisitTime==0){
			//取远程数据
			doRetrieve();
		}
	}

	@Override
	protected void doSend() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onSendBegin() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onSendSuccess() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onSendFailure() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doRetrieve() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onRetrieveBegin() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onRetrieveSuccess() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onRetrieveFailure() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onParseJSONResultFailue() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void refreshListView(List<Object> results) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void refreshMultView(JSONObject json) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addProgress(ProgressBar pb) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void refresh() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void doItLater() {
		// TODO Auto-generated method stub
		
	}

}

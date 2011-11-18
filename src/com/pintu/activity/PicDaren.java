package com.pintu.activity;

import java.util.List;

import org.json.JSONObject;

import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.pintu.R;
import com.pintu.activity.base.SubMainCallBack;
import com.pintu.activity.base.TempletActivity;

/**
 * 
 * 可切换的活动必须实现SubMainCallBack
 * @author lwz
 *
 */
public class PicDaren extends TempletActivity  implements SubMainCallBack {

	@Override
	protected int getLayout() {
		// TODO Auto-generated method stub
		return R.layout.comingsoon;
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
		// TODO Auto-generated method stub

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
	public void refresh(ImageButton refreshBtn) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void putObj(String key, Object value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object getObj(String key) {
		// TODO Auto-generated method stub
		return null;
	}

}

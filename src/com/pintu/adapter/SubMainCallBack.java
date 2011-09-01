package com.pintu.adapter;

import com.pintu.activity.HeadSwitchActivity;

import android.widget.ProgressBar;

public interface SubMainCallBack {
	//添加子活动取数据的进度条
	public void addProgress(ProgressBar pb);	
	public void refresh();
	//子活动临时存取数据到主活动
	public void putObj(String key, Object value);
	public Object getObj(String key);
	
//--------------------------- 示例实现代码 ------------------------------
	
//	@Override
//	public void putObj(String key, Object value) {	
//		if(this.getParent()!=null){
//			HeadSwitchActivity frame = (HeadSwitchActivity)this.getParent();
//			frame.sharedRepository.put(key, value);
//		}
//	}
//
//	@Override
//	public Object getObj(String key) {
//		if(this.getParent()!=null){
//			HeadSwitchActivity frame = (HeadSwitchActivity)this.getParent();
//			return frame.sharedRepository.get(key);
//		}
//		return null;
//	}

	
}

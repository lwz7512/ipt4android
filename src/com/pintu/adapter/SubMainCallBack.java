package com.pintu.adapter;


import android.widget.ImageButton;
import android.widget.ProgressBar;

public interface SubMainCallBack {
	
	//添加子活动取数据的进度条，为了让子活动显示进度状态
	public void addProgress(ProgressBar pb);	
	//为子活动刷新数据时，能处理按钮的状态
	public void refresh(ImageButton refreshBtn);
	
	//子活动临时存取数据到主活动
	public void putObj(String key, Object value);
	public Object getObj(String key);
	
	
	
	
	
//--------------------------- 临时缓存对象示例实现代码 ------------------------------
	
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

package com.pintu.activity;

import com.pintu.R;

import android.os.Bundle;

public class StoryList extends FullScreenActivity {

	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stories);
		
		//获得组件引用
		getViews();
		//添加事件监听
		addEventListeners();
		
	}
	
	private void addEventListeners() {
		// TODO Auto-generated method stub
		
	}

	private void getViews() {
		// TODO Auto-generated method stub
		
	}
	
	protected void onDestroy(){
		super.onDestroy();
		
	}
	
	
	
} //end of class

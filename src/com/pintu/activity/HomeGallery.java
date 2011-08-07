package com.pintu.activity;

import com.pintu.R;
import com.pintu.adapter.GalleryImageAdapter;
import com.pintu.activity.PictureEdit;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ProgressBar;

public class HomeGallery extends FullScreenActivity {

	
	
	private ImageButton postImg;
	
	private GridView gallery;
	
	private ImageButton refresh;
	
	private ProgressBar progressbar;
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.homegallery);
		//获得组件引用
		getViews();
		//添加事件监听
		addEventListeners();
		
	}
	
	private void getViews(){
		postImg = (ImageButton)findViewById(R.id.post_image_btn);
		
		gallery = (GridView)findViewById(R.id.ptgallery);
		//初始化画廊数据
		gallery.setAdapter(new GalleryImageAdapter(this));
		
		refresh = (ImageButton)findViewById(R.id.refresh_btn);
		progressbar = (ProgressBar)findViewById(R.id.top_refresh_progressBar);
	}
	
	private void addEventListeners(){
		postImg.setOnClickListener(postImgListener);
		//TODO, add other ...
	}
	
	private OnClickListener postImgListener = new OnClickListener(){
			public void onClick(View v){
				Intent intent = new Intent();
				//启动编辑界面
				intent.setClass(HomeGallery.this, PictureEdit.class);
				startActivity(intent);
			}
	};
	
	
	

} //end of class

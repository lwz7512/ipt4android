package com.pintu.activity;

import com.pintu.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DetailPicture extends FullScreenActivity {

	private static String TAG = "DetailPicture";
	
	// Header
	private Button top_back;
	private TextView tv_title;
	private ProgressBar get_details_progressBar;
	
	//Body
	private ImageView profile_image;
	private TextView user_name;
	private TextView user_info;
	private TextView created_at;
	private ImageView t_picture;
	private TextView send_source;
	private TextView storynum;
	private TextView commentnum;
	
	//Footer
	private TextView tv_taste;
	private TextView tv_comment;
	private TextView tv_favorite;
	private TextView tv_forward;
	private TextView tv_report;
	
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.picdetail);
		//获得组件引用
		getViews();
		//添加事件监听
		addEventListeners();

	}
	
	private void getViews(){
		
	}
	
	private void addEventListeners(){
		
	}
	
	
	
	
} //end of activity

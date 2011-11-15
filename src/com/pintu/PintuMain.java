package com.pintu;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.pintu.activity.HomeGallery;
import com.pintu.activity.LogonSys;
import com.pintu.activity.base.FullScreenActivity;

public class PintuMain extends FullScreenActivity {
	// 停留3秒
	private final int SPLASH_DISPLAY_LENGHT = 3000; 
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);

		new Handler().postDelayed(new Runnable() {
			public void run() {
				forwardTo();
			}
		}, SPLASH_DISPLAY_LENGHT);
			
		checkAppStatus();
	}
	
	private void checkAppStatus() {
		if(PintuApp.mApi.isDebugMode()){
			Toast.makeText(this, "Warning, running in DEBUG mode!", 
					Toast.LENGTH_LONG).show();
		}
	}
	
	private void forwardTo(){
		boolean logoned = PintuApp.isLoggedin();
		Intent it = new Intent();
		if(logoned){
			it.setClass(this, HomeGallery.class);
		}else{
			it.setClass(this, LogonSys.class);
		}
		//打开新活动
		startActivity(it);
		//关闭欢迎页面
		finish();
	}
	
} // end of class
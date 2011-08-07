package com.pintu.activity;

import com.pintu.PintuApp;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Window;

/**
 * @author anonymous
 */
public abstract class FullScreenActivity extends Activity {

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setupFullScreen();

	}

	private void setupFullScreen(){
		//系统状态条
//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		//应用标题栏		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		//禁制横屏
		//add by lwz7512 @ 2011/08/03
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
	}	
	
	
    public SharedPreferences getPreferences() {
        return PintuApp.mPref;
    }
	
} //end of class

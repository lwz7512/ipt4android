package com.pintu.activity;

import android.content.ComponentName;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.pintu.PintuApp;
import com.pintu.R;
import com.pintu.activity.base.FullScreenActivity;

public class AboutThis extends FullScreenActivity {

	// Header
	//返回按钮
	private ImageButton top_back;
	//顶部标题
	private TextView tv_title;
	//版本信息
	private TextView version;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.about);
				
		// set real version
		ComponentName comp = new ComponentName(this, getClass());
		PackageInfo pinfo = null;
		try {
			pinfo = getPackageManager()
					.getPackageInfo(comp.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		top_back = (ImageButton) findViewById(R.id.top_back);
		top_back.setOnClickListener(mGoListener);
		
		tv_title = (TextView) findViewById(R.id.tv_title);
		tv_title.setText(R.string.about);

		version = (TextView) findViewById(R.id.version);
		String appName = getText(R.string.app_name).toString();
		String versionName = String.format("v %s", pinfo.versionName);
		String versionState = PintuApp.VERSION_STATE;
		version.setText(appName+" "+versionName+" "+versionState);			
		
	}
	
	private OnClickListener mGoListener = new OnClickListener() {
		public void onClick(View v) {
			finish();
		}
	};

	
}

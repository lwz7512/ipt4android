package com.pintu.activity;

import android.content.ComponentName;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.widget.TextView;

import com.pintu.R;
import com.pintu.activity.base.FullScreenActivity;

public class AboutThis extends FullScreenActivity {

	private String versionRelease = null;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.about);
		
		versionRelease=android.os.Build.VERSION.RELEASE;
		
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

		TextView version = (TextView) findViewById(R.id.version);
		version.setText(String.format("v %s", pinfo.versionName));
		
	}
	
}

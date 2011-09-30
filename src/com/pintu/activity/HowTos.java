package com.pintu.activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.pintu.R;
import com.pintu.activity.base.FullScreenActivity;

public class HowTos extends FullScreenActivity {

	// Header
	//返回按钮
	private Button top_back;
	//顶部标题
	private TextView tv_title;

	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.help);
		
		top_back = (Button) findViewById(R.id.top_back);
		top_back.setOnClickListener(mGoListener);
		tv_title = (TextView) findViewById(R.id.tv_title);

	}

	private OnClickListener mGoListener = new OnClickListener() {
		public void onClick(View v) {
			finish();
		}
	};

	
}

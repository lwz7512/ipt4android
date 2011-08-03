package com.pintu.activity;

import com.pintu.R;
import com.pintu.adapter.HeadSwitchAdapter;

import android.app.ActivityGroup;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;

public abstract class HeadSwitchActivity extends ActivityGroup {

	private GridView gvTopBar;
	private HeadSwitchAdapter topImgAdapter;
	// 装载sub Activity的容器
	private FrameLayout container;
	
	private int iconSize= 48;
	private int[] iconArray;
		

	//TODO, 子类必须重载这个方法来填充图标资源
	public abstract  int[] initNavIcons();
	//TODO, 子类需要重载这个方法来切换视图
	public abstract Intent switchByIndex(int index);
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//先设置全屏参数
		setupFullScreen();
		//然后设置布局
		setContentView(R.layout.headswitch);
				
		//生成导航栏
		setupNavBar();

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
	
	
	private void setupNavBar(){
		//初始化导航图标资源
		iconArray = initNavIcons();
		
		//必须有图标文件才行
		if(iconArray==null) return;
		
		container = (FrameLayout) findViewById(R.id.Container);
		gvTopBar = (GridView) this.findViewById(R.id.gvTopBar);
		// 设置每行列数
		gvTopBar.setNumColumns(iconArray.length);
		// 选中的时候为透明色
		gvTopBar.setSelector(new ColorDrawable(Color.TRANSPARENT));
		// 位置居中
		gvTopBar.setGravity(Gravity.CENTER);
		// 垂直间隔
		gvTopBar.setVerticalSpacing(0);
		// 项目点击事件
		gvTopBar.setOnItemClickListener(new ItemClickEvent());
		topImgAdapter = new HeadSwitchAdapter(this, iconArray, iconSize, iconSize,R.drawable.topbar_itemselector);
		// 设置菜单Adapter
		gvTopBar.setAdapter(topImgAdapter);
		//默认打开第0页
		switchActivity(0);
		
	}
	
	private class ItemClickEvent implements OnItemClickListener {
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
			switchActivity(arg2);
		}
	}	

	/**
	 * 根据ID打开指定的Activity
	 * @param id GridView选中项的序号
	 */
	private void switchActivity(int id){
		topImgAdapter.SetFocus(id);//选中项获得高亮
		container.removeAllViews();//必须先清除容器中所有的View
		//获取子类给出的视图
		Intent intent =switchByIndex(id);
		if(intent==null) return;
		
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		//Activity 转为 View
		Window subActivity = getLocalActivityManager().startActivity("subActivity", intent);
		//容器添加View
		container.addView(subActivity.getDecorView(),LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	}
	
//	public Intent sample_switchByIndex(int index){
//		Intent result;
//		switch(index){
//		case 0:
//			result = new Intent(this,TargetActivity);
//			break;
//		case 1:
//			result = new Intent(this,TargetActivity);
//			break;
//		//...
//			
//		}
//		return result;
//	}

//	public int[] sample_initNavIcons(){
//		return { 
//			R.drawable.topbar_home,
//			R.drawable.topbar_user, 
//			R.drawable.topbar_shoppingcart,
//			R.drawable.topbar_note };
//	}
	
	
} //end of class

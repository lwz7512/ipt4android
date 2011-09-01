package com.pintu.activity;

import java.util.HashMap;
import java.util.Map;

import com.pintu.R;
import com.pintu.adapter.HeadSwitchAdapter;
import com.pintu.adapter.SubMainCallBack;

import android.app.Activity;
import android.app.ActivityGroup;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public abstract class HeadSwitchActivity extends ActivityGroup {

	private GridView gvTopBar;
	private HeadSwitchAdapter topImgAdapter;
	// 装载sub Activity的容器
	private FrameLayout container;
	//当前活动
	private SubMainCallBack activity;
	
	private int iconSize= 36;
	private int[] iconArray;
	private int[] txtArray;
	
	private Button top_back;
	private ProgressBar 	details_prgrsBar;
	
	//存放一个数据容器，作为子活动存取临时数据的目标
	protected Map<String, Object> sharedRepository;

	//TODO, 子类必须重载这个方法来填充图标资源
	public abstract  int[] initNavIcons();
	//TODO, 子类必须重载这个方法来提供菜单文字
	public abstract  int[] initNavTxts();
	//TODO, 子类需要重载这个方法来切换视图
	public abstract Intent switchByIndex(int index);
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//先设置全屏参数
		setupFullScreen();
		//然后设置布局
		setContentView(R.layout.headswitch);
				
		top_back = (Button) findViewById(R.id.top_back);
		top_back.setOnClickListener(mGoListener);		
		details_prgrsBar = (ProgressBar) findViewById(R.id.details_prgrsBar);
		
		//生成导航栏
		setupNavBar();
		
		//初始化数据仓库
		sharedRepository = new HashMap<String, Object>();
	}
	
	private OnClickListener mGoListener = new OnClickListener() {
		public void onClick(View v) {
			finish();
		}
	};

	
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
		txtArray = initNavTxts();
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
		topImgAdapter = new HeadSwitchAdapter(this, iconArray, txtArray, iconSize,R.drawable.topbar_itemselector);
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
		//保存当前视图
		activity = getCurrentAct();
		activity.addProgress(details_prgrsBar);
	}
	
	private SubMainCallBack getCurrentAct(){		
		return (SubMainCallBack) getLocalActivityManager().getActivity("subActivity");
	}
	
	
} //end of class

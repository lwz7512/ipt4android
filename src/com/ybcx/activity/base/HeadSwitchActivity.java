package com.ybcx.activity.base;

import java.util.HashMap;
import java.util.Map;

import android.app.ActivityGroup;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.ybcx.R;
import com.ybcx.PintuApp;
import com.ybcx.adapter.HeadSwitchAdapter;

public abstract class HeadSwitchActivity extends ActivityGroup {
	
	private static final String TAG = "HeadSwitchActivity";

	private GridView gvTopBar;
	private HeadSwitchAdapter topImgAdapter;
	// 装载sub Activity的容器
	private FrameLayout container;
	//当前活动
	private SubMainCallBack activity;
	
	private int[] iconArray;
	private int[] txtArray;
	
	private ImageButton top_back;
	private ProgressBar 	details_prgrsBar;
	private ImageButton comn_refresh_btn;
	
	//存放一个数据容器，作为子活动存取临时数据的目标
	protected Map<String, Object> sharedRepo;
	

	//TODO, 子类必须重载这个方法来填充图标资源
	public abstract  int[] initNavIcons();
	//TODO, 子类必须重载这个方法来提供菜单文字
	public abstract  int[] initNavTxts();
	//TODO, 子类需要重载这个方法来切换视图
	public abstract Intent switchByIndex(int index);
	
	//显示刷新按钮供子类使用
	protected void showRefreshBtn(){
		comn_refresh_btn.setVisibility(View.VISIBLE);
	}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//先设置全屏参数
		setupFullScreen();
		//然后设置布局
		setContentView(R.layout.headswitch);
				
		top_back = (ImageButton) findViewById(R.id.top_back);
		top_back.setOnClickListener(mGoListener);		
		
		comn_refresh_btn = (ImageButton) findViewById(R.id.comn_refresh_btn);
		comn_refresh_btn.setOnClickListener(refreshListener);
		
		details_prgrsBar = (ProgressBar) findViewById(R.id.details_prgrsBar);
		
		//建立底部导航栏
		setupNavBar();
		
		//初始化数据仓库
		sharedRepo = new HashMap<String, Object>();
	}
	
	public void cacheRepo(String key, Object value){
		Log.d(TAG, "Cache the: "+key);
		sharedRepo.put(key, value);
	}
	
	public Object getRepo(String key){
		return sharedRepo.get(key);
	}
	
	protected void onDestroy(){
		super.onDestroy();
		//退出时清理缓存数据
		sharedRepo.clear();
	}
	
	private OnClickListener mGoListener = new OnClickListener() {
		public void onClick(View v) {
			goWhere();
		}
	};
	
	//子类可以重载该方法以实现新的导航
	protected void goWhere(){
		finish();
	}

	private OnClickListener refreshListener = new OnClickListener() {
		public void onClick(View v) {
			activity.refresh(comn_refresh_btn);
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
		if(txtArray==null) return;
		
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
		//指定内容：图标、文字、高亮背景图
		topImgAdapter = new HeadSwitchAdapter(this, iconArray, txtArray, R.drawable.greenframe);
		// 设置菜单Adapter
		gvTopBar.setAdapter(topImgAdapter);		
				
	}
	
	private class ItemClickEvent implements OnItemClickListener {
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
			switchActivity(arg2);
		}
	}	

	/**
	 * 根据ID打开指定的Activity
	 * @param index GridView选中项的序号
	 */
	protected void switchActivity(int index){
		//FIXME, 这里不判断的话会造成FC，很奇怪这个值竟然会是3
		//由小明修正的这个错误
		//2012/12/06
		if(index>topImgAdapter.getCount()-1){
			//如果越界，就默认选中第一个
			index = 0;
		}
		
		//选中项获得高亮，将背景图设置到文字上去
		topImgAdapter.SetFocus(index);
		//必须先清除容器中所有的View
		container.removeAllViews();
		
		//获取子类给出的视图
		Intent intent =switchByIndex(index);
		if(intent==null) return;
		//只创建一个活动
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		//Activity 转为 View
		Window subActivity = getLocalActivityManager().startActivity("subActivity", intent);
		//容器添加View
		container.addView(subActivity.getDecorView(),LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		
		//保存当前视图
		activity = getCurrentAct();
		//为子活动添加查询进度条控制
		activity.addProgress(details_prgrsBar);
		
		//如果发现子活动请求刷新，就执行刷新方法
		TempletActivity subAct = (TempletActivity) activity;
		if(subAct.AUTOREFRESH){
			activity.refresh(comn_refresh_btn);
		}
	}
	
	private SubMainCallBack getCurrentAct(){		
		return (SubMainCallBack) getLocalActivityManager().getActivity("subActivity");
	}
	
	
	protected void rememberLastVisitIndex(String type, int tabIndex){
		PintuApp.mPref.edit().putInt(type, tabIndex).commit();
	}
	
	protected int getLastVisitIndex(String type){
		return PintuApp.mPref.getInt(type, 0);
	}
	
	
} //end of class

package com.pintu.activity;

import java.util.ArrayList;
import java.util.List;

import com.pintu.PintuApp;
import com.pintu.R;
import com.pintu.adapter.GalleryImageAdapter;
import com.pintu.activity.PictureEdit;
import com.pintu.data.TPicDesc;
import com.pintu.task.GenericTask;
import com.pintu.task.RetrieveGalleryTask;
import com.pintu.task.TaskAdapter;
import com.pintu.task.TaskListener;
import com.pintu.task.TaskManager;
import com.pintu.task.TaskParams;
import com.pintu.task.TaskResult;
import com.pintu.tool.SimpleImageLoader;
import com.pintu.util.DateTimeHelper;
import com.pintu.util.Preferences;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

public class HomeGallery extends FullScreenActivity {
	
	static final String TAG = "HomeGallery";
	
	private ImageButton postImg;
	
	private GridView gallery;
	private GalleryImageAdapter gridAdptr;
	
	private ImageButton refresh;
	
	private ProgressBar progressbar;
	
	private GenericTask mRetrieveTask;
	
    // Refresh data at startup if last refresh was this long ago or greater.
	// 默认5分钟后才能刷新，小于这个间隔不给取
    private static final long REFRESH_THRESHOLD = 1 * 60 * 1000;
    
    protected TaskManager taskManager = new TaskManager();

     
    @Override//Activity life cycle method
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.homegallery);
		//获得组件引用
		getViews();
		//添加事件监听
		addEventListeners();
		//读取数据库数据
		retrieveGalleryFromDB();
	}
    	
    @Override//Activity life cycle method
    protected void onDestroy() {
        Log.d(TAG, "onDestroy.");
        super.onDestroy();
        taskManager.cancelAll();
    }
	
	private void getViews(){
		postImg = (ImageButton)findViewById(R.id.post_image_btn);
		
		gallery = (GridView)findViewById(R.id.ptgallery);
		//初始化画廊数据
		gridAdptr = new GalleryImageAdapter(this);
		gallery.setAdapter(gridAdptr);
		
		refresh = (ImageButton)findViewById(R.id.refresh_btn);
		progressbar = (ProgressBar)findViewById(R.id.top_refresh_progressBar);
	}
	
	private void addEventListeners(){
		
		postImg.setOnClickListener(postImgListener);
		refresh.setOnClickListener(refreshListener);
		
		//TODO, 添加点击画廊看详情的动作
		
	}
	
	private OnClickListener postImgListener = new OnClickListener(){
			public void onClick(View v){
				Intent intent = new Intent();
				//启动编辑界面
				intent.setClass(HomeGallery.this, PictureEdit.class);
				startActivity(intent);
			}
	};
	
	private OnClickListener refreshListener = new OnClickListener(){
		public void onClick(View v){
			retrieveRemoteGallery();
		}
	};
	
	private void retrieveRemoteGallery(){
		long lastRefreshTime = PintuApp.mPref.getLong(Preferences.LAST_GALLERY_REFRESH_TIME, 0);
		long nowTime = DateTimeHelper.getNowTime();
		long diff = nowTime - lastRefreshTime;
		boolean shouldRetrieve;
		if (diff > REFRESH_THRESHOLD) {
			shouldRetrieve = true;
		}else{
			shouldRetrieve = false;
			this.updateProgress("Can not retrieve data currentlly, do later...");
		}
		if(shouldRetrieve){
			doRetrieve(lastRefreshTime,nowTime);
		}
		
	}
	
    public void doRetrieve(long startTime, long endTime) {
        Log.d(TAG, "Attempting retrieve gallery data...");

        if (mRetrieveTask != null
                && mRetrieveTask.getStatus() == GenericTask.Status.RUNNING) {
            return;
        } else {
            mRetrieveTask = new RetrieveGalleryTask();
            mRetrieveTask.setListener(mRetrieveTaskListener);
              
            TaskParams params = new TaskParams();
            params.put("api", PintuApp.mApi);
            params.put("startTime", startTime);
            params.put("endTime", endTime);
            mRetrieveTask.execute(params);

            // Add Task to manager
            taskManager.addTask(mRetrieveTask);
        }
    }
    
    private TaskListener mRetrieveTaskListener = new TaskAdapter() {
    	public void onPreExecute(GenericTask task){
    		progressbar.setVisibility(View.VISIBLE);
    		refresh.setVisibility(View.GONE);
    	}
    	public void onPostExecute(GenericTask task, TaskResult result) {    		
    		if(result==TaskResult.OK){
    			//保存画廊更新时间
    			Editor pref = PintuApp.mPref.edit();
    			pref.putLong(Preferences.LAST_GALLERY_REFRESH_TIME, DateTimeHelper.getNowTime());
    			//必须得提交设置，否则不生效
    			pref.commit();
    		}else if(result==TaskResult.FAILED){
    			HomeGallery.this.updateProgress("Gallery retrieve thumbnail failed!");
    		}else if(result==TaskResult.IO_ERROR){
    			HomeGallery.this.updateProgress("Gallery retrieve thumbnail IO Error!");
    		}else if(result==TaskResult.JSON_PARSE_ERROR){
    			HomeGallery.this.updateProgress("Gallery data parse Error!");
    		}
			progressbar.setVisibility(View.GONE);  
			refresh.setVisibility(View.VISIBLE);
    	}    	
    	
    	//结果拿到了，填充视图并入库
    	public void deliverRetreivedList(List<Object> results){
    		// 刷新列表
    		List<TPicDesc> items =new ArrayList<TPicDesc>();
    		for(Object o:results){
    			items.add((TPicDesc) o);
    		}
    		//先入库，更合理
    		int successNum = PintuApp.dbApi.insertThumbnails(items);
    		Log.i(TAG, ">>> inserted db record size: "+successNum);
    		
    		//刷库更新画廊，这样最简单
    		retrieveGalleryFromDB();
    	}

		@Override
		public String getName() {
			return "RetrieveGalleryTask";
		}
    	
    }; //end of mRetrieveTaskListener
    
   
    private void retrieveGalleryFromDB(){
		List<TPicDesc> items = PintuApp.dbApi.getCachedThumbnails();
		gridAdptr.refresh(items);
		Log.i(TAG, ">>> cached recode size: "+items.size());    	
    }
    
    
    
    
	private void updateProgress(String message) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}
	
	
//TODO,	------------------- option menu definition ---------------------------------

    protected static final int OPTIONS_MENU_ID_LOGOUT = 1;
    protected static final int OPTIONS_MENU_ID_PREFERENCES = 2;
    protected static final int OPTIONS_MENU_ID_ABOUT = 3;
    protected static final int OPTIONS_MENU_ID_SEARCH = 4;
    protected static final int OPTIONS_MENU_ID_REPLIES = 5;
    protected static final int OPTIONS_MENU_ID_DM = 6;
    protected static final int OPTIONS_MENU_ID_TWEETS = 7;
    protected static final int OPTIONS_MENU_ID_TOGGLE_REPLIES = 8;
    protected static final int OPTIONS_MENU_ID_FOLLOW = 9;
    protected static final int OPTIONS_MENU_ID_UNFOLLOW = 10;
    protected static final int OPTIONS_MENU_ID_IMAGE_CAPTURE = 11;
    protected static final int OPTIONS_MENU_ID_PHOTO_LIBRARY = 12;
    protected static final int OPTIONS_MENU_ID_EXIT = 13;

    /**
     * 如果增加了Option Menu常量的数量，则必须重载此方法， 以保证其他人使用常量时不产生重复
     * 
     * @return 最大的Option Menu常量
     */
    protected int getLastOptionMenuId() {
        return OPTIONS_MENU_ID_EXIT;
    }

	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuItem item;
        item = menu.add(0, OPTIONS_MENU_ID_PREFERENCES, 0, R.string.omenu_settings);
        item.setIcon(android.R.drawable.ic_menu_preferences);

        item = menu.add(0, OPTIONS_MENU_ID_LOGOUT, 0, R.string.omenu_signout);
        item.setIcon(android.R.drawable.ic_menu_revert);

        item = menu.add(0, OPTIONS_MENU_ID_ABOUT, 0, R.string.omenu_about);
        item.setIcon(android.R.drawable.ic_menu_info_details);

        item = menu.add(0, OPTIONS_MENU_ID_EXIT, 0, R.string.omenu_exit);
        item.setIcon(android.R.drawable.ic_menu_rotate);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case OPTIONS_MENU_ID_LOGOUT:
            logout();
            return true;
        case OPTIONS_MENU_ID_SEARCH:
//            onSearchRequested();
            return true;
        case OPTIONS_MENU_ID_PREFERENCES:
           
            return true;
        case OPTIONS_MENU_ID_ABOUT: 
        	//TODO, 添加关于界面
//        	Intent intent = new Intent().setClass(this, AboutActivity.class);
//        	startActivity(intent);
            return true;
        case OPTIONS_MENU_ID_EXIT:
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    
    private  void logout() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(HomeGallery.this);
    	builder.setTitle("提示");
    	builder.setMessage("确实要注销吗?");
    	builder.setPositiveButton("确定", okListener);
    	builder.setNegativeButton("取消",cancelListener);
    	
    	Dialog dialog = builder.create();
        dialog.show();
    }
    
    private final DialogInterface.OnClickListener okListener = new DialogInterface.OnClickListener(){
		@Override
		public void onClick(DialogInterface dialog, int which) {
			backToInstall();			
		}
    };
    
    private final DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener(){
		@Override
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();			
		}
    };
    
    
    private void backToInstall() {

    	PintuApp.dbApi.clearData();

        // Clear SharedPreferences
        SharedPreferences.Editor editor = PintuApp.mPref.edit();
        editor.clear();
        editor.commit();

        // 提供用户手动情况所有缓存选项
        SimpleImageLoader.clearAll();

        // TODO: cancel notifications.        

        //close ui
        finish();
    }


	

} //end of class

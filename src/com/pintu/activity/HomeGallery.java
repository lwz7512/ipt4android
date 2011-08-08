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
import com.pintu.util.DateTimeHelper;
import com.pintu.util.Preferences;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ProgressBar;

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
    private static final long REFRESH_THRESHOLD = 5 * 60 * 1000;
    
    protected TaskManager taskManager = new TaskManager();

     
    @Override//Activity life cycle method
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.homegallery);
		//获得组件引用
		getViews();
		//添加事件监听
		addEventListeners();
		//读取画廊数据
		retrieveGallery();
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
		//TODO, add other ...
	}
	
	private OnClickListener postImgListener = new OnClickListener(){
			public void onClick(View v){
				Intent intent = new Intent();
				//启动编辑界面
				intent.setClass(HomeGallery.this, PictureEdit.class);
				startActivity(intent);
			}
	};
	
	private void retrieveGallery(){
		long lastRefreshTime = PintuApp.mPref.getLong(Preferences.LAST_GALLERY_REFRESH_TIME, 0);
		long nowTime = DateTimeHelper.getNowTime();
		long diff = nowTime - lastRefreshTime;
		boolean shouldRetrieve;
		if (diff > REFRESH_THRESHOLD) {
			shouldRetrieve = true;
		}else{
			shouldRetrieve = false;
		}
		if(shouldRetrieve){
			doRetrieve();
		}
		
	}
	
    public void doRetrieve() {
        Log.d(TAG, "Attempting retrieve gallery data...");

        if (mRetrieveTask != null
                && mRetrieveTask.getStatus() == GenericTask.Status.RUNNING) {
            return;
        } else {
            mRetrieveTask = new RetrieveGalleryTask();
            mRetrieveTask.setListener(mRetrieveTaskListener);
  
            long lastRefreshTime = PintuApp.mPref.getLong(Preferences.LAST_GALLERY_REFRESH_TIME, 0);
    		long nowTime = DateTimeHelper.getNowTime();
            
            TaskParams params = new TaskParams();
            params.put("api", PintuApp.mApi);
            params.put("startTime", lastRefreshTime);
            params.put("endTime", nowTime);
            mRetrieveTask.execute(params);

            // Add Task to manager
            taskManager.addTask(mRetrieveTask);
        }
    }
    
    private TaskListener mRetrieveTaskListener = new TaskAdapter() {
    	public void onPreExecute(GenericTask task){
    		//TODO, 显示进度条
    	}
    	public void onPostExecute(GenericTask task, TaskResult result) {
    		//TODO, 根据result结果处理
    		if(result==TaskResult.OK){
    			//TODO, 隐藏进度条
    			
    			//TODO, 保存画廊更新时间
    			
    		}else if(result==TaskResult.FAILED){
    			
    		}else if(result==TaskResult.IO_ERROR){
    			
    		}
    	};
    	
    	public void deliverRetreivedList(List<Object> results){
    		// 刷新列表
    		List<TPicDesc> items =new ArrayList<TPicDesc>();
    		for(Object o:results){
    			items.add((TPicDesc) o);
    		}
    		gridAdptr.refresh(items);
    		
    		//TODO, 更新数据库
    		
    	};

		@Override
		public String getName() {
			return "RetrieveGalleryTask";
		}
    	
    };

} //end of class

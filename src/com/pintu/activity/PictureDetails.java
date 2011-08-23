package com.pintu.activity;

import java.text.ParseException;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.pintu.PintuApp;
import com.pintu.R;
import com.pintu.data.TPicDetails;
import com.pintu.task.GenericTask;
import com.pintu.task.RetrieveDetailTask;
import com.pintu.task.TaskAdapter;
import com.pintu.task.TaskListener;
import com.pintu.task.TaskManager;
import com.pintu.task.TaskParams;
import com.pintu.task.TaskResult;
import com.pintu.tool.SimpleImageLoader;
import com.pintu.util.DateTimeHelper;

public class PictureDetails extends FullScreenActivity {

	private static String TAG = "DetailPicture";
	
	// Header
	//返回按钮
	private Button top_back;
	//顶部标题
	private TextView tv_title;
	//加载进度条
	private ProgressBar details_prgrsBar;
	
	//Body
	//头像
	private ImageView profile_image;
	//贴图作者
	private TextView user_name;
	//用户资料
	private TextView user_info;
	//发布时间
	private TextView created_at;
	//更多用户资料
	private ImageButton person_more;
	
	//品图图片
	private ImageView t_picture;
	//标签，没有内容就隐藏
	private TextView tv_tags;
	//描述，没有内容就隐藏
	private TextView tv_description;
	//发布来源
	private TextView send_source;
	//故事数（品图数），点击查看列表
	private Button storynum;
	//评论数，点击查看列表
	private Button commentnum;
	
	//Footer
	//品图
	private TextView tv_taste;
	//评论
	private TextView tv_comment;
	//收藏
	private TextView tv_favorite;
	//转发到微博
	private TextView tv_forward;
	//举报
	private TextView tv_report;
		
	//获取详情任务
	private GenericTask mRetrieveTask;
	protected TaskManager taskManager = new TaskManager();
	
	//保存一个详情对象吧，方便以后下载原始图
	private TPicDetails details = null;
	
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.picdetail);
		//获得组件引用
		getViews();
		//添加事件监听
		addEventListeners();
		//获得画廊传递过来的贴图id
		shouldRetrieve();
	}
	
	protected void onDestroy(){
		super.onDestroy();
		taskManager.cancelAll();
	}
	
	private void getViews(){
		top_back = (Button) findViewById(R.id.top_back);
		tv_title = (TextView) findViewById(R.id.tv_title);
		//设置标题文字
		tv_title.setText(R.string.picdetails);
		details_prgrsBar = (ProgressBar) findViewById(R.id.details_prgrsBar);
		
		profile_image = (ImageView) findViewById(R.id.profile_image);
		user_name = (TextView) findViewById(R.id.user_name);
		user_info = (TextView) findViewById(R.id.user_info);
		created_at = (TextView) findViewById(R.id.created_at);
		person_more = (ImageButton) findViewById(R.id.person_more);
		t_picture = (ImageView) findViewById(R.id.t_picture);
		tv_tags = (TextView) findViewById(R.id.tv_tags);
		tv_description = (TextView) findViewById(R.id.tv_description);
		send_source = (TextView) findViewById(R.id.send_source);
		storynum = (Button) findViewById(R.id.storynum);
		commentnum = (Button) findViewById(R.id.commentnum);
		
		tv_taste = (TextView) findViewById(R.id.tv_taste);
		tv_comment = (TextView) findViewById(R.id.tv_comment);
		tv_favorite = (TextView) findViewById(R.id.tv_favorite);
		tv_forward = (TextView) findViewById(R.id.tv_forward);
		tv_report = (TextView) findViewById(R.id.tv_report);
	}
	
	private void addEventListeners(){
		top_back.setOnClickListener(mGoListener);
		//查看作者详情
		person_more.setOnClickListener(viewUserAction);
		//查看故事列表
		storynum.setOnClickListener(storyListAction);
		//查看评论列表
		commentnum.setOnClickListener(commentsAction);
		
		//添加品图故事
		tv_taste.setOnClickListener(toTasteActivity);
		//添加评论
		tv_comment.setOnClickListener(toCommentsActivity);
		//收藏，不跳转吧？
		tv_favorite.setOnClickListener(toFavoriteActivity);
		//转发，不跳转吧？
		tv_forward.setOnClickListener(toFowardActivity);
		//具备，跳转吗？似乎应该写点说明
		tv_report.setOnClickListener(toReportActivity);
	}
	
	private OnClickListener mGoListener = new OnClickListener() {
		public void onClick(View v) {
			finish();
		}
	};
	
	//添加品图故事
	private OnClickListener toTasteActivity = new OnClickListener(){
		@Override
		public void onClick(View v){
			Intent it = new Intent();
			//准备启动故事编辑
			it.setClass(PictureDetails.this, StoryEdit.class);
			String tpicUrl = null;
			if(details!=null){
				tpicUrl = PintuApp.mApi.composeImgUrlById(details.mobImgId);
				it.putExtra("tpicUrl", tpicUrl);
				it.putExtra("author", details.author);
				it.putExtra("pubTime", details.publishTime);	
				it.putExtra("tpId", details.id);
				//启动故事编辑
				startActivity(it);
			}else{
				updateProgress("picture is blank, waiting for it to add story...");
			}
		}
	};
	
	
	//查看故事列表
	private OnClickListener storyListAction = new OnClickListener(){
		@Override
		public void onClick(View v) {			
			String tpicUrl = null;
			Intent it = new Intent();
			//准备启动StoryList
			it.setClass(PictureDetails.this, StoryList.class);
			if(details!=null && details.id!=null){
				tpicUrl = PintuApp.mApi.composeImgUrlById(details.mobImgId);
				it.putExtra("tpicUrl", tpicUrl);
				it.putExtra("author", details.author);
				it.putExtra("pubTime", details.publishTime);	
				it.putExtra("tpId", details.id);
				//启动故事列表
				startActivity(it);				
			}else{
				updateProgress("picture is blank, waiting for it to view stories...");
			}
		}		
	};
	
	//查看评论列表
	private OnClickListener commentsAction = new OnClickListener(){
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			
		}		
	};
	
	//查看作者详情
	private OnClickListener viewUserAction = new OnClickListener(){
		@Override
		public void onClick(View v){
			//TODO, Forward to user activity...
		}
	};
	
	
	//添加评论
	private OnClickListener toCommentsActivity = new OnClickListener(){
		@Override
		public void onClick(View v){
			//TODO, Forward to user activity...
		}
	};
	
	//收藏，不跳转吧？
	private OnClickListener toFavoriteActivity = new OnClickListener(){
		@Override
		public void onClick(View v){
			//TODO, Forward to user activity...
		}
	};
	
	//转发，不跳转吧？
	private OnClickListener toFowardActivity = new OnClickListener(){
		@Override
		public void onClick(View v){
			//TODO, Forward to user activity...
		}
	};
	
	//举报，跳转吗？似乎应该写点说明
	private OnClickListener toReportActivity = new OnClickListener(){
		@Override
		public void onClick(View v){
			//TODO, Forward to user activity...
		}
	};
	
	
	
	
	private void shouldRetrieve(){
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		// Must has extras
		if (null == extras) {
			Log.e(TAG, this.getClass().getName() + " must has extras.");
			finish();
			return;
		}
		String tpId = extras.getString("tpId");
		if(tpId!=null){
			doRetrieve(tpId);
		}else{
			updateProgress("Warning, tpId is null!");
			Log.e(TAG, "Warning, tpId is null!");
		}
		
	}

    private void doRetrieve(String tpId) {
        Log.d(TAG, "Attempting retrieve gallery data...");

        if (mRetrieveTask != null
                && mRetrieveTask.getStatus() == GenericTask.Status.RUNNING) {
            return;
        } else {
            mRetrieveTask = new RetrieveDetailTask();
            mRetrieveTask.setListener(mRetrieveTaskListener);
              
            TaskParams params = new TaskParams();
            params.put("tpId", tpId);
            mRetrieveTask.execute(params);

            // Add Task to manager
            taskManager.addTask(mRetrieveTask);
        }
    }

	
    private TaskListener mRetrieveTaskListener = new TaskAdapter() {
		@Override
		public void onPreExecute(GenericTask task) {
			details_prgrsBar.setVisibility(View.VISIBLE);
		}
		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.OK) {// 成功发送
				//do nothing currently...				
			} else if (result == TaskResult.IO_ERROR) {
				updateProgress(getString(R.string.page_status_unable_to_update));
			}else if(result==TaskResult.JSON_PARSE_ERROR){
    			updateProgress("Response to json data parse Error!");
    		}
			details_prgrsBar.setVisibility(View.GONE);
		}
		//Update UI elements with TPicDetails
		public void deliveryResponseJson(JSONObject json){			
			try {
				//保存下来，跳转时要用来传参
				details = TPicDetails.parseJsonToObj(json);
				if(details!=null){
					//格式化化为XXX以前，而不是显示绝对时间
					details.publishTime = DateTimeHelper.getRelativeTimeByFormatDate(details.publishTime, PictureDetails.this);
					details.author = getShortUserName(details.author);
					updateUIwithPicDetails(details);
				}else{
					updateProgress("details is null can not update UI!");
				}
			} catch (JSONException e) {
				updateProgress("Parse json to details Error!");
				e.printStackTrace();
			}catch (ParseException e) {
				updateProgress("Parse publishTime to Date Error!");
				e.printStackTrace();
			}							 		
		}
		@Override
		public String getName() {
			return "PicDetailsTask";
		}    	
    };
    
    private String getShortUserName(String userName){
    	String showName = userName;
    	if(userName.contains("@")){
    		int atPos = userName.indexOf("@");
    		showName = userName.substring(0, atPos);
    	}
    	return showName;
    }
    
    
    private void updateUIwithPicDetails(TPicDetails details){
    	
    	String profileUrl = PintuApp.mApi.composeImgUrlByPath(details.avatarImgPath);
    	//显示头像
    	SimpleImageLoader.display(profile_image, profileUrl);
    	String tpicUrl = PintuApp.mApi.composeImgUrlById(details.mobImgId);
    	//显示品图手机图片
    	SimpleImageLoader.display(t_picture, tpicUrl);
    	
    	user_name.setText(details.author);
    	String userInfo = getText(R.string.level_zh)+"  "+details.level;
    	//等级和积分之间空4个格
    	userInfo += "    ";
    	userInfo += getText(R.string.score_zh)+"  "+details.score;
    	user_info.setText(userInfo);
    	//显示格式化后的相对时间
    	created_at.setText(details.publishTime);
    	tv_tags.setText(details.tags);
    	tv_description.setText(details.description);
    	//TODO, ADD PIC SOURCE LATER...
    	send_source.setText("AndroidClient");
    	
    	if(details.storiesNum!=null && Integer.valueOf(details.storiesNum)>0){
    		storynum.setText(details.storiesNum);    		
    	}
    	if(details.commentsNum!=null && Integer.valueOf(details.commentsNum)>0){
    		commentnum.setText(details.commentsNum);
    	}
    	
    	if(details.allowStory==0){
    		tv_taste.setEnabled(false);
    	}
    	
    }
    
	private void updateProgress(String message) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}    
    
    
	
} //end of activity

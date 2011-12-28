package com.ybcx.activity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.ybcx.R;
import com.ybcx.activity.base.TempletActivity;
import com.ybcx.adapter.GalleryImageAdapter;
import com.ybcx.data.TPicDesc;
import com.ybcx.task.RetrieveGalleryTask;
import com.ybcx.task.TaskParams;

public class TagPicGallery extends TempletActivity {

	private ImageButton top_back;
	private TextView title;
	private ProgressBar progressbar;
	
	private GridView gallery;
	private GalleryImageAdapter gridAdptr;
	
	private String currentTagId;
	
	@Override
	protected int getLayout() {
		return R.layout.tagpicgallery;
	}

	@Override
	protected void getViews() {
		top_back = (ImageButton) findViewById(R.id.top_back);		
		title = (TextView) findViewById(R.id.tv_title);				
		progressbar = (ProgressBar) findViewById(R.id.details_prgrsBar);

		gallery = (GridView)findViewById(R.id.taggallery);
		//初始化画廊数据
		gridAdptr = new GalleryImageAdapter(this);
		gallery.setAdapter(gridAdptr);
		
		Intent it = getIntent();
		Bundle extras = it.getExtras();
		if(extras==null) return;
		
		//记下要查询的标签类型
		currentTagId = extras.getString("tagId");
		//中间标题显示该类标签
		String tagName = extras.getString("tagName");
		title.setText(tagName);
	}

	@Override
	protected void addEventListeners() {
		top_back.setOnClickListener(mGoListener);
		gallery.setOnItemClickListener(cellClickListener);
	}
	
	private OnClickListener mGoListener = new OnClickListener() {
		public void onClick(View v) {
			finish();
		}
	};
	
	private OnItemClickListener cellClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			// 将截图ID取出来，送到详情活动中查询
			TPicDesc cellSelected = gridAdptr.getItem(position);
			String tpId = cellSelected.tpId;
			Intent it = new Intent();
			it.setClass(TagPicGallery.this, PictureDetails.class);			
			it.putExtra("tpId", tpId);
			//打开详情活动
			startActivity(it);
		}		
	};

	@Override
	protected void justDoIt() {
		if(currentTagId!=null){
			doRetrieve();
		}
	}


	@Override
	protected void doRetrieve() {
		if(!checkTaskStatus()) return;
		
		mRetrieveTask = new RetrieveGalleryTask();
        mRetrieveTask.setListener(mRetrieveTaskListener);
        
        TaskParams params = new TaskParams();
        params.put("tagId", currentTagId);
        params.put("pageNum", "1");
        params.put("method", "getThumbnailsByTag");
        mRetrieveTask.execute(params);
        
        this.manageTask(mRetrieveTask);
	}

	@Override
	protected void onRetrieveBegin() {
		progressbar.setVisibility(View.VISIBLE);
	}

	@Override
	protected void onRetrieveSuccess() {
		progressbar.setVisibility(View.GONE);  
	}

	@Override
	protected void onRetrieveFailure() {
		progressbar.setVisibility(View.GONE);  
		updateProgress("retrieve pics failed!");
	}

	@Override
	protected void onParseJSONResultFailue() {
		progressbar.setVisibility(View.GONE);  
		updateProgress("data parse Error!");
	}

	@Override
	protected void refreshListView(List<Object> results) {
   		//如果没有取到数据就不处理了
		if(results.size()==0){
			updateProgress("No New pictures, Try later...");
			return;
		}
		
		// 准备缓存数据
		List<TPicDesc> items =new ArrayList<TPicDesc>();
		for(Object o:results){
			items.add((TPicDesc) o);
		}
		
		//显示
		gridAdptr.refresh(items);

	}

	@Override
	protected void refreshMultView(JSONObject json) {
		//do nothing here
	}
	
	@Override
	protected void doSend() {
		//do nothing here
	}

	@Override
	protected void onSendBegin() {
		//do nothing here
	}

	@Override
	protected void onSendSuccess() {
		//do nothing here
	}

	@Override
	protected void onSendFailure() {
		//do nothing here
	}

}

package com.pintu.activity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.pintu.R;
import com.pintu.activity.base.TempletActivity;
import com.pintu.adapter.TagsAdapter;
import com.pintu.data.Tag;
import com.pintu.task.RetrieveHotTagsTask;
import com.pintu.task.TaskParams;

public class TagList extends TempletActivity {

	private ListView tag_lv;
	private TagsAdapter tagAdptr;
	
	private ImageButton top_back;
	private TextView title;
	private ProgressBar progressbar;
	
	@Override
	protected int getLayout() {
		return R.layout.taglist;
	}

	@Override
	protected void getViews() {
		
		top_back = (ImageButton) findViewById(R.id.top_back);
		
		title = (TextView) findViewById(R.id.tv_title);
		title.setText(R.string.tagtitle);
		
		progressbar = (ProgressBar) findViewById(R.id.details_prgrsBar);
		
		tagAdptr = new TagsAdapter(this);
		tag_lv = (ListView) findViewById(R.id.taglist);
		tag_lv.setAdapter(tagAdptr);
	}

	@Override
	protected void addEventListeners() {
		top_back.setOnClickListener(mGoListener);
		
		tag_lv.setOnItemClickListener(itemClickListener);
	}
	
	private OnClickListener mGoListener = new OnClickListener() {
		public void onClick(View v) {
			finish();
		}
	};
	
	private OnItemClickListener itemClickListener = new OnItemClickListener(){
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Tag selected = (Tag) tagAdptr.getItem(position);
			Intent it = new Intent();
			it.putExtra("tagName", selected.name);
			it.putExtra("tagId", selected.id);
			it.setClass(TagList.this, TagPicGallery.class);
			startActivity(it);
		}
	};

	@Override
	protected void justDoIt() {
		// 初始化结束就开始查询
		doRetrieve();
	}


	@Override
	protected void doRetrieve() {
		this.checkTaskStatus();
		this.mRetrieveTask = new RetrieveHotTagsTask();
		this.mRetrieveTask.setListener(mRetrieveTaskListener);
		this.mRetrieveTask.execute(new TaskParams());
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
		updateProgress("retrieve hot tags failed!");
	}

	@Override
	protected void onParseJSONResultFailue() {
		progressbar.setVisibility(View.GONE);  
		updateProgress("data parse Error!");
	}

	@Override
	protected void refreshListView(List<Object> results) {
		if(results.size()==0){
			updateProgress("No hot tags in system currently!");
			return;
		}
		ArrayList<Tag> tags = new ArrayList<Tag>();
		for(Object o : results){
			tags.add((Tag)o);
		}
		tagAdptr.refresh(tags);
	}

	@Override
	protected void refreshMultView(JSONObject json) {
		// do nothing here...
	}
	

	@Override
	protected void doSend() {
		// do nothing here...
	}

	@Override
	protected void onSendBegin() {
		// do nothing here...
	}

	@Override
	protected void onSendSuccess() {
		// do nothing here...
	}

	@Override
	protected void onSendFailure() {
		// do nothing here...
	}

}

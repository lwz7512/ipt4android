package com.pintu.activity;

import java.util.List;

import org.json.JSONObject;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.pintu.PintuApp;
import com.pintu.R;
import com.pintu.activity.base.SubMainCallBack;
import com.pintu.activity.base.TempletActivity;
import com.pintu.adapter.MessageAdapter;
import com.pintu.data.TMsg;
import com.pintu.task.SendTask;
import com.pintu.task.TaskParams;
import com.pintu.util.IptHelper;

/**
 * 数据处理逻辑：
 * 应用启动后，自动查询个人消息，并缓存入库
 * 如果取到的消息入库重复或者失败，则不提醒新消息
 * 进入此活动后取缓存的未读信息，并提交消息状态为已读
 * 
 * @author lwz
 *
 */
public class AndiMessage extends TempletActivity implements SubMainCallBack {

	private ListView msges_lv;
	private MessageAdapter msgAdptr;
	private View mListHeader;
	private View mListFooter;
	
	private int pageNum = 0;
	//新取回的消息
	private List<TMsg> msgs;
	
	@Override
	protected int getLayout() {
		return R.layout.admsgs;
	}

	@Override
	protected void getViews() {
		msgAdptr = new MessageAdapter(this);
		msges_lv = (ListView) findViewById(R.id.msges_lv);
		
		//先添加头部和尾部
		mListHeader = View.inflate(this, R.layout.msg_header, null);
		msges_lv.addHeaderView(mListHeader, null, true);
		mListFooter = View.inflate(this, R.layout.listview_footer, null);
		msges_lv.addFooterView(mListFooter, null, true);
		
		//最后设置适配器
		msges_lv.setAdapter(msgAdptr);
		
	}

	@Override
	protected void addEventListeners() {
		msges_lv.setOnItemClickListener(listener);

	}
	
	private OnItemClickListener listener = new OnItemClickListener(){
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			//如果点击了列表顶部按钮，转向撰写活动
			if(position==0){
				Intent it = new Intent();
				it.setClass(AndiMessage.this, MsgEdit.class);
				startActivity(it);
			}
			//点击了更多
			if(position == msges_lv.getCount()-1){
				doMore();
			}
			//点击消息回复
			if(position>0 && position<msges_lv.getCount()-1){
				int realIndex = position - 1;
				TMsg msg = (TMsg) msgAdptr.getItem(realIndex);
				Intent it = new Intent();
				it.putExtra("sender", IptHelper.getShortUserName(msg.senderName));
				it.putExtra("content", msg.content);
				it.setClass(AndiMessage.this, MsgEdit.class);
				startActivity(it);
			}
			
		}		
	};

	@Override
	protected void justDoIt() {
		//从缓存中取未读消息
		msgs = PintuApp.dbApi.getUnreadedMsgs(1);
		if(msgs.size()>0){
			msgAdptr.refresh(msgs);	
			//取消标题栏消息通知
			PintuApp.cancelNotification();
		}else{
			//展示已读信息
			doMore();
			return;
		}		
		
		//更新本地状态				
		for(TMsg msg : msgs){
			PintuApp.dbApi.updateMsgReaded(msg.id);
		}
		
		//更新远程消息状态为已读
		doSend();			
		
	}

	private void doMore(){
		pageNum ++;
		List<TMsg> msgs = PintuApp.dbApi.getMoreMsgs(pageNum);
		if(msgs.size()>0)
			msgAdptr.append(msgs);

	}

	@Override
	protected void doSend() {
		this.checkTaskStatus();
		
		mSendTask = new SendTask();
		mSendTask.setListener(mSendTaskListener);
		int mode = SendTask.TYPE_MSG_READED;
		String msgIds = readedMsgIds();
		TaskParams params = new TaskParams();
		params.put("msgIds" , msgIds);
		params.put("mode", mode);
		mSendTask.execute(params);
		
		this.manageTask(mSendTask);
	}
	
	private String readedMsgIds(){
		StringBuffer ids = new StringBuffer();
		for(TMsg msg : msgs){
			ids.append(msg.id).append(",");
		}
		return ids.toString();
	}

	@Override
	protected void onSendBegin() {
		//do nothing here... 
	}

	@Override
	protected void onSendSuccess() {
		//do nothing here... 
	}

	@Override
	protected void onSendFailure() {
		//do nothing here... 
	}

	@Override
	protected void doRetrieve() {
		//do nothing here ...
	}

	@Override
	protected void onRetrieveBegin() {
		//do nothing here ...
	}

	@Override
	protected void onRetrieveSuccess() {
		//do nothing here ...
	}

	@Override
	protected void onRetrieveFailure() {
		//do nothing here ...
	}

	@Override
	protected void onParseJSONResultFailue() {
		//do nothing here ...
	}

	@Override
	protected void refreshListView(List<Object> results) {
		//do nothing here ...
	}

	@Override
	protected void refreshMultView(JSONObject json) {
		//do nothing here ...
	}
	
	@Override
	public void addProgress(ProgressBar pb) {
		//do nothing here ...
	}

	@Override
	public void refresh(ImageButton refreshBtn) {
		//do nothing here ...
	}

	@Override
	public void putObj(String key, Object value) {
		//do nothing here ...
	}

	@Override
	public Object getObj(String key) {
		//do nothing here ...
		return null;
	}


}

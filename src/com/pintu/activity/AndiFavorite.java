package com.pintu.activity;

import java.util.List;

import org.json.JSONObject;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.pintu.R;
import com.pintu.activity.base.TempletActivity;
import com.pintu.adapter.FavoPicsAdapter;
import com.pintu.adapter.SubMainCallBack;
import com.pintu.data.TPicItem;

/**
 * 数据处理逻辑：
 * 先取缓存显示，调用刷新方法时，判断时间间隔
 * 超过1分钟了，允许重新查询
 * @author lwz
 *
 */
public class AndiFavorite extends TempletActivity implements SubMainCallBack {

	
	private ListView markpics_lv;
	private FavoPicsAdapter fpAdptr;
	
	@Override
	protected int getLayout() {
		return R.layout.adfavorite;
	}

	@Override
	protected void getViews() {
		fpAdptr = new FavoPicsAdapter(this);
		markpics_lv = (ListView) findViewById(R.id.markpics_lv);
		markpics_lv.setAdapter(fpAdptr);
	}

	@Override
	protected void addEventListeners() {
		markpics_lv.setOnItemClickListener(listener);

	}
	
	private OnItemClickListener listener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO 跳转到查看图片详情
			TPicItem selectedPic = (TPicItem) fpAdptr.getItem(position);
			String picId = selectedPic.id;
			
		}		
	};

	@Override
	protected void justDoIt() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doItLater() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doSend() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onSendBegin() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onSendSuccess() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onSendFailure() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doRetrieve() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onRetrieveBegin() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onRetrieveSuccess() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onRetrieveFailure() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onParseJSONResultFailue() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void refreshListView(List<Object> results) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void refreshMultView(JSONObject json) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void addProgress(ProgressBar pb) {
		// TODO Auto-generated method stub

	}

	@Override
	public void refresh(ImageButton refreshBtn) {
		// TODO Auto-generated method stub

	}

	@Override
	public void putObj(String key, Object value) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object getObj(String key) {
		// TODO Auto-generated method stub
		return null;
	}


}

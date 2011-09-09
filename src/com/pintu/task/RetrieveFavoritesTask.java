package com.pintu.task;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;

import com.pintu.PintuApp;
import com.pintu.api.PTApi;
import com.pintu.data.TPicItem;
import com.pintu.http.HttpException;

public class RetrieveFavoritesTask extends GenericTask {

	private static String TAG = "RetrieveFavoritesTask";
	
	private List<Object> retrievedPics;
	
	@Override
	protected TaskResult _doInBackground(TaskParams... params) {
		JSONArray jsPics = null;
		TaskParams param = params[0];
		String userId = param.get("userId").toString();
		String pageNum = param.get("pageNum").toString();
		//根据查询方法不同，供不同的活动使用
		String method = param.get("method").toString();
		
		try {
			if(method.equals(PTApi.GETFAVORITEPICS)){
				jsPics = PintuApp.mApi.getFavoriteTpics(userId, pageNum);
			}else if(method.equals(PTApi.GETTPICSBYUSER)){
				jsPics = PintuApp.mApi.getTpicsByUser(userId, pageNum);				
			}
		} catch (HttpException e) {
			e.printStackTrace();
			return TaskResult.FAILED;
		} catch (JSONException e) {
			e.printStackTrace();
			return TaskResult.JSON_PARSE_ERROR;
		}
		
		if(jsPics!=null){
			jsonPicToItems(jsPics);			
		}else{
			return TaskResult.FAILED;
		}

		return TaskResult.OK;
	}

	private void jsonPicToItems(JSONArray jsPics) {
		retrievedPics = new ArrayList<Object>();
		try {
			for (int i = 0; i < jsPics.length(); i++) {
				TPicItem tpic = TPicItem.parseJsonToObj(jsPics
						.getJSONObject(i));
				retrievedPics.add(tpic);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	
	@Override
	protected void _onPostExecute(TaskResult result) {
    	if(result==TaskResult.OK){
    		if(this.getListener()!=null && retrievedPics!=null){
    			//回调监听方法传结果
    			this.getListener().deliverRetrievedList(retrievedPics);
    		}else{
    			//listener is null or retrieved pics is null!
    			Log.d(TAG, "listener is null or retrieved hotpics is null!");
    		}
    	}else{
    		//ERROR!
    		Log.d(TAG, "Fetching  data ERROR!");
    	}
	}

}

package com.pintu.task;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.pintu.PintuApp;
import com.pintu.data.TPicDetails;
import com.pintu.data.Tag;
import com.pintu.http.HttpException;

public class RetrieveHotTagsTask extends GenericTask {

private static String TAG = "RetrieveHotTagsTask";	
	
	private List<Object> retrievedHottags;
	
	@Override
	protected TaskResult _doInBackground(TaskParams... params) {
		JSONArray jsTags = null;
		try {
			jsTags = PintuApp.mApi.getHotTags();			
		} catch (HttpException e) {
			e.printStackTrace();
			return TaskResult.FAILED;
		} catch (JSONException e) {
			e.printStackTrace();
			return TaskResult.JSON_PARSE_ERROR;
		}

		if(jsTags!=null){
			jsonToTag(jsTags);			
		}else{
			return TaskResult.FAILED;
		}

		return TaskResult.OK;
	}
	
	private void jsonToTag(JSONArray jsTags) {
		retrievedHottags = new ArrayList<Object>();
		try {
			for (int i = 0; i < jsTags.length(); i++) {
				JSONObject jsTag = jsTags.getJSONObject(i);
				retrievedHottags.add(Tag.parseJsonToObj(jsTag));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	protected void _onPostExecute(TaskResult result) {
	   	if(result==TaskResult.OK){
    		if(this.getListener()!=null && retrievedHottags!=null){
    			//回调监听方法传结果
    			this.getListener().deliverRetrievedList(retrievedHottags);
    		}else{
    			//listener is null or retrieved tags is null!
    			Log.d(TAG, "listener is null or retrieved hottags is null!");
    		}
    	}else{
    		//ERROR!
    		Log.d(TAG, "Fetching  data ERROR!");
    	}
	}

}

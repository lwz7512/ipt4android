package com.ybcx.task;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.ybcx.PintuApp;
import com.ybcx.data.UserInfo;
import com.ybcx.http.HttpException;

public class RetrieveDarenTask extends GenericTask {

	private static final String TAG = "RetrieveDarenTask";
	public static final String PIC_DAREN = "picdaren";
	public static final String CMNT_DAREN = "cmntdaren";
	
	private List<Object> users;
	
	@Override
	protected TaskResult _doInBackground(TaskParams... params) {
		TaskParams param = params[0];
		
		String darenType = param.get("type").toString();
		
		JSONArray jsdarens = null;
		try {
			if(darenType.equals(PIC_DAREN)){
				jsdarens = PintuApp.mApi.getPicDaren();
			}else if(darenType.equals(CMNT_DAREN)){
				jsdarens = PintuApp.mApi.getCmntDaren();	
			}
		} catch (HttpException e) {
			e.printStackTrace();
			return TaskResult.FAILED;
		} catch (JSONException e) {
			e.printStackTrace();
			return TaskResult.JSON_PARSE_ERROR;
		}
		
		if(jsdarens!=null){
			jsonToUserInfo(jsdarens);			
		}else{
			return TaskResult.FAILED;
		}

		return TaskResult.OK;

	}
	
	private void jsonToUserInfo(JSONArray jsdarens){
		users = new ArrayList<Object>();
		try {
			for (int i = 0; i < jsdarens.length(); i++) {
				JSONObject jsobj = jsdarens.getJSONObject(i);
				UserInfo user = UserInfo.parseJsonToObj(jsobj);
				users.add(user);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	protected void _onPostExecute(TaskResult result) {
    	if(result==TaskResult.OK){
    		if(this.getListener()!=null && users!=null){
    			//回调监听方法传结果
    			this.getListener().deliverRetrievedList(users);
    		}else{
    			//listener is null or retrieved pics is null!
    			Log.d(TAG, "listener is null or retrieved users is null!");
    		}
    	}else{
    		//ERROR!
    		Log.d(TAG, "Fetching  data ERROR!");
    	}
	}
	

}

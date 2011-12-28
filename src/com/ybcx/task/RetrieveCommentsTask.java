package com.ybcx.task;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;

import com.ybcx.PintuApp;
import com.ybcx.data.CommentInfo;
import com.ybcx.data.StoryInfo;
import com.ybcx.http.HttpException;

public class RetrieveCommentsTask extends GenericTask {

	private static String TAG = "RetrieveCommentsTask";
	
    //存放整理好的评论
    private List<Object> retrievedComments;

	
	@Override
	protected TaskResult _doInBackground(TaskParams... params) {
    	TaskParams param = params[0];
        //根据参数查询缩略图
    	String tpId = param.get("tpId").toString();
    	JSONArray jsComments = null;
		try {
			jsComments = PintuApp.mApi.getCommensByTpId(tpId);
		} catch (HttpException e) {
			e.printStackTrace();
			return TaskResult.FAILED;
		} catch (JSONException e) {
			e.printStackTrace();
			return TaskResult.JSON_PARSE_ERROR;
		}
		
		if(jsComments!=null){
			jsonCommentToObjs(jsComments);
		}else{
			return TaskResult.FAILED;
		}		
    	
		return TaskResult.OK;

	}
	
	private void jsonCommentToObjs(JSONArray jsComments){
		retrievedComments = new ArrayList<Object>();
		try{
			for(int i=0;i<jsComments.length();i++){
				CommentInfo ci = CommentInfo.parseJsonToObj(jsComments.getJSONObject(i));				
				retrievedComments.add(ci);
			}			
		}catch(JSONException e){
			e.printStackTrace();
			Log.w(TAG, ">>> json Array getJSONObject Exception!");
		}		
	}

	@Override
	protected void _onPostExecute(TaskResult result) {
    	if(result==TaskResult.OK){
    		if(this.getListener()!=null && retrievedComments!=null){
    			//回调监听方法传结果
    			this.getListener().deliverRetrievedList(retrievedComments);
    		}else{
    			//listener is null or retrieved pics is null!
    			Log.d(TAG, "listener is null or retrieved stories is null!");
    		}
    	}else{
    		//ERROR!
    		Log.d(TAG, "Fetching  data ERROR!");
    	}
	}

}

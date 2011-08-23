package com.pintu.task;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;

import com.pintu.PintuApp;
import com.pintu.data.StoryInfo;
import com.pintu.http.HttpException;

public class RetrieveStoriesTask extends GenericTask {

	static final String TAG = "RetrieveStoriesTask";
	
    //存放取回的数据
    private List<Object> retrievedStories;

	
	@Override
	protected TaskResult _doInBackground(TaskParams... params) {
    	TaskParams param = params[0];
        //根据参数查询缩略图
    	String tpId = param.get("tpId").toString();
    	JSONArray jsStories = null;
		try {
			jsStories = PintuApp.mApi.getStoriesByTpId(tpId);
		} catch (HttpException e) {
			e.printStackTrace();
			return TaskResult.FAILED;
		} catch (JSONException e) {
			e.printStackTrace();
			return TaskResult.JSON_PARSE_ERROR;
		}
		
		if(jsStories!=null){
			jsonStroyToObjs(jsStories);
		}else{
			return TaskResult.FAILED;
		}		
    	
		return TaskResult.OK;
	}
	
	private void jsonStroyToObjs(JSONArray jsStories){
		retrievedStories = new ArrayList<Object>();
		try{
			for(int i=0;i<jsStories.length();i++){
				StoryInfo si = new StoryInfo();
				si.author = jsStories.getJSONObject(i).getString("author");
				si.content = jsStories.getJSONObject(i).getString("content");
				si.classical = jsStories.getJSONObject(i).getInt("classical");
				si.egg = jsStories.getJSONObject(i).getInt("egg");
				si.flower = jsStories.getJSONObject(i).getInt("flower");
				si.heart = jsStories.getJSONObject(i).getInt("heart");
				si.star = jsStories.getJSONObject(i).getInt("star");
				si.follow = jsStories.getJSONObject(i).getString("follow");
				si.id = jsStories.getJSONObject(i).getString("id");
				si.owner =  jsStories.getJSONObject(i).getString("owner");
				si.publishTime =  jsStories.getJSONObject(i).getString("publishTime");
				retrievedStories.add(si);
			}			
		}catch(JSONException e){
			e.printStackTrace();
			Log.w(TAG, ">>> json Array getJSONObject Exception!");
		}
	}
	
    protected void onPostExecute(TaskResult result){
    	//必须继承父类动作
    	super.onPostExecute(result);
    	
    	if(result==TaskResult.OK){
    		if(this.getListener()!=null && retrievedStories!=null){
    			//回调监听方法传结果
    			this.getListener().deliverRetrievedList(retrievedStories);
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

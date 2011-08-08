package com.pintu.task;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.pintu.api.PTApi;
import com.pintu.data.TPicDesc;

import android.util.Log;


public class RetrieveGalleryTask extends GenericTask {
	static final String TAG = "RetrieveGalleryTask";
    private String _errorMsg;
    //存放取回的数据
    private List<Object> retrievedPics;

    public String getErrorMsg() {
        return _errorMsg;
    }

    @Override
    protected TaskResult _doInBackground(TaskParams... params) {
    	TaskParams param = params[0];
        //TODO, 根据参数查询缩略图
    	PTApi api = (PTApi) param.get("api");
    	String startTime = param.get("startTime").toString();
    	String endTime = param.get("endTime").toString();
    	//向底层发请求
    	String galleryJsonStr = api.getCommunityPicsByTime(startTime, endTime);
    	jsonToTPicDesc(galleryJsonStr);
    	
        return TaskResult.OK;
    }
    
    private void jsonToTPicDesc(String jsonStr){
    	if(jsonStr!=null){
    		try {
    			JSONArray jsObjs = new JSONArray(jsonStr);
    			retrievedPics = new ArrayList<Object>();
    			for(int i=0;i<jsObjs.length();i++){
    				TPicDesc item  = new TPicDesc();
    				item.tpId = jsObjs.getJSONObject(i).getString("tpId");
    				item.thumbnailId = jsObjs.getJSONObject(i).getString("thumbnailId");
    				item.status = jsObjs.getJSONObject(i).getString("status");
    				retrievedPics.add(item);
    			}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}else{
    		Log.w(TAG, ">>> json data is null!");
    	}
    }
    
    protected void onPostExecute(TaskResult result){
    	super.onPostExecute(result);
    	if(result==TaskResult.OK){
    		if(this.getListener()!=null && retrievedPics!=null){
    			//回调监听方法传结果
    			this.getListener().deliverRetreivedList(retrievedPics);
    		}else{
    			//listener is null or retrieved pics is null!
    			Log.d(TAG, "listener is null or retrieved pics is null!");
    		}
    	}else{
    		//ERROR!
    		Log.d(TAG, "Fetching  data ERROR!");
    	}
    }
    

}

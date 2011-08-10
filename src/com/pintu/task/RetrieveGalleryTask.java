package com.pintu.task;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.pintu.api.PTApi;
import com.pintu.data.TPicDesc;
import com.pintu.http.HttpException;

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
        //根据参数查询缩略图
    	PTApi api = (PTApi) param.get("api");
    	String startTime = param.get("startTime").toString();
    	String endTime = param.get("endTime").toString();
    	//向底层发请求
    	JSONArray jsPics = null;
		try {
			jsPics = api.getCommunityPicsByTime(startTime, endTime);
		} catch (HttpException e) {
			e.printStackTrace();
			return TaskResult.FAILED;
		}
		if(jsPics==null){
			jsonToTPicDesc(jsPics);			
		}else{
			return TaskResult.FAILED;
		}
    	
        return TaskResult.OK;
    }
    
    private void jsonToTPicDesc(JSONArray jsPics){
    	if(jsPics!=null){
    		try {
    			retrievedPics = new ArrayList<Object>();
    			for(int i=0;i<jsPics.length();i++){
    				TPicDesc item  = new TPicDesc();
    				item.tpId = jsPics.getJSONObject(i).getString("tpId");
    				item.thumbnailId = jsPics.getJSONObject(i).getString("thumbnailId");
    				item.status = jsPics.getJSONObject(i).getString("status");
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
    	//必须继承父类动作
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
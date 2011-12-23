package com.pintu.task;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.pintu.PintuApp;
import com.pintu.api.PTApi;
import com.pintu.data.TPicDesc;
import com.pintu.http.HttpException;

import android.util.Log;


public class RetrieveGalleryTask extends GenericTask {
	
	private static  String TAG = "RetrieveGalleryTask";
	
    private String _errorMsg;
    //存放取回的数据
    private List<Object> retrievedPics;

    public String getErrorMsg() {
        return _errorMsg;
    }

    @Override
    protected TaskResult _doInBackground(TaskParams... params) {
    	TaskParams param = params[0];    	        
    	//得到的缩略图数据
    	JSONArray jsPics = null;
		try {
			//从热门标签中点进去查询某类标签的图片
			if(param.has("method") && param.get("method").equals("getThumbnailsByTag")){
	    		String tagId = param.getString("tagId").toString();
	    		String pageNum = param.getString("pageNum");
	    		jsPics = PintuApp.mApi.getThumbnailsByTag(tagId, pageNum);
	    	}else{
	    		//首页画廊，根据时间参数查询最近的缩略图
	        	String startTime = param.get("startTime").toString();
	        	String endTime = param.get("endTime").toString();
	    		jsPics = PintuApp.mApi.getCommunityPicsByTime(startTime, endTime);
	    	}
		} catch (HttpException e) {
			e.printStackTrace();
			return TaskResult.FAILED;
		} catch (JSONException e) {
			e.printStackTrace();
			return TaskResult.JSON_PARSE_ERROR;
		}
		//我靠，这里必须得判断一下
		if(jsPics!=null){
			jsonToTPicDesc(jsPics);			
		}else{
			return TaskResult.FAILED;
		}
    	
        return TaskResult.OK;
    }
    
    private void jsonToTPicDesc(JSONArray jsPics){
    		try {
    			retrievedPics = new ArrayList<Object>();
    			for(int i=0;i<jsPics.length();i++){
    				TPicDesc item  = new TPicDesc();
    				item.tpId = jsPics.getJSONObject(i).getString("tpId");
    				item.thumbnailId = jsPics.getJSONObject(i).getString("thumbnailId");
    				item.status = jsPics.getJSONObject(i).getString("status");
    				item.creationTime = jsPics.getJSONObject(i).getString("creationTime");
    				//根据ID获取URL路径
    				String tbnlUrl = PintuApp.mApi.composeImgUrlById(item.thumbnailId);
    				item.url = tbnlUrl;
    				retrievedPics.add(item);
    			}
			} catch (JSONException e) {
				e.printStackTrace();
				Log.w(TAG, ">>> json Array getJSONObject Exception!");
			}
    }
    
    protected void _onPostExecute(TaskResult result){
    	
    	if(result==TaskResult.OK){
    		if(this.getListener()!=null && retrievedPics!=null){
    			//回调监听方法传结果
    			this.getListener().deliverRetrievedList(retrievedPics);
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

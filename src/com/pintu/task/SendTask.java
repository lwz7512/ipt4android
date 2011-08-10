package com.pintu.task;

import java.io.File;

import android.util.Log;

import com.pintu.api.PTApi;
import com.pintu.http.HttpException;

public class SendTask extends GenericTask {
	
	public static final int TYPE_NORMAL = 0;
	public static final int TYPE_REPLY = 1;
	public static final int TYPE_REPOST = 2;
	public static final int TYPE_PHOTO = 3;
	
	private static final String TAG = "SendTask";
	private String postResult;

	@Override
	protected TaskResult _doInBackground(TaskParams... params) {
		TaskParams param = params[0];
		try {

			PTApi api = (PTApi) param.get("api");
			int mode = param.getInt("mode");

			// Log.d(TAG, "Send Status. Mode : " + mode);

			switch (mode) {

			case TYPE_REPLY:

				break;

			case TYPE_REPOST:

				break;

			case TYPE_PHOTO:
				File mFile = (File) param.get("file");
				String tags = param.getString("tags");
				String description = param.getString("description");
				String allowStory = param.getString("allowStory");
				if (null != mFile) {
					// 发送图片
					api.postPicture(mFile, tags, description, allowStory);
				} else {
					Log.e("SendTask",
							"Cann't send status in PICTURE mode, photo is null");
				}
				break;

			case TYPE_NORMAL:

			default:
				break;
			}
		} catch (HttpException e) {
			// Log.e(TAG, e.getMessage(), e);

			return TaskResult.IO_ERROR;
		}

		return TaskResult.OK;
		
	} //end of doInBackground
	
	protected void onPostExecute(TaskResult result){
	   	//必须继承父类动作
    	super.onPostExecute(result);
 
    	if(result==TaskResult.OK){
    		if(this.getListener()!=null && postResult!=null){
    			//回调监听方法传结果
    			this.getListener().deliverResponseString(postResult);
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
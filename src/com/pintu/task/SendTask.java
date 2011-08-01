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
    
    

    @Override
    protected TaskResult _doInBackground(TaskParams... params) {
        TaskParams param = params[0];
        try {
            
        	File mFile = (File)param.get("file");
        	String status = param.getString("status");
        	PTApi api = (PTApi)param.get("api"); 
            int mode = param.getInt("mode");

//            Log.d(TAG, "Send Status. Mode : " + mode);

            // Send status in different way
            switch (mode) {

            case TYPE_REPLY:
                
                break;

            case TYPE_REPOST:
                
            	break;

            case TYPE_PHOTO:
                if (null != mFile) {                    
                    //发送图片
                	api.updateStatus(status, mFile);
                } else {
                    Log.e("SendTask", "Cann't send status in PICTURE mode, photo is null");
                }
                break;

            case TYPE_NORMAL:
            
            default:
                break;
            }
        } catch (HttpException e) {
//            Log.e(TAG, e.getMessage(), e);

            return TaskResult.IO_ERROR;
        }

        return TaskResult.OK;
    }
 
     

}
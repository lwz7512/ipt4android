package com.pintu.task;

import java.io.File;

import android.util.Log;

import com.pintu.PintuApp;
import com.pintu.api.PTApi;
import com.pintu.http.HttpException;
import com.pintu.tool.SimpleImageLoader;

public class SendTask extends GenericTask {

	public static final int TYPE_NORMAL = 0;
	public static final int TYPE_PHOTO = 1;
	public static final int TYPE_STORY = 2;
	public static final int TYPE_COMMENT = 3;
	public static final int TYPE_VOTE = 4;
	public static final int TYPE_MESSAGE = 5;
	public static final int TYPE_NOTE = 6;
	

	private static final String TAG = "SendTask";
	private String postResult;

	@Override
	protected TaskResult _doInBackground(TaskParams... params) {
		TaskParams param = params[0];
		try {

			int mode = param.getInt("mode");

			switch (mode) {

			case TYPE_PHOTO:
				File mFile = (File) param.get("file");
				String tags = param.getString("tags");
				String description = param.getString("description");
				String allowStory = param.getString("allowStory");
				if (null != mFile) {
					// 尝试无损压缩图片，以减小上传文件尺寸，
					// 图片质量90%，宽高范围：1200/800，超范围缩小3/4
					// 可以大幅减小文件尺寸
					try {
						mFile = SimpleImageLoader.compressRawImage(mFile);
					} catch (Exception e) {
						Log.e("SendTask", "Compress image file Error!");
						return TaskResult.IO_ERROR;
					}
					// 发送图片
					postResult = PintuApp.mApi.postPicture(mFile, tags,
							description, allowStory);
				} else {
					Log.e("SendTask",
							"Cann't send status in PICTURE mode, photo is null");
				}
				break;

			case TYPE_STORY:
				String story = param.getString("story");
				String sfollow = param.getString("follow");
				PintuApp.mApi.postStory(sfollow, story);

				break;

			case TYPE_COMMENT:
				String comment = param.getString("content");
				String cfollow = param.getString("follow");
				PintuApp.mApi.postComment(cfollow, comment);

				break;

			case TYPE_VOTE:
				// TODO, POST VOTE ...
				
				break;
			
			case TYPE_MESSAGE:
				//TODO, POST MESSAGE ...
				
				break;
				
			case TYPE_NOTE:
				//TODO, POST NOTE ...
				
				break;
				

			default:
				break;
			}
		} catch (HttpException e) {
			// Log.e(TAG, e.getMessage(), e);

			return TaskResult.IO_ERROR;
		}

		return TaskResult.OK;

	} // end of doInBackground

	protected void _onPostExecute(TaskResult result) {

		if (result == TaskResult.OK) {
			if (this.getListener() != null && postResult != null) {
				Log.d(TAG, ">>> call deliverResponseString: \n" + postResult);
				// 回调监听方法传结果
				this.getListener().deliverResponseString(postResult);
			} else {				
				Log.d(TAG, "listener is null or retrieved pics is null!");
			}
		} else {			
			Log.d(TAG, "Fetching  data ERROR!");
		}

	}

}
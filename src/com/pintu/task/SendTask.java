package com.pintu.task;

import java.io.File;

import android.util.Log;

import com.pintu.PintuApp;
import com.pintu.api.PTApi;
import com.pintu.http.HttpException;
import com.pintu.tool.SimpleImageLoader;

public class SendTask extends GenericTask {

	//登录
	public static final int TYPE_NORMAL = 0;
	public static final int TYPE_PHOTO = 1;
	public static final int TYPE_STORY = 2;
	public static final int TYPE_COMMENT = 3;
	public static final int TYPE_VOTE = 4;
	//消息
	public static final int TYPE_MESSAGE = 5;
	//消息状态更新
	public static final int TYPE_MSG_READED = 6;
	
	//TODO, 帖子，这块后期考虑吧，估计得设计跟帖机制
	public static final int TYPE_NOTE = 7;
	//收藏
	public static final int TYPE_MARK = 8;
	//学堂知识
	public static final int TYPE_XUETANG = 9;
	

	private static final String TAG = "SendTask";
	private String postResult;

	@Override
	protected TaskResult _doInBackground(TaskParams... params) {
		TaskParams param = params[0];
		try {

			int mode = param.getInt("mode");

			switch (mode) {
			
			case TYPE_NORMAL:
				//登录验证
				String account = param.getString("account");
				String password = param.getString("password");
				postResult = PintuApp.mApi.logon(account, password);

			case TYPE_PHOTO:
				//发送贴图
				sendPic(param);
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
				//故事作者，方便通知他有人投票了
				String receiver = param.getString("receiver");
				//故事编号
				String follow = param.getString("follow");
				String type = param.getString("type");
				String amount = param.getString("amount");
				PintuApp.mApi.postVote(receiver, follow, type, amount);
				
				break;
				
			case TYPE_MARK:
				String picId = param.getString("picId");
				String userId = param.getString("userId");
				PintuApp.mApi.markThePic(userId, picId);
				break;
			
			case TYPE_MESSAGE:
				String msgSender = param.getString("userId");
				String msgReceiver = param.getString("receiver");
				String msgContent = param.getString("content");
				PintuApp.mApi.postMessage(msgSender, msgReceiver, msgContent);
				
				break;
				
			case TYPE_MSG_READED://更新消息为已读
				String msgIds = param.getString("msgIds");
				PintuApp.mApi.updateMsgReaded(msgIds);
				
				break;
				
			case TYPE_NOTE:
				//TODO, POST NOTE ...
				
				break;
				
			case TYPE_XUETANG:
				//TODO, XUTANG ...
				
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
	
	/**
	 * 	尝试无损压缩图片，以减小上传文件尺寸，
	 *  图片质量90%，宽高范围：1200/800，超范围缩小3/4
	 *  可以大幅减小文件尺寸
	 * @param param
	 * @return
	 */
	private TaskResult sendPic(TaskParams param){
		File mFile = (File) param.get("file");
		if (null != mFile) {
			try {
				String tags = param.get("tags").toString();
				String description = param.get("description").toString();
				String allowStory = param.get("isOriginal").toString();
				mFile = SimpleImageLoader.compressRawImage(mFile);
				postResult = PintuApp.mApi.postPicture(mFile, tags,description, allowStory);
			} catch (HttpException e) {				
				e.printStackTrace();
				return TaskResult.FAILED;
			} catch (Exception e) {
				Log.e("SendTask", "Compress image file Error!");
				return TaskResult.IO_ERROR;				
			}
		} else {
			Log.e("SendTask",
					"Cann't send status in PICTURE mode, photo is null");
		}
		
		return TaskResult.OK;
	}

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
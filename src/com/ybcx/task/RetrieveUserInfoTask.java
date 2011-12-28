package com.ybcx.task;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.ybcx.PintuApp;
import com.ybcx.http.HttpException;

public class RetrieveUserInfoTask extends GenericTask {

	private static String TAG = "RetrieveUserInfoTask";

	private JSONObject usrInfo;

	@Override
	protected TaskResult _doInBackground(TaskParams... params) {
		TaskParams param = params[0];
		// 根据用户查资产
		String userId = param.get("userId").toString();
		try {
			usrInfo = PintuApp.mApi.getUserDetail(userId);
		} catch (HttpException e) {
			e.printStackTrace();
			return TaskResult.FAILED;
		} catch (JSONException e) {
			e.printStackTrace();
			return TaskResult.JSON_PARSE_ERROR;
		}
		return TaskResult.OK;
	}

	@Override
	protected void _onPostExecute(TaskResult result) {
		if (result == TaskResult.OK) {
			if (this.getListener() != null && usrInfo != null) {
				// 回调监听方法传结果
				this.getListener().deliveryResponseJson(usrInfo);
			} else {
				// listener is null or retrieved pics is null!
				Log.d(TAG, "listener is null or user details  is null!");
			}
		} else {
			// ERROR!
			Log.d(TAG, "Fetching  data ERROR!");
		}
	}

}

package com.pintu.api;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.pintu.http.HttpException;
import com.pintu.http.Response;
import com.pintu.http.SimpleHttpClient;
import com.pintu.util.UTF8Formater;

public class PTImpl implements PTApi {

	private static String TAG = "PTImpl";

	private SimpleHttpClient client;

	// localhost ip used by emulator!
	private String host = "http://10.0.2.2:8080";
	// local test servlet
	// private String service = "/ipintu/upload";

	// WIFI IP used by mobile phone!
	// private String host = "http://10.127.0.11:8080";
	// Real service context
	private String service = "/ipintu/pintuapi";

	// TODO, remote host IP used in product environment

	public PTImpl() {
		client = new SimpleHttpClient();
	}

	private String getBaseURL() {
		return host + service;
	}

	@Override
	public Response getImgByUrl(String url) throws HttpException {
		// FIXME, 这个地方犯了个错误花了3个小时多才发现：
		// url参数没传到get中，get里面用的是getBaseURL()
		// 低级错误啊！痛心啊！
		// lwz7512 @ 2011/08/12
		return client.get(url, false);
	}

	@Override
	public String composeImgUrlById(String imgId) {
		return getBaseURL() + "?method=" + PTApi.GETIMAGEFILE + "&tpId="
				+ imgId;
	}

	@Override
	public String composeImgUrlByPath(String imgPath) {
		return getBaseURL() + "?method=" + PTApi.GETIMAGEBYPATH + "&path="
				+ imgPath;
	}

	@Override
	public String postPicture(File pic, String tags, String desc,
			String allowStory) throws HttpException {

		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();

		BasicNameValuePair methodParam = new BasicNameValuePair("method",
				PTApi.UPLOADPICTURE);
		tags = UTF8Formater.changeToUnicode(tags);
		BasicNameValuePair tagsParam = new BasicNameValuePair("tags", tags);
		desc = UTF8Formater.changeToUnicode(desc);
		BasicNameValuePair descParam = new BasicNameValuePair("description",
				desc);
		BasicNameValuePair storyableParam = new BasicNameValuePair(
				"allowStory", allowStory);

		params.add(methodParam);
		params.add(tagsParam);
		params.add(descParam);
		params.add(storyableParam);

		Response resp = client.post(getBaseURL(), params, pic, false);

		return resp.asString();

	}

	@Override
	public JSONArray getCommunityPicsByTime(String startTime, String endTime)
			throws HttpException, JSONException {

		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();

		BasicNameValuePair methodParam = new BasicNameValuePair("method",
				PTApi.GETGALLERYBYTIME);
		BasicNameValuePair startTimeParam = new BasicNameValuePair("startTime",
				startTime);
		BasicNameValuePair endTimeParam = new BasicNameValuePair("endTime",
				endTime);

		params.add(methodParam);
		params.add(startTimeParam);
		params.add(endTimeParam);
		// 所有的请求，除了下载下载图片文件，都采用post的方式提交
		Response resp = client.post(getBaseURL(), params, null, false);
		String jsonStr = resp.asString();
		Log.d(TAG, ">>> json Gallery: " + jsonStr);
		return new JSONArray(jsonStr);
	}

	@Override
	public JSONObject getPictureDetailsById(String tpId) throws HttpException,
			JSONException {
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();

		BasicNameValuePair methodParam = new BasicNameValuePair("method",
				PTApi.GETPICDETAIL);
		BasicNameValuePair tpIdParam = new BasicNameValuePair("tpId", tpId);

		params.add(methodParam);
		params.add(tpIdParam);

		Response resp = client.post(getBaseURL(), params, null, false);
		String jsonStr = resp.asString();
		Log.d(TAG, ">>> json Pic Details: " + jsonStr);

		return new JSONObject(jsonStr);
	}

	@Override
	public String postStory(String follow, String story) throws HttpException {
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();

		BasicNameValuePair methodParam = new BasicNameValuePair("method",
				PTApi.ADDSTORY);
		BasicNameValuePair tpIdParam = new BasicNameValuePair("follow", follow);
		//中文编码下，解决乱码问题
		story = UTF8Formater.changeToUnicode(story);
		BasicNameValuePair contentParam = new BasicNameValuePair("content", story);

		params.add(methodParam);
		params.add(tpIdParam);
		params.add(contentParam);
		
		Response resp = client.post(getBaseURL(), params, null, false);

		return resp.asString();
	}

} // end of class

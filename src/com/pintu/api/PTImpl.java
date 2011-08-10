package com.pintu.api;

import java.io.File;
import java.util.ArrayList;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;

import com.pintu.http.HttpException;
import com.pintu.http.Response;
import com.pintu.http.SimpleHttpClient;
import com.pintu.util.UTF8Formater;

public class PTImpl implements PTApi {

	private SimpleHttpClient client;

	// localhost ip used by emulator!
	 private String host = "http://10.0.2.2:8080";
	 // local test servlet
	 private String service = "/ipintu/upload";

	// WIFI IP used by mobile phone!
//	private String host = "http://10.127.0.11:8080";
	 // Real service context
//	private String service = "/ipintu/pintuapi";

	//TODO,  remote host IP used in product environment



	public PTImpl() {
		client = new SimpleHttpClient();
	}

	private String getBaseURL() {
		return host + service;
	}

	@Override
	public Response getImgByUrl(String url) {
		return client.get(getBaseURL(), null, false);
	}

	@Override
	public String composeImgUrl(String imgId) {
		return getBaseURL()+"?method="+PTApi.GETIMAGEFILE+"&picId="+imgId;
	}

	@Override
	public String postPicture(File pic, String tags, String desc, String allowStory) {

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
		
		try {
			return resp.asString();
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;		
	}

	@Override
	public JSONArray getCommunityPicsByTime(String startTime, String endTime) throws HttpException {
		
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		
		BasicNameValuePair methodParam = new BasicNameValuePair("method",PTApi.GETGALLERYBYTIME);
		BasicNameValuePair startTimeParam = new BasicNameValuePair("startTime",startTime);
		BasicNameValuePair endTimeParam = new BasicNameValuePair("endTime",endTime);
		
		params.add(methodParam);
		params.add(startTimeParam);
		params.add(endTimeParam);
				
		Response resp =  client.get(getBaseURL(), params, false);
		return resp.asJSONArray();
	}

} // end of class

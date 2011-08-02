package com.pintu.api;

import java.io.File;
import java.util.ArrayList;

import org.apache.http.message.BasicNameValuePair;

import com.pintu.http.Response;
import com.pintu.http.SimpleHttpClient;

public class PTImpl implements PTApi {
	
	private SimpleHttpClient client;
	
	//localhost ip used by emulator!
	private String host = "http://10.0.2.2:8080";
	
	//WIFI IP used by mobile phone!
//	private String host = "http://10.127.0.8:8080";
	
	//remote host IP used in product environment
	
	//Real service context
//	private String service = "/ipintu/pintuapi";
	
	//local test servlet
	private String service = "/ipintu/upload";
	
	
	
	public PTImpl(){
		client = new SimpleHttpClient();
	}
	
	private String getBaseURL(){
		return host+service;
	}

	@Override
	public Response getImgByUrl(String url) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String composeImgUrl(String type, String imgId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void postPicture(File pic, String tags,String desc, String allowStory) {
		
		ArrayList<BasicNameValuePair> params= new ArrayList<BasicNameValuePair>();
		
		BasicNameValuePair methodParam = new BasicNameValuePair("method",PTApi.UPLOADPICTURE);
		BasicNameValuePair tagsParam = new BasicNameValuePair("tags",tags);
		BasicNameValuePair descParam = new BasicNameValuePair("description",desc);
		BasicNameValuePair storyableParam = new BasicNameValuePair("allowStory",allowStory);
		
		params.add(methodParam);
		params.add(tagsParam);
		params.add(descParam);
		params.add(storyableParam);
		
		client.post(getBaseURL(), params, pic, false);
	}

}

package com.ybcx.data;

import org.json.JSONException;
import org.json.JSONObject;

public class CommentInfo {

	public String id;
	//评论目标图ID
	public String follow;
	//评论用户名
	public String author;
	//评论用户ID
	public String owner;
	//评论内容
	public String content;
	//评论发布时间
	public String publishTime;
	
	public static  CommentInfo parseJsonToObj(JSONObject json) throws JSONException{
		CommentInfo ci = new CommentInfo();
		ci.author = json.getString("author");
		ci.content = json.getString("content");
		ci.follow = json.getString("follow");
		ci.id = json.getString("id");
		ci.owner = json.getString("owner");
		ci.publishTime = json.getString("publishTime");
		return ci;
	}
	
}

package com.pintu.data;

import org.json.JSONException;
import org.json.JSONObject;

public class StoryInfo {

	// 品图（故事）唯一标识
	public String id;
	// 贴图ID，对应于TPicItem.id
	public String follow;
	// 品图作者ID
	public String owner;
	//品图作者名称
	public String author;
	// 发表时间
	public String publishTime;
	// 品图内容
	public String content;
	// 是否被投票为经典
	public int classical;
	
	//鲜花数目
	public int flower;
	//爱心数目
	public int heart;
	//鸡蛋数目
	public int egg;
	//经典数目
	public int star;
	
	public static StoryInfo parseJsonToObj(JSONObject json) throws JSONException{
		StoryInfo si = new StoryInfo();
		si.author = json.getString("author");
		si.content = json.getString("content");
		si.classical = json.getInt("classical");
		si.egg = json.getInt("egg");
		si.flower = json.getInt("flower");
		si.heart = json.getInt("heart");
		si.star = json.getInt("star");
		si.follow = json.getString("follow");
		si.id = json.getString("id");
		si.owner =  json.getString("owner");
		si.publishTime =  json.getString("publishTime");

		return si;
	}

}

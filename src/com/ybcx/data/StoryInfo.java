package com.ybcx.data;

import org.json.JSONException;
import org.json.JSONObject;

public class StoryInfo {

	// 品图（故事）唯一标识
	public String id;
	/**
	 *  贴图ID，对应于TPicItem.id
	 */
	public String follow;
	// 品图作者ID
	public String owner;
	//品图作者名称
	public String author;
	
	//新加属性作者头像
	//2011/08/31
	public String avatarImgPath;
	
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
	
	public static StoryInfo parseJsonToObj(JSONObject json) {
		StoryInfo si = new StoryInfo();
		si.avatarImgPath = json.optString("avatarImgPath");
		si.author = json.optString("author");
		si.content = json.optString("content");
		si.classical = json.optInt("classical");
		si.egg = json.optInt("egg");
		si.flower = json.optInt("flower");
		si.heart = json.optInt("heart");
		si.star = json.optInt("star");
		si.follow = json.optString("follow");
		si.id = json.optString("id");
		si.owner =  json.optString("owner");
		si.publishTime =  json.optString("publishTime");		
		
		return si;
	}

}

package com.pintu.data;

import org.json.JSONException;
import org.json.JSONObject;

public class TPicDetails {
	
	//贴图ID
	public String id;
	//作者ID
	public String owner;
	//贴图作者
	public String author;	
	//用户头像文件路径
	public String avatarImgPath;
	//用户积分
	public String score;	
	//用户等级
	public String level;

	//到达客户端时，再格式化为xx分钟或者xx小时前
	public String publishTime;	
	
	//用于显示的相对时间
	public String relativeTime;
	
	//贴图标签
	public String tags;
	//贴图描述
	public String description;

	//是否允许品图，这个比较重要
	public int allowStory;
		
	//生成的移动图ID，这个ID是由pId+"_Mob"构成
	public String mobImgId;
	
	//生成的原始图ID，这个ID是由pId+"_Raw"构成
	public String rawImgId;
	
	//品图（故事）数目
	public String storiesNum;
	//评论数目
	public String commentsNum;
	
	
	public static TPicDetails parseJsonToObj(JSONObject json) throws JSONException{
		TPicDetails details = new TPicDetails();
		details.id = json.optString("id");
		details.owner = json.optString("owner");
		details.author = json.optString("author");
		details.avatarImgPath = json.optString("avatarImgPath");
		details.score = json.optString("score");
		details.level = json.optString("level");		
		details.publishTime = json.optString("publishTime");
		details.tags = json.optString("tags");
		details.description = json.optString("description");
		details.allowStory = json.getInt("allowStory");		
		details.mobImgId = json.optString("mobImgId");
		details.rawImgId = json.optString("rawImgId");
		details.storiesNum = json.optString("storiesNum");
		details.commentsNum = json.optString("commentsNum");
		
		return details;
	}
	

}

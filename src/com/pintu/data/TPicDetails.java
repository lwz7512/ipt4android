package com.pintu.data;

import org.json.JSONException;
import org.json.JSONObject;

public class TPicDetails {
	
	//贴图ID
	public String id;
	//贴图作者
	public String owner;	
	//用户头像文件路径
	public String avatarImgPath;
	//用户积分
	public String score;	
	//用户等级
	public String level;

	//发布时间，可以保存为毫秒数
	//到达客户端时，再格式化为xx分钟或者xx小时前
	public String publishTime;	
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
	public String storyNum;
	//评论数目
	public String commentsNum;
	
	
	public static TPicDetails parseJsonToObj(JSONObject json) throws JSONException{
		TPicDetails details = new TPicDetails();
		details.id = json.getString("id");
		details.owner = json.getString("owner");
		details.avatarImgPath = json.getString("avatarImgPath");
		details.score = json.getString("score");
		details.level = json.getString("level");		
		details.publishTime = json.getString("publishTime");
		details.tags = json.getString("tags");
		details.description = json.getString("description");
		details.allowStory = json.getInt("allowStory");		
		details.mobImgId = json.getString("mobImgId");
		details.rawImgId = json.getString("rawImgId");
		details.rawImgId = json.getString("rawImgId");
		details.storyNum = json.getString("storyNum");
		details.commentsNum = json.getString("commentsNum");
		
		return details;
	}
	

}

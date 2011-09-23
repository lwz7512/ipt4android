package com.pintu.data;

import org.json.JSONObject;

public class UserInfo {

	public String id;
	//用户名
	public String account;
	//肖像
	public String avatar;
	//角色
	public String role;
	//级别
	public String level;
	//总积分
	public int score;
	//可兑换积分
	public int exchangeScore;
	
	//贴图数
	public int tpicNum;
	//品图数
	public int storyNum;

	
	public static UserInfo parseJsonToObj(JSONObject json){
		UserInfo usr = new UserInfo();
		usr.id = json.optString("id");
		usr.account = json.optString("account");
		usr.avatar = json.optString("avatar");
		usr.exchangeScore = json.optInt("exchangeScore");
		usr.level = json.optString("level");
		usr.role = json.optString("role");
		usr.score = json.optInt("score");
		usr.tpicNum = json.optInt("tpicNum");
		usr.storyNum = json.optInt("storyNum");

		return usr;
	}

	
}

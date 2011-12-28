package com.ybcx.data;

import org.json.JSONObject;

public class UserDetails extends UserInfo {

	
	//用户资产Map 
	public int seaShell;
	
	public int copperShell;
	
	public int silverShell;
	
	public int goldShell;
	
	
	public static UserDetails parseJsonToObj(JSONObject json){
		UserDetails details = new UserDetails();
		details.id = json.optString("id");
		details.account = json.optString("account");
		details.avatar = json.optString("avatar");
		details.exchangeScore = json.optInt("exchangeScore");
		details.level = json.optString("level");
		details.role = json.optString("role");
		details.score = json.optInt("score");

		details.tpicNum = json.optInt("tpicNum");
		details.storyNum = json.optInt("storyNum");
		
		details.seaShell = json.optInt("seaShell");
		details.copperShell = json.optInt("copperShell");
		details.silverShell = json.optInt("silverShell");
		details.goldShell = json.optInt("goldShell");
		
		return details;
	}

}

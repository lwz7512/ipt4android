package com.pintu.data;

import org.json.JSONObject;

public class TPicItem {

	// 贴图ID
	public String id;
	//作者ID
	public String owner;
	// 格式化后的时间，展示时重新格式化为相对时间
	public String publishTime;
	// 生成的移动图ID，这个ID是由pId+"_Mob"构成
	public String mobImgId;

	public static TPicItem parseJsonToObj(JSONObject json){
		TPicItem pic = new TPicItem();
		pic.id = json.optString("id");
		pic.owner = json.optString("owner");
		pic.publishTime = json.optString("publishTime");
		pic.mobImgId = json.optString("mobImgId");
		
		return pic;
	}
	
}

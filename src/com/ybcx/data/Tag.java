package com.ybcx.data;

import org.json.JSONException;
import org.json.JSONObject;

public class Tag {

	public String id;
	
	public String name;
	
	public int browseCount;
	
	public String type="customized";
	
	public static  Tag parseJsonToObj(JSONObject json) throws JSONException{
		Tag tag = new Tag();
		tag.id = json.optString("id");
		tag.name = json.optString("name");
		tag.browseCount = json.optInt("browseCount");
		
		return tag;
	}
}

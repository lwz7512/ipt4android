package com.ybcx.data;

import org.json.JSONObject;

public class TMsg {

	public String id;
	public String sender;
	public String senderName;
	public String senderAvatar;
	
	public String receiver;
	public String receiverName;
	//消息内容
	public String content;
	
	//是否已读： '1'已读，'0'未读
	public String readed;
	//标准时间格式：年-月-日 时:分:秒
	public String writeTime;
	
	public static TMsg parseJsonToObj(JSONObject json) {
		TMsg msg = new TMsg();
		msg.id = json.optString("id");
		msg.sender = json.optString("sender");
		msg.senderName = json.optString("senderName");
		msg.senderAvatar = json.optString("senderAvatar");
		msg.receiver = json.optString("receiver");
		msg.receiverName = json.optString("receiverName");
		msg.content = json.optString("content");
		msg.readed = String.valueOf(json.optInt("readed"));
		msg.writeTime = json.optString("writeTime");
		
		return msg;
	}
	
}

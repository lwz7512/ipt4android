package com.pintu.data;

public class TPicDesc {

	//贴图ID
	public String tpId;
	//缩略图ID，贴图ID加_Thumbnail
	public String thumbnailId;
	
	//图的状态，热图，经典等等
	//0: 默认状态
	//1: 有故事状态
	//2: 热图状态
	//3: 经典状态
	public String status;
	//URL 地址，用作图片缓存索引
	public String url;
	//创建时间，毫秒数
	public String creationTime;
	
}

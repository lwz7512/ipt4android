package com.pintu.api;

import java.io.File;

import com.pintu.http.Response;

public interface PTApi {
	
	//注意： 这里的方法名静态变量要跟ipintu中的ExtVisitorInterface定义一致
	public static final String UPLOADPICTURE = "upload";
	
	public static final String APPLYFORUSER = "applyForUser";
	
	public static final String OTHERMETHOD = "otherMethod";
	
	public static final String THUMBNAIL_PIC = "thumbnailPicture";
	
	public static final String GETGALLERYBYTIME = "getGalleryByTime";

	//贴一张图
	public void postPicture(File pic, String tags,String desc, String allowStory);
	//根据图片类型和编号合成URL地址
	public String composeImgUrl(String type, String imgId);
	//获取一个图片响应，图形包含在响应流中
	public Response getImgByUrl(String url);
	//获取指定时间的画廊数据
	public String getCommunityPicsByTime(String startTime, String endTime);
	
}

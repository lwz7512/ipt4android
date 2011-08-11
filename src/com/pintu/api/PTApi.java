package com.pintu.api;

import java.io.File;

import org.json.JSONArray;

import com.pintu.http.HttpException;
import com.pintu.http.Response;

public interface PTApi {
	
	//注意： 这里的方法名静态变量要跟ipintu中的ExtVisitorInterface定义一致
	public static final String UPLOADPICTURE = "upload";
	
	public static final String APPLYFORUSER = "applyForUser";
	
	public static final String OTHERMETHOD = "otherMethod";
		
	public static final String GETGALLERYBYTIME = "getGalleryByTime";
	
	public static final String GETIMAGEFILE = "getImageFile";

	//贴一张图
	public String postPicture(File pic, String tags,String desc, String allowStory) throws HttpException;
	//根据编号合成URL地址，图片类型可以从编号后缀解析出来
	public String composeImgUrl(String imgId);
	//获取一个图片响应，图形包含在响应流中
	public Response getImgByUrl(String url) throws HttpException;
	//获取指定时间的画廊数据
	public JSONArray getCommunityPicsByTime(String startTime, String endTime) throws HttpException;
	
}

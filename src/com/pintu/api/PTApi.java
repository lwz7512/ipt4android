package com.pintu.api;

import java.io.File;
import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.pintu.http.HttpException;
import com.pintu.http.Response;

public interface PTApi {
	
	//注意： 这里的方法名静态变量要跟ipintu中的ExtVisitorInterface定义一致
	public static final String UPLOADPICTURE = "upload";
	
	public static final String APPLYFORUSER = "applyForUser";
	
	public static final String OTHERMETHOD = "otherMethod";
		
	public static final String GETGALLERYBYTIME = "getGalleryByTime";
	
	public static final String GETIMAGEFILE = "getImageFile";
	
	public static final String GETIMAGEBYPATH = "getImageByPath";
	
	public static final String GETPICDETAIL = "getPicDetail";
	
	public static final String ADDSTORY = "addStory";
	
	public static final String ADDCOMMENT ="addComment";
	
	public static final String GETSTORIESOFPIC ="getStoriesOfPic";
	
	public static final String GETCOMMENTSOFPIC ="getCommentsOfPic";
	
	public static final String  ADDVOTE= "addVote";
	

	//贴一张图
	public String postPicture(File pic, String tags,String desc, String allowStory) throws HttpException;
	//根据编号合成URL地址，图片类型可以从编号后缀解析出来
	public String composeImgUrlById(String imgId);
	//根据文件磁盘路径获取该图片的请求url
	public String composeImgUrlByPath(String imgPath);
	//获取一个图片响应，图形包含在响应流中
	public Response getImgByUrl(String url) throws HttpException ;
	//获取指定时间的画廊数据
	public JSONArray getCommunityPicsByTime(String startTime, String endTime) throws HttpException, JSONException;
	//获得贴图详情数据
	public JSONObject getPictureDetailsById(String tpId) throws HttpException,JSONException;
	//添加故事
	public String postStory(String follow, String story) throws HttpException;
	//获取故事列表
	public JSONArray getStoriesByTpId(String tpId) throws HttpException,JSONException;
	
	
	
}

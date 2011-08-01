package com.pintu.api;

import java.io.File;

import com.pintu.http.Response;

public interface PTApi {
	
	//注意： 这里的方法名静态变量要跟ipintu中的ExtVisitorInterface定义一致
	public static final String UPLOADPICTURE = "upload";
	
	public static final String APPLYFORUSER = "applyForUser";
	
	public static final String OTHERMETHOD = "otherMethod";
	
	
	public String composeImgUrl(String type, String imgId);

	public Response getImgByUrl(String url);
	
	public void updateStatus(String status, File image);
}

package com.pintu.api;

import java.io.File;

import com.pintu.http.Response;

public interface PTApi {
	
	public String composeImgUrl(String type, String imgId);

	public Response getImgByUrl(String url);
	
	public void updateStatus(String status, File image);
}

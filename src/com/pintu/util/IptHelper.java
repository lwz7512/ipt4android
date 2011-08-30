package com.pintu.util;

public class IptHelper {

    public static String getShortUserName(String userName){
    	String showName = userName;
    	if(userName.contains("@")){
    		int atPos = userName.indexOf("@");
    		showName = userName.substring(0, atPos);
    	}
    	return showName;
    }

	
}

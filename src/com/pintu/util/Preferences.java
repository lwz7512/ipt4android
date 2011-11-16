package com.pintu.util;

/**
 * 存放偏好设置的Key
 * @author lwz
 *
 */
public class Preferences {
	
	//记录登录用户
	public static String LOGON_USERID = "userid";
	//记录客服用户
	public static String KEFU_USERID = "b8931b314c24dca4";
			
	//PintuApp中记录，注销时清0
	public static String LAST_LOGIN_TIME = "lastLoginTime";
	//画廊访问时记录
	public static String LAST_GALLERY_REFRESH_TIME = "gallerRefreshTime";
	//进入画廊时记录，多视图切换时取回作为是否取新数据的条件
	public static String LAST_VISIT_TIME = "lastVisitTime";
	//上次拍照生成文件名，方便照相模式返回活动时获取
	public static String LAST_CAPTURE_FILE = "lastCaptureFile";
	
	//当前标签页索引键，子类也要用来保存退出时的状态
	//依靠它来打开相应的标签
	
	//今日热图	
	public static final String HOT_INDEX = "myassets_index";
	//我的资产
	public static final String MYASSETS_INDEX = "myassets_index";
	//社区动态标签组
	public static final String CMNT_INDEX = "cmnt_index";
	//夜市标签组
	public static final String MARKET_INDEX = "market_index";
	
	
}

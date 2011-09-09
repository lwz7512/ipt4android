package com.pintu.util;

/**
 * 存放偏好设置的Key
 * @author lwz
 *
 */
public class Preferences {

	//PintuApp中记录，注销时清0
	public static String LAST_LOGIN_TIME = "lastLoginTime";
	//画廊访问时记录
	public static String LAST_GALLERY_REFRESH_TIME = "gallerRefreshTime";
	//进入画廊时记录，多视图切换时取回作为是否取新数据的条件
	public static String LAST_VISIT_TIME = "lastVisitTime";
	
	//当前标签页索引键，子类也要用来保存退出时的状态
	//依靠它来打开相应的标签
	public static final String MYASSETS_INDEX = "myassets_index";
	//社区动态标签组
	public static final String CMNT_INDEX = "cmnt_index";
	//夜市标签组
	public static final String MARKET_INDEX = "market_index";
	
	
}

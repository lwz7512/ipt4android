package com.ybcx.upgrade;

/**
 * 安装包更新信息配置文件
 * 
 * @author lwz
 *
 */
public class SampleUpdateXML {

	/**update.xml, deployed in: /ipintu/download/update.xml
	 * <?xml version="1.0" encoding="utf-8"?> 
		 <info>  
		    <version>2.0</version>  
		    <size>500K</size>  
		    <apkurl>http://ipintu.com/ipintu/download/PintuMain-release.apk</apkurl>  
		    <description>检测到最新版本，请及时更新！</description>  
		</info>
	 */
	
	public static final String VERSION = "version";
	public static final String SIZE = "size";
	public static final String APKURL = "apkurl";
	public static final String DESCRIPTION = "description";
	
}

package com.ybcx.upgrade;

import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;

import android.util.Xml;

public class UpdateInfoParser {

	/* 
	 * 用pull解析器解析服务器返回的xml文件 (xml封装了版本号) 
	 */  
	public static UpdateInfo getUpdataInfo(InputStream is) throws Exception{  
	    XmlPullParser  parser = Xml.newPullParser();    
	    parser.setInput(is, "utf-8");//设置解析的数据源  
	    
	    UpdateInfo info = new UpdateInfo();//实体  
	    
	    int type = parser.getEventType();  
	    while(type != XmlPullParser.END_DOCUMENT ){  
	        switch (type) {  
	        
	        case XmlPullParser.START_TAG:  
	            if(SampleUpdateXML.VERSION.equals(parser.getName())){  
	                info.setVersion(parser.nextText()); //获取版本号  
	            }else if (SampleUpdateXML.SIZE.equals(parser.getName())){  
	                info.setSize(parser.nextText()); //获取要升级文件大小  
	            }else if(SampleUpdateXML.APKURL.equals(parser.getName())){
	            	info.setApkurl(parser.nextText()); //获取该文件下载路径 
	            }else if (SampleUpdateXML.DESCRIPTION.equals(parser.getName())){  
	                info.setDescription(parser.nextText()); //获取该文件的信息  
	            }  
	            break;  
	        }  
	        type = parser.next();  
	    }  
	    return info;  
	}  
	
}

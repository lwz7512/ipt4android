package com.pintu.util;

public class UTF8Formater {

	/**
	 * 在客户端将字符编码为UTF-8格式
	 * @param str
	 * @return
	 */
	public static String changeToUnicode(String str) {
		StringBuffer strBuff = new StringBuffer();
		for (int i = 0; i < str.length(); i++) {
			String temp = Integer.toHexString(str.charAt(i));
			if (temp.length() != 4) {
				temp = "00" + temp;
			}
			if (temp.equals("00d")) {
				temp = "0" + temp;
			}
			if (temp.equals("00a")) {
				temp = "0" + temp;
			}
			strBuff.append(temp.substring(0, temp.length() - 2));
			strBuff.append(temp.substring(temp.length() - 2, temp.length()));
		}
		String returnData = strBuff.toString();
		return returnData;
	}

	/**
	 * 在服务端将获取的Unicode字符串解码成中文字
	 */
	public static String changeToWord(String str) {
		String retData = null;
		String tempStr = new String(str);
		String[] chStr = new String[str.length() / 4];
		for (int i = 0; i < str.length(); i++) {
			if (i % 4 == 3) {
				chStr[i / 4] = new String(tempStr.substring(0, 4));
				tempStr = tempStr.substring(4, tempStr.length());
			}
		}
		char[] retChar = new char[chStr.length];
		for (int i = 0; i < chStr.length; i++) {
			retChar[i] = (char) Integer.parseInt(chStr[i], 16);
		}
		retData = String.valueOf(retChar, 0, retChar.length);
		return retData;
	}

}

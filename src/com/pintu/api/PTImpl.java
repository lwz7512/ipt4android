package com.pintu.api;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.SortedMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.pintu.http.HttpException;
import com.pintu.http.Response;
import com.pintu.http.SimpleHttpClient;

/**
 * 此类为远程查询方法的实现：
 * 用于构造httpclient需要的参数并提交
 * 此类不主动获取用户，从接口方法中得到
 * 如果接口方法中没有用户，则使用httpclient中内置用户
 * 2011/09/09
 * @author lwz
 *
 */
public class PTImpl implements PTApi {

	private static String TAG = "PTImpl";

	private SimpleHttpClient client;

	// localhost ip used by emulator!
	private String emulator = "http://10.0.2.2:8080";
	
	//remote server used in product environment
//	private String host = "http://192.168.0.100:8080";
	private String host = "http://ipintu.com";
	// Real service context
	private String service = "/ipintu/pintuapi";
	
	//debug or release flag
	private boolean isDebug = true;

	public PTImpl(String userId) {
		client = new SimpleHttpClient(userId);
	}
	
	public boolean isDebugMode(){
		if(isDebug){
			return true;
		}else{
			return false;
		}
	}
	
	//登录成功后要更新记录在client中的用户
	public void updateUser(String userId){
		client.setUserId(userId);
	}

	private String getBaseURL() {
		if(isDebug){
			return emulator + service;			
		}else{
			return host + service;
		}
	}

	@Override
	public Response getImgByUrl(String url) throws HttpException {
		// FIXME, 这个地方犯了个错误花了3个小时多才发现：
		// url参数没传到get中，get里面用的是getBaseURL()
		// 低级错误啊！痛心啊！
		// lwz7512 @ 2011/08/12
		url = url+"&source=android";
		return client.get(url, false);
	}

	@Override
	public String composeImgUrlById(String imgId) {
		return getBaseURL() + "?method=" + PTApi.GETIMAGEFILE + "&tpId="
				+ imgId;
	}

	@Override
	public String composeImgUrlByPath(String imgPath) {
		return getBaseURL() + "?method=" + PTApi.GETIMAGEBYPATH + "&path="
				+ imgPath;
	}

	/**
	 * FIXME, 这里使用了独立的客户端来上传图片
	 * 而不与其他操作共用，以解决底层传输错误：
	 * java.net.SocketException: Broken pipe
	 */	
	public String postPicture(File pic, String tags, String desc, String isOriginal) throws HttpException {
		HttpClient httpclient = new DefaultHttpClient();
		HttpEntity resEntity = null;
		String response = null;
		try {
			HttpPost httppost = new HttpPost(getBaseURL());

			FileBody file = new FileBody(pic);
			StringBody methodValue = null;
			StringBody tagsValue = null;
			StringBody descriptionValue = null;
			StringBody isOriginalValue = null;
			StringBody userValue = null;
			StringBody sourceValue = null;
			try {
				methodValue = new StringBody(PTApi.UPLOADPICTURE);	
				
				//FIXME, 修正中文字符编码的问题，在这里设置下就行了
				//2011/11/27
				Charset cs = Charset.forName("UTF-8");	
				tagsValue = new StringBody(tags, cs);
				descriptionValue = new StringBody(desc, cs);
							
				isOriginalValue = new StringBody(isOriginal);
				
				userValue = new StringBody(client.getUserId());
				sourceValue = new StringBody("android");				
				
			} catch (UnsupportedEncodingException e) {
				Log.e(TAG, "param encoding error!!!");
				e.printStackTrace();
			}

			MultipartEntity reqEntity = new MultipartEntity();
			reqEntity.addPart("method", methodValue);
			reqEntity.addPart("photo", file);
			reqEntity.addPart("tags", tagsValue);
			reqEntity.addPart("description", descriptionValue);
			reqEntity.addPart("isOriginal", isOriginalValue);
			reqEntity.addPart("userId", userValue);
			reqEntity.addPart("source", sourceValue);

			httppost.setEntity(reqEntity);
		
			HttpResponse resp = httpclient.execute(httppost);
			
			resEntity = resp.getEntity();
			
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				httpclient.getConnectionManager().shutdown();
			} catch (Exception ignore) {
			}
		}
		
		if (resEntity != null) {
			response = resEntity.toString();
		}

		return response;
	}

	@Override
	public JSONArray getCommunityPicsByTime(String startTime, String endTime)
			throws HttpException, JSONException {

		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		BasicNameValuePair methodParam = new BasicNameValuePair("method",
				PTApi.GETGALLERYBYTIME);
		BasicNameValuePair startTimeParam = new BasicNameValuePair("startTime",
				startTime);
		BasicNameValuePair endTimeParam = new BasicNameValuePair("endTime",
				endTime);
		params.add(methodParam);
		params.add(startTimeParam);
		params.add(endTimeParam);
		// 所有的请求，除了下载下载图片文件，都采用post的方式提交
		Response resp = client.post(getBaseURL(), params, null, false);
		String jsonStr = resp.asString();
		Log.d(TAG, ">>> json Gallery: " + jsonStr);
		return new JSONArray(jsonStr);
	}

	@Override
	public JSONObject getPictureDetailsById(String tpId) throws HttpException,
			JSONException {
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		BasicNameValuePair methodParam = new BasicNameValuePair("method",
				PTApi.GETPICDETAIL);
		BasicNameValuePair tpIdParam = new BasicNameValuePair("tpId", tpId);
		params.add(methodParam);
		params.add(tpIdParam);

		Response resp = client.post(getBaseURL(), params, null, false);
		String jsonStr = resp.asString();
		Log.d(TAG, ">>> json Pic Details: " + jsonStr);

		return new JSONObject(jsonStr);
	}

	@Override
	public String postStory(String follow, String story) throws HttpException {
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		BasicNameValuePair methodParam = new BasicNameValuePair("method",
				PTApi.ADDSTORY);
		BasicNameValuePair tpIdParam = new BasicNameValuePair("follow", follow);		
		BasicNameValuePair contentParam = new BasicNameValuePair("content",story);
		params.add(methodParam);
		params.add(tpIdParam);
		params.add(contentParam);
		Response resp = client.post(getBaseURL(), params, null, false);

		return resp.asString();
	}

	@Override
	public JSONArray getStoriesByTpId(String tpId) throws HttpException,
			JSONException {
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		BasicNameValuePair methodParam = new BasicNameValuePair("method",
				PTApi.GETSTORIESOFPIC);
		BasicNameValuePair tpIdParam = new BasicNameValuePair("tpId", tpId);

		params.add(methodParam);
		params.add(tpIdParam);

		Response resp = client.post(getBaseURL(), params, null, false);
		String jsonStr = resp.asString();
		Log.d(TAG, ">>> json Stories: " + jsonStr);

		return new JSONArray(jsonStr);
	}

	@Override
	public String postComment(String follow, String comment)
			throws HttpException {
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		BasicNameValuePair methodParam = new BasicNameValuePair("method",
				PTApi.ADDCOMMENT);
		BasicNameValuePair tpIdParam = new BasicNameValuePair("follow", follow);		
		BasicNameValuePair contentParam = new BasicNameValuePair("content",comment);
		params.add(methodParam);
		params.add(tpIdParam);
		params.add(contentParam);

		Response resp = client.post(getBaseURL(), params, null, false);

		return resp.asString();
	}

	@Override
	public JSONArray getCommensByTpId(String tpId) throws HttpException,
			JSONException {
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		BasicNameValuePair methodParam = new BasicNameValuePair("method",
				PTApi.GETCOMMENTSOFPIC);
		BasicNameValuePair tpIdParam = new BasicNameValuePair("tpId", tpId);

		params.add(methodParam);
		params.add(tpIdParam);

		Response resp = client.post(getBaseURL(), params, null, false);
		String jsonStr = resp.asString();
		Log.d(TAG, ">>> json Stories: " + jsonStr);

		return new JSONArray(jsonStr);
	}

	@Override
	public String postVote(String receiver, String follow, String type, String amount)
			throws HttpException {
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		BasicNameValuePair methodParam = new BasicNameValuePair("method",
				PTApi.ADDVOTE);
		BasicNameValuePair recvParam = new BasicNameValuePair("receiver", receiver);
		BasicNameValuePair foloParam = new BasicNameValuePair("follow", follow);
		BasicNameValuePair typeParam = new BasicNameValuePair("type", type);
		BasicNameValuePair amtParam = new BasicNameValuePair("amount", amount);

		params.add(methodParam);		
		params.add(recvParam);
		params.add(foloParam);
		params.add(typeParam);
		params.add(amtParam);

		Response resp = client.post(getBaseURL(), params, null, false);

		return resp.asString();
	}

	@Override
	public JSONArray getHotPicToday() throws HttpException, JSONException {
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		BasicNameValuePair methodParam = new BasicNameValuePair("method",
				PTApi.GETHOTPICTURE);
		params.add(methodParam);

		Response resp = client.post(getBaseURL(), params, null, false);
		String jsonStr = resp.asString();
		Log.d(TAG, ">>> json pics: " + jsonStr);

		return new JSONArray(jsonStr);
	}

	@Override
	public JSONArray getClassicPics() throws HttpException,
			JSONException {
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		BasicNameValuePair methodParam = new BasicNameValuePair("method",
				PTApi.GETClASSICALPICS);
		params.add(methodParam);

		Response resp = client.post(getBaseURL(), params, null, false);
		String jsonStr = resp.asString();
		Log.d(TAG, ">>> json Stories: " + jsonStr);

		return new JSONArray(jsonStr);
	}

	@Override
	public void markThePic(String userId, String picId) throws HttpException {
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		BasicNameValuePair methodParam = new BasicNameValuePair("method",
				PTApi.MARKTHEPIC);
		BasicNameValuePair userParam = new BasicNameValuePair("userId", userId);
		BasicNameValuePair picParam = new BasicNameValuePair("picId", picId);

		params.add(methodParam);
		params.add(userParam);
		params.add(picParam);

		Response resp = client.post(getBaseURL(), params, null, false);
		String jsonStr = resp.asString();
		Log.d(TAG, ">>> mark the pic: " + jsonStr);
	}

	@Override
	public JSONArray getFavoriteTpics(String userId, String pageNum)
			throws HttpException, JSONException {
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		BasicNameValuePair methodParam = new BasicNameValuePair("method",
				PTApi.GETFAVORITEPICS);
		BasicNameValuePair userParam = new BasicNameValuePair("userId", userId);
		BasicNameValuePair pageParam = new BasicNameValuePair("pageNum",
				pageNum);

		params.add(methodParam);
		params.add(userParam);
		params.add(pageParam);

		Response resp = client.post(getBaseURL(), params, null, false);
		String jsonStr = resp.asString();
		Log.d(TAG, ">>> json favorites: " + jsonStr);

		return new JSONArray(jsonStr);
	}

	@Override
	public JSONArray getTpicsByUser(String userId, String pageNum)
			throws HttpException, JSONException {
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		BasicNameValuePair methodParam = new BasicNameValuePair("method",
				PTApi.GETTPICSBYUSER);
		BasicNameValuePair userParam = new BasicNameValuePair("userId", userId);
		BasicNameValuePair pageParam = new BasicNameValuePair("pageNum",
				pageNum);

		params.add(methodParam);
		params.add(userParam);
		params.add(pageParam);

		Response resp = client.post(getBaseURL(), params, null, false);
		String jsonStr = resp.asString();
		Log.d(TAG, ">>> json tpics: " + jsonStr);

		return new JSONArray(jsonStr);
	}

	@Override
	public JSONArray getStoriesByUser(String userId, String pageNum)
			throws HttpException, JSONException {
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		BasicNameValuePair methodParam = new BasicNameValuePair("method",
				PTApi.GETSTORIESBYUSER);
		BasicNameValuePair userParam = new BasicNameValuePair("userId", userId);
		BasicNameValuePair pageParam = new BasicNameValuePair("pageNum",
				pageNum);

		params.add(methodParam);
		params.add(userParam);
		params.add(pageParam);

		Response resp = client.post(getBaseURL(), params, null, false);
		String jsonStr = resp.asString();
		Log.d(TAG, ">>> json stories: " + jsonStr);

		return new JSONArray(jsonStr);
	}
	
	@Override
	public JSONObject getUserDetail(String userId) throws HttpException,
			JSONException {
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		BasicNameValuePair methodParam = new BasicNameValuePair("method",
				PTApi.GETUSERDETAIL);
		BasicNameValuePair userParam = new BasicNameValuePair("userId", userId);

		params.add(methodParam);
		params.add(userParam);

		Response resp = client.post(getBaseURL(), params, null, false);
		String jsonStr = resp.asString();
		Log.d(TAG, ">>> json user Details: " + jsonStr);

		return new JSONObject(jsonStr);
	}

	@Override
	public JSONObject getUserEstate(String userId) throws HttpException,
			JSONException {
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		BasicNameValuePair methodParam = new BasicNameValuePair("method",
				PTApi.GETUSERESTATE);
		BasicNameValuePair userParam = new BasicNameValuePair("userId", userId);

		params.add(methodParam);
		params.add(userParam);

		Response resp = client.post(getBaseURL(), params, null, false);
		String jsonStr = resp.asString();
		Log.d(TAG, ">>> json user Estate: " + jsonStr);

		return new JSONObject(jsonStr);
	}

	@Override
	public String postMessage(String userId, String receiver, String content)
			throws HttpException {
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		BasicNameValuePair methodParam = new BasicNameValuePair("method",
				PTApi.SENDMSG);
		BasicNameValuePair userParam = new BasicNameValuePair("userId", userId);
		BasicNameValuePair targetParam = new BasicNameValuePair("receiver", receiver);		
		BasicNameValuePair contentParam = new BasicNameValuePair("content", content);

		params.add(methodParam);
		params.add(userParam);
		params.add(targetParam);
		params.add(contentParam);

		Response resp = client.post(getBaseURL(), params, null, false);
		String jsonStr = resp.asString();
		Log.d(TAG, ">>> post the msg: " + jsonStr);
		return jsonStr;
	}

	@Override
	public JSONArray getNewMessages(String userId) throws HttpException, JSONException {
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		BasicNameValuePair methodParam = new BasicNameValuePair("method",
				PTApi.GETUSERMSG);
		BasicNameValuePair userParam = new BasicNameValuePair("userId", userId);

		params.add(methodParam);
		params.add(userParam);

		Response resp = client.post(getBaseURL(), params, null, false);
		String jsonStr = resp.asString();
		Log.d(TAG, ">>> json msgs: " + jsonStr);

		return new JSONArray(jsonStr);
	}

	@Override
	public String updateMsgReaded(String msgIds) throws HttpException {
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		BasicNameValuePair methodParam = new BasicNameValuePair("method",
				PTApi.CHANGEMSGSTATE);
		BasicNameValuePair msgParam = new BasicNameValuePair("msgIds", msgIds);

		params.add(methodParam);
		params.add(msgParam);

		Response resp = client.post(getBaseURL(), params, null, false);

		return resp.asString();
	}

	@Override
	public String logon(String account, String password) throws HttpException {
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		BasicNameValuePair methodParam = new BasicNameValuePair("method",
				PTApi.LOGON);
		BasicNameValuePair actParam = new BasicNameValuePair("account", account);
		BasicNameValuePair pwdParam = new BasicNameValuePair("password", password);

		params.add(methodParam);
		params.add(actParam);
		params.add(pwdParam);

		Response resp = client.post(getBaseURL(), params, null, false);

		return resp.asString();
	}

	@Override
	public String commonQuery(String method, String name, String value) throws HttpException {
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		BasicNameValuePair methodParam = new BasicNameValuePair("method",method);
		BasicNameValuePair param = new BasicNameValuePair(name, value);

		params.add(methodParam);
		params.add(param);

		Response resp = client.post(getBaseURL(), params, null, false);

		return resp.asString();
	}

	@Override
	public JSONArray getPicDaren() throws HttpException ,JSONException {
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		BasicNameValuePair methodParam = new BasicNameValuePair("method",PTApi.PICDARENSTATISTICS);		

		params.add(methodParam);	

		Response resp = client.post(getBaseURL(), params, null, false);

		String jsonStr = resp.asString();
		Log.d(TAG, ">>> json daren: " + jsonStr);

		return new JSONArray(jsonStr);

	}

	@Override
	public JSONArray getCmntDaren() throws HttpException, JSONException {
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		BasicNameValuePair methodParam = new BasicNameValuePair("method",PTApi.CMTDARENSTATISTICS);		

		params.add(methodParam);	

		Response resp = client.post(getBaseURL(), params, null, false);

		String jsonStr = resp.asString();
		Log.d(TAG, ">>> json daren: " + jsonStr);

		return new JSONArray(jsonStr);
	}


	
} // end of class

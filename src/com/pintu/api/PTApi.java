package com.pintu.api;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.pintu.http.HttpException;
import com.pintu.http.Response;
import com.pintu.http.CountingMultiPartEntity.ProgressListener;

public interface PTApi {

	// 注意： 这里的方法名静态变量要跟ipintu中的ExtVisitorInterface定义一致
	public static final String UPLOADPICTURE = "upload";

	public static final String APPLYFORUSER = "applyForUser";

	public static final String OTHERMETHOD = "otherMethod";

	public static final String GETGALLERYBYTIME = "getGalleryByTime";

	public static final String GETIMAGEFILE = "getImageFile";

	public static final String GETIMAGEBYPATH = "getImageByPath";

	public static final String GETPICDETAIL = "getPicDetail";

	public static final String ADDSTORY = "addStory";

	public static final String ADDCOMMENT = "addComment";

	public static final String GETSTORIESOFPIC = "getStoriesOfPic";

	public static final String GETCOMMENTSOFPIC = "getCommentsOfPic";

	public static final String ADDVOTE = "addVote";

	public static final String GETHOTPICTURE = "getHotPicture";

	public static final String GETClASSICALPICS = "classicalStatistics";

	public static final String MARKTHEPIC = "markThePic";

	public static final String GETFAVORITEPICS = "getFavoriteTpics";

	public static final String GETTPICSBYUSER = "getTpicsByUser";

	public static final String GETSTORIESBYUSER = "getStoriesByUser";

	public static final String GETUSERESTATE = "getUserEstate";

	public static final String GETUSERDETAIL = "getUserDetail";

	public static final String SENDMSG = "sendMsg";

	public static final String GETUSERMSG = "getUserMsg";

	public static final String CHANGEMSGSTATE = "changeMsgState";

	public static final String LOGON = "logon";

	public static final String GETPICCOOLCOUNT = "getPicCoolCount";

	// 贴图达人
	public static final String PICDARENSTATISTICS = "pictureDarenStatistics";

	// 评论达人
	public static final String CMTDARENSTATISTICS = "commentDarenStatistics";

	// 随机获取系统32个图~
	public static final String GETRANDGALLERY = "getRandGallery";

	// 最热标签
	public static final String GETHOTTAGS = "getHotTags";

	// 查看某tag的所有图片
	public static final String GETTHUMBNAILSBYTAG = "getThumbnailsByTag";

	// TODO, ADD OTHER REMOTE METHODS...
	
	//获取更新配置文件的地址
	//里面记录了更新包的信息
	public String getConfigURL();

	/**
	 * 有上传进度的贴图API
	 * 
	 * @param pic
	 * @param tags
	 * @param desc
	 * @param isOriginal
	 * @param listener
	 *            上传字节数回调函数
	 * @return
	 * @throws HttpException
	 */
	public String postPictureWithProgress(File pic, String tags, String desc,
			String isOriginal, ProgressListener listener) throws HttpException;

	public boolean isDebugMode();

	// 通用的简单查询：
	// 提交三个参数：一个方法名称，一个参数名，一个参数值
	// 返回：一个字符串
	public String commonQuery(String method, String name, String value)
			throws HttpException;

	// 登录成功后要更新记录在client中的用户
	public void updateUser(String userId);

	// 贴一张图
	public String postPicture(File pic, String tags, String desc,
			String isOriginal) throws HttpException;

	// 根据编号合成URL地址，图片类型可以从编号后缀解析出来
	public String composeImgUrlById(String imgId);

	// 根据文件磁盘路径获取该图片的请求url
	public String composeImgUrlByPath(String imgPath);

	// 获取一个图片响应，图形包含在响应流中
	public Response getImgByUrl(String url) throws HttpException;

	// 获取指定时间的画廊数据
	public JSONArray getCommunityPicsByTime(String startTime, String endTime)
			throws HttpException, JSONException;

	// 获得贴图详情数据
	public JSONObject getPictureDetailsById(String tpId) throws HttpException,
			JSONException;

	// 添加故事
	public String postStory(String follow, String story) throws HttpException;

	// 获取故事列表
	public JSONArray getStoriesByTpId(String tpId) throws HttpException,
			JSONException;

	// 发送评价
	public String postComment(String follow, String comment)
			throws HttpException;

	// 获取评价列表
	public JSONArray getCommensByTpId(String tpId) throws HttpException,
			JSONException;

	// 发送投票
	public String postVote(String receiver, String follow, String type,
			String amount) throws HttpException;

	// 获取今日热图
	public JSONArray getHotPicToday() throws HttpException, JSONException;

	// 获取经典作品
	public JSONArray getClassicPics() throws HttpException, JSONException;

	// 获得用户基本资料
	public JSONObject getUserDetail(String userId) throws HttpException,
			JSONException;

	// 获得用户资产，包括用户详细资料
	public JSONObject getUserEstate(String userId) throws HttpException,
			JSONException;

	// 收藏喜欢的图片
	public void markThePic(String userId, String picId) throws HttpException;

	// 查看收藏图片列表已有API
	public JSONArray getFavoriteTpics(String userId, String pageNum)
			throws HttpException, JSONException;

	// 查看某人的贴图
	public JSONArray getTpicsByUser(String userId, String pageNum)
			throws HttpException, JSONException;

	// 查看某人的故事
	public JSONArray getStoriesByUser(String userId, String pageNum)
			throws HttpException, JSONException;

	// 发送消息
	public String postMessage(String userId, String receiver, String content)
			throws HttpException;

	// 获取最新消息
	public JSONArray getNewMessages(String userId) throws HttpException,
			JSONException;

	// 更新消息为已读
	public String updateMsgReaded(String msgIds) throws HttpException;

	// 登录验证
	public String logon(String account, String password) throws HttpException;

	// 获取贴图达人
	public JSONArray getPicDaren() throws HttpException, JSONException;

	// 获取评论达人
	public JSONArray getCmntDaren() throws HttpException, JSONException;

	// 获取热门标签
	public JSONArray getHotTags() throws HttpException, JSONException;

	// 获取某标签的图片
	public JSONArray getThumbnailsByTag(String tagId, String pageNum)
			throws HttpException, JSONException;

	// 随便看看
	public JSONArray getRandomGallery() throws HttpException, JSONException;

} // end of interface

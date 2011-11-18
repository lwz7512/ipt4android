package com.pintu.db;

import java.util.List;

import com.pintu.data.TMsg;
import com.pintu.data.StoryInfo;
import com.pintu.data.TPicDesc;
import com.pintu.data.TPicDetails;
import com.pintu.data.TPicItem;

public interface CacheDao {

	//注销时清除数据库
	public void clearData();
	
	//缓存缩略图
	public int insertThumbnails(List<TPicDesc> thumbnails);
	//删除最老的toDeleteNum条缩略图记录
	public void deleteOldThumbnails(int toDeleteNum);
	//查看缓存的缩略图
	public List<TPicDesc> getCachedThumbnails();
	//查看缓存的缩略图记录数
	public int cachedThumbnailSize();
	
	//缓存热图
	public void insertHotPics(List<TPicDetails> hotpics);
	//获取缓存的热图
	public List<TPicDetails> getCachedHotPics();
	//缓存经典故事
	public void insertClassicPics(List<TPicDetails> clscpics);
	//获取缓存的经典
	public List<TPicDetails> getCachedClassicPics();

	//查询缓存是否有收藏的图片
	public boolean hasAlreadyMarked(String tpId);
	//缓存取回的收藏图片，只包含作者、头像、图片、发布时间
	//每次是全部从远程取回，然后删除缓存，并插入
	public void insertMarkedPics(List<TPicItem> pics);
	//获取收藏的全部图片列表
	public List<TPicItem> getCachedFavoritePics();
	//收藏时缓存
	public void insertOneMarkedPic(TPicItem pic);
	
	//为安全起见：
	//缓存自己的图片，只插入不删除，插入时要判断重复
	public void insertMyPics(List<TPicItem> pics);
	//按页码取出缓存的自己的图片
	public List<TPicItem> getCachedMyPics(String owner, int pageNum);	
	
	//为安全起见：
	//缓存自己的消息，只插入不删除，插入时要判断重复
	//返回成功插入数目，以便做通知
	public int insertMyMsgs(List<TMsg> msgs);
	//按页码取出缓存的自己的消息
	public List<TMsg> getUnreadedMsgs(int pageNum);
	//更新消息状态为已读
	public void updateMsgReaded(String msgId);
	//获得更多消息，查看老的已读消息
	public List<TMsg> getMoreMsgs(int pageNum);
	
}

package com.pintu.db;

import java.util.List;

import com.pintu.data.StoryInfo;
import com.pintu.data.TPicDesc;
import com.pintu.data.TPicDetails;

public interface CacheDao {

	//清除数据库
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
	public void insertClassicStories(List<StoryInfo> storyies);
	//获取缓存的经典
	public List<StoryInfo> getCachedClassicStories();
	
}

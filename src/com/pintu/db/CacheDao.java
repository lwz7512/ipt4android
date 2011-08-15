package com.pintu.db;

import java.util.List;

import com.pintu.data.TPicDesc;

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
}

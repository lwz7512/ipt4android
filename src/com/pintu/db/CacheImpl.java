package com.pintu.db;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.pintu.data.StoryInfo;
import com.pintu.data.TPicDesc;
import com.pintu.data.TPicDetails;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class CacheImpl implements CacheDao {

	private static final String TAG = "CacheImpl";
	
	private SQLiteDatabase ptdb;
	
	private SimpleDateFormat dateFormat;
	
	
	public CacheImpl(Context ctxt){
		PintuDB db = PintuDB.getInstance(ctxt);
		ptdb = db.getDb(true);
		dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}

	@Override
	public int insertThumbnails(List<TPicDesc> thumbnails) {
		Query q = new Query(ptdb);
		int successRecord = 0;
		try{
			ptdb.beginTransaction();	
			//不分顺序插入
			for(TPicDesc pic : thumbnails){
				long result = -1;
				//检查是否有重复记录，没有才入库，否则发生约束冲突
				if(!checkThumbnailExist(pic.tpId)){
					result = q.into(PintuTables.ThumbnailTable.TABLE_NAME)
							.values(thumbnailToContentValues(pic))
							.insert();														
				}				
                if (-1 == result) {
                    Log.e(TAG, "cann't insert the thumbnail : " + pic.thumbnailId);
                } else {
                	successRecord ++;
                    Log.v(TAG, String.format("Insert a thumbnail into database : %s", pic.thumbnailId));
                }
			}
			ptdb.setTransactionSuccessful();
		}finally{
			ptdb.endTransaction();
		}
		
		//判断是否超过最大记录数，如果超过就删除超过部分
		int beyondSize = cachedThumbnailSize()-PintuTables.ThumbnailTable.MAX_ROW_NUMBER;
		if(beyondSize>0){
			Log.d(TAG, ">>> Beyond max thumbnail size, to delete: "+beyondSize);
			deleteOldThumbnails(beyondSize);
		}else{
			Log.d(TAG, ">>> not beyond max thumbnail size..");
		}
		
		return successRecord;
	}
	
	private boolean checkThumbnailExist(String tpId){
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM ").append(PintuTables.ThumbnailTable.TABLE_NAME).append(" WHERE ")
                .append(PintuTables.ThumbnailTable.Columns.TP_ID).append(" =?");

		Cursor c = ptdb.rawQuery(sql.toString(), new String[] { tpId });
		boolean exist = c.getCount()>1?true:false;
		c.close();
		return exist;
	}
	
	 private ContentValues thumbnailToContentValues(TPicDesc pic){
		 ContentValues v = new ContentValues();
		 v.put(PintuTables.ThumbnailTable.Columns.TP_ID, pic.tpId);
		 v.put(PintuTables.ThumbnailTable.Columns.THUMBNAIL_ID, pic.thumbnailId);
		 v.put(PintuTables.ThumbnailTable.Columns.STATUS, pic.status);
		 v.put(PintuTables.ThumbnailTable.Columns.URL, pic.url);
		 String creationTime = pic.creationTime;
		 //有生成时间为空的情况？
		 if(creationTime==null)
			 creationTime = String.valueOf(new Date().getTime());	
		 Date ctDate = new Date(Long.valueOf(creationTime));
		 String sqlDate = dateFormat.format(ctDate);
		 v.put(PintuTables.ThumbnailTable.Columns.CREATION_TIME, sqlDate);		 
		 
		 return v;
	 }

	 @Override
	 public List<TPicDesc> getCachedThumbnails() {
		 List<TPicDesc> list = new ArrayList<TPicDesc>();
		 Query q = new Query(ptdb);
		 Cursor c = q.from(PintuTables.ThumbnailTable.TABLE_NAME)
				 			.orderBy(PintuTables.ThumbnailTable.Columns.CREATION_TIME+" DESC")
				 			.select();
	        try {
	            while (c.moveToNext()) {
	                list.add(cursorToThumbnail(c));
	            }
	        } finally {
	            c.close();
	        }
		
		 return list;
	 }
	 
	 
	 private TPicDesc cursorToThumbnail(Cursor c){
		 TPicDesc pic = new TPicDesc();
		 pic.tpId = c.getString(c.getColumnIndex(PintuTables.ThumbnailTable.Columns.TP_ID));
		 pic.thumbnailId = c.getString(c.getColumnIndex(PintuTables.ThumbnailTable.Columns.THUMBNAIL_ID));
		 pic.status = c.getString(c.getColumnIndex(PintuTables.ThumbnailTable.Columns.STATUS));
		 pic.url = c.getString(c.getColumnIndex(PintuTables.ThumbnailTable.Columns.URL));
		 
		 String savedTime = c.getString(c.getColumnIndex(PintuTables.ThumbnailTable.Columns.CREATION_TIME)); 
		 try {
			 long ldte = dateFormat.parse(savedTime).getTime();
			 pic.creationTime = String.valueOf(ldte);
		} catch (ParseException e) {
			// do nothing temporally...
			e.printStackTrace();
		}
		 
		 return pic;
	 }
	 
	@Override
	public void deleteOldThumbnails(int toDeleteNum) {
		Query q = new Query(ptdb);
		//找出最老的记录，时间升序排列
		Cursor c = q.from(PintuTables.ThumbnailTable.TABLE_NAME, 
				new String[]{PintuTables.ThumbnailTable.Columns.TP_ID})
				.orderBy(PintuTables.ThumbnailTable.Columns.CREATION_TIME+" ASC")
				.limit(toDeleteNum)
				.select();
		//保存被删除记录ID
		ArrayList<String> toDeleteIds = new ArrayList<String>();
	      try {
	            while (c.moveToNext()) {
	            	toDeleteIds.add(c.getString(0));
	            }
	        } finally {
	            c.close();
	        }
	     //批量删除 
	      try{
	    	  ptdb.beginTransaction();
	    	  for(String id : toDeleteIds){
	    		  q.from(PintuTables.ThumbnailTable.TABLE_NAME)
	    		    .where(PintuTables.ThumbnailTable.Columns.TP_ID+"=?",id)
	    		    .delete();
	    	  }	
	    	  ptdb.setTransactionSuccessful();
	      }finally{
	    	  ptdb.endTransaction();
	      }
		
	}

	@Override
	public int cachedThumbnailSize() {
		Cursor c = ptdb.rawQuery("SELECT COUNT(*) FROM "+PintuTables.ThumbnailTable.TABLE_NAME, null);
		int size = c.getCount();
		c.close();
		
		return size;
	}

	@Override
	public void clearData() {
		
		ptdb.execSQL("DELETE FROM " + PintuTables.ThumbnailTable.TABLE_NAME);
		ptdb.execSQL("DELETE FROM " + PintuTables.HotpicTable.TABLE_NAME);
		
	}

	@Override
	public void insertHotPics(List<TPicDetails> hotpics) {
		//先删除已缓存的东西
		ptdb.execSQL("DELETE FROM " + PintuTables.HotpicTable.TABLE_NAME);
		
		Query q = new Query(ptdb);
		try{
			ptdb.beginTransaction();	
			//不分顺序插入
			for(TPicDetails pic : hotpics){
				long result = -1;				
					result = q.into(PintuTables.HotpicTable.TABLE_NAME)
								.values(hotPicToContentValues(pic))
								.insert();																			
                if (-1 == result) {
                    Log.e(TAG, "cann't insert the hotpic : " + pic.id);
                } else {
                    Log.v(TAG, String.format("Insert a hotpic into database : %s", pic.id));
                }
			}
			ptdb.setTransactionSuccessful();
		}finally{
			ptdb.endTransaction();
		}

	}
	
	 private ContentValues hotPicToContentValues(TPicDetails pic){
		 ContentValues v = new ContentValues();
		 v.put(PintuTables.HotpicTable.Columns.ID, pic.id);
		 v.put(PintuTables.HotpicTable.Columns.AUTHOR, pic.author);
		 v.put(PintuTables.HotpicTable.Columns.AVATARIMGPATH, pic.avatarImgPath);
		 v.put(PintuTables.HotpicTable.Columns.MOBIMGID, pic.mobImgId);
		 v.put(PintuTables.HotpicTable.Columns.STORIESNUM, pic.storiesNum);
		 v.put(PintuTables.HotpicTable.Columns.COMMENTSNUM, pic.commentsNum);
		 v.put(PintuTables.HotpicTable.Columns.CREATION_TIME, pic.publishTime);		 
		 
		 return v;
	 }


	@Override
	public List<TPicDetails> getCachedHotPics() {
		 List<TPicDetails> list = new ArrayList<TPicDetails>();
		 Query q = new Query(ptdb);
		 Cursor c = q.from(PintuTables.HotpicTable.TABLE_NAME)
				 			.orderBy(PintuTables.HotpicTable.Columns.CREATION_TIME+" DESC")
				 			.select();
	        try {
	            while (c.moveToNext()) {
	                list.add(cursorToHotPic(c));
	            }
	        } finally {
	            c.close();
	        }
		
		 return list;
	}
	
	 private TPicDetails cursorToHotPic(Cursor c){
		 TPicDetails pic = new TPicDetails();
		 pic.id = c.getString(c.getColumnIndex(PintuTables.HotpicTable.Columns.ID));
		 pic.author = c.getString(c.getColumnIndex(PintuTables.HotpicTable.Columns.AUTHOR));
		 pic.avatarImgPath = c.getString(c.getColumnIndex(PintuTables.HotpicTable.Columns.AVATARIMGPATH));
		 pic.mobImgId = c.getString(c.getColumnIndex(PintuTables.HotpicTable.Columns.MOBIMGID));
		 pic.storiesNum = c.getString(c.getColumnIndex(PintuTables.HotpicTable.Columns.STORIESNUM));
		 pic.commentsNum = c.getString(c.getColumnIndex(PintuTables.HotpicTable.Columns.COMMENTSNUM));		 
		 pic.publishTime = c.getString(c.getColumnIndex(PintuTables.HotpicTable.Columns.CREATION_TIME)); 
		
		 
		 return pic;
	 }


	@Override
	public void insertClassicStories(List<StoryInfo> storyies) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<StoryInfo> getCachedClassicStories() {
		// TODO Auto-generated method stub
		return null;
	}

	
	
	
	
}

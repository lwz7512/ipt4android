package com.pintu.db;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.pintu.data.TPicDesc;

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
				long result = q.into(PintuTables.ThumbnailTable.TABLE_NAME)
						.values(thumbnailToContentValues(pic))
						.insert();				
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
		 //FIXME, 好像模拟器上数据库时间比本地小8个小时，补上时差？
		 //8小时毫秒数
		 long eightHourMiliSeconds = 8*60*60*1000;
		 Date ctDate = new Date(Long.valueOf(creationTime)+eightHourMiliSeconds);
//		 Date ctDate = new Date(Long.valueOf(creationTime));
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
//		Query q = new Query(ptdb);	
//		return q.from(PintuTables.ThumbnailTable.TABLE_NAME, 
//				new String[]{PintuTables.ThumbnailTable.Columns.TP_ID})
//				.select().getCount();		
		return ptdb.rawQuery("SELECT COUNT(*) FROM "+PintuTables.ThumbnailTable.TABLE_NAME, null).getCount();
	}

	@Override
	public void clearData() {
		
		ptdb.execSQL("DELETE FROM " + PintuTables.ThumbnailTable.TABLE_NAME);
		
		
	}

	
	
	
	
}

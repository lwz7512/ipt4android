package com.ybcx.db;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ybcx.data.StoryInfo;
import com.ybcx.data.TMsg;
import com.ybcx.data.TPicDesc;
import com.ybcx.data.TPicDetails;
import com.ybcx.data.TPicItem;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class CacheImpl implements CacheDao {

	private static final String TAG = "CacheImpl";

	private SQLiteDatabase ptdb;

	private SimpleDateFormat dateFormat;

	public CacheImpl(Context ctxt) {
		PintuDB db = PintuDB.getInstance(ctxt);
		ptdb = db.getDb(true);
		dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}

	@Override
	public void clearData() {
		ptdb.execSQL("DELETE FROM " + PintuTables.ThumbnailTable.TABLE_NAME);
		ptdb.execSQL("DELETE FROM " + PintuTables.HotpicTable.TABLE_NAME);
		ptdb.execSQL("DELETE FROM " + PintuTables.ClassicPics.TABLE_NAME);
		ptdb.execSQL("DELETE FROM " + PintuTables.FavoritePicsTable.TABLE_NAME);
		ptdb.execSQL("DELETE FROM " + PintuTables.MyMessageTable.TABLE_NAME);
		ptdb.execSQL("DELETE FROM " + PintuTables.MyPicsTable.TABLE_NAME);
		
		// TODO, DELETE CACHE DATA IN TABLE...

	}

	private <T> void checkBlankList(List<T> results) {
		if (results == null)
			return;
		if (results != null && results.size() == 0)
			return;
	}
	
	private boolean checkRecordExist(String table, String id){
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT(*) FROM ")
				.append(table)
				.append(" WHERE ")
				.append(" id=?");

		Cursor c = ptdb.rawQuery(sql.toString(), new String[] { id });
		boolean exist = false; 
		if (c.moveToFirst()) {
             exist = c.getInt(0) > 0;
         }
		c.close();
		return exist;
		
	}

	@Override
	public int insertThumbnails(List<TPicDesc> thumbnails) {
		Query q = new Query(ptdb);
		int successRecord = 0;
		try {
			ptdb.beginTransaction();
			// 不分顺序插入
			for (TPicDesc pic : thumbnails) {
				long result = -1;
				// 检查是否有重复记录，没有才入库，否则发生约束冲突
				if (!checkThumbnailExist(pic.tpId)) {
					result = q.into(PintuTables.ThumbnailTable.TABLE_NAME)
							.values(thumbnailToContentValues(pic)).insert();
				}
				if (-1 == result) {
					Log.e(TAG, "cann't insert the thumbnail : "
							+ pic.thumbnailId);
				} else {
					successRecord++;
					Log.v(TAG, String.format(
							"Insert a thumbnail into database : %s",
							pic.thumbnailId));
				}
			}
			ptdb.setTransactionSuccessful();
		} finally {
			ptdb.endTransaction();
		}

		// 判断是否超过最大记录数，如果超过就删除超过部分
		int beyondSize = cachedThumbnailSize()
				- PintuTables.ThumbnailTable.MAX_ROW_NUMBER;
		if (beyondSize > 0) {
			Log.d(TAG, ">>> Beyond max thumbnail size, to delete: "
					+ beyondSize);
			deleteOldThumbnails(beyondSize);
		} else {
			Log.d(TAG, ">>> not beyond max thumbnail size..");
		}

		return successRecord;
	}

	private boolean checkThumbnailExist(String tpId) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT(*) FROM ")
				.append(PintuTables.ThumbnailTable.TABLE_NAME)
				.append(" WHERE ")
				.append(PintuTables.ThumbnailTable.Columns.TP_ID).append(" =?");

		Cursor c = ptdb.rawQuery(sql.toString(), new String[] { tpId });
		boolean exist = false; 
		if (c.moveToFirst()) {
             exist = c.getInt(0) > 0;
         }
		c.close();

		return exist;
	}

	private ContentValues thumbnailToContentValues(TPicDesc pic) {
		ContentValues v = new ContentValues();
		v.put(PintuTables.ThumbnailTable.Columns.TP_ID, pic.tpId);
		v.put(PintuTables.ThumbnailTable.Columns.THUMBNAIL_ID, pic.thumbnailId);
		v.put(PintuTables.ThumbnailTable.Columns.STATUS, pic.status);
		v.put(PintuTables.ThumbnailTable.Columns.URL, pic.url);
		String creationTime = pic.creationTime;
		// 有生成时间为空的情况？
		if (creationTime == null)
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
		Cursor c = q
				.from(PintuTables.ThumbnailTable.TABLE_NAME)
				.orderBy(
						PintuTables.ThumbnailTable.Columns.CREATION_TIME
								+ " DESC").select();
		try {
			while (c.moveToNext()) {
				list.add(cursorToThumbnail(c));
			}
		} finally {
			c.close();
		}

		return list;
	}

	private TPicDesc cursorToThumbnail(Cursor c) {
		TPicDesc pic = new TPicDesc();
		pic.tpId = c.getString(c
				.getColumnIndex(PintuTables.ThumbnailTable.Columns.TP_ID));
		pic.thumbnailId = c
				.getString(c
						.getColumnIndex(PintuTables.ThumbnailTable.Columns.THUMBNAIL_ID));
		pic.status = c.getString(c
				.getColumnIndex(PintuTables.ThumbnailTable.Columns.STATUS));
		pic.url = c.getString(c
				.getColumnIndex(PintuTables.ThumbnailTable.Columns.URL));

		String savedTime = c
				.getString(c
						.getColumnIndex(PintuTables.ThumbnailTable.Columns.CREATION_TIME));
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
		// 找出最老的记录，时间升序排列
		Cursor c = q
				.from(PintuTables.ThumbnailTable.TABLE_NAME,
						new String[] { PintuTables.ThumbnailTable.Columns.TP_ID })
				.orderBy(
						PintuTables.ThumbnailTable.Columns.CREATION_TIME
								+ " ASC").limit(toDeleteNum).select();
		// 保存被删除记录ID
		ArrayList<String> toDeleteIds = new ArrayList<String>();
		try {
			while (c.moveToNext()) {
				toDeleteIds.add(c.getString(0));
			}
		} finally {
			c.close();
		}
		// 批量删除
		try {
			ptdb.beginTransaction();
			for (String id : toDeleteIds) {
				q.from(PintuTables.ThumbnailTable.TABLE_NAME)
						.where(PintuTables.ThumbnailTable.Columns.TP_ID + "=?",
								id).delete();
			}
			ptdb.setTransactionSuccessful();
		} finally {
			ptdb.endTransaction();
		}

	}

	@Override
	public int cachedThumbnailSize() {
		Cursor c = ptdb.rawQuery("SELECT COUNT(*) FROM "
				+ PintuTables.ThumbnailTable.TABLE_NAME, null);
		int size = c.getCount();
		c.close();

		return size;
	}

	@Override
	public void insertHotPics(List<TPicDetails> hotpics) {
		this.checkBlankList(hotpics);
		// 先删除已缓存的东西
		ptdb.execSQL("DELETE FROM " + PintuTables.HotpicTable.TABLE_NAME);

		Query q = new Query(ptdb);
		try {
			ptdb.beginTransaction();
			// 不分顺序插入
			for (TPicDetails pic : hotpics) {
				long result = -1;
				result = q.into(PintuTables.HotpicTable.TABLE_NAME)
						.values(picContentValues(pic)).insert();
				if (-1 == result) {
					Log.e(TAG, "cann't insert the hotpic : " + pic.id);
				} else {
					Log.v(TAG, String.format(
							"Insert a hotpic into database : %s", pic.id));
				}
			}
			ptdb.setTransactionSuccessful();
		} finally {
			ptdb.endTransaction();
		}

	}

	private ContentValues picContentValues(TPicDetails pic) {
		ContentValues v = new ContentValues();
		v.put(PintuTables.HotpicTable.Columns.ID, pic.id);
		v.put(PintuTables.HotpicTable.Columns.AUTHOR, pic.author);
		v.put(PintuTables.HotpicTable.Columns.AVATARIMGPATH, pic.avatarImgPath);
		v.put(PintuTables.HotpicTable.Columns.MOBIMGID, pic.mobImgId);
		v.put(PintuTables.HotpicTable.Columns.BROWSENUM, pic.browseCount);
		v.put(PintuTables.HotpicTable.Columns.COMMENTSNUM, pic.commentNum);
		v.put(PintuTables.HotpicTable.Columns.CREATION_TIME, pic.publishTime);

		return v;
	}

	@Override
	public List<TPicDetails> getCachedHotPics() {
		List<TPicDetails> list = new ArrayList<TPicDetails>();
		Query q = new Query(ptdb);
		Cursor c = q
				.from(PintuTables.HotpicTable.TABLE_NAME)
				.orderBy(
						PintuTables.HotpicTable.Columns.CREATION_TIME + " DESC")
				.select();
		try {
			while (c.moveToNext()) {
				list.add(cursorToPic(c));
			}
		} finally {
			c.close();
		}

		return list;
	}

	private TPicDetails cursorToPic(Cursor c) {
		TPicDetails pic = new TPicDetails();
		pic.id = c.getString(c
				.getColumnIndex(PintuTables.HotpicTable.Columns.ID));
		pic.author = c.getString(c
				.getColumnIndex(PintuTables.HotpicTable.Columns.AUTHOR));
		pic.avatarImgPath = c.getString(c
				.getColumnIndex(PintuTables.HotpicTable.Columns.AVATARIMGPATH));
		pic.mobImgId = c.getString(c
				.getColumnIndex(PintuTables.HotpicTable.Columns.MOBIMGID));
		pic.commentNum = c.getString(c
				.getColumnIndex(PintuTables.HotpicTable.Columns.COMMENTSNUM));
		pic.browseCount = c.getString(c
				.getColumnIndex(PintuTables.HotpicTable.Columns.BROWSENUM));
		pic.publishTime = c.getString(c
				.getColumnIndex(PintuTables.HotpicTable.Columns.CREATION_TIME));

		return pic;
	}

	@Override
	public void insertClassicPics(List<TPicDetails> clscpics) {
		this.checkBlankList(clscpics);
		// 先删除已缓存的东西
		ptdb.execSQL("DELETE FROM " + PintuTables.ClassicPics.TABLE_NAME);

		Query q = new Query(ptdb);
		try {
			ptdb.beginTransaction();
			// 不分顺序插入
			for (TPicDetails pic : clscpics) {
				long result = -1;
				result = q.into(PintuTables.ClassicPics.TABLE_NAME)
						.values(picContentValues(pic)).insert();
				if (-1 == result) {
					Log.e(TAG, "cann't insert the hotpic : " + pic.id);
				} else {
					Log.v(TAG, String.format(
							"Insert a hotpic into database : %s", pic.id));
				}
			}
			ptdb.setTransactionSuccessful();
		} finally {
			ptdb.endTransaction();
		}

	}

	@Override
	public List<TPicDetails> getCachedClassicPics() {
		List<TPicDetails> list = new ArrayList<TPicDetails>();
		Query q = new Query(ptdb);
		Cursor c = q
				.from(PintuTables.ClassicPics.TABLE_NAME)
				.orderBy(
						PintuTables.ClassicPics.Columns.CREATION_TIME + " DESC")
				.select();
		try {
			while (c.moveToNext()) {
				list.add(cursorToPic(c));
			}
		} finally {
			c.close();
		}

		return list;

	}
	
	@Override
	public boolean hasAlreadyMarked(String tpId) {
		// 检查此图是否已经被收藏，作为收藏按钮状态依据
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT(*) FROM ")
				.append(PintuTables.FavoritePicsTable.TABLE_NAME)
				.append(" WHERE ")
				.append(PintuTables.FavoritePicsTable.Columns.ID).append(" =?");

		Cursor c = ptdb.rawQuery(sql.toString(), new String[] { tpId });
		boolean exist = false; 
		if (c.moveToFirst()) {
             exist = c.getInt(0) > 0;
         }
		c.close();

		return exist;
	}

	
	@Override
	public void insertOneMarkedPic(TPicItem pic) {
		Query q = new Query(ptdb);
		long result = -1;
		result = q.into(PintuTables.FavoritePicsTable.TABLE_NAME)
				.values(picItemToContentValues(pic)).insert();
		if (-1 == result) {
			Log.e(TAG, "cann't mark the pic : " + pic.id);
		} else {
			Log.v(TAG, String.format("Insert a pic into database : %s",
					pic.id));
		}
	}

	
	@Override
	public void insertMarkedPics(List<TPicItem> pics) {
		this.checkBlankList(pics);
		// 先删除已缓存的东西
		ptdb.execSQL("DELETE FROM " + PintuTables.FavoritePicsTable.TABLE_NAME);

		Query q = new Query(ptdb);
		try {
			ptdb.beginTransaction();
			// 不分顺序插入
			for (TPicItem pic : pics) {
				long result = -1;
				result = q.into(PintuTables.FavoritePicsTable.TABLE_NAME)
						.values(picItemToContentValues(pic)).insert();
				if (-1 == result) {
					Log.e(TAG, "cann't mark the pic : " + pic.id);
				} else {
					Log.v(TAG, String.format("Insert a pic into database : %s",
							pic.id));
				}
			}
			ptdb.setTransactionSuccessful();
		} finally {
			ptdb.endTransaction();
		}

	}

	private ContentValues picItemToContentValues(TPicItem pic) {
		ContentValues cvs = new ContentValues();
		cvs.put(PintuTables.FavoritePicsTable.Columns.ID, pic.id);
		cvs.put(PintuTables.FavoritePicsTable.Columns.OWNER, pic.owner);
		cvs.put(PintuTables.FavoritePicsTable.Columns.MOBIMGID, pic.mobImgId);
		cvs.put(PintuTables.FavoritePicsTable.Columns.CREATION_TIME,
				pic.publishTime);

		return cvs;
	}

	@Override
	public List<TPicItem> getCachedFavoritePics() {
		List<TPicItem> list = new ArrayList<TPicItem>();
		Query q = new Query(ptdb);
		Cursor c = q
				.from(PintuTables.FavoritePicsTable.TABLE_NAME)
				.orderBy(
						PintuTables.FavoritePicsTable.Columns.CREATION_TIME
								+ " DESC").select();
		try {
			while (c.moveToNext()) {
				list.add(cursorToPicItem(c));
			}
		} finally {
			c.close();
		}

		return list;
	}

	private TPicItem cursorToPicItem(Cursor c) {
		TPicItem pic = new TPicItem();
		pic.id = c.getString(c
				.getColumnIndex(PintuTables.FavoritePicsTable.Columns.ID));
		pic.mobImgId = c
				.getString(c
						.getColumnIndex(PintuTables.FavoritePicsTable.Columns.MOBIMGID));
		pic.owner = c.getString(c
				.getColumnIndex(PintuTables.FavoritePicsTable.Columns.OWNER));
		pic.publishTime = c
				.getString(c
						.getColumnIndex(PintuTables.FavoritePicsTable.Columns.CREATION_TIME));

		return pic;
	}

	@Override
	public void insertMyPics(List<TPicItem> pics) {
		this.checkBlankList(pics);

		Query q = new Query(ptdb);
		try {
			ptdb.beginTransaction();
			// 不分顺序插入
			for (TPicItem pic : pics) {
				// 如果已经缓存了，就跳过
				boolean exist = checkRecordExist(PintuTables.MyPicsTable.TABLE_NAME,pic.id);
				if (exist) continue;

				long result = -1;
				result = q.into(PintuTables.MyPicsTable.TABLE_NAME)
						.values(picItemToContentValues(pic)).insert();
				if (-1 == result) {
					Log.e(TAG, "cann't mark the pic : " + pic.id);
				} else {
					Log.v(TAG, String.format("Insert a pic into database : %s",
							pic.id));
				}
			}
			ptdb.setTransactionSuccessful();
		} finally {
			ptdb.endTransaction();
		}

	}

	/**
	 * 如果pageNum为0时，取出前25条 如果pageNum为1时，跳过25条取25，以此类推
	 * 
	 * 读取指定页数数据示例： SQL:Select * From TABLE_NAME Limit 9 Offset 10;
	 * 表示从TABLE_NAME表获取数据，跳过10行，取9行
	 * @param pageNum 页码数，从1开始取
	 * @return List<TPicItem> 图片列表
	 */
	@Override
	public List<TPicItem> getCachedMyPics(String owner, int pageNum) {
		if(pageNum<1) return null;
		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM ").append(PintuTables.MyPicsTable.TABLE_NAME)
				.append(" WHERE ").append(PintuTables.MyPicsTable.Columns.OWNER)
				.append("='").append(owner).append("'")
				.append(" ORDER BY ")
				.append(PintuTables.MyPicsTable.Columns.CREATION_TIME)
				.append(" DESC").append(" LIMIT ").append(25)
				.append(" OFFSET ").append((pageNum-1) * 25);

		Cursor c = ptdb.rawQuery(sql.toString(), null);
		List<TPicItem> list = new ArrayList<TPicItem>();
		try {
			while (c.moveToNext()) {
				list.add(cursorToPicItem(c));
			}
		} finally {
			c.close();
		}

		return list;
	}

	@Override
	public int insertMyMsgs(List<TMsg> msgs) {
		this.checkBlankList(msgs);

		int inserted = 0;
		Query q = new Query(ptdb);
		try {
			ptdb.beginTransaction();
			// 不分顺序插入
			for (TMsg msg : msgs) {
				// 如果已经缓存了，就跳过
				boolean exist = checkRecordExist(PintuTables.MyMessageTable.TABLE_NAME,msg.id);
				if (exist) continue;

				long result = -1;
				result = q.into(PintuTables.MyMessageTable.TABLE_NAME)
						.values(msgToContentValues(msg)).insert();
				if (-1 == result) {
					Log.e(TAG, "cann't insert the msg : " + msg.id);
				} else {
					inserted ++;
					Log.v(TAG, String.format("Insert a msg into database : %s",
							msg.id));
				}
			}
			ptdb.setTransactionSuccessful();
		} finally {
			ptdb.endTransaction();
		}
		
		return inserted;
	}

	/**
	 * 页码数从1开始取，每页最多25条
	 */
	@Override
	public List<TMsg> getUnreadedMsgs(int pageNum) {
		if(pageNum<1) return null;
		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM ")
				.append(PintuTables.MyMessageTable.TABLE_NAME)
				.append(" WHERE ").append(PintuTables.MyMessageTable.Columns.READED)
				.append("='0'")
				.append(" ORDER BY ")
				.append(PintuTables.MyMessageTable.Columns.CREATION_TIME)
				.append(" DESC").append(" LIMIT ").append(25)
				.append(" OFFSET ").append((pageNum-1) * 25);

		Cursor c = ptdb.rawQuery(sql.toString(), null);
		List<TMsg> list = new ArrayList<TMsg>();
		try {
			while (c.moveToNext()) {
				list.add(cursorToMessage(c));
			}
		} finally {
			c.close();
		}
		return list;
	}



	private ContentValues msgToContentValues(TMsg msg) {
		ContentValues v = new ContentValues();
		v.put(PintuTables.MyMessageTable.Columns.ID, msg.id);
		v.put(PintuTables.MyMessageTable.Columns.CONTENT, msg.content);
		v.put(PintuTables.MyMessageTable.Columns.SENDER, msg.sender);
		v.put(PintuTables.MyMessageTable.Columns.SENDERNAME, msg.senderName);
		v.put(PintuTables.MyMessageTable.Columns.SENDERAVATAR, msg.senderAvatar);
		v.put(PintuTables.MyMessageTable.Columns.RECEIVER, msg.receiver);
		v.put(PintuTables.MyMessageTable.Columns.RECEIVERNAME, msg.receiverName);
		v.put(PintuTables.MyMessageTable.Columns.READED, msg.readed);
		v.put(PintuTables.MyMessageTable.Columns.CREATION_TIME,
				msg.writeTime);

		return v;
	}

	private TMsg cursorToMessage(Cursor c) {
		TMsg msg = new TMsg();
		msg.id = c.getString(c
				.getColumnIndex(PintuTables.MyMessageTable.Columns.ID));
		msg.content = c.getString(c
				.getColumnIndex(PintuTables.MyMessageTable.Columns.CONTENT));
		msg.sender = c.getString(c
				.getColumnIndex(PintuTables.MyMessageTable.Columns.SENDER));
		msg.senderName = c.getString(c
				.getColumnIndex(PintuTables.MyMessageTable.Columns.SENDERNAME));
		msg.senderAvatar = c.getString(c
				.getColumnIndex(PintuTables.MyMessageTable.Columns.SENDERAVATAR));
		msg.receiver = c.getString(c
				.getColumnIndex(PintuTables.MyMessageTable.Columns.RECEIVER));
		msg.receiverName = c.getString(c
				.getColumnIndex(PintuTables.MyMessageTable.Columns.RECEIVERNAME));
		msg.readed = c.getString(c
				.getColumnIndex(PintuTables.MyMessageTable.Columns.READED));
		msg.writeTime = c
				.getString(c
						.getColumnIndex(PintuTables.MyMessageTable.Columns.CREATION_TIME));

		return msg;
	}

	@Override
	public void updateMsgReaded(String msgId) {
		Query q = new Query(ptdb);
		ContentValues v = new ContentValues();
		v.put(PintuTables.MyMessageTable.Columns.READED, "1");	
		
		q.from(PintuTables.MyMessageTable.TABLE_NAME)
		  .where("id=?", msgId)
		  .values(v)
		  .update();
	}

	@Override
	public List<TMsg> getMoreMsgs(int pageNum) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM ")
				.append(PintuTables.MyMessageTable.TABLE_NAME)				
				.append(" ORDER BY ")
				.append(PintuTables.MyMessageTable.Columns.CREATION_TIME)
				.append(" DESC").append(" LIMIT ").append(25)
				.append(" OFFSET ").append((pageNum-1) * 25);

		Cursor c = ptdb.rawQuery(sql.toString(), null);
		List<TMsg> list = new ArrayList<TMsg>();
		try {
			while (c.moveToNext()) {
				list.add(cursorToMessage(c));
			}
		} finally {
			c.close();
		}
		return list;

		
	}




	// ----------- TODO, XIAOMING TO IMPLEMENT THE REMAINING...

} // end of class

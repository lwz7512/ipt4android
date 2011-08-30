package com.pintu.db;


public class PintuTables {

	public static class ThumbnailTable{
		
		public static final String TABLE_NAME = "t_thumbnail";
		//缩略图数目：4*8=32
		public static final int MAX_ROW_NUMBER = 32;
		
		public static class Columns {			
			public static final String TP_ID = "tpId";
			public static final String THUMBNAIL_ID = "thumbnailId";
			public static final String STATUS = "status";
			public static final String URL = "url";
			//毫秒数
			public static final String CREATION_TIME = "creationTime";
		}
		
		public static String getCreateSQL() {
            String createString = TABLE_NAME + "( "             		
            		+ Columns.TP_ID + " TEXT PRIMARY KEY, "
            		+ Columns.THUMBNAIL_ID + " TEXT, "
                    + Columns.STATUS + " TEXT, "
            		+ Columns.URL + " TEXT, "
                    + Columns.CREATION_TIME + " DATE " + ");";

            return "CREATE TABLE " + createString;
		}
		public static String getDropSQL() {
            return "DROP TABLE " + TABLE_NAME;
        }
		public static String[] getIndexColumns() {
			return new String[] {
					Columns.TP_ID,Columns.THUMBNAIL_ID,
					Columns.STATUS,Columns.URL,Columns.CREATION_TIME
			};
		}
	} //end of ThumbnailTable
	
	public static class HotpicTable{
		
		public static final String TABLE_NAME = "t_hotpic";
		
		public static class Columns {			
			public static final String ID = "id";
			public static final String AUTHOR = "author";
			public static final String AVATARIMGPATH = "avatarImgPath";
			public static final String MOBIMGID = "mobImgId";
			public static final String STORIESNUM = "storiesNum";
			public static final String COMMENTSNUM = "commentsNum";	
			//标准时间格式yyyy-MM-dd HH:mm:ss
			public static final String CREATION_TIME = "creationTime";
		}
		
		public static String getCreateSQL() {
            String createString = TABLE_NAME + "( "             		
            		+ Columns.ID + " TEXT PRIMARY KEY, "
            		+ Columns.AUTHOR + " TEXT, "
                    + Columns.AVATARIMGPATH + " TEXT, "
            		+ Columns.MOBIMGID + " TEXT, "
            		+ Columns.STORIESNUM + " TEXT, "
            		+ Columns.COMMENTSNUM + " TEXT, "            		
                    + Columns.CREATION_TIME + " DATE " + ");";

            return "CREATE TABLE " + createString;
		}
		public static String getDropSQL() {
            return "DROP TABLE " + TABLE_NAME;
        }
		public static String[] getIndexColumns() {
			return new String[] {
					Columns.ID,Columns.AUTHOR,Columns.AVATARIMGPATH,
					Columns.MOBIMGID,Columns.STORIESNUM,Columns.COMMENTSNUM,
					Columns.CREATION_TIME
			};
		}
		
	} //end of HotpicTable
	
	public static class ClassicStoryTable{
		
		
	} //end of ClassicStoryTable
	
	
	//TODO, ADD OTHER TABLE DEFINITION...
	
	
} //end of PintuTables

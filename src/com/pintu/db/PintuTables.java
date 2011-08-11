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
	
	//TODO, ADD OTHER TABLE DEFINITION...
	
	
} //end of PintuTables

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
		public static final String TABLE_NAME = "t_clscstory";
		
		public static class Columns {			
			public static final String ID = "id";
			public static final String AUTHOR = "author";
			public static final String AVATARIMGPATH = "avatarImgPath";
			public static final String CONTENT = "content";
			public static final String FOLLOW = "follow";
			//标准时间格式yyyy-MM-dd HH:mm:ss
			public static final String CREATION_TIME = "creationTime";
		}
		
		public static String getCreateSQL() {
            String createString = TABLE_NAME + "( "             		
            		+ Columns.ID + " TEXT PRIMARY KEY, "
            		+ Columns.AUTHOR + " TEXT, "
                    + Columns.AVATARIMGPATH + " TEXT, "
            		+ Columns.CONTENT + " TEXT, "
            		+ Columns.FOLLOW + " TEXT, "
                    + Columns.CREATION_TIME + " DATE " + ");";

            return "CREATE TABLE " + createString;
		}
		public static String getDropSQL() {
            return "DROP TABLE " + TABLE_NAME;
        }
		public static String[] getIndexColumns() {
			return new String[] {
					Columns.ID,Columns.AUTHOR,Columns.AVATARIMGPATH,
					Columns.CONTENT,Columns.FOLLOW,
					Columns.CREATION_TIME
			};
		}
		
	} //end of ClassicStoryTable
	
	public static class FavoritePicsTable{
		public static final String TABLE_NAME = "t_favorites";
		
		public static class Columns {
			//贴图对象唯一编号
			public static final String ID = "id";
			public static final String OWNER = "owner";
			public static final String MOBIMGID = "mobImgId";
			//标准时间格式yyyy-MM-dd HH:mm:ss
			public static final String CREATION_TIME = "creationTime";
		}
		
		public static String getCreateSQL() {
            String createString = TABLE_NAME + "( "             		
            		+ Columns.ID + " TEXT PRIMARY KEY, "
            		+ Columns.OWNER + " TEXT, "
            		+ Columns.MOBIMGID + " TEXT, "
                    + Columns.CREATION_TIME + " DATE " + ");";

            return "CREATE TABLE " + createString;
		}
		public static String getDropSQL() {
            return "DROP TABLE " + TABLE_NAME;
        }
		public static String[] getIndexColumns() {
			return new String[] {
					Columns.ID,Columns.OWNER,
					Columns.MOBIMGID,Columns.CREATION_TIME
			};
		}
				
	} //end of FavoritePicsTable
	
	public static class MyPicsTable{
		public static final String TABLE_NAME = "t_mypics";		
		
		public static class Columns {		
			//贴图对象唯一编号
			public static final String ID = "id";
			//因为跟收藏图片共用数据结构，所以这里也需要owner字段
			public static final String OWNER = "owner";
			public static final String MOBIMGID = "mobImgId";
			//标准时间格式yyyy-MM-dd HH:mm:ss
			public static final String CREATION_TIME = "creationTime";
		}
		
		public static String getCreateSQL() {
            String createString = TABLE_NAME + "( "             		
            		+ Columns.ID + " TEXT PRIMARY KEY, "
            		+ Columns.OWNER + " TEXT, "
            		+ Columns.MOBIMGID + " TEXT, "
                    + Columns.CREATION_TIME + " DATE " + ");";

            return "CREATE TABLE " + createString;
		}
		public static String getDropSQL() {
            return "DROP TABLE " + TABLE_NAME;
        }
		public static String[] getIndexColumns() {
			return new String[] {
					Columns.ID,Columns.OWNER,Columns.MOBIMGID,Columns.CREATION_TIME
			};
		}
		
	} //end of MyPicsTable
	
	public static class MyStoriesTable{
		public static final String TABLE_NAME = "t_mystories";
		
		public static class Columns {			
			public static final String ID = "id";
			public static final String CONTENT = "content";
			//加上这个字段吧，将来好处理按用户查询
			public static final String OWNER = "owner";
			//图片ID
			public static final String FOLLOW = "follow";
			public static final String FLOWER = "flower";
			public static final String HEART = "heart";
			public static final String EGG = "egg";
			public static final String STAR = "star";			
			//标准时间格式yyyy-MM-dd HH:mm:ss
			public static final String CREATION_TIME = "creationTime";
		}
		
		public static String getCreateSQL() {
            String createString = TABLE_NAME + "( "             		
            		+ Columns.ID + " TEXT PRIMARY KEY, "
            		+ Columns.CONTENT + " TEXT, "
            		+ Columns.OWNER + " TEXT, "
                    + Columns.FOLLOW + " TEXT, "
            		+ Columns.FLOWER + " TEXT, "
            		+ Columns.HEART + " TEXT, "
            		+ Columns.EGG + " TEXT, "
            		+ Columns.STAR + " TEXT, "
                    + Columns.CREATION_TIME + " DATE " + ");";

            return "CREATE TABLE " + createString;
		}
		public static String getDropSQL() {
            return "DROP TABLE " + TABLE_NAME;
        }
		public static String[] getIndexColumns() {
			return new String[] {
					Columns.ID,Columns.CONTENT,Columns.OWNER,Columns.FOLLOW,
					Columns.FLOWER,Columns.HEART,Columns.EGG,Columns.STAR, 
					Columns.CREATION_TIME
			};
		}
		
	} //end of MyPicsTable
	
	public static class MyMessageTable{
		public static final String TABLE_NAME = "t_mymsges";
		
		public static class Columns {			
			public static final String ID = "id";
			public static final String SENDER = "sender";
			public static final String SENDERNAME = "senderName";
			public static final String SENDERAVATAR = "senderAvatar";
			public static final String RECEIVER = "receiver";
			public static final String RECEIVERNAME = "receiverName";
			public static final String CONTENT = "content";
			public static final String READED = "readed";			
			//标准时间格式yyyy-MM-dd HH:mm:ss
			public static final String CREATION_TIME = "creationTime";
		}
		
		public static String getCreateSQL() {
            String createString = TABLE_NAME + "( "             		
            		+ Columns.ID + " TEXT PRIMARY KEY, "
            		+ Columns.SENDER + " TEXT, "
            		+ Columns.SENDERNAME + " TEXT, "
            		+ Columns.SENDERAVATAR + " TEXT, "
            		+ Columns.RECEIVER + " TEXT, "
            		+ Columns.RECEIVERNAME + " TEXT, "
            		+ Columns.CONTENT + " TEXT, "
            		+ Columns.READED + " TEXT, "
                    + Columns.CREATION_TIME + " DATE " + ");";

            return "CREATE TABLE " + createString;
		}
		public static String getDropSQL() {
            return "DROP TABLE " + TABLE_NAME;
        }
		public static String[] getIndexColumns() {
			return new String[] {
					Columns.ID,Columns.SENDER,Columns.SENDERNAME,
					Columns.SENDERAVATAR,Columns.RECEIVER,Columns.RECEIVERNAME,
					Columns.CONTENT,Columns.READED,Columns.CREATION_TIME
			};
		}
	} //end of MyMessageTable
	
	
		
//	--------------------  下面的都留给小明 ---------------------------------------
	
	public static class XuetangTable{
		public static final String TABLE_NAME = "t_xuetang";
		//TODO, construct table ...
		
		
	} //end of MyMessageTable

	//贴条子所用表，需要仔细考虑下表结构，要包含一级回帖关系
	public static class NotesTable{
		public static final String TABLE_NAME = "t_notes";
		//TODO, construct table ...
		
		
	} //end of NotesTable
	
// ----------------------- 下面的上线后慢慢做 -----------------------------------	
	
	public static class EventsTable{
		public static final String TABLE_NAME = "t_events";
		//TODO, construct table ...
		
		
	} //end of EventsTable
	
	public static class NewsTable{
		public static final String TABLE_NAME = "t_news";
		//TODO, construct table ...
		
		
	} //end of NewsTable
	
	
	
} //end of PintuTables

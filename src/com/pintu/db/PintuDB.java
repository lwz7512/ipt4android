package com.pintu.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class PintuDB {

	private static final String TAG = PintuDB.class.getSimpleName();
	
    /**
     * SQLite Database file name
     */
    private static final String DATABASE_NAME = "ipintu.db";
    /**
     * Database Version
     */
    private static final int DATABASE_VERSION = 2;

    //单件实例
    private static PintuDB instance = null;
    
    /**
     * SQLiteDatabase Open Helper
     */
    private DatabaseHelper mOpenHelper = null;

    private PintuDB(Context context){
    	mOpenHelper = new DatabaseHelper(context);
    }
    
    public static synchronized PintuDB getInstance(Context context){
    	if(instance==null){
    		instance = new PintuDB(context);
    	}
    	return instance;
    }
    
    /**
     * Get SQLiteDatabase Open Helper
     * 
     * @return
     */
    public SQLiteOpenHelper getSQLiteOpenHelper() {
        return mOpenHelper;
    }

    /**
     * Get Database Connection
     * 
     * @param writeable
     * @return
     */
    public SQLiteDatabase getDb(boolean writeable) {
        if (writeable) {
            return mOpenHelper.getWritableDatabase();
        } else {
            return mOpenHelper.getReadableDatabase();
        }
    }

    /**
     * Close Database
     */
    public void close() {
        if (null != instance) {
            mOpenHelper.close();
            instance = null;
        }
    }

    
    /**
     * SQLiteOpenHelper
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        // Construct
        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        
        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(TAG, "Create Database.");
            // TODO: create tables
            createAllTables(db);
        }
        
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.d(TAG, "Upgrade Database.");
            // TODO: DROP TABLE
            onCreate(db);
        }
        
        private static void createAllTables(SQLiteDatabase db) {
        	db.execSQL(PintuTables.ThumbnailTable.getCreateSQL());
        }

//        private static void dropAllTables(SQLiteDatabase db) {
//        	db.execSQL(PintuTables.ThumbnailTable.getDropSQL());
//        }


    } //end of database helper
    

	
} //end of PintuDB

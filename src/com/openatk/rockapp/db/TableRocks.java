package com.openatk.rockapp.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class TableRocks {
	// Database table
	public static final String TABLE_NAME = "rocks";
	public static final String COL_ID = "_id";
	public static final String COL_REMOTE_ID = "remote_id";
	
	public static final String COL_DELETED = "deleted";
	public static final String COL_DELETED_CHANGED = "deleted_changed";

	public static final String COL_LAT = "lat";
    public static final String COL_LNG = "lon";
    public static final String COL_POS_CHANGED = "pos_changed";

    public static final String COL_PICKED = "picked";
    public static final String COL_PICKED_CHANGED = "picked_changed";

    public static final String COL_COMMENTS = "comments";
    public static final String COL_COMMENTS_CHANGED = "comments_changed";

    public static final String COL_PICTURE_PATH = "picture";
    public static final String COL_PICTURE_CHANGED = "picture_changed";
    public static final String COL_PICTURE_URL = "picture_url";
    public static final String COL_PICTURE_REMOTE_ID = "picture_remote_id";


	public static String[] COLUMNS = { COL_ID, COL_REMOTE_ID, 
		COL_DELETED, COL_DELETED_CHANGED, COL_LAT, COL_LNG, COL_POS_CHANGED, COL_PICKED, COL_PICKED_CHANGED, COL_COMMENTS,
		COL_COMMENTS_CHANGED, COL_PICTURE_PATH, COL_PICTURE_CHANGED, COL_PICTURE_URL, COL_PICTURE_REMOTE_ID };
	
	// Database creation SQL statement
	private static final String DATABASE_CREATE_V4 = "create table " 
	      + TABLE_NAME
	      + "(" 
	      + COL_ID + " integer primary key autoincrement," 
	      + COL_REMOTE_ID + " text default '',"
	      + COL_LAT + " real," 
	      + COL_LNG + " real,"
	      + COL_POS_CHANGED + " integer default 0,"
	      + COL_PICKED + " text,"
	      + COL_PICKED_CHANGED + " integer default 0,"
	      + COL_COMMENTS + " text default '',"
	      + COL_COMMENTS_CHANGED + " integer default 0,"
	      + COL_PICTURE_PATH + " text default '',"
	      + COL_PICTURE_CHANGED + " integer default 0,"
	      + COL_PICTURE_REMOTE_ID + " text default '',"
	      + COL_PICTURE_URL + " text default '',"
	      + COL_DELETED + " integer default 0,"
	      + COL_DELETED_CHANGED + " text" //Added
	      + ");";

	
	public static void onCreate(SQLiteDatabase database) {
	  database.execSQL(DATABASE_CREATE_V4);
	}
	
	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		Log.d("TableFields - onUpgrade", "Upgrade from " + Integer.toString(oldVersion) + " to " + Integer.toString(newVersion));
    	int version = oldVersion + 1;
    	switch(version){
    		case 1: //Launch
    			//Do nothing this is the gplay launch version
    		case 2: //V2
    			//Nothing changed in this table
    		case 4:
    			//Added COL_DELETED_CHANGED
    			Log.d("TableRocks - onUpgrade", "upgrading to v4");

    			Cursor dbCursor = database.query(TABLE_NAME, null, null, null, null, null, null);
    			String[] columnNames = dbCursor.getColumnNames();
    			for(int i=0; i<columnNames.length; i++){
    				Log.d("Column Name:", columnNames[i]);
    			}
    			
    			database.beginTransaction();
    			try {
        			database.execSQL("create table backup(_id, trello_id, lat, lon, picked, comments, picture, deleted)");
        			database.execSQL("insert into backup select _id, trello_id, lat, lon, picked, comments, picture, deleted from rocks");
        			database.execSQL("drop table rocks");
        			database.execSQL(DATABASE_CREATE_V4);
        			database.execSQL("insert into " + TABLE_NAME + " (_id, remote_id, lat, lon, picked, comments, picture, deleted) select _id, trello_id, lat, lon, picked, comments, picture, deleted from backup");
        			database.execSQL("drop table backup");
        			database.setTransactionSuccessful();
    			} finally {
    				database.endTransaction();
    			}
    	}
	    //database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
	    //onCreate(database);
	}
}

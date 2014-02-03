package com.openatk.rockapp.db;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class TableRocks {
	// Database table
	public static final String TABLE_NAME = "rocks";
	public static final String COL_ID = "_id";
	public static final String COL_REMOTE_ID = "trello_id";
	public static final String COL_HAS_SYNCED = "has_synced";
	
	public static final String COL_DELETED = "deleted";

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


	public static String[] COLUMNS = { COL_ID, COL_REMOTE_ID, COL_HAS_SYNCED, 
		COL_DELETED, COL_LAT, COL_LNG, COL_POS_CHANGED, COL_PICKED, COL_PICKED_CHANGED, COL_COMMENTS,
		COL_COMMENTS_CHANGED, COL_PICTURE_PATH, COL_PICTURE_CHANGED, COL_PICTURE_URL, COL_PICTURE_REMOTE_ID };
	
	// Database creation SQL statement
	private static final String DATABASE_CREATE = "create table " 
	      + TABLE_NAME
	      + "(" 
	      + COL_ID + " integer primary key autoincrement," 
	      + COL_REMOTE_ID + " text default '',"
	      + COL_HAS_SYNCED + " integer default 0,"
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
	      + COL_DELETED + " integer default 0"
	      + ");";

	
	public static void onCreate(SQLiteDatabase database) {
	  database.execSQL(DATABASE_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		Log.d("TableFields - onUpgrade", "Upgrade from " + Integer.toString(oldVersion) + " to " + Integer.toString(newVersion));
    	int version = oldVersion;
    	switch(version){
    		case 1: //Launch
    			//Do nothing this is the gplay launch version
    		case 2: //V2
    			//Nothing changed in this table
    	}
	    //database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
	    //onCreate(database);
	}
}

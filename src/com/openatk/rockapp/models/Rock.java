package com.openatk.rockapp.models;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import com.openatk.rockapp.db.TableRocks;
import com.openatk.rockapp.trello.TrelloCard;
import com.openatk.rockapp.trello.TrelloObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

/* A class which knows everything about a given rock */
public class Rock {
	
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
	
	private int id = BLANK_ROCK_ID;
	private String remoteId = "";
	private boolean hasSynced = false;
	private boolean deleted = false;
	private double lat;
	private double lon;
	private Date posChanged;
	
	private boolean picked = false;
	private Date pickedChanged;
	
	private String comments;
	private Date commentsChanged;
	
	private String picture;
	private Date pictureChanged;
	private String pictureURL;
	private String pictureRemoteId;
	
	private RockListener listener = null;
	
	public static final int BLANK_ROCK_ID = -1; 
	
	public static final String IMAGE_PATH = Environment.getExternalStorageDirectory() + "/com.openatk.rockapp/images";
	public static final String IMAGE_FILENAME_PATTERN = "rock_%d.png";
	
	public interface RockListener {
		public void	RockPopulateMap(Rock theRock);
	}
	
	public Rock() {
		
	}
	
	public Rock(RockListener listener) {
		this.listener = listener;
	}
	
	public Rock(RockListener listener, double latitude, double longitude, boolean picked) {
		this.listener = listener;
		this.lat = latitude;
		this.lon = longitude;
		this.picked = picked;
		this.deleted = false;
	}
	
	public TrelloCard toTrelloCard(Context context){
		TrelloCard card = null;
		if(this != null){
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			card = new TrelloCard();
			card.setLocalId(Integer.toString(this.getId()));
			card.setId(this.getTrelloId());
			//BoardId
			card.setBoardId(prefs.getString("rockBoardTrelloId", ""));
			//boardIdChanged
			//Closed
			card.setClosed(this.getDeleted());
			//TODO closed changed
			//Name
			card.setName("Lat: " + Double.toString(this.getLat()) + " Lng: " + Double.toString(this.getLon()));
			card.setName_changed(this.getPosChanged());
			//ListId
			if(this.isPicked()){
				card.setListId(prefs.getString("rockListPickedTrelloId", ""));
			} else {
				card.setListId(prefs.getString("rockListFieldTrelloId", ""));
			}
			card.setListId_changed(this.getPickedChanged());
			//Desc
			//if(this.getComments() == null) this.setComments("");
			card.setDesc(this.getComments());
			card.setDesc_changed(this.getCommentsChanged());
		}
		return card;
	}
	
	// Returns a single rock located by its id
	public static Rock getRockById(SQLiteDatabase database, Integer id) {
		if(id != null){
			//Find current field
			Rock theRock = null;
			String where = TableRocks.COL_ID + " = " + Integer.toString(id);
			Cursor cursor = database.query(TableRocks.TABLE_NAME, TableRocks.COLUMNS, where, null, null, null, null);
			if(cursor.moveToFirst()) {
				theRock = Rock.cursorToRock(cursor);
			}
			cursor.close();
			return theRock;
		} else {
			return null;
		}		
	}
	
	// Returns a single rock located by its trello_id
	public static Rock getRockByTrelloId(SQLiteDatabase database, String id) {
		if(id != null){
			//Find current field
			Rock theRock = null;
			String where = TableRocks.COL_REMOTE_ID + " = '" + id + "'";
			Cursor cursor = database.query(TableRocks.TABLE_NAME, TableRocks.COLUMNS, where, null, null, null, null);
			if(cursor.moveToFirst()) {
				theRock = Rock.cursorToRock(cursor);
			}
			cursor.close();
			return theRock;
		} else {
			return null;
		}		
	}
	
	// Returns all rocks in the database that aren't deleted
	public static ArrayList<Rock> getRocks(SQLiteDatabase database) { 
		ArrayList<Rock> rocks = new ArrayList<Rock>();
		String where = TableRocks.COL_DELETED + " = 0";
		Cursor cursor = database.query(TableRocks.TABLE_NAME, TableRocks.COLUMNS, where, null, null, null, null);
		while(cursor.moveToNext()) {
			rocks.add(Rock.cursorToRock(cursor));
		}
		cursor.close();
		return rocks;
	}
	
	// Returns all rocks in the database
	public static ArrayList<Rock> getAllRocks(SQLiteDatabase database) { 
		ArrayList<Rock> rocks = new ArrayList<Rock>();
		Cursor cursor = database.query(TableRocks.TABLE_NAME, TableRocks.COLUMNS, null, null, null, null, null);
		while(cursor.moveToNext()) {
			rocks.add(Rock.cursorToRock(cursor));
		}
		cursor.close();
		return rocks;
	}
	
	// Returns all of the "picked" rocks in the database
	public static ArrayList<Rock> getPickedRocks(SQLiteDatabase database) { 
		ArrayList<Rock> rocks = new ArrayList<Rock>();
		String where = TableRocks.COL_PICKED + " = 'true'";
		Cursor cursor = database.query(TableRocks.TABLE_NAME, TableRocks.COLUMNS, null, null, null, null, null);
		while(cursor.moveToNext()) {
			rocks.add(Rock.cursorToRock(cursor));
		}
		cursor.close();
		return rocks;
	}
	
	// Returns all of the "not-picked" rocks in the database
	public static ArrayList<Rock> getNotPickedRocks(SQLiteDatabase database) { 
		ArrayList<Rock> rocks = new ArrayList<Rock>();
		String where = TableRocks.COL_PICKED + " = 'false'";
		Cursor cursor = database.query(TableRocks.TABLE_NAME, TableRocks.COLUMNS, null, null, null, null, null);
		while(cursor.moveToNext()) {
			rocks.add(Rock.cursorToRock(cursor));
		}
		cursor.close();
		return rocks;
	}

	public void save(SQLiteDatabase database) {		
		//Find in database, see what changed
		Rock oldRock = Rock.getRockById(database, this.getId());
				
		ContentValues vals = new ContentValues();
		//vals.put(TableRocks.COL_REMOTE_ID, this.getTrelloId()); //IDK this messed it up
		if(oldRock == null || oldRock.getLat() != this.getLat()) vals.put(TableRocks.COL_LAT, this.getLat());
		if(oldRock == null || oldRock.getLon() != this.getLon()) vals.put(TableRocks.COL_LNG, this.getLon());
		if(oldRock == null || oldRock.getLon() != this.getLon() || oldRock.getLat() != this.getLat()) vals.put(TableRocks.COL_POS_CHANGED, TrelloObject.DateToUnix(this.getPosChanged()));
		
		if(oldRock == null || oldRock.isPicked() != this.isPicked()){
			vals.put(TableRocks.COL_PICKED, Boolean.toString(this.isPicked()));
			vals.put(TableRocks.COL_PICKED_CHANGED, TrelloObject.DateToUnix(this.getPickedChanged()));
			Log.d("RockApp Rock save", "Picked changed");
		}
		if(oldRock == null || oldRock.getComments() == null && this.getComments() != null || oldRock.getComments() != null && oldRock.getComments().contentEquals(this.getComments()) == false){
			vals.put(TableRocks.COL_COMMENTS, this.getComments());
			vals.put(TableRocks.COL_COMMENTS_CHANGED, TrelloObject.DateToUnix(this.getCommentsChanged()));
			Log.d("RockApp Rock save", "Comment changed");
		}
		if(oldRock == null || oldRock.getPicture() == null && this.getPicture() != null || oldRock.getPicture() != null && oldRock.getPicture().contentEquals(this.getPicture()) == false){
			vals.put(TableRocks.COL_PICTURE_PATH, this.getPicture());
			vals.put(TableRocks.COL_PICTURE_CHANGED, TrelloObject.DateToUnix(this.getPictureChanged()));
			Log.d("RockApp Rock save", "Picture changed");
		}

		if(oldRock == null || oldRock.getDeleted() != this.getDeleted()){
			int intDeleted = 0;
			if(this.getDeleted() == true){
				intDeleted = 1;
			}
			vals.put(TableRocks.COL_DELETED, intDeleted);
		}
				
		if(this.id < 0) {
			//New rock
			long newid = database.insert(TableRocks.TABLE_NAME, null, vals);
			if(newid != -1){
				this.id = (int) newid;
			}
		} else {
			//Update rock
			if(vals.size() > 0){
				String where = TableRocks.COL_ID + " = " + this.getId();
				database.update(TableRocks.TABLE_NAME, vals, where, null);
			}
		}
		
		this.listener.RockPopulateMap(this);
	}
	
	/*
	 * Internal method which can translate the result of the DB request (a Cursor object)
	 * into our custom Rock object for consumption in the rest of the application
	 */
	private static Rock cursorToRock(Cursor cursor) {
		//Must set changed after because setLat etc will set changed to new Date()
		Rock rock = new Rock();
		rock.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex(TableRocks.COL_ID))));
		rock.setTrelloId(cursor.getString(cursor.getColumnIndex(TableRocks.COL_REMOTE_ID)));
		rock.setLat(cursor.getDouble(cursor.getColumnIndex(TableRocks.COL_LAT)));
		rock.setLon(cursor.getDouble(cursor.getColumnIndex(TableRocks.COL_LNG)));
		rock.setPosChanged(TrelloObject.UnixToDate(cursor.getLong(cursor.getColumnIndex(TableRocks.COL_POS_CHANGED))));
		
		rock.setPicked(Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(TableRocks.COL_PICKED))));
		rock.setPickedChanged(TrelloObject.UnixToDate(cursor.getLong(cursor.getColumnIndex(TableRocks.COL_PICKED_CHANGED))));

		rock.setComments(cursor.getString(cursor.getColumnIndex(TableRocks.COL_COMMENTS)));
		rock.setCommentsChanged(TrelloObject.UnixToDate(cursor.getLong(cursor.getColumnIndex(TableRocks.COL_COMMENTS_CHANGED))));

		rock.setPicture(cursor.getString(cursor.getColumnIndex(TableRocks.COL_PICTURE_PATH)));
		rock.setPictureChanged(TrelloObject.UnixToDate(cursor.getLong(cursor.getColumnIndex(TableRocks.COL_PICTURE_CHANGED))));

		
		int intHasSynced = cursor.getInt(cursor.getColumnIndex(TableRocks.COL_HAS_SYNCED));
		if(intHasSynced == 1){
			rock.setHasSynced(true);
		} else {
			rock.setHasSynced(false);
		}
		//TODO ****** THIS IS WRONG ***********
		int intDeleted = cursor.getInt(cursor.getColumnIndex(TableRocks.COL_DELETED));
		if(intDeleted == 1){
			rock.setDeleted(true);
		} else {
			rock.setDeleted(false);
		}		
		return rock;
	}
	
	public void setListener(RockListener listener){
		this.listener = listener;
	}
	
	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTrelloId() { //TODO
		return this.remoteId;
	}

	public void setTrelloId(String remoteId) { //TODO
		this.remoteId = remoteId;
	}

	public double getLat() {
		return this.lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
		this.setPosChanged(new Date()); //TODO Internet
	}
	
	public double getLon() {
		return this.lon;
	}

	public void setLon(double lon) {
		this.lon = lon;
		this.setPosChanged(new Date()); //TODO Internet
	}
	
	public boolean isPicked() {
		return this.picked;
	}

	public void setPicked(boolean picked) {
		this.picked = picked;
		this.setPickedChanged(new Date()); //TODO Internet
	}

	public String getComments() {
		return this.comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
		this.setCommentsChanged(new Date()); //TODO Internet
	}

	public String getPicture() {
		return this.picture;
	}

	public void setPicture(String picture) {
		this.picture = picture;
	}
	
	public void deletePicture() {
		if(this.picture != null) {
			File pic = new File(this.picture);
			pic.delete();
			this.picture = null;
		}
	}
	
	public boolean getDeleted() {
		return this.deleted;
	}
	
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	
	public boolean getHasSynced(){
		return this.hasSynced;
	}
	
	public void setHasSynced(boolean hasSynced){
		this.hasSynced = hasSynced;
	}

	public Date getPosChanged() {
		return posChanged;
	}

	public void setPosChanged(Date posChanged) {
		this.posChanged = posChanged;
	}

	public Date getPickedChanged() {
		return pickedChanged;
	}

	public void setPickedChanged(Date pickedChanged) {
		this.pickedChanged = pickedChanged;
	}

	public Date getCommentsChanged() {
		return commentsChanged;
	}

	public void setCommentsChanged(Date commentsChanged) {
		this.commentsChanged = commentsChanged;
	}

	public Date getPictureChanged() {
		return pictureChanged;
	}

	public void setPictureChanged(Date pictureChanged) {
		this.pictureChanged = pictureChanged;
	}

	public String getPictureURL() {
		return pictureURL;
	}

	public void setPictureURL(String pictureURL) {
		this.pictureURL = pictureURL;
	}

	public String getPictureRemoteId() {
		return pictureRemoteId;
	}

	public void setPictureRemoteId(String pictureRemoteId) {
		this.pictureRemoteId = pictureRemoteId;
	}
	
}
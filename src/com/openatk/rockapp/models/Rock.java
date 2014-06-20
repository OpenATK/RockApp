package com.openatk.rockapp.models;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.openatk.libtrello.TrelloCard;
import com.openatk.libtrello.TrelloObject;
import com.openatk.rockapp.db.DatabaseHelper;
import com.openatk.rockapp.db.TableRocks;

/* A class which knows everything about a given rock */
public class Rock {
    //Rocks data
    private RockData data;
	    
	public static final int BLANK_ROCK_ID = -1; 
	
	public static final String IMAGE_PATH = Environment.getExternalStorageDirectory() + "/com.openatk.rockapp/images";
	public static final String IMAGE_FILENAME_PATTERN = "rock_%d.png";
	
	public Rock() {
		this.data = new RockData();
	}
	
	public Rock(RockData data) {
		this.data = data;
	}
	
	public Rock(double latitude, double longitude, boolean picked) {
		this.data = new RockData();
		this.data.lat = latitude;
		this.data.lon = longitude;
		this.data.picked = picked;
		this.data.deleted = false;
	}
	
	public TrelloCard toTrelloCard(Context context){
		TrelloCard card = null;
		if(this != null){
			SharedPreferences prefs = context.getSharedPreferences("com.openatk.rockapp", Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
			card = new TrelloCard();
			card.setLocalId(Integer.toString(this.getId()));
			card.setId(this.getTrelloId());
			//BoardId
			card.setBoardId(prefs.getString("rockBoardTrelloId", ""));
			//boardIdChanged
			//Closed
			card.setClosed(this.getDeleted());
			//Closed changed
			card.setClosed_changed(this.getDeletedChanged());
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
		Cursor cursor = database.query(TableRocks.TABLE_NAME, TableRocks.COLUMNS, where, null, null, null, null);
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
		Cursor cursor = database.query(TableRocks.TABLE_NAME, TableRocks.COLUMNS, where, null, null, null, null);
		while(cursor.moveToNext()) {
			rocks.add(Rock.cursorToRock(cursor));
		}
		cursor.close();
		return rocks;
	}
	
	public static void save(RockData data, SQLiteDatabase database){
		//Adds or updates rock in database from its data
		Rock oldRock = Rock.getRockById(database, data.getId());
		ContentValues vals = new ContentValues();
		//vals.put(TableRocks.COL_REMOTE_ID, this.getTrelloId()); //IDK this messed it up
		if(oldRock == null || oldRock.getLat() != data.getLat()) vals.put(TableRocks.COL_LAT, data.getLat());
		if(oldRock == null || oldRock.getLon() != data.getLon()) vals.put(TableRocks.COL_LNG, data.getLon());
		if(oldRock == null || oldRock.getLon() != data.getLon() || oldRock.getLat() != data.getLat()) vals.put(TableRocks.COL_POS_CHANGED, TrelloObject.DateToUnix(data.getPosChanged()));
		
		if(oldRock == null || oldRock.isPicked() != data.isPicked()){
			vals.put(TableRocks.COL_PICKED, Boolean.toString(data.isPicked()));
			vals.put(TableRocks.COL_PICKED_CHANGED, TrelloObject.DateToUnix(data.getPickedChanged()));
			Log.d("RockApp Rock save", "Picked changed");
		}
		if(oldRock == null || oldRock.getComments() == null && data.getComments() != null || oldRock.getComments() != null && oldRock.getComments().contentEquals(data.getComments()) == false){
			vals.put(TableRocks.COL_COMMENTS, data.getComments());
			vals.put(TableRocks.COL_COMMENTS_CHANGED, TrelloObject.DateToUnix(data.getCommentsChanged()));
			Log.d("RockApp Rock save", "Comment changed");
		}
		if(oldRock == null || oldRock.getPicture() == null && data.getPicture() != null || oldRock.getPicture() != null && oldRock.getPicture().contentEquals(data.getPicture()) == false){
			vals.put(TableRocks.COL_PICTURE_PATH, data.getPicture());
			vals.put(TableRocks.COL_PICTURE_CHANGED, TrelloObject.DateToUnix(data.getPictureChanged()));
			Log.d("RockApp Rock save", "Picture changed");
		}

		if(oldRock == null || oldRock.getDeleted() != data.isDeleted()){
			int intDeleted = 0;
			if(data.isDeleted() == true){
				intDeleted = 1;
			}
			vals.put(TableRocks.COL_DELETED, intDeleted);
		}
		
		if(oldRock == null || oldRock.getDeletedChanged() != data.getDeletedChanged()){
			vals.put(TableRocks.COL_DELETED_CHANGED,  DatabaseHelper.dateToStringUTC(data.getDeletedChanged()));
		}
				
		if(data.id < 0) {
			//New rock
			long newid = database.insert(TableRocks.TABLE_NAME, null, vals);
			if(newid != -1){
				data.id = (int) newid;
			}
		} else {
			//Update rock
			if(vals.size() > 0){
				String where = TableRocks.COL_ID + " = " + data.getId();
				database.update(TableRocks.TABLE_NAME, vals, where, null);
			}
		}
	}

	public void save(SQLiteDatabase database) {		
		Rock.save(this.data, database);
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

		int intDeleted = cursor.getInt(cursor.getColumnIndex(TableRocks.COL_DELETED));
		if(intDeleted == 1){
			rock.setDeleted(true);
		} else {
			rock.setDeleted(false);
		}		
		rock.setDeletedChanged(DatabaseHelper.stringToDateUTC(cursor.getString(cursor.getColumnIndex(TableRocks.COL_DELETED_CHANGED))));
		return rock;
	}
	
	
	public int getId() {
		return this.data.id;
	}

	public void setId(int id) {
		this.data.id = id;
	}

	public String getTrelloId() {
		return this.data.remoteId;
	}

	public void setTrelloId(String remoteId) {
		this.data.remoteId = remoteId;
	}

	public double getLat() {
		return this.data.lat;
	}

	public void setLat(double lat) {
		this.data.lat = lat;
		this.setPosChanged(new Date()); //TODO Internet
	}
	
	public double getLon() {
		return this.data.lon;
	}

	public void setLon(double lon) {
		this.data.lon = lon;
		this.setPosChanged(new Date()); //TODO Internet
	}
	
	public boolean isPicked() {
		return this.data.picked;
	}

	public void setPicked(boolean picked) {
		this.data.picked = picked;
		this.setPickedChanged(new Date()); //TODO Internet
	}

	public String getComments() {
		return this.data.comments;
	}

	public void setComments(String comments) {
		this.data.comments = comments;
		this.setCommentsChanged(new Date()); //TODO Internet
	}

	public String getPicture() {
		return this.data.picture;
	}

	public void setPicture(String picture) {
		this.data.picture = picture;
	}
	
	public void deletePicture() {
		if(this.data.picture != null) {
			File pic = new File(this.data.picture);
			pic.delete();
			this.data.picture = null;
		}
	}
	
	public boolean getDeleted() {
		return this.data.deleted;
	}
	
	public Date getDeletedChanged() {
		return this.data.deletedChanged;
	}
	
	public void setDeleted(boolean deleted) {
		this.data.deleted = deleted;
	}	
	
	public void setDeletedChanged(Date deletedChanged){
		this.data.deletedChanged = deletedChanged;
	}

	public Date getPosChanged() {
		return this.data.posChanged;
	}

	public void setPosChanged(Date posChanged) {
		this.data.posChanged = posChanged;
	}

	public Date getPickedChanged() {
		return this.data.pickedChanged;
	}

	public void setPickedChanged(Date pickedChanged) {
		this.data.pickedChanged = pickedChanged;
	}

	public Date getCommentsChanged() {
		return this.data.commentsChanged;
	}

	public void setCommentsChanged(Date commentsChanged) {
		this.data.commentsChanged = commentsChanged;
	}

	public Date getPictureChanged() {
		return this.data.pictureChanged;
	}

	public void setPictureChanged(Date pictureChanged) {
		this.data.pictureChanged = pictureChanged;
	}

	public String getPictureURL() {
		return this.data.pictureURL;
	}

	public void setPictureURL(String pictureURL) {
		this.data.pictureURL = pictureURL;
	}

	public String getPictureRemoteId() {
		return this.data.pictureRemoteId;
	}

	public void setPictureRemoteId(String pictureRemoteId) {
		this.data.pictureRemoteId = pictureRemoteId;
	}
	
	
	public RockData getData(){
		return this.data;
	}
	
	public void setData(RockData data){
		this.data = data;
	}
}
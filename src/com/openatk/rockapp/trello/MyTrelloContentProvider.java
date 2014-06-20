package com.openatk.rockapp.trello;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.openatk.libtrello.TrelloBoard;
import com.openatk.libtrello.TrelloCard;
import com.openatk.libtrello.TrelloContentProvider;
import com.openatk.libtrello.TrelloList;
import com.openatk.rockapp.MainActivity;
import com.openatk.rockapp.db.DatabaseHelper;
import com.openatk.rockapp.db.TableRocks;
import com.openatk.rockapp.models.Rock;
import com.openatk.rockapp.models.RockHelper;

public class MyTrelloContentProvider extends TrelloContentProvider {
	
	private SQLiteOpenHelper dbHelper;
	private RockHelper rockHelper;
	
	public MyTrelloContentProvider(){
		dbHelper = new DatabaseHelper(getContext());
		rockHelper = new RockHelper(getContext());
	}

	//Custom implemented in every app
	@Override
	public List<TrelloCard> getCards(String boardTrelloId){
		Log.d("MyTrelloContentProvider", "getCards()");
		//Return all custom data as cards
		dbHelper = new DatabaseHelper(getContext());
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		List<Rock> rocks = Rock.getAllRocks(database);		
		database.close();
		Log.d("MyTrelloContentProvider - getCards", "# Rocks:" + Integer.toString(rocks.size()));
		dbHelper.close();
		
		List<TrelloCard> trelloCards = new ArrayList<TrelloCard>();
		for(int i=0; i<rocks.size(); i++){
			Rock rock = rocks.get(i);
			trelloCards.add(rock.toTrelloCard(this.getContext()));
		}
		return trelloCards;
	}
	
	@Override
	public TrelloCard getCard(String id){
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Rock rock = Rock.getRockByTrelloId(database, id);
		database.close();
		dbHelper.close();
		return rock.toTrelloCard(this.getContext());
	}
	
	//Custom implemented in every app
	@Override
	public List<TrelloList> getLists(String boardTrelloId){
		Log.d("MyTrelloContentProvider", "getLists()");
		//Return all custom data as TrelloLists
		//RockApp has 2 lists
		List<TrelloList> lists = new ArrayList<TrelloList>();
		
		SharedPreferences prefs = this.getContext().getSharedPreferences("com.openatk.rockapp", Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
		
		TrelloList listPicked = new TrelloList();
		if(prefs.contains("rockListPickedLocalId")){
			listPicked.setLocalId(prefs.getString("rockListPickedLocalId", "0"));
			listPicked.setId(prefs.getString("rockListPickedTrelloId", ""));
			listPicked.setName(prefs.getString("rockListPickedName", ""));
			listPicked.setName_changed(TrelloContentProvider.stringToDate(prefs.getString("rockListPickedName_change", "")));
			listPicked.setClosed_changed(TrelloContentProvider.stringToDate(prefs.getString("rockListPickedClosed_change", "")));
			listPicked.setBoardId(prefs.getString("rockBoardTrelloId", ""));
		} else {			
			Date theDate = new Date();		//TODO from Internet	
			String pickedLocalId = "0";
			String pickedTrelloId = "";
			String pickedName = "Rocks Picked Up";
			String changeDate = TrelloContentProvider.dateToUnixString(theDate);
			
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("rockListPickedLocalId", pickedLocalId);
			editor.putString("rockListPickedTrelloId", pickedTrelloId);
			editor.putString("rockListPickedName", pickedName);
			editor.putString("rockListPickedName_change", changeDate);
			editor.putString("rockListPickedClosed_change", changeDate);
			editor.commit();
			
			listPicked.setLocalId(pickedLocalId);
			listPicked.setId(pickedTrelloId);
			listPicked.setName(pickedName);
			listPicked.setName_changed(theDate);
			listPicked.setClosed(false);
			listPicked.setClosed_changed(theDate);
			listPicked.setBoardId(prefs.getString("rockBoardTrelloId", ""));
		}
		lists.add(listPicked);
		
		
		TrelloList listField = new TrelloList();
		if(prefs.contains("rockListFieldLocalId")){			
			listField.setLocalId(prefs.getString("rockListFieldLocalId", "0"));
			listField.setId(prefs.getString("rockListFieldTrelloId", ""));
			listField.setName(prefs.getString("rockListFieldName", ""));
			listField.setName_changed(TrelloContentProvider.stringToDate(prefs.getString("rockListFieldName_change", "")));
			listField.setClosed_changed(TrelloContentProvider.stringToDate(prefs.getString("rockListFieldClosed_change", "")));
			listField.setBoardId(prefs.getString("rockBoardTrelloId", ""));
		} else {			
			Date theDate = new Date();	//TODO from Internet
			
			String dateChange = TrelloContentProvider.dateToUnixString(theDate);
			String fieldLocalId = "1";
			String fieldTrelloId = "";
			String fieldName = "Rocks In Field";
			
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("rockListFieldLocalId", fieldLocalId);
			editor.putString("rockListFieldTrelloId", fieldTrelloId);
			editor.putString("rockListFieldName", fieldName);
			editor.putString("rockListFieldName_change", dateChange);
			editor.putString("rockListFieldClosed_change", dateChange);
			editor.commit();
			
			listField.setLocalId(fieldLocalId);
			listField.setId(fieldTrelloId);
			listField.setName(fieldName);
			listField.setName_changed(theDate);
			listField.setClosed(false);
			listField.setClosed_changed(theDate);
			listField.setBoardId(prefs.getString("rockBoardTrelloId", ""));
		}			
		lists.add(listField);
		
		return lists;
	}
	
	
	
	//Custom implemented in every app
	@Override
	public List<TrelloBoard> getBoards(){
		Log.d("MyTrelloContentProvider", "getBoards()");
		//Return all custom data as boards, always return boards
		//RockApp has 1 board
		List<TrelloBoard> boards = new ArrayList<TrelloBoard>();
		SharedPreferences prefs = this.getContext().getSharedPreferences("com.openatk.rockapp", Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
		TrelloBoard trelloBoard = new TrelloBoard();
		if(prefs.contains("rockBoardLocalId")){
			Log.d("MyTrelloContentProvider", "Has local id, giving current local board");
			Log.d("MyTrelloContentProvider", "Its trelloId:" + prefs.getString("rockBoardTrelloId", ""));

			trelloBoard.setLocalId(prefs.getString("rockBoardLocalId", "0"));
			trelloBoard.setId(prefs.getString("rockBoardTrelloId", ""));
			trelloBoard.setName(prefs.getString("rockBoardName", ""));
			trelloBoard.setName_changed(TrelloContentProvider.stringToDate(prefs.getString("rockBoardName_change", "")));
			trelloBoard.setDesc(prefs.getString("rockBoardDesc", ""));
			trelloBoard.setDesc_changed(TrelloContentProvider.stringToDate(prefs.getString("rockBoardDesc_change", "")));
			trelloBoard.setClosed(false);
			trelloBoard.setClosed_changed(TrelloContentProvider.stringToDate(prefs.getString("rockBoardClosed_change", "")));
			trelloBoard.setOrganizationId(prefs.getString("rockBoardOrganizationId", ""));
			trelloBoard.setOrganizationId_changed(TrelloContentProvider.stringToDate(prefs.getString("rockBoardOrganizationId_change", "")));
			
			trelloBoard.setLastSyncDate(prefs.getString("rockBoardSyncDate", ""));
			trelloBoard.setLastTrelloActionDate(prefs.getString("rockBoardTrelloActionDate", ""));
		} else {		
			Log.d("MyTrelloContentProvider", "No local id, creating new local board");
			Date theDate = new Date();			
			String localId = "0";
			String trelloId = "";
			String name = "OpenATK - RockApp";
			String dateChange = TrelloContentProvider.dateToUnixString(theDate);
			
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("rockBoardLocalId", localId);
			editor.putString("rockBoardTrelloId", trelloId);
			editor.putString("rockBoardName", name);
			editor.putString("rockBoardName_change", dateChange);
			editor.putString("rockBoardDesc", "");
			editor.putString("rockBoardDesc_change", dateChange);
			editor.putString("rockBoardClosed_change", dateChange);
			editor.putString("rockBoardOrganizationId", "");
			editor.putString("rockBoardOrganizationId_change", dateChange);
			editor.putString("rockBoardSyncDate", TrelloContentProvider.dateToUnixString(new Date(0)));
			editor.putString("rockBoardTrelloActionDate", TrelloContentProvider.dateToUnixString(new Date(0)));
			editor.commit();
			trelloBoard.setLocalId(localId);
			trelloBoard.setId(trelloId);
			trelloBoard.setName(name);
			trelloBoard.setName_changed(theDate);
		}
		boards.add(trelloBoard);
		
		return boards;
	}
	
	@Override
	public int updateCard(TrelloCard tcard){
		Log.d("MyTrelloContentProvider - updateCard", "updating card:" + tcard.getSource().getLocalId());
		Log.d("MyTrelloContentProvider - updateCard", "updating card:" + tcard.getId());
		Rock rock = null;
		dbHelper = new DatabaseHelper(getContext());
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		if(tcard.getSource().getLocalId() != null) {
			rock = Rock.getRockById(database, Integer.parseInt(tcard.getSource().getLocalId()));
		} else  {
			rock = Rock.getRockByTrelloId(database, tcard.getId());
		}
		database.close();
		dbHelper.close();
		
		if(rock == null) {
			Log.d("MyTrelloContentProvider - updateCard", "Could not find card");
			return 0;
		}
		
		Boolean delete = false;
		ContentValues rockValues = new ContentValues();
		if(tcard.getId() != null){
			rockValues.put(TableRocks.COL_REMOTE_ID, tcard.getId());
		}
		
		//Board id
		if(tcard.getBoardId() != null){
			SharedPreferences prefs = this.getContext().getSharedPreferences("com.openatk.rockapp", Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
			if(tcard.getBoardId().contentEquals(prefs.getString("rockBoardTrelloId", "something")) == false){
				delete = true;
			}
		}
		
		//List id
		if(tcard.getListId() != null){
			//Set picked
			SharedPreferences prefs = this.getContext().getSharedPreferences("com.openatk.rockapp", Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
			if(prefs.getString("rockListPickedTrelloId", "").contentEquals(tcard.getListId())) {
				rockValues.put(TableRocks.COL_PICKED, "true");
			} else if(prefs.getString("rockListFieldTrelloId", "").contentEquals(tcard.getListId())) {
				rockValues.put(TableRocks.COL_PICKED, "false");
			} else {
				Log.d("", "ERROR!! Rock moved to a invaild list. Deleting rock.");
				//TODO delete the rock?
				delete = true;
			}
			rockValues.put(TableRocks.COL_PICKED_CHANGED, TrelloContentProvider.dateToUnixString(tcard.getListId_changed()));
		}
		
		//Name
		if(tcard.getName() != null){
			if(tcard.getName().contentEquals(rockHelper.getName(rock)) == false){
				//Move rock
				Pattern p = Pattern.compile("^Lat: ([-]?)([0-9]{1,3})[.]([0-9]+) Lng: ([-]?)([0-9]{1,3})[.]([0-9]+)$");
				Matcher m = p.matcher(tcard.getName());
				if(m.find()){
					Log.d("SyncController - updateCard","Lat:" + m.group(1) + m.group(2) + "." + m.group(3));
					Log.d("SyncController - updateCard","Lng:" + m.group(4) + m.group(5) + "." + m.group(6));
					Double lat = Double.parseDouble(m.group(1) + m.group(2) + "." + m.group(3));
					Double lng = Double.parseDouble(m.group(4) + m.group(5) + "." + m.group(6));
					rockValues.put(TableRocks.COL_LAT, Double.toString(lat));
					rockValues.put(TableRocks.COL_LNG, Double.toString(lng));
				} else {
					delete = true;
				}
			}
			rockValues.put(TableRocks.COL_POS_CHANGED, TrelloContentProvider.dateToUnixString(tcard.getName_changed()));
		}
		
		//Desc
		if(tcard.getDesc() != null){
			if(tcard.getDesc().contentEquals(rockHelper.getDesc(rock)) == false){
				rockValues.put(TableRocks.COL_COMMENTS, tcard.getDesc());
			}
			rockValues.put(TableRocks.COL_COMMENTS_CHANGED, TrelloContentProvider.dateToUnixString(tcard.getDesc_changed()));
		}
		
		//Closed
		if(tcard.getClosed() != null){
			Boolean trelloClosed = tcard.getClosed();
			if(rock.getDeleted() != trelloClosed){
				if(trelloClosed){
					delete = true;
				} else {
					rockValues.put(TableRocks.COL_DELETED, 0); //TODO idk if we need this
				}
			}
			//TODO closed_changed in TableRocks?
		}
		
		//TODO could have pos, etc here but not needed in this case
		
		if(delete == true){
			//Delete this rock from local db
			database = dbHelper.getWritableDatabase();
			String where = TableRocks.COL_ID + " = " + Integer.toString(rock.getId());
			database.delete(TableRocks.TABLE_NAME, where, null);
			database.close();
			dbHelper.close();
		} else if(rockValues.size() > 0) {
			//Update this rock in local db
			Log.d("MyTrelloContentProvider - updateCard", "Updating in db");
			database = dbHelper.getWritableDatabase();
			String where = TableRocks.COL_ID + " = " + Integer.toString(rock.getId());
			database.update(TableRocks.TABLE_NAME, rockValues, where, null);
			database.close();
			dbHelper.close();
		} else {
			Log.d("MyTrelloContentProvider - updateCard", "Nothing to update.");
		}
		
		LocalBroadcastManager.getInstance(this.getContext()).sendBroadcast(new Intent(MainActivity.INTENT_ROCKS_UPDATED));
		return 1;
	}
	
	@Override
	public int updateList(TrelloList tlist){
		Log.d("MyTrelloContentProvider", "updateList()");
		SharedPreferences prefs = this.getContext().getSharedPreferences("com.openatk.rockapp", Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
		Boolean isListPicked = false;
		Boolean isListField = false;
		if(tlist.getSource().getLocalId() != null){
			if(tlist.getSource().getLocalId().contentEquals(prefs.getString("rockListPickedLocalId", ""))){
				isListPicked = true;
			} else if(tlist.getSource().getLocalId().contentEquals(prefs.getString("rockListFieldLocalId", ""))){
				isListField = true;
			}			
		} else if(tlist.getId() != null){
			if(tlist.getId().contentEquals(prefs.getString("rockListPickedTrelloId", ""))) {
				isListPicked = true;
			} else if(tlist.getId().contentEquals(prefs.getString("rockListFieldTrelloId", ""))) {
				isListField = true;
			}
		}
		
		String key = null;
		if(isListPicked){
			key = "Picked";
		} else if(isListField) {
			key = "Field";
		} else {
			return 0;
		}
		
		SharedPreferences.Editor editor = prefs.edit();
		Boolean delete = false;
		//Name
		Date new_nameChanged = null;
		if(tlist.getName() != null){
			if(tlist.getName().contentEquals(prefs.getString("rockList"+key+"Name", "")) == false){
				//Delete, renamed the list to something else
				delete = true;
				//Deleting no need to set name_changed date
			} else {
				//Might as well update name_changed even tho it doesn't matter in this case
				new_nameChanged = tlist.getName_changed();
			}
		}
		//TrelloId
		if(tlist.getId() != null){
			editor.putString("rockList"+key+"TrelloId", tlist.getId());
		}
		//Closed
		Date new_closedChanged = null;
		if(tlist.getClosed() != null){
			if(tlist.getClosed() == true){
				//Delete
				delete = true;
			} else {
				new_closedChanged = tlist.getClosed_changed();
			}
		}
		//BoardId
		if(tlist.getBoardId() != null){
			if(tlist.getBoardId().contentEquals(prefs.getString("rockBoardTrelloId", "something")) == false){
				delete = true;
			}
		}
		//TODO could have pos, etc here but not needed in this case	
		if(delete){
			editor.remove("rockList"+key+"LocalId"); //Will cause it to be remade
			//Delete all rocks that were in this list
			String query;
			if(isListPicked) {
				query = "true";
			} else {
				query = "false";
			}
			dbHelper = new DatabaseHelper(getContext());
			SQLiteDatabase database = dbHelper.getWritableDatabase();
			String where = TableRocks.COL_PICKED + " = '" + query + "'";
			database.delete(TableRocks.TABLE_NAME, where, null);
			database.close();
			dbHelper.close();
			LocalBroadcastManager.getInstance(this.getContext()).sendBroadcast(new Intent(MainActivity.INTENT_ROCKS_UPDATED));
		} else {
			if(new_nameChanged != null) {
				editor.putString("rockList"+key+"Name_change", TrelloContentProvider.dateToUnixString(new_nameChanged));
			}
			if(new_closedChanged != null) {
				editor.putString("rockList"+key+"Closed_change", TrelloContentProvider.dateToUnixString(new_closedChanged));
			}
		}
		editor.commit();
		return 1;
	}
	
	
	@Override
	public int updateOrganization(String oldOrganizationId, String newOrganizationId){
		Log.d("MyTrelloContentProvider", "updateOrganization()");
		Log.d("MyTrelloContentProvider", "updateOrganization old:" + oldOrganizationId + " new:" + newOrganizationId);
		//Organization has changed, delete everything
		SharedPreferences prefs = this.getContext().getSharedPreferences("com.openatk.rockapp", Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
		SharedPreferences.Editor editor = prefs.edit();
		editor.remove("rockBoardLocalId"); //Will cause it to be remade
		//Delete lists
		editor.remove("rockListPickedLocalId"); //Will cause it to be remade
		editor.remove("rockListFieldLocalId"); //Will cause it to be remade
		//Delete cards
		dbHelper = new DatabaseHelper(getContext());
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		database.delete(TableRocks.TABLE_NAME, null, null);
		database.close();
		dbHelper.close();
		LocalBroadcastManager.getInstance(this.getContext()).sendBroadcast(new Intent(MainActivity.INTENT_ROCKS_UPDATED));
		editor.commit();
		return 0;
	}
	
	@Override
	public int updateBoard(TrelloBoard tBoard){
		Log.d("MyTrelloContentProvider", "updateBoard()");
		SharedPreferences prefs = this.getContext().getSharedPreferences("com.openatk.rockapp", Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
		Boolean isIt = false;
		if(tBoard.getSource().getLocalId() != null){
			if(tBoard.getSource().getLocalId().contentEquals(prefs.getString("rockBoardLocalId", "something"))){
				isIt = true;
			}
		}
		if(isIt){
			Boolean delete = false;
			SharedPreferences.Editor editor = prefs.edit();
			if(tBoard.getId() != null && tBoard.getId().contentEquals(prefs.getString("rockBoardTrelloId", "something")) == false){
				editor.putString("rockBoardTrelloId", tBoard.getId());
				Log.d("MyTrelloContentProvider", "updateBoard new trello id:" + tBoard.getId());
			}
			//Name
			if(tBoard.getName() != null) {
				if(tBoard.getName().contentEquals(prefs.getString("rockBoardName", "")) == false){
					delete = true;
				}
				editor.putString("rockBoardName_change", TrelloContentProvider.dateToUnixString(tBoard.getName_changed()));
			}
			//Desc
			if(tBoard.getDesc() != null){
				editor.putString("rockBoardDesc", tBoard.getDesc());
				editor.putString("rockBoardDesc_change", TrelloContentProvider.dateToUnixString(tBoard.getDesc_changed()));
			}
			//Closed
			if(tBoard.getClosed() != null){
				if(tBoard.getClosed() == true){
					delete = true;
				}
				editor.putString("rockBoardClosed_change", TrelloContentProvider.dateToUnixString(tBoard.getClosed_changed()));
			}
			//Organization Id
			if(tBoard.getOrganizationId() != null){
				if(prefs.getString("rockBoardOrganizationId", "").length() == 0){
					//Just added the board on trello, its sending back trello id and organization id 
					editor.putString("rockBoardOrganizationId", tBoard.getOrganizationId());
					editor.putString("rockBoardOrganizationId_change", TrelloContentProvider.dateToUnixString(tBoard.getOrganizationId_change()));
				} else if(tBoard.getOrganizationId().contentEquals(prefs.getString("rockBoardOrganizationId", "")) == false){
					//Means it's organization id changed... delete the board
					delete = true;
				}
			}
			//LastSync
			if(tBoard.getLastSyncDate() != null){
				editor.putString("rockBoardSyncDate", tBoard.getLastSyncDate());
			}
			//LastTrelloAction
			if(tBoard.getLastTrelloActionDate() != null){			
				editor.putString("rockBoardTrelloActionDate", tBoard.getLastTrelloActionDate());
			}
			//TODO labels, etc, don't need for this i guess but could add...
			
			if(delete){
				Log.d("MyTrelloContentProvider", "updateBoard deleting it");
				editor.remove("rockBoardLocalId"); //Will cause it to be remade
				editor.remove("rockBoardTrelloId"); //Will cause it to be remade
				//Delete lists
				editor.remove("rockListPickedLocalId"); //Will cause it to be remade
				editor.remove("rockListFieldLocalId"); //Will cause it to be remade
				//Delete cards
				dbHelper = new DatabaseHelper(getContext());
				SQLiteDatabase database = dbHelper.getWritableDatabase();
				database.delete(TableRocks.TABLE_NAME, null, null);
				database.close();
				dbHelper.close();
				LocalBroadcastManager.getInstance(this.getContext()).sendBroadcast(new Intent(MainActivity.INTENT_ROCKS_UPDATED));
			}
			editor.commit();
		} else {
			return 0;
		}
		return 1;
	}
	
	@Override
	public void insertCard(TrelloCard tcard){
		Log.d("MyTrelloContentProvider","insertCard()");
		Log.d("MyTrelloContentProvider","insert Card name: " + tcard.getName());
		//Check if we have this one already...
		dbHelper = new DatabaseHelper(getContext());
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Rock theRock = Rock.getRockByTrelloId(database, tcard.getId());
		database.close();
		dbHelper.close();
		if(theRock != null){
			Log.d("MyTrelloContentProvider","We already have this card!");
			return; //Already have it. Was an action directly after the insert. TODO maybe?
		}
		//TODO Check for exact duplicates?
		
		//Add a new rock
		ContentValues rockValues = new ContentValues();
		if(tcard.getName() != null){
			//Valid rock check
			Pattern p = Pattern.compile("^Lat: ([-]?)([0-9]{1,3})[.]([0-9]+) Lng: ([-]?)([0-9]{1,3})[.]([0-9]+)$");
			Matcher m = p.matcher(tcard.getName());
			Log.d("MyTrelloContentProvider - insertCard","Name:" + tcard.getName());
			if(m.find()){
				Log.d("MyTrelloContentProvider - insertCard","Lat:" + m.group(1) + m.group(2) + "." + m.group(3));
				Log.d("MyTrelloContentProvider - insertCard","Lng:" + m.group(4) + m.group(5) + "." + m.group(6));
				Double lat = Double.parseDouble(m.group(1) + m.group(2) + "." + m.group(3));
				Double lng = Double.parseDouble(m.group(4) + m.group(5) + "." + m.group(6));
				rockValues.put(TableRocks.COL_LAT, Double.toString(lat));
				rockValues.put(TableRocks.COL_LNG, Double.toString(lng));
				rockValues.put(TableRocks.COL_POS_CHANGED, TrelloContentProvider.dateToUnixString(tcard.getName_changed()));
			} else {
				//Invalid rock
				Log.d("MyTrelloContentProvider - insertCard","Invaild Rock");
				return;
			}
		}
		if(tcard.getDesc() != null){
			rockValues.put(TableRocks.COL_COMMENTS, tcard.getDesc());
			rockValues.put(TableRocks.COL_COMMENTS_CHANGED, TrelloContentProvider.dateToUnixString(tcard.getDesc_changed()));
		}
		if(tcard.getListId() != null){
			//Set picked
			SharedPreferences prefs = this.getContext().getSharedPreferences("com.openatk.rockapp", Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
			if(prefs.getString("rockListPickedTrelloId", "").contentEquals(tcard.getListId())) {
				Log.d("SyncController - insertCard","Rock Picked");
				rockValues.put(TableRocks.COL_PICKED, "true");
			} else if(prefs.getString("rockListFieldTrelloId", "").contentEquals(tcard.getListId())) {
				Log.d("SyncController - insertCard","Rock In Field");
				rockValues.put(TableRocks.COL_PICKED, "false");
			} else {
				Log.d("SyncController - insertCard", "ERROR!! Rock added to a invaild list. Not adding rock.");
				//TODO delete the rock?
				return;
			}
			rockValues.put(TableRocks.COL_PICKED_CHANGED, TrelloContentProvider.dateToUnixString(tcard.getListId_changed()));
		} else {
			Log.d("SyncController - insertCard", "ERROR!! Rock has not list id. Not adding rock.");
			return;
		}
		rockValues.put(TableRocks.COL_DELETED, 0);
		rockValues.put(TableRocks.COL_REMOTE_ID, tcard.getId());
		
		dbHelper = new DatabaseHelper(getContext());
		database = dbHelper.getWritableDatabase();
		database.insert(TableRocks.TABLE_NAME, null, rockValues);
		database.close();
		dbHelper.close();
		LocalBroadcastManager.getInstance(this.getContext()).sendBroadcast(new Intent(MainActivity.INTENT_ROCKS_UPDATED));
	}
	
	@Override
	public void insertList(TrelloList tList){
		//TODO check if we have this one already just like above...
		
		//Check to see if this matches a list we want, if we don't have them already
		SharedPreferences prefs = this.getContext().getSharedPreferences("com.openatk.rockapp", Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
		if(tList.getName() != null && tList.getName().contentEquals("Rocks Picked Up")){
			if(prefs.getString("rockListPickedTrelloId", "").contentEquals("")){
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString("rockListPickedTrelloId", tList.getId());
				editor.putString("rockListPickedName_change", TrelloContentProvider.dateToUnixString(tList.getName_changed()));
				editor.putString("rockListPickedClosed_change", TrelloContentProvider.dateToUnixString(tList.getClosed_changed()));
				editor.commit();			
			}
		} else if(tList.getName() != null && tList.getName().contentEquals("Rocks In Field")){
			if(prefs.getString("rockListFieldTrelloId", "").contentEquals("")){
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString("rockListFieldTrelloId", tList.getId());
				editor.putString("rockListFieldName_change", TrelloContentProvider.dateToUnixString(tList.getName_changed()));
				editor.putString("rockListFieldClosed_change", TrelloContentProvider.dateToUnixString(tList.getClosed_changed()));
				editor.commit();
			}
		}
	}
	
	@Override
	public void insertBoard(TrelloBoard tBoard){
		//Check if this is our board if we don't have it already
		SharedPreferences prefs = this.getContext().getSharedPreferences("com.openatk.rockapp", Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
		if(tBoard.getName().contentEquals("OpenATK - RockApp")){
			Log.d("SyncController - insertBoard", "Found new board on trello named the same as ours.");

			if(prefs.getString("rockBoardTrelloId", "").contentEquals("")){
				Log.d("SyncController - insertBoard", "This is our trello board. Should we use it or make our own? PROMPT");
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString("rockBoardTrelloId", tBoard.getId());
				editor.putString("rockBoardName", tBoard.getName());
				editor.putString("rockBoardName_change", TrelloContentProvider.dateToUnixString(tBoard.getName_changed()));
				editor.putString("rockBoardClosed_change", TrelloContentProvider.dateToUnixString(tBoard.getClosed_changed()));
				editor.putString("rockBoardSyncDate", tBoard.getLastSyncDate());
				editor.putString("rockBoardTrelloActionDate", tBoard.getLastTrelloActionDate());
				editor.commit();
			} else {
				Log.d("SyncController - insertBoard", "We are already syncing to a Trello board. Ignore it.");
			}
		}
	}
}

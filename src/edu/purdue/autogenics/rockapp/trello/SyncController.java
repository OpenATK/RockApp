package edu.purdue.autogenics.rockapp.trello;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;


import edu.purdue.autogenics.libcommon.rock.Rock;
import edu.purdue.autogenics.libtrello.IBoard;
import edu.purdue.autogenics.libtrello.ICard;
import edu.purdue.autogenics.libtrello.IList;
import edu.purdue.autogenics.libtrello.ISyncController;

public class SyncController implements ISyncController {

	private Context AppContext;
	
	public SyncController(Context appContext) {
		super();
		AppContext = appContext;
	}

	@Override
	public void updateBoard(IBoard localBoard, IBoard trelloBoard) {
		if(trelloBoard.getName().contentEquals("RockApp") == false){
			//Delete all lists and cards
			Log.d("SyncController - updateBoard", "Trello Board Name changed");
			
			//Delete all rocks
			ArrayList<Rock> rockList  = Rock.getRocks(AppContext);
			for(int i=0; i < rockList.size(); i++){
				Rock curRock = rockList.get(i);
				curRock.setDeleted(true);
				curRock.save();
			}
			
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AppContext);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("BoardId", "");
			editor.putString("PickedListTrelloId", "");
			editor.putString("NotPickedListTrelloId", "");
			editor.commit();
		}
	}

	@Override
	public void addBoard(IBoard trelloBoard) {
		if(trelloBoard.getName().contentEquals("RockApp")){
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AppContext);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("BoardId", trelloBoard.getTrelloId().trim());
			editor.commit();
		}
	}

	@Override
	public void setBoardTrelloId(IBoard localBoard, String newId) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AppContext);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("BoardId", newId);
		editor.commit();
	}
	
	@Override
	public void setBoardLocalChanges(IBoard localBoard, Boolean changes) {
		//Unused
	}

	@Override
	public void updateList(IList localList, IList trelloList) {
		if(localList.getName().contentEquals("Rocks Picked Up")  && trelloList.getName().contentEquals(localList.getName()) == false){
			//Delete all rocks in this list
			ArrayList<Rock> rockList  = Rock.getRocks(AppContext);
			for(int i=0; i < rockList.size(); i++){
				Rock curRock = rockList.get(i);
				if(curRock.isPicked()){
					curRock.setDeleted(true);
					curRock.save();
				}
			}
			
			
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AppContext);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("PickedListTrelloId", "");
			editor.commit();
		} else if(localList.getName().contentEquals("Rocks In Field")  && trelloList.getName().contentEquals(localList.getName()) == false){
			//Delete all rocks in this list
			ArrayList<Rock> rockList  = Rock.getRocks(AppContext);
			for(int i=0; i < rockList.size(); i++){
				Rock curRock = rockList.get(i);
				if(curRock.isPicked() == false){
					curRock.setDeleted(true);
					curRock.save();
				}
			}
			
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AppContext);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("NotPickedListTrelloId", "");
			editor.commit();
		}
	}

	@Override
	public void addList(IList trelloList) {
		if(trelloList.getName().contentEquals("Rocks Picked Up")){
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AppContext);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("PickedListTrelloId", trelloList.getTrelloId());
			editor.commit();
		} else if(trelloList.getName().contentEquals("Rocks In Field")){
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AppContext);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("NotPickedListTrelloId", trelloList.getTrelloId());
			editor.commit();
		}
	}

	@Override
	public void setListTrelloId(IList localList, String newId) {
		if(localList.getName().contentEquals("Rocks Picked Up")){
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AppContext);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("PickedListTrelloId", newId);
			editor.commit();
		} else if(localList.getName().contentEquals("Rocks In Field")){
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AppContext);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("NotPickedListTrelloId", newId);
			editor.commit();
		}
	}

	@Override
	public void setListLocalChanges(IList localList, Boolean changes) {
		//Unused
	}
	
	@Override
	public void updateCard(ICard localCard, ICard trelloCard) {
		//Card from Trello, update if needed
		Boolean needsUpdate = false;
		if(localCard.getListId().contentEquals(trelloCard.getListId()) == false){
			//Pick or UnPick rock
			needsUpdate = true;
		}
		if(localCard.getName().contentEquals(trelloCard.getName()) == false){
			//Move rock
			needsUpdate = true;
		}
		if(localCard.getDesc().contentEquals(trelloCard.getDesc()) == false){
			//Set comments
			needsUpdate = true;
		}
		if(localCard.getClosed() != trelloCard.getClosed()){
			//Delete Rock
			needsUpdate = true;
		}
		
		if(needsUpdate){
			int localId = (Integer)localCard.getLocalId();
			Rock theRock = Rock.getRock(AppContext, localId);
			
			if(localCard.getListId().contentEquals(trelloCard.getListId()) == false){
				//Pick or UnPick rock
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AppContext);
				if(trelloCard.getListId().contentEquals(prefs.getString("PickedListTrelloId", ""))){
					theRock.setPicked(true);
				} else if(trelloCard.getListId().contentEquals(prefs.getString("NotPickedListTrelloId", ""))){
					theRock.setPicked(false);
				} else {
					//Not in a valid list remove rock
					theRock.setDeleted(true);
				}
			}
			if(localCard.getName().contentEquals(trelloCard.getName()) == false){
				//Move rock
				Pattern p = Pattern.compile("^Lat: ([-]?)([0-9]{1,3})[.]([0-9]+) Lng: ([-]?)([0-9]{1,3})[.]([0-9]+)$");
				Matcher m = p.matcher(trelloCard.getName());
				if(m.find()){
					Log.d("SyncController - updateCard","Lat:" + m.group(1) + m.group(2) + "." + m.group(3));
					Log.d("SyncController - updateCard","Lng:" + m.group(4) + m.group(5) + "." + m.group(6));
					Double lat = Double.parseDouble(m.group(1) + m.group(2) + "." + m.group(3));
					Double lng = Double.parseDouble(m.group(4) + m.group(5) + "." + m.group(6));
					
					theRock.setLat(lat);
					theRock.setLon(lng);
				} else {
					//No longer a vaild rock
					theRock.setDeleted(true);
				}
			}
			if(localCard.getDesc().contentEquals(trelloCard.getDesc()) == false){
				//Set comments
				theRock.setComments(trelloCard.getDesc());
			}
			if(localCard.getClosed() != trelloCard.getClosed()){
				//Delete Rock
				theRock.setDeleted(trelloCard.getClosed());
			}
			theRock.save(true); 
		}
	}

	@Override
	public void addCard(ICard trelloCard) {
		//Try to convert to valid rock, add if conversion successful
		String name = trelloCard.getName();
		Log.d("SyncController - addCard", "Name: ***" + name + "***");
		if(trelloCard.getClosed() == false){
			Pattern p = Pattern.compile("^Lat: ([-]?)([0-9]{1,3})[.]([0-9]+) Lng: ([-]?)([0-9]{1,3})[.]([0-9]+)$");
			Matcher m = p.matcher(name);
			if(m.find()){
				Log.d("SyncController - addCard","Lat:" + m.group(1) + m.group(2) + "." + m.group(3));
				Log.d("SyncController - addCard","Lng:" + m.group(4) + m.group(5) + "." + m.group(6));
				Double lat = Double.parseDouble(m.group(1) + m.group(2) + "." + m.group(3));
				Double lng = Double.parseDouble(m.group(4) + m.group(5) + "." + m.group(6));
				//Add rock in local db
				Rock rock = new Rock(AppContext, lat, lng, false);
				
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AppContext);
				String pickedListId = prefs.getString("PickedListTrelloId", "");
				String notPickedListId = prefs.getString("NotPickedListTrelloId", "");
				rock.setTrelloId(trelloCard.getTrelloId());
				rock.setComments(trelloCard.getDesc());
				
				Boolean inValidList = true;
				if(trelloCard.getListId().contentEquals(pickedListId)){
					rock.setPicked(true);
				} else if(trelloCard.getListId().contentEquals(notPickedListId)){
					rock.setPicked(false);
				} else {
					Log.d("SyncController - addCard", "Rock not in a vaild list");
					inValidList = false;
				}
				
				if(inValidList){
					rock.save();
				}
			}
		}
	}

	@Override
	public void setCardTrelloId(ICard localCard, String newId) {
		int localId = (Integer)localCard.getLocalId();
		Rock theRock = Rock.getRock(AppContext, localId);
		theRock.setTrelloId(newId);
		theRock.save(false);
	}
	
	@Override
	public void setCardLocalChanges(ICard localCard, Boolean changes) {
		int localId = (Integer)localCard.getLocalId();
		Rock theRock = Rock.getRock(AppContext, localId);
		theRock.setChanged(changes);
		theRock.save(false);
	}

	@Override
	public List<ICard> getLocalCards() {
		//Now get database Rocks and compare to allMarkers
		List<ICard> cardList = new ArrayList<ICard>();
		ArrayList<Rock> rockList  = Rock.getRocks(AppContext);

		for(int i=0; i < rockList.size(); i++){
			Rock curRock = rockList.get(i);
			RockCard rockCard = new RockCard(curRock.getContext(), curRock.getLat(), curRock.getLon(), curRock.isPicked());
			rockCard.setDeleted(curRock.getDeleted());
			rockCard.setTrelloId(curRock.getTrelloId());
			rockCard.setId(curRock.getId());
			rockCard.setComments(curRock.getComments());
			rockCard.setChanged(curRock.getChanged());
			cardList.add(rockCard);
		}
		return cardList;
	}

	@Override
	public List<IList> getLocalLists() {
		List<IList> listList = new ArrayList<IList>();
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AppContext);
		String pickedListId = prefs.getString("PickedListTrelloId", "");
		String notPickedListId = prefs.getString("NotPickedListTrelloId", "");
		String boardId = prefs.getString("BoardId", "");
		
		RockList pickedList = new RockList(pickedListId, "", boardId, "Rocks Picked Up", false, false);
		RockList notPickedList = new RockList(notPickedListId, "", boardId, "Rocks In Field", false, false);
			
		listList.add(pickedList);
		listList.add(notPickedList);
		
		return listList;
	}

	@Override
	public List<IBoard> getLocalBoards() {
		List<IBoard> boardList = new ArrayList<IBoard>();
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AppContext);
		String boardId = prefs.getString("BoardId", "");
		RockBoard board = new RockBoard(boardId, "", "RockApp", "", false, false);
		boardList.add(board);
		
		return boardList;
	}

}

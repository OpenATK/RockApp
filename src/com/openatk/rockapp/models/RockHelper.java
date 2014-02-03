package com.openatk.rockapp.models;

import com.openatk.rockapp.models.Rock;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class RockHelper {
	Context context;
	
	public RockHelper(Context context) {
		this.context = context;
	}
	
	
	public String getListId(Rock rock) {
		//Info gathered from picked status
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.context);
		String listId = "";
		if(rock.isPicked()){
			listId = prefs.getString("PickedListTrelloId", "");
		} else {
			listId = prefs.getString("NotPickedListTrelloId", "");
		}
		return listId;
	}

	public String getBoardId(Rock rock) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.context);
		return prefs.getString("BoardId", "");
	}
	
	public boolean vaildListId(String id){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.context);
		if(id.contentEquals(prefs.getString("PickedListTrelloId", ""))){
			return true;
		}
		if(id.contentEquals(prefs.getString("NotPickedListTrelloId", ""))){
			return true;
		}
		return false;
	}
	
	public String getName(Rock rock) {
		//Generated from lat/lng info
		Double lat = rock.getLat();
		Double lng = rock.getLon();
		String stringLat = Double.toString(lat);
		String stringLng =  Double.toString(lng);
		String newName = "Lat: " + stringLat + " Lng: " + stringLng;
		return newName;
	}
	
	public String getDesc(Rock rock) {
		String desc = rock.getComments();
		if(desc == null){
			desc = "";
		}
		return desc;
	}
	
	public boolean getPicked(String listId){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.context);
		if(listId.contentEquals(prefs.getString("PickedListTrelloId", ""))){
			return true;
		}
		if(listId.contentEquals(prefs.getString("NotPickedListTrelloId", ""))){
			return false;
		}
		return false;
	}
}

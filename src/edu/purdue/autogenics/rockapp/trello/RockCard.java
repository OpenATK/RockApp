package edu.purdue.autogenics.rockapp.trello;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import edu.purdue.autogenics.libcommon.rock.Rock;
import edu.purdue.autogenics.libtrello.ICard;

public class RockCard extends Rock implements ICard {
	
	public RockCard() {
		super();
	}
	
	public RockCard(Context context) {
		super(context);
	}

	public RockCard(Context context, double latitude, double longitude, boolean picked) {
		super(context, latitude, longitude, picked);
	}
	
	@Override
	public String getTrelloId() {
		return super.getTrelloId();
	}
	
	@Override
	public Boolean hasLocalChanges() {
		return this.getChanged();
	}

	@Override
	public void setLocalChanges(Boolean changes) {
		Log.d("RockCard","Setting local changes");
		this.setChanged(changes);
	}

	@Override
	public String getListId() {
		//Info gathered from picked status
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getContext());
		String listId = "";
		if(this.isPicked()){
			listId = prefs.getString("PickedListTrelloId", "");
		} else {
			listId = prefs.getString("NotPickedListTrelloId", "");
		}
		return listId;
	}

	@Override
	public String getBoardId() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getContext());
		return prefs.getString("BoardId", "");
	}

	@Override
	public String getName() {
		//Generated from lat/lng info
		Double lat = this.getLat();
		Double lng = this.getLon();
		String stringLat = Double.toString(lat);
		String stringLng =  Double.toString(lng);
		String newName = "Lat: " + stringLat + " Lng: " + stringLng;
		return newName;
	}

	@Override
	public String getDesc() {
		String desc = this.getComments();
		if(desc == null){
			desc = "";
		}
		return desc;
	}

	@Override
	public List<String> getLabelNames() {
		List<String> labelNames = new ArrayList<String>();
		return labelNames;
	}

	@Override
	public List<String> getLabels() {
		List<String> labels = new ArrayList<String>();
		return labels;
	}

	@Override
	public Boolean getClosed() {
		return this.getDeleted();
	}

	@Override
	public Object getLocalId() {
		return this.getId();
	}

}

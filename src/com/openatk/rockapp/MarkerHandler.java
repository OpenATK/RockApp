package com.openatk.rockapp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.openatk.openatklib.atkmap.ATKMap;
import com.openatk.openatklib.atkmap.models.ATKPoint;
import com.openatk.openatklib.atkmap.views.ATKPointView;
import com.openatk.rockapp.R;
import com.openatk.rockapp.db.DatabaseHelper;
import com.openatk.rockapp.models.Rock;
import com.openatk.rockapp.models.RockData;

public class MarkerHandler {

	private ATKMap map;
	private DatabaseHelper dbHelper;
	private int selectedRockId;
	private int RockState;
	private Context context;
	
	public MarkerHandler(Context context, ATKMap theMap, DatabaseHelper dbHelper) {
		this.context = context;
		this.map = theMap;
		this.dbHelper = dbHelper;
	}

	public void setRockState(int RockState) {
		this.RockState = RockState;		
		
		List<ATKPointView> mapPoints = map.getPointViews();	
		Iterator<ATKPointView> iterator = mapPoints.iterator();
		while (iterator.hasNext()) {
			ATKPointView point = iterator.next();
			RockData data = (RockData) point.getData();

			if (RockState == MainActivity.STATE_ROCKS_PICKED_UP && data.isPicked() == true) {
				point.show();
			} else if (RockState == MainActivity.STATE_ROCKS_NOT_PICKED_UP && data.isPicked() == false) {
				point.show();
			} else if (RockState == MainActivity.STATE_ROCKS_BOTH) {
				point.show();
			} else {
				if (data.getId() == selectedRockId) {
					point.show(); // Selected rock is always visible
				} else {
					point.hide();
				}
			}
		}
	}

	public void updatePointView(RockData data){
		this.updatePointView(data, false);
	}
	public void updatePointView(RockData data, boolean selected){
		//Find Point on map
		ATKPointView point = this.map.getPointView(data.id);
		if(point == null && data.isDeleted() == false) {
			//Add point
			Log.d("MarkerHandler", "AddPoint" + Double.toString(data.lat));
			ATKPoint newPoint = new ATKPoint(data.id, new LatLng(data.lat, data.lon));
			ATKPointView added = map.addPoint(newPoint);
			added.setData(data);
			
			this.updateIcon(added, selected);
		} else {
			if(data.isDeleted() == false){
				//Update pointview
				point.setData(data);
				this.updateIcon(point, selected);
				point.update();
			} else {
				//Remove pointview
				point.remove();
			}
		}
	}
	
	public void populateMap(int selectedRockId) {
		//Run when map needs setup or fully refreshed, shouldn't do on orientation change
		//TODO set some pref in case trello update when gone and need full refresh? Or array of ones changed?
		
		//Remove all points on map and readd them
		Log.d("populateMap", "populateMap");
		this.selectedRockId = selectedRockId;

		List<ATKPointView> mapPoints = map.getPointViews();	

		// Remove  all rocks on map
		Iterator<ATKPointView> iterator2 = mapPoints.iterator();
		while (iterator2.hasNext()) {
			ATKPointView curMarker = iterator2.next();
			curMarker.remove();
			iterator2.remove();
		}
		
		// Now get database Rocks
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		ArrayList<Rock> rockList = Rock.getRocks(database);
		dbHelper.close();
		
		// Add rocks to map
		Iterator<Rock> iterator3 = rockList.iterator();
		while (iterator3.hasNext()) {
			Rock curRock = iterator3.next();
			if (curRock.getId() == selectedRockId) {
				this.updatePointView(curRock.getData(), true);
			} else {
				this.updatePointView(curRock.getData(), false);
			}
			iterator3.remove();
		}
	}

	public void selectMarker(int theRockId) {
		Log.d("selectMarker", "selectMarker");

		// Uses old getPicked() data
		int oldSelectedRockId = selectedRockId;
		selectedRockId = theRockId;
		
		//Select the new rock
		ATKPointView point = this.map.getPointView(theRockId);
		if(point != null){
			this.updateIcon(point, true);
		} else {
			Log.d("selectMarker", "new rock null:" + Integer.toString(theRockId));
		}
		
		//Unselect the old rock
		point = this.map.getPointView(oldSelectedRockId);
		if(point != null){
			this.updateIcon(point, false);
		} else {
			Log.d("selectMarker", "old rock null" + Integer.toString(oldSelectedRockId));
		}
	}
	
	public void updateIcon(ATKPointView point){
		this.updateIcon(point, false);
	}
	public void updateIcon(ATKPointView point, boolean selected){
		RockData data = (RockData) point.getData();
		int icon;
		if (selected) {
			if (data.picked == true) {
				icon = R.drawable.rock_picked_selected;
			} else {
				icon = R.drawable.rock_selected;
			}
		} else {
			if (data.picked == true) {
				icon = R.drawable.rock_picked;
			} else {
				icon = R.drawable.rock;
			}
		}
		
		//Get dimensions from icon
		Bitmap bitmapIcon = BitmapFactory.decodeResource(this.context.getResources(), icon);
		point.setIcon(bitmapIcon);
		
		if(selected){
			//Superdraggable (Drag without long hold)
			point.setSuperDraggable(true);
		} else {
			//Not superdraggable
			point.setSuperDraggable(false);
		}
		
		if (RockState == MainActivity.STATE_ROCKS_PICKED_UP && data.isPicked() == true) {
			point.show();
		} else if (RockState == MainActivity.STATE_ROCKS_NOT_PICKED_UP && data.isPicked() == false) {
			point.show();
		} else if (RockState == MainActivity.STATE_ROCKS_BOTH) {
			point.show();
		} else {
			if (data.getId() == selectedRockId) {
				point.show(); // Selected rock is always visible
			} else {
				point.hide();
			}
		}
		
	}
}

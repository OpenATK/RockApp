package com.openatk.rockapp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import pl.mg6.android.maps.extensions.GoogleMap;
import pl.mg6.android.maps.extensions.Marker;
import pl.mg6.android.maps.extensions.MarkerOptions;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.openatk.rockapp.R;
import com.openatk.rockapp.db.DatabaseHelper;
import com.openatk.rockapp.models.Rock;
import com.openatk.rockapp.models.Rock.RockListener;

public class MarkerHandler {

	private GoogleMap map;
	private DatabaseHelper dbHelper;
	private int selectedRockId;
	private int RockState;
	private RockListener listener;

	public MarkerHandler(RockListener listener, GoogleMap theMap, DatabaseHelper dbHelper, int currentRockSelected) {
		map = theMap;
		this.dbHelper = dbHelper;
		this.listener = listener;
		selectedRockId = currentRockSelected;
	}

	public void setRockState(int RockState) {
		this.RockState = RockState;
	}

	public void populateMap(int selectedRockId) {
		Log.d("populateMap", "populateMap");
		this.selectedRockId = selectedRockId;

		MarkerOptions options = new MarkerOptions();
		List<Marker> mapMarkers = map.getMarkers();
		List<Marker> allMarkers = new ArrayList<Marker>();

		// Get all markers from clusters
		Iterator<Marker> iterator = mapMarkers.iterator();
		while (iterator.hasNext()) {
			Marker current = iterator.next();
			if (current.isCluster()) {
				allMarkers.addAll(current.getMarkers());
			} else {
				allMarkers.add(current);
			}
		}

		// Now get database Rocks and compare to allMarkers
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		ArrayList<Rock> rockList = Rock.getRocks(database);
		dbHelper.close();

		// Remove delete all rocks on map
		Iterator<Marker> iterator2 = allMarkers.iterator();
		while (iterator2.hasNext()) {
			Marker curMarker = iterator2.next();
			Rock curRock = (Rock) curMarker.getData();
			if (rockList.contains(curRock) == false || true) {
				curMarker.remove();
				iterator2.remove();
			}
		}

		// Add rocks to map
		Iterator<Rock> iterator3 = rockList.iterator();
		while (iterator3.hasNext()) {
			Rock curRock = iterator3.next();
			int icon;
			if (curRock.getId() == selectedRockId) {
				if (curRock.isPicked()) {
					icon = R.drawable.rock_picked_selected;
				} else {
					icon = R.drawable.rock_selected;
				}
			} else {
				if (curRock.isPicked()) {
					icon = R.drawable.rock_picked;
				} else {
					icon = R.drawable.rock;
				}
			}
			
			Marker added = map.addMarker(options.position(new LatLng(curRock.getLat(), curRock.getLon())).icon(BitmapDescriptorFactory.fromResource(icon)));
			curRock.setListener(listener);
			added.setData(curRock);
			if (curRock.getId() == selectedRockId) {
				added.setDraggable(true);
			}

			if (RockState == MainActivity.STATE_ROCKS_PICKED_UP
					&& curRock.isPicked() == true) {
				added.setVisible(true);
			} else if (RockState == MainActivity.STATE_ROCKS_NOT_PICKED_UP
					&& curRock.isPicked() == false) {
				added.setVisible(true);
			} else if (RockState == MainActivity.STATE_ROCKS_BOTH) {
				added.setVisible(true);
			} else {
				if (curRock.getId() == selectedRockId) {
					added.setVisible(true); // Selected rock is always visible
				} else {
					added.setVisible(false);
				}
			}

			iterator3.remove();
		}
	}

	public void selectMarker(int theRockId) {
		Log.d("selectMarker", "selectMarker");

		// Uses old getPicked() data
		int oldSelectedRockId = selectedRockId;
		selectedRockId = theRockId;

		List<Marker> mapMarkers = map.getMarkers();
		List<Marker> allMarkers = new ArrayList<Marker>();

		// Get all markers from clusters
		Iterator<Marker> iterator = mapMarkers.iterator();
		while (iterator.hasNext()) {
			Marker current = iterator.next();
			if (current.isCluster()) {
				allMarkers.addAll(current.getMarkers());
			} else {
				allMarkers.add(current);
			}
		}

		// Find marker with this rock id
		Iterator<Marker> iterator1 = allMarkers.iterator();
		while (iterator1.hasNext()) {
			Marker curMarker = iterator1.next();
			Rock curRock = (Rock) curMarker.getData();
			if (curRock.getId() == selectedRockId) {
				changeMarkerHelper(curMarker, curRock);
			}
			if (curRock.getId() == oldSelectedRockId) {
				changeMarkerHelper(curMarker, curRock);
			}
		}
	}

	public void changeMarkerIcon(Rock theRock) {
		// Uses new getPicked() data
		List<Marker> mapMarkers = map.getMarkers();
		List<Marker> allMarkers = new ArrayList<Marker>();

		// Get all markers from clusters
		Iterator<Marker> iterator = mapMarkers.iterator();
		while (iterator.hasNext()) {
			Marker current = iterator.next();
			if (current.isCluster()) {
				allMarkers.addAll(current.getMarkers());
			} else {
				allMarkers.add(current);
			}
		}

		// Find marker with this rock id
		Iterator<Marker> iterator1 = allMarkers.iterator();
		Boolean found = false;
		while (iterator1.hasNext() && found == false) {
			Marker curMarker = iterator1.next();
			Rock curRock = (Rock) curMarker.getData();
			if (curRock.getId() == theRock.getId()) {
				changeMarkerHelper(curMarker, theRock);
				found = true;
			}
		}
	}

	private void changeMarkerHelper(Marker theMarker, Rock theRock) {
		Log.d("changeMarkerHelper", "changeMarkerHelper");
		int icon;
		Boolean selected = false;
		if(theRock.getId() == selectedRockId) selected = true;
		
		Boolean picked = theRock.isPicked();
		
		if (selected) {
			if (picked == true) {
				icon = R.drawable.rock_picked_selected;
			} else {
				icon = R.drawable.rock_selected;
			}
		} else {
			if (picked == true) {
				icon = R.drawable.rock_picked;
			} else {
				icon = R.drawable.rock;
			}
		}
		theMarker.setIcon(BitmapDescriptorFactory.fromResource(icon));
		
		if (theRock.getId() == selectedRockId) {
			theMarker.setDraggable(true);
		}
		if (RockState == MainActivity.STATE_ROCKS_PICKED_UP && theRock.isPicked() == true) {
			theMarker.setVisible(true);
		} else if (RockState == MainActivity.STATE_ROCKS_NOT_PICKED_UP && theRock.isPicked() == false) {
			theMarker.setVisible(true);
		} else if (RockState == MainActivity.STATE_ROCKS_BOTH) {
			theMarker.setVisible(true);
		} else {
			if (theRock.getId() == selectedRockId) {
				theMarker.setVisible(true); // Selected rock is always visible
			} else {
				theMarker.setVisible(false);
			}
		}
	}

	public Marker getMarkerByRockId(int theRockId) {
		List<Marker> mapMarkers = map.getMarkers();
		List<Marker> allMarkers = new ArrayList<Marker>();

		// Get all markers from clusters
		Iterator<Marker> iterator = mapMarkers.iterator();
		while (iterator.hasNext()) {
			Marker current = iterator.next();
			if (current.isCluster()) {
				allMarkers.addAll(current.getMarkers());
			} else {
				allMarkers.add(current);
			}
		}

		// Find marker with this rock id
		Iterator<Marker> iterator1 = allMarkers.iterator();
		while (iterator1.hasNext()) {
			Marker curMarker = iterator1.next();
			Rock curRock = (Rock) curMarker.getData();
			if (curRock.getId() == theRockId) {
				return curMarker;
			}
		}
		return null;
	}
}

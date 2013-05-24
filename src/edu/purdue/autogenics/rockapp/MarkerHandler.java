package edu.purdue.autogenics.rockapp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import pl.mg6.android.maps.extensions.GoogleMap;
import pl.mg6.android.maps.extensions.Marker;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import edu.purdue.autogenics.libcommon.rock.Rock;

public class MarkerHandler {
	
	private GoogleMap map;
	private Context context;
	private int selectedRockId;
	
	public MarkerHandler(GoogleMap theMap, Context theContext, int currentRockSelected){
		map = theMap;
		context = theContext;
		selectedRockId = currentRockSelected;
	}
	
	public void populateMap(int RockState){
		MarkerOptions options = new MarkerOptions();
		List <Marker> mapMarkers = map.getMarkers();
		List <Marker> allMarkers = new ArrayList<Marker>();
		
		//Get all markers from clusters
		Iterator<Marker> iterator = mapMarkers.iterator();
		while (iterator.hasNext()) {
			Marker current = iterator.next();
			if(current.isCluster()){
				allMarkers.addAll(current.getMarkers());
			} else {
				allMarkers.add(current);
			}
		}
		
		//Now get database Rocks and compare to allMarkers
		ArrayList<Rock> rockList  = Rock.getRocks(context);
		
		//Remove deleted/changed rocks *** DELETEING ALL FOR NOW ***
		Iterator<Marker> iterator2 = allMarkers.iterator();
		while (iterator2.hasNext()) {
			Marker curMarker = iterator2.next();
			Rock curRock = (Rock) curMarker.getData();
			if(rockList.contains(curRock) == false || true){
				curMarker.remove();
				iterator2.remove();
			} else {
				rockList.remove(curRock);
				iterator2.remove();
			}
			
			if(RockState == MainActivity.STATE_ROCKS_PICKED_UP && curRock.isPicked() == true){
				//curMarker.setVisible(true);
			} else if(RockState == MainActivity.STATE_ROCKS_NOT_PICKED_UP && curRock.isPicked() == false){
				//curMarker.setVisible(true);
			} else if(RockState == MainActivity.STATE_ROCKS_BOTH) {
				//curMarker.setVisible(true);
			} else {
				//curMarker.setVisible(false);
			}
			
		}
		
		//Add and update rocks that are different
		Iterator<Rock> iterator3 = rockList.iterator();
		while (iterator3.hasNext()) {
			Rock curRock = iterator3.next();
			
			//Log.d("Adding Rock", "Lat: " + Double.toString(curRock.getLat()) + "  Lng: " + Double.toString(curRock.getLon()));
			int icon;
			if(curRock.getId() == selectedRockId){
				if(curRock.isPicked()){
					icon = R.drawable.rock_picked_selected;
				} else {
					icon = R.drawable.rock_selected;
				}
			} else {
				if(curRock.isPicked()){
					icon = R.drawable.rock_picked;
				} else {
					icon = R.drawable.rock;
				}
			}
			
			Marker added = map.addMarker(options.position(new LatLng(curRock.getLat(), curRock.getLon())).icon(BitmapDescriptorFactory.fromResource(icon)));
			added.setData(curRock);
			
			if(RockState == MainActivity.STATE_ROCKS_PICKED_UP && curRock.isPicked() == true){
				added.setVisible(true);
			} else if(RockState == MainActivity.STATE_ROCKS_NOT_PICKED_UP && curRock.isPicked() == false){
				added.setVisible(true);
			} else if(RockState == MainActivity.STATE_ROCKS_BOTH) {
				added.setVisible(true);
			} else {
				added.setVisible(false);
			}
			
			iterator3.remove();
		}
	}
	public void selectMarker(int theRockId){
		//Uses old getPicked() data
		int oldSelectedRockId = selectedRockId;
		selectedRockId = theRockId;
		
		List <Marker> mapMarkers = map.getMarkers();
		List <Marker> allMarkers = new ArrayList<Marker>();
		
		//Get all markers from clusters
		Iterator<Marker> iterator = mapMarkers.iterator();
		while (iterator.hasNext()) {
			Marker current = iterator.next();
			if(current.isCluster()){
				allMarkers.addAll(current.getMarkers());
			} else {
				allMarkers.add(current);
			}
		}
		
		//Find marker with this rock id
		Iterator<Marker> iterator1 = allMarkers.iterator();
		while (iterator1.hasNext()) {
			Marker curMarker = iterator1.next();
			Rock curRock = (Rock) curMarker.getData();
			if(curRock.getId() == selectedRockId){
				curMarker.remove();
				changeMarkerHelper(curRock, curRock.isPicked());
			}
			if(curRock.getId() == oldSelectedRockId){
				curMarker.remove();
				changeMarkerHelper(curRock, curRock.isPicked());
			}
		}
	}
	public void changeMarkerIcon(Rock theRock){
		//Uses new getPicked() data
		List <Marker> mapMarkers = map.getMarkers();
		List <Marker> allMarkers = new ArrayList<Marker>();
		
		//Get all markers from clusters
		Iterator<Marker> iterator = mapMarkers.iterator();
		while (iterator.hasNext()) {
			Marker current = iterator.next();
			if(current.isCluster()){
				allMarkers.addAll(current.getMarkers());
			} else {
				allMarkers.add(current);
			}
		}
		
		//Find marker with this rock id
		Iterator<Marker> iterator1 = allMarkers.iterator();
		Boolean found = false;
		while (iterator1.hasNext() && found == false) {
			Marker curMarker = iterator1.next();
			Rock curRock = (Rock) curMarker.getData();
			if(curRock.getId() == theRock.getId()){
				curMarker.remove();
				changeMarkerHelper(theRock, theRock.isPicked());
				found = true;
			}
		}
	}
	private void changeMarkerHelper(Rock theRock, Boolean picked){
		MarkerOptions options = new MarkerOptions();
		int icon;
		if(theRock.getId() == selectedRockId){
			if(picked == true){
				icon = R.drawable.rock_picked_selected;
			} else {
				icon = R.drawable.rock_selected;
			}
		} else {
			if(picked == true){
				icon = R.drawable.rock_picked;
			} else {
				icon = R.drawable.rock;
			}
		}
		Marker added = map.addMarker(options.position(new LatLng(theRock.getLat(), theRock.getLon())).icon(BitmapDescriptorFactory.fromResource(icon)));
		added.setData(theRock);
		if(theRock.getId() == selectedRockId){
			added.setDraggable(true);
		}
	}
	
	public Marker getMarkerByRockId(int theRockId){
		List <Marker> mapMarkers = map.getMarkers();
		List <Marker> allMarkers = new ArrayList<Marker>();
		
		//Get all markers from clusters
		Iterator<Marker> iterator = mapMarkers.iterator();
		while (iterator.hasNext()) {
			Marker current = iterator.next();
			if(current.isCluster()){
				allMarkers.addAll(current.getMarkers());
			} else {
				allMarkers.add(current);
			}
		}
		
		//Find marker with this rock id
		Iterator<Marker> iterator1 = allMarkers.iterator();
		while (iterator1.hasNext()) {
			Marker curMarker = iterator1.next();
			Rock curRock = (Rock) curMarker.getData();
			if(curRock.getId() == theRockId){
				return curMarker;
			}
		}
		return null;
	}
}

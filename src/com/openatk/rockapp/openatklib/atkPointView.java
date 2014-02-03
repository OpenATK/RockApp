package com.openatk.rockapp.openatklib;

import pl.mg6.android.maps.extensions.GoogleMap;
import pl.mg6.android.maps.extensions.Marker;
import pl.mg6.android.maps.extensions.MarkerOptions;

import com.google.android.gms.maps.model.BitmapDescriptor;

public class atkPointView {
	private atkPoint point;
	private GoogleMap map;
	private atkPointClickListener clickListener;
	
	private Marker marker;
	private MarkerOptions markerOptions;
	
	public atkPointView(){
		
	}
	
	public atkPointView(GoogleMap map, atkPoint point){
		this.map = map;
		this.point = point;
		this.drawPoint();
		markerOptions = new MarkerOptions().position(point.position);
	}
	
	public atkPoint getAtkPoint(){
		return point;
	}
	
	public void update(atkPoint point){
		this.point = point;
		if(marker != null){
			marker.setPosition(point.position);
		}
	}
	
	public void remove(){
		if(marker != null) marker.remove();
		marker = null;
	}
	
	public void hide(){
		this.remove();
	}
	
	public void show(){
		if(marker == null) drawPoint();
	}
	
	public void changeIcon(BitmapDescriptor icon){
		if(marker != null) marker.setIcon(icon);
		markerOptions.icon(icon);
	}
	
	public void setOnClickListener(atkPointClickListener listener){
		this.clickListener = listener;
	}
	
	public Boolean wasClicked(Marker clickedMarker){  //TODO protected?
		//Returns null if wasn't clicked, false if clicked and consumed, true if clicked and not consumed
		Boolean consumed = null;
		if(this.marker == clickedMarker){
			consumed = false;
			//Check if we have a click listener
			if(this.clickListener != null){
				consumed = this.clickListener.onClick(this);
			}
		}
		return consumed;
	}
	
	private void drawPoint(){
		//Draw the point on the map
		markerOptions.position(point.position);
		marker = this.map.addMarker(markerOptions);
	}
}
